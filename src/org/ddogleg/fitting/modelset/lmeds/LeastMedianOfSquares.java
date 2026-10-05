/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ddogleg.fitting.modelset.lmeds;

import lombok.Getter;
import lombok.Setter;
import org.ddogleg.fitting.modelset.*;
import org.ddogleg.fitting.modelset.ransac.Ransac;
import org.ddogleg.sorting.QuickSelect;
import org.ddogleg.struct.DogArray_F64;
import org.ddogleg.struct.DogArray_I32;
import org.ddogleg.struct.Factory;
import org.ddogleg.struct.FastArray;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.ddogleg.fitting.modelset.ransac.Ransac.addSelect;
import static org.ddogleg.fitting.modelset.ransac.Ransac.randomDraw;

/// Another technique similar to RANSAC known as Least Median of Squares (LMedS). For each iteration a small
/// number N points are selected. A model is fit to these points and then the error is computed for the whole
/// set. The model which minimizes the median is selected as the final model.
///
/// Inliers are selected using Rousseeuw reweighting by default, this uses the median value to derive
/// a normal distribution. Alternatively, N-best error fraction can be used to define the inlier set.
@SuppressWarnings("NullAway.Init")
public class LeastMedianOfSquares<Model, Point> implements ModelMatcherPost<Model, Point>, InlierFraction {
	/// random number generator for selecting points
	@Getter private final long randSeed;

	// Each trial has its own seed to enable concurrent implementations that will produce identical results
	protected final FastArray<Random> trialRNG = new FastArray<>(Random.class);

	/// number of times it performs its fit cycle
	@Getter protected final int totalCycles;
	/// how many points it samples to generate a model from
	@Setter protected int sampleSize;

	// if the best model has more than this error then it is considered a bad match
	protected final double maxMedianError;
	protected final ModelManager<Model> modelManager;

	/// Used to create model generators for each thread
	@Getter @Nullable Factory<ModelGenerator<Model, Point>> factoryGenerator;

	/// Used to create distance functions for each thread
	@Getter @Nullable Factory<DistanceFromModel<Model, Point>> factoryDistance;

	// list of indexes converting it from match set to input list
	protected int[] matchToInput = new int[1];

	protected volatile double bestMedian;

	// The specifies the error fraction its optimizing against. Almost always this should be 0.5
	protected double errorFraction = 0.5; // 0.5 = median

	protected final List<Point> inlierSet = new ArrayList<>();
	/// Fraction of samples which will be considered an inlier. <= 0 disables it
	@Getter protected double inlierFraction;

	/// Computes an inlier threshold by assuming the distribution is normal. Sigma defines
	/// the number of stdev the error can be for it to be an inlier. If inlier fraction is
	/// specified it will take precedence.
	@Getter @Setter protected double inlierRouseeuwSigma = 2.5;

	/// Largest error an inlier can have. With [#inlierRouseeuwSigma] it's the computed threshold. With
	/// [#inlierFraction] or too few points to estimate sigma, it's the error of the worst inlier and points with
	/// an identical error might have been excluded. NaN until process() has been called.
	@Getter protected double inlierThreshold = Double.NaN;

	protected @Nullable TrialHelper helper;

	/// Optional function for initializing generator and distance functions
	protected @Setter @Nullable Ransac.InitializeModels<Model, Point> initializeModels;

	Class<Model> modelType;
	Class<Point> pointType;

	/// Configures the algorithm.
	///
	/// @param randSeed Random seed used internally.
	/// @param totalCycles Number of random draws it will make when estimating model parameters.
	/// @param maxMedianError If the best median error is larger than this it is considered a failure.
	public LeastMedianOfSquares( long randSeed,
								 int totalCycles,
								 double maxMedianError,
								 ModelManager<Model> modelManager,
								 Class<Point> pointType ) {
		if (totalCycles <= 0)
			throw new IllegalArgumentException("Number of cycles must be positive");

		this.randSeed = randSeed;
		this.totalCycles = totalCycles;
		this.maxMedianError = maxMedianError;
		this.pointType = pointType;
		this.modelManager = modelManager;

		this.modelType = (Class)modelManager.createModelInstance().getClass();
	}

