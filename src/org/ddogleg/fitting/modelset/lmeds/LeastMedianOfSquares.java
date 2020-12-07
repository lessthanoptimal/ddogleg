/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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


import org.ddogleg.fitting.modelset.*;
import org.ddogleg.fitting.modelset.ransac.Ransac;
import org.ddogleg.sorting.QuickSelect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * <p>
 * Another technique similar to RANSAC known as Least Median of Squares (LMedS).  For each iteration a small
 * number N points are selected. A model is fit to these points and then the error is computed for the whole
 * set.  The model which minimizes the median is selected as the final model.  No pruning or formal
 * selection of inlier set is done.
 * </p>
 * @author Peter Abeles
 */
// TODO Better algorithm for selecting the inlier set.
// Maybe revert this back to the way it was before and just have it be a separate alg entirely.
@SuppressWarnings("NullAway.Init")
public class LeastMedianOfSquares<Model, Point> implements ModelMatcher<Model, Point>, InlierFraction {
	// random number generator for selecting points
	private Random rand;
	private long randSeed;

	// number of times it performs its fit cycle
	private int totalCycles;
	// how many points it samples to generate a model from
	private int sampleSize;
	// if the best model has more than this error then it is considered a bad match
	private double maxMedianError;
	// fits a model to the provided data
	private ModelGenerator<Model,Point> generator;
	// computes the error for a point to the model
	private DistanceFromModel<Model,Point> errorMetric;

	// where the initial small set of points is stored
	private List<Point> smallSet = new ArrayList<Point>();

	// parameter being considered
	private Model candidate;
	// the parameter with the best error
	private Model bestParam;
	private double bestMedian;

	// The specifies the error fraction its optimizing against. Almost always this should be 0.5
	private double errorFraction = 0.5; // 0.5 = median

	// copy of the input data set so that it can be modified
	protected List<Point> dataSet = new ArrayList<>();

	// stores all the errors for quicker sorting
	private double []errors = new double[1];

	// list of indexes converting it from match set to input list
	private int []matchToInput = new int[1];

	private List<Point> inlierSet;
	private double inlierFrac;

	/**
	 * Configures the algorithm.
	 *
	 * @param randSeed Random seed used internally.
	 * @param totalCycles Number of random draws it will make when estimating model parameters.
	 * @param maxMedianError If the best median error is larger than this it is considered a failure.
	 * @param inlierFraction Data which is this fraction or lower is considered an inlier and used to
	 *                          recompute model parameters at the end.  Set to 0 to turn off. Domain: 0 to 1.
	 * @param generator Creates a list of model hypotheses from a small set of points.
	 * @param errorMetric Computes the error between a point and a model
	 */
	public LeastMedianOfSquares( long randSeed ,
								 int totalCycles ,
								 double maxMedianError ,
								 double inlierFraction ,
								 ModelManager<Model> modelManager,
								 ModelGenerator<Model,Point> generator,
								 DistanceFromModel<Model,Point> errorMetric )
	{
		this.randSeed = randSeed;
		this.rand = new Random(randSeed);
		this.totalCycles = totalCycles;
		this.maxMedianError = maxMedianError;
		this.inlierFrac = inlierFraction;
		this.generator = generator;
		this.errorMetric = errorMetric;

		bestParam = modelManager.createModelInstance();
		candidate = modelManager.createModelInstance();
		this.sampleSize = generator.getMinimumPoints();

		if( inlierFrac > 0.0 ) {
			inlierSet = new ArrayList<Point>();
		} else if( inlierFrac > 1.0 ) {
			throw new IllegalArgumentException("Inlier fraction must be <= 1");
		}
	}

	/**
	 * Configures the algorithm.
	 *
	 * @param randSeed Random seed used internally.
	 * @param totalCycles Number of random draws it will make when estimating model parameters.
	 * @param generator Creates a list of model hypotheses from a small set of points.
	 * @param errorMetric Computes the error between a point and a model
	 */
	public LeastMedianOfSquares( long randSeed ,
								 int totalCycles ,
								 ModelManager<Model> modelManager,
								 ModelGenerator<Model,Point> generator,
								 DistanceFromModel<Model,Point> errorMetric )
	{
		this(randSeed,totalCycles,Double.MAX_VALUE,0,modelManager,generator,errorMetric);
	}

	/**
	 * Number of points it samples to compute a model from.  Typically this is the minimum number of points needed.
	 *
	 * @param sampleSize Number of points sampled when computing the model.
	 */
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	@Override
	public boolean process(List<Point> _dataSet) {
		if( _dataSet.size() < sampleSize )
			return false;

		dataSet.clear();
		dataSet.addAll(_dataSet);
        
		int N = dataSet.size();

		// make sure the array is large enough.  If not declare a new one that is
		if( errors.length < N ) {
			errors = new double[ N ];
			matchToInput = new int[N];
		}

		bestMedian = Double.MAX_VALUE;

		for( int i = 0; i < totalCycles; i++ ) {
			Ransac.randomDraw(dataSet, sampleSize, smallSet, rand);

			if( generator.generate(smallSet, candidate) ) {
				errorMetric.setModel(candidate);
				errorMetric.distances(_dataSet,errors);

				double median = QuickSelect.select(errors, (int)(N*errorFraction+0.5), N);

				if( median < bestMedian ) {
					bestMedian = median;
					Model t = bestParam;
					bestParam = candidate;
					candidate = t;
				}
			}
		}

		// if configured to do so compute the inlier set
		computeInlierSet(_dataSet, N);

		return bestMedian <= maxMedianError;
	}

	private void computeInlierSet(List<Point> dataSet, int n) {
		int numPts = (int)(n *inlierFrac);

		if( inlierFrac > 0 && numPts > sampleSize ) {
			inlierSet.clear();
			errorMetric.setModel(bestParam);
			errorMetric.distances(dataSet,errors);

			int []indexes = new int[n];
			QuickSelect.selectIndex(errors,numPts, n,indexes);
			for( int i = 0; i < numPts; i++ ) {
				int origIndex = indexes[i];
				inlierSet.add( dataSet.get(origIndex) );
				matchToInput[i] = origIndex;
			}
		} else {
			inlierSet = dataSet;
		}
	}

	@Override
	public double getErrorFraction() {
		return errorFraction;
	}

	@Override
	public void setErrorFraction(double errorFraction) {
		this.errorFraction = errorFraction;
	}

	@Override
	public Model getModelParameters() {
		return bestParam;
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
	public int getInputIndex(int matchIndex) {
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
		this.rand = new Random(randSeed);
	}

	@Override
	public Class<Point> getPointType() {
		return errorMetric.getPointType();
	}

	@Override
	public Class<Model> getModelType() {
		return errorMetric.getModelType();
	}
}
