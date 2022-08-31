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

package org.ddogleg.fitting.modelset.ransac;

import lombok.Setter;
import org.ddogleg.fitting.modelset.*;
import org.ddogleg.struct.DogArray_I32;
import org.ddogleg.struct.Factory;
import org.ddogleg.struct.FastArray;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * <p>
 * RANSAC is an abbreviation for "RANdom SAmple Consensus" and is an iterative algorithm.  The model with the
 * largest set of inliers is found by randomly sampling the set of points and fitting a model.  The algorithm
 * terminates when the maximum number of iterations has been reached. An inlier is defined as a point which
 * has an error less than a user specified threshold to the estimated model being considered.  The algorithm was
 * first published by Fischler and Bolles in 1981."
 * </p>
 *
 * <p>
 * Sample Points: By default the minimum number of points are sampled.  The user to override this default and set
 * it to any number.
 * </p>
 *
 * @author Peter Abeles
 */
public class Ransac<Model, Point> implements ModelMatcherPost<Model, Point>, InlierThreshold {
	// how many points are drawn to generate the model
	protected int sampleSize;

	// how close a point needs to be considered part of the model
	protected double thresholdFit;

	@Nullable Factory<ModelGenerator<Model, Point>> factoryGenerator;
	@Nullable Factory<DistanceFromModel<Model, Point>> factoryDistance;

	// Used to create new models
	protected ModelManager<Model> masterModelManager;

	// used to randomly select points/samples
	protected long randSeed;

	// Each trial has its own seed to enable concurrent implementations that will produce identical results
	protected final FastArray<Random> trialRNG = new FastArray<>(Random.class);

	// the maximum number of iterations it will perform
	protected int maxIterations;

	// RANSAC's internal state while trying to find the best solution
	protected @Nullable TrialHelper helper;

	/** Optional function for initializing generator and distance functions */
	protected @Setter @Nullable InitializeModels<Model, Point> initializeModels;

	Class<Model> modelType;
	Class<Point> pointType;

	/**
	 * Creates a new instance of the ransac algorithm.  The number of points sampled will default to the
	 * minimum number.  To override this default invoke {@link #setSampleSize(int)}.
	 *
	 * @param randSeed The random seed used by the random number generator.
	 * @param maxIterations The maximum number of iterations the RANSAC algorithm will perform.
	 * @param thresholdFit How close of a fit a points needs to be to the model to be considered a fit.
	 */
	public Ransac( long randSeed,
				   int maxIterations,
				   double thresholdFit,
				   ModelManager<Model> modelManager,
				   Class<Point> pointType ) {
		if (maxIterations <= 0)
			throw new IllegalArgumentException("Number of iterations must be positive");
		this.masterModelManager = modelManager;
		this.randSeed = randSeed;
		this.maxIterations = maxIterations;
		this.pointType = pointType;
		this.thresholdFit = thresholdFit;
		modelType = (Class)modelManager.createModelInstance().getClass();
	}