	/// Configures the algorithm.
	///
	/// @param randSeed Random seed used internally.
	/// @param totalCycles Number of random draws it will make when estimating model parameters.
	public LeastMedianOfSquares( long randSeed,
								 int totalCycles,
								 ModelManager<Model> modelManager,
								 Class<Point> pointType ) {
		this(randSeed, totalCycles, Double.MAX_VALUE, modelManager, pointType);
	}

	@Override
	public void setModel( Factory<ModelGenerator<Model, Point>> factoryGenerator,
						  Factory<DistanceFromModel<Model, Point>> factoryDistance ) {
		this.factoryGenerator = factoryGenerator;
		this.factoryDistance = factoryDistance;
		this.helper = new TrialHelper();
		sampleSize = helper.modelGenerator.getMinimumPoints();
	}

	/// Fraction of samples which will be considered an inlier. <= 0 disables it
	public void setInlierFraction( double inlierFraction ) {
		if (inlierFraction > 1.0)
			throw new IllegalArgumentException("Inlier fraction must be <= 1");
		this.inlierFraction = inlierFraction;
	}

	@Override
	public boolean process( List<Point> dataSet ) {
		if (dataSet.size() < sampleSize)
			return false;

		checkTrialGenerators();

		int N = dataSet.size();

		// make sure the array is large enough.  If not declare a new one that is
		if (matchToInput.length < N) {
			matchToInput = new int[N];
		}
		TrialHelper helper = Objects.requireNonNull(this.helper, "Need to call setModel()");
		helper.initialize(N);

		bestMedian = Double.MAX_VALUE;

		for (int trial = 0; trial < totalCycles; trial++) {
			// See RANSAC for a detailed description for why this is done. It's related to concurrency
			randomDraw(helper.selectedIdx, N, sampleSize, trialRNG.get(trial));
			addSelect(helper.selectedIdx, sampleSize, dataSet, helper.initialSample);

			if (!helper.modelGenerator.generate(helper.initialSample, helper.candidate))
				continue;

			helper.modelDistance.setModel(helper.candidate);
			helper.modelDistance.distances(dataSet, helper.errors.data);

			double median = QuickSelect.select(helper.errors.data, selectThresholdIndex(N, errorFraction), N);

			if (median < bestMedian) {
				helper.swapModels();
				bestMedian = median;
			}
		}

		computeInliers(dataSet, N, helper);

		// If bestMedian == MAX_VALUE that means no model was found. This needs to fail even if maxMedianError
		// has been set to MAX_VALUE.
		return bestMedian != Double.MAX_VALUE && bestMedian < maxMedianError;
	}

	/// Computes the inlier set for the best model using [#inlierFraction] or [#inlierRouseeuwSigma]
	protected void computeInliers( List<Point> dataSet, int N, TrialHelper helper ) {
		inlierThreshold = Double.NaN;
		inlierSet.clear();
		helper.modelDistance.setModel(helper.bestParam);
		helper.modelDistance.distances(dataSet, helper.errors.data);

		if (inlierFraction > 0) {
			computeInliersFromFraction(dataSet, N, helper);
		} else {
			computeInliersFromSigma(dataSet, N, helper);
		}
	}

	/// Selects the [#inlierFraction] of points with the smallest error. Assumes errors have been computed.
	protected void computeInliersFromFraction( List<Point> dataSet, int n, TrialHelper helper ) {
		// ceil() so there's at least as many inliers as requested. Tolerance avoids rounding up 3.0000000000000004.
		// Can't exceed n since process() already rejected n < sampleSize
		int numPts = Math.max(sampleSize, (int)Math.ceil(n*inlierFraction - 1e-12));
		selectSmallestErrors(dataSet, n, numPts, helper);
	}

	/// Adds the numPts points with the smallest errors to the inlier set and sets the threshold to the largest error
	protected void selectSmallestErrors( List<Point> dataSet, int n, int numPts, TrialHelper helper ) {
		int[] indexes = new int[n];
		// k is an index, so the numPts smallest are at 0 to numPts-1
		QuickSelect.selectIndex(helper.errors.data, numPts - 1, n, indexes);
		for (int i = 0; i < numPts; i++) {
			int origIndex = indexes[i];
			inlierSet.add(dataSet.get(origIndex));
			matchToInput[i] = origIndex;
		}
		inlierThreshold = helper.errors.data[indexes[numPts - 1]];
	}

	/// Computes inlier using Rousseeuw's reweighting. Assumes errors have been computed.
	///
	/// Rousseeuw and Leroy, "Robust Regression and Outlier Detection", 1987
	protected void computeInliersFromSigma( List<Point> dataSet, int N, TrialHelper helper ) {
		// p in the paper is the number of points in the minimal sample, which have zero error. Not the model's DOF.
		// If there are no other points then the noise can't be estimated.
		if (N <= sampleSize) {
			selectSmallestErrors(dataSet, N, N, helper);
			return;
		}

		// select() will modify the array, so a copy is needed
		helper.workErrors.setTo(helper.errors.data, 0, N);
		double median = QuickSelect.select(helper.workErrors.data, selectThresholdIndex(N, 0.5), N);

		// sigma = 1.4826*(1 + 5/(n - p))*sqrt(median(r^2)). Distance is not squared so sqrt(median(r^2)) = median(|r|)
		// p=sampleSize should be verified more carefully. Original justification is heuristic and obtaining
		// the model DOF is problematic.
		double sigma = 1.4826*(1.0 + 5.0/(N - sampleSize))*median;
		inlierThreshold = inlierRouseeuwSigma*sigma;

		for (int i = 0; i < N; i++) {
			double error = helper.errors.data[i];
			if (Math.abs(error) <= inlierThreshold) {
				matchToInput[inlierSet.size()] = i;
				inlierSet.add(dataSet.get(i));
			}
		}
	}

	/// Index of the element at the specified fraction. Clamped so it's always a valid index
	protected static int selectThresholdIndex( int N, double fraction ) {
		return Math.min(N - 1, (int)(N*fraction + 0.5));
	}

	/// If the maximum number of iterations has changed then re-generate the RNG for each trial
	protected void checkTrialGenerators() {
		if (trialRNG.size == totalCycles) {
			return;
		}
		Random rand = new Random(randSeed);
		trialRNG.resize(totalCycles);
		for (int i = 0; i < totalCycles; i++) {
			trialRNG.set(i, new Random(rand.nextLong()));
		}
	}

	protected class TrialHelper {
		// generates an initial model given a set of points
		ModelGenerator<Model, Point> modelGenerator = Objects.requireNonNull(factoryGenerator).newInstance();

		// computes the distance a point is from the model
		DistanceFromModel<Model, Point> modelDistance = Objects.requireNonNull(factoryDistance).newInstance();

		// Initial sample to generate the model from
		final List<Point> initialSample = new ArrayList<>();

		// the best model found so far
		Model bestParam = modelManager.createModelInstance();
		// the current model being considered
		Model candidate = modelManager.createModelInstance();

		// Which indexes were selected
		protected final DogArray_I32 selectedIdx = new DogArray_I32();

		// stores all the errors for quicker sorting
		protected final DogArray_F64 errors = new DogArray_F64();
		// work space for computing the median without modifying errors
		protected final DogArray_F64 workErrors = new DogArray_F64();

		public void initialize( int datasetSize ) {
			selectedIdx.reset();
			errors.resize(datasetSize);
			if (matchToInput.length != datasetSize) {
				matchToInput = new int[datasetSize];
			}

			if (initializeModels != null)
				initializeModels.initialize(modelGenerator, modelDistance);
		}

		public void swapModels() {
			Model t = bestParam;
			bestParam = candidate;
			candidate = t;
		}
	}

	@Override
	public double getErrorFraction() {
		return errorFraction;
	}

	@Override
	public void setErrorFraction( double errorFraction ) {
		this.errorFraction = errorFraction;
	}

	@Override
	public Model getModelParameters() {
		return Objects.requireNonNull(helper).bestParam;
	}

	/// Returns the inlier set. If [#inlierFraction] > 0 then it's that fraction of points with the smallest error,
	/// otherwise it's found using Rousseeuw's reweighting and [#inlierRouseeuwSigma].
	///
	/// @return Set of points that are inliers to the returned model parameters.
	@Override
	public List<Point> getMatchSet() {
		return inlierSet;
	}

	@Override
	public int getInputIndex( int matchIndex ) {
		return matchToInput[matchIndex];
	}

	/// Value of the best median error.
	@Override
	public double getFitQuality() {
		return bestMedian;
	}

	@Override
	public int getMinimumSize() {
		return sampleSize;
	}

	@Override
	public void reset() {
		trialRNG.resize(0);
	}

	@Override
	public Class<Point> getPointType() {
		return pointType;
	}

	@Override
	public Class<Model> getModelType() {
		return modelType;
	}
}
