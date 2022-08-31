/*
 * Copyright (c) 2012-2022, Peter Abeles. All Rights Reserved.
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

/**
 * <p>
 * Another technique similar to RANSAC known as Least Median of Squares (LMedS).  For each iteration a small
 * number N points are selected. A model is fit to these points and then the error is computed for the whole
 * set.  The model which minimizes the median is selected as the final model.  No pruning or formal
 * selection of inlier set is done.
 * </p>
 *
 * @author Peter Abeles
 */
// TODO Better algorithm for selecting the inlier set.
// Maybe revert this back to the way it was before and just have it be a separate alg entirely.
@SuppressWarnings("NullAway.Init")
public class LeastMedianOfSquares<Model, Point> implements ModelMatcherPost<Model, Point>, InlierFraction {
	/** random number generator for selecting points */
	@Getter private final long randSeed;

	// Each trial has its own seed to enable concurrent implementations that will produce identical results
	protected final FastArray<Random> trialRNG = new FastArray<>(Random.class);

	/** number of times it performs its fit cycle */
	@Getter protected final int totalCycles;
	// how many points it samples to generate a model from
	protected int sampleSize;
	// if the best model has more than this error then it is considered a bad match
	protected final double maxMedianError;
	protected final ModelManager<Model> ModelManager;

	/** Used to create model generators for each thread */
	@Getter @Nullable Factory<ModelGenerator<Model, Point>> factoryGenerator;

	/** Used to create distance functions for each thread */
	@Getter @Nullable Factory<DistanceFromModel<Model, Point>> factoryDistance;

	// list of indexes converting it from match set to input list
	protected int[] matchToInput = new int[1];

	protected volatile double bestMedian;

	// The specifies the error fraction its optimizing against. Almost always this should be 0.5
	protected double errorFraction = 0.5; // 0.5 = median

	protected List<Point> inlierSet;
	protected final double inlierFrac;

	protected @Nullable TrialHelper helper;

	/** Optional function for initializing generator and distance functions */
	protected @Setter @Nullable Ransac.InitializeModels<Model, Point> initializeModels;

	Class<Model> modelType;
	Class<Point> pointType;

	/**
	 * Configures the algorithm.
	 *
	 * @param randSeed Random seed used internally.
	 * @param totalCycles Number of random draws it will make when estimating model parameters.
	 * @param maxMedianError If the best median error is larger than this it is considered a failure.
	 * @param inlierFraction Data which is this fraction or lower is considered an inlier and used to
	 * recompute model parameters at the end.  Set to 0 to turn off. Domain: 0 to 1.
	 */
	public LeastMedianOfSquares( long randSeed,
								 int totalCycles,
								 double maxMedianError,
								 double inlierFraction,
								 ModelManager<Model> modelManager,
								 Class<Point> pointType ) {
		if (totalCycles <= 0)
			throw new IllegalArgumentException("Number of cycles must be positive");

		this.randSeed = randSeed;
		this.totalCycles = totalCycles;
		this.maxMedianError = maxMedianError;
		this.inlierFrac = inlierFraction;
		this.pointType = pointType;
		this.ModelManager = modelManager;

		this.modelType = (Class)modelManager.createModelInstance().getClass();

		if (inlierFrac > 0.0) {
			inlierSet = new ArrayList<>();
		} else if (inlierFrac > 1.0) {
			throw new IllegalArgumentException("Inlier fraction must be <= 1");
		}
	}

	/**
	 * Configures the algorithm.
	 *
	 * @param randSeed Random seed used internally.
	 * @param totalCycles Number of random draws it will make when estimating model parameters.
	 */
	public LeastMedianOfSquares( long randSeed,
								 int totalCycles,
								 ModelManager<Model> modelManager,
								 Class<Point> pointType ) {
		this(randSeed, totalCycles, Double.MAX_VALUE, 0, modelManager, pointType);
	}

	@Override
	public void setModel( Factory<ModelGenerator<Model, Point>> factoryGenerator,
						  Factory<DistanceFromModel<Model, Point>> factoryDistance ) {
		this.factoryGenerator = factoryGenerator;
		this.factoryDistance = factoryDistance;
		this.helper = new TrialHelper();
		sampleSize = helper.modelGenerator.getMinimumPoints();
	}

	/**
	 * Number of points it samples to compute a model from.  Typically this is the minimum number of points needed.
	 *
	 * @param sampleSize Number of points sampled when computing the model.
	 */
	public void setSampleSize( int sampleSize ) {
		this.sampleSize = sampleSize;
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

			double median = QuickSelect.select(helper.errors.data, (int)(N*errorFraction + 0.5), N);

			if (median < bestMedian) {
				helper.swapModels();
				bestMedian = median;
			}
		}

		// if configured to do so compute the inlier set
		computeInlierSet(dataSet, N, helper);

		return bestMedian <= maxMedianError;
	}

	protected void computeInlierSet( List<Point> dataSet, int n,
									 TrialHelper helper ) {
		int numPts = (int)(n*inlierFrac);

		if (inlierFrac > 0 && numPts > sampleSize) {
			inlierSet.clear();
			helper.modelDistance.setModel(helper.bestParam);
			helper.modelDistance.distances(dataSet, helper.errors.data);

			int[] indexes = new int[n];
			QuickSelect.selectIndex(helper.errors.data, numPts, n, indexes);
			for (int i = 0; i < numPts; i++) {
				int origIndex = indexes[i];
				inlierSet.add(dataSet.get(origIndex));
				matchToInput[i] = origIndex;
			}
		} else {
			inlierSet = dataSet;
		}
	}

	/**
	 * If the maximum number of iterations has changed then re-generate the RNG for each trial
	 */
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
		Model bestParam = ModelManager.createModelInstance();
		// the current model being considered
		Model candidate = ModelManager.createModelInstance();

		// Which indexes were selected
		protected final DogArray_I32 selectedIdx = new DogArray_I32();

		// stores all the errors for quicker sorting
		protected final DogArray_F64 errors = new DogArray_F64();

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

	/**
	 * If configured to computer the inlier set it returns the computed inliers.  Otherwise
	 * it returns the data set orginally passed in.
	 *
	 * @return Set of points that are inliers to the returned model parameters..
	 */
	@Override
	public List<Point> getMatchSet() {
		return inlierSet;
	}

	@Override
	public int getInputIndex( int matchIndex ) {
		return matchToInput[matchIndex];
	}

	/**
	 * Value of the best median error.
	 */
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