	@Override
	public boolean process( List<Point> dataSet ) {
		// see if it has the minimum number of points
		if (dataSet.size() < sampleSize)
			return false;

		// make sure there is a RNG for each trial
		checkTrialGenerators();

		// iterate until it has exhausted all iterations or stop if the entire data set
		// is in the inlier set
		TrialHelper helper = Objects.requireNonNull(this.helper, "Need to call setModel()");
		helper.reset();
		for (int trial = 0; trial < maxIterations && helper.bestFitPoints.size() != dataSet.size(); trial++) {
			// sample the a small set of points, then make sure the index ordering is back to the original
			// This more convoluted way of sampling the array is needed to ensure single and threaded code
			// produces the exact same results. To always produce the same results the order of the sampled
			// array has to be the same at the start of each trial.
			//
			// The original code, where it modified a copy of dataSet would be slightly faster in the single
			// thread case
			randomDraw(helper.selectedIdx, dataSet.size(), sampleSize, trialRNG.get(trial));
			addSelect(helper.selectedIdx, sampleSize, dataSet, helper.initialSample);

			// get the candidate(s) for this sample set
			if (!helper.modelGenerator.generate(helper.initialSample, helper.candidateParam))
				continue;

			// see if it can find a model better than the current best one
			if (!helper.selectMatchSet(dataSet, helper.bestFitPoints.size(), thresholdFit, helper.candidateParam))
				continue;

			// save this results
			if (helper.bestFitPoints.size() < helper.candidatePoints.size()) {
				helper.swapCandidateWithBest();
			}
		}

		return helper.bestFitPoints.size() > 0;
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
	 * If the maximum number of iterations has changed then re-generate the RNG for each trial
	 */
	protected void checkTrialGenerators() {
		if (trialRNG.size == maxIterations) {
			return;
		}
		Random rand = new Random(randSeed);
		trialRNG.resize(maxIterations);
		for (int i = 0; i < maxIterations; i++) {
			trialRNG.set(i, new Random(rand.nextLong()));
		}
	}

	/**
	 * Performs a random draw in the dataSet.  When an element is selected it is moved to the end of the list
	 * so that it can't be selected again.
	 *
	 * @param dataSet List that points are to be selected from.  Modified.
	 */
	public static <T> void randomDraw( List<T> dataSet, int numSample,
									   List<T> initialSample, Random rand ) {
		initialSample.clear();

		for (int i = 0; i < numSample; i++) {
			// index of last element that has not been selected
			int indexLast = dataSet.size() - i - 1;
			// randomly select an item from the list which has not been selected
			int indexSelected = rand.nextInt(indexLast + 1);

			T a = dataSet.get(indexSelected);
			initialSample.add(a);

			// Swap the selected item with the last unselected item in the list. This way the selected
			// item can't be selected again and the last item can now be selected
			dataSet.set(indexSelected, dataSet.set(indexLast, a));
		}
	}

	/**
	 * Randomly selects a set of points in indexes. The selects points are put at the end of the array
	 */
	public static void randomDraw( DogArray_I32 indexes, int size, int numSample, Random rand ) {
		// If this is the first time, fill it numbers counting up to size
		if (indexes.size != size) {
			indexes.resize(size);
			for (int i = 0; i < size; i++) {
				indexes.data[i] = i;
			}
		}

		for (int i = 0; i < numSample; i++) {
			// index of last element that has not been selected
			int last = size - i - 1;
			// randomly select an item from the list which has not been selected
			int selected = rand.nextInt(last + 1);

			// put the selected value at the end
			int tmp = indexes.get(last);
			indexes.set(last, indexes.get(selected));
			indexes.set(selected, tmp);
		}
	}

	/**
	 * Adds the selected elements to the initialSample list and undoes the shuffling. There is probably a slightly
	 * more elegant way to do this...
	 */
	public static <T> void addSelect( DogArray_I32 indexes, int numSample, List<T> dataSet, List<T> initialSample ) {
		initialSample.clear();
		int start = indexes.size - numSample;
		for (int i = start; i < indexes.size; i++) {
			int selectedIdx = indexes.get(i);
			initialSample.add(dataSet.get(selectedIdx));
			if (selectedIdx < start)
				indexes.set(selectedIdx, selectedIdx);
		}
		for (int i = start; i < indexes.size; i++) {
			indexes.set(i, i);
		}
	}

	protected class TrialHelper {
		// generates an initial model given a set of points
		ModelGenerator<Model, Point> modelGenerator = Objects.requireNonNull(factoryGenerator).newInstance();

		// computes the distance a point is from the model
		DistanceFromModel<Model, Point> modelDistance = Objects.requireNonNull(factoryDistance).newInstance();

		List<Point> initialSample = new ArrayList<>();

		// list of points which are a candidate for the best fit set
		List<Point> candidatePoints = new ArrayList<>();

		// list of samples from the best fit model
		List<Point> bestFitPoints = new ArrayList<>();

		// the best model found so far
		Model bestFitParam = masterModelManager.createModelInstance();
		// the current model being considered
		Model candidateParam = masterModelManager.createModelInstance();

		// list of indexes converting it from match set to input list
		protected int[] matchToInput = new int[1];
		protected int[] bestMatchToInput = new int[1];

		// Which indexes were selected
		DogArray_I32 selectedIdx = new DogArray_I32();

		/**
		 * Looks for points in the data set which closely match the current best
		 * fit model in the optimizer.
		 *
		 * @param dataSet The points being considered
		 */
		protected boolean selectMatchSet( List<Point> dataSet, int bestModelSize, double threshold, Model param ) {
			if (dataSet.size() > matchToInput.length) {
				matchToInput = new int[dataSet.size()];
				bestMatchToInput = new int[dataSet.size()];
			}

			candidatePoints.clear();
			modelDistance.setModel(param);

			// If it fails more than this it can't possibly beat the best model and should stop
			int maxFailures = dataSet.size() - bestModelSize;

			for (int i = 0; i < dataSet.size() && maxFailures >= 0; i++) {
				Point point = dataSet.get(i);

				double distance = modelDistance.distance(point);
				if (distance < threshold) {
					matchToInput[candidatePoints.size()] = i;
					candidatePoints.add(point);
				} else {
					maxFailures--;
				}
			}

			return maxFailures >= 0;
		}

		/**
		 * Turns the current candidates into the best ones.
		 */
		protected void swapCandidateWithBest() {
			List<Point> tempPts = candidatePoints;
			candidatePoints = bestFitPoints;
			bestFitPoints = tempPts;

			int[] tempIndex = matchToInput;
			matchToInput = bestMatchToInput;
			bestMatchToInput = tempIndex;

			Model m = candidateParam;
			candidateParam = bestFitParam;
			bestFitParam = m;
		}

		public void reset() {
			candidatePoints.clear();
			bestFitPoints.clear();
			selectedIdx.reset();

			if (initializeModels != null)
				initializeModels.initialize(modelGenerator, modelDistance);
		}
	}

	@Override
	public List<Point> getMatchSet() {
		return Objects.requireNonNull(helper).bestFitPoints;
	}

	@Override
	public int getInputIndex( int matchIndex ) {
		return Objects.requireNonNull(helper).bestMatchToInput[matchIndex];
	}

	@Override
	public Model getModelParameters() {
		return Objects.requireNonNull(helper).bestFitParam;
	}

	@Override
	public double getFitQuality() {
		return Objects.requireNonNull(helper).bestFitPoints.size();
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations( int maxIterations ) {
		this.maxIterations = maxIterations;
	}

	@Override
	public int getMinimumSize() {
		return sampleSize;
	}

	@Override
	public void reset() {
		trialRNG.resize(0);
	}

	/**
	 * Override the number of points that are sampled and used to generate models.  If this value
	 * is not set it defaults to the minimum number.
	 *
	 * @param sampleSize Number of sample points.
	 */
	public void setSampleSize( int sampleSize ) {
		this.sampleSize = sampleSize;
	}

	@Override
	public double getThresholdFit() {
		return thresholdFit;
	}

	@Override
	public void setThresholdFit( double thresholdFit ) {
		this.thresholdFit = thresholdFit;
	}

	@Override
	public Class<Point> getPointType() {
		return pointType;
	}

	@Override
	public Class<Model> getModelType() {
		return modelType;
	}

	@FunctionalInterface
	public interface InitializeModels<Model, Point> {
		void initialize( ModelGenerator<Model, Point> generator, DistanceFromModel<Model, Point> distance );
	}
}