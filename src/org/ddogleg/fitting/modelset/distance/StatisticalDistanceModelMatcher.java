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

package org.ddogleg.fitting.modelset.distance;

import org.ddogleg.fitting.modelset.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * Outliers are removed by first fitting a model to all the data points.  Points which
 * deviate the most are removed and the best fit parameters are recomputed.  This is done
 * until some error metric changes very little or the maximum number of iterations has been
 * exceeded.  Works well when the inlier set is much greater than the noise.
 * </p>
 *
 * <p>
 * Anyone have a good reference paper for this approach?
 * </p>
 *
 * @author Peter Abeles
 */
public class StatisticalDistanceModelMatcher<Model, Point> implements ModelMatcher<Model,Point> {

	// maximum number of times it will perform the pruning process
	private final int maxIterations;
	// if the error changes by less than this amount it stops iterating
	private final double minChange;

	// current best fit parameters
	protected Model param;
	protected Model currParam;

	// what computes the error metrics
	private final StatisticalFit<Model,Point> errorAlg;

	// error in previous iteration
	protected double oldCenter;
	// the error for the current fit parameters
	protected double centerError;
	// if the center error is less than this value it stops iterating immediately
	private final double exitCenterError;
	// if the error is more than this amount it failed
	private final double failError;
	// the minimum number of points that can be left over before it is considered a failure
	private final int minFitPoints;

	// computes a set of model parameters from a list of points
	private final ModelGenerator<Model,Point> modelFitter;
	// computes the difference between the model and a point
	private final DistanceFromModel<Model,Point> modelError;

	// converts the model into an array parameter format
	private final ModelCodec<Model> codec;

	// list containing points that are to be pruned
	ArrayDeque<PointIndex<Point>> pruneList = new ArrayDeque<>();
	// set of points which fit the model
	private final List<Point> inliers = new ArrayList<>();
	// list of indexes converting it from match set to input list
	private int []matchToInput = new int[1];

	/**
	 * Creates a new model matcher.  The type of statistics it uses is specified by "statistics".
	 * 0 for mean and standard deviation and 1 for median and percentile.  If statistics=0 is set then the threshold
	 * correspond to the number of standard deviations a point can be away from the mean before it is
	 * pruned.  If statistics=1 then points which have a percentile error more than that value are pruned.
	 *
	 * @param maxIterations   The maximum number of iterations it will perform.
	 * @param minChange	   It will stop iterating of the change in error is less than this amount.
	 * @param exitCenterError If the error is less than this value it will stop iterating.
	 * @param failError	   If the final error is more than this amount it failed.
	 * @param minFitPoints	If fewer than this number of points remain, then it failed.
	 * @param statistics	  0 = mean statistics and 1 = percentile statistics
	 * @param pruneThreshold  Points which exceed this statistic are pruned. See {@link StatisticalDistance} for details.
	 * @param modelFitter	 Fits a model to a set of points
	 * @param modelError	  Computes the error between a point and the model.
	 */
	public StatisticalDistanceModelMatcher(int maxIterations,
										   double minChange,
										   double exitCenterError,
										   double failError,
										   int minFitPoints,
										   StatisticalDistance statistics,
										   double pruneThreshold,
										   ModelManager<Model> modelManager ,
										   ModelGenerator<Model,Point> modelFitter,
										   DistanceFromModel<Model,Point> modelError,
										   ModelCodec<Model> codec ) {
		this.maxIterations = maxIterations;
		this.minChange = minChange;
		this.exitCenterError = exitCenterError;
		this.failError = failError;
		this.minFitPoints = minFitPoints;
		this.modelFitter = modelFitter;
		this.modelError = modelError;
		this.codec = codec;

		param = modelManager.createModelInstance();
		currParam = modelManager.createModelInstance();

		switch (statistics) {
			case MEAN:
				errorAlg = new FitByMeanStatistics<>(pruneThreshold);
				break;

			case PERCENTILE:
				errorAlg = new FitByMedianStatistics<>(pruneThreshold);
				break;

			default:
				throw new IllegalArgumentException("Unknown statistics selected");
		}
	}

	@Override
	public boolean process(List<Point> dataSet ) {
		// there must be at least the minFitPoints for it to run
		if (dataSet.size() < minFitPoints)
			return false;

		if( dataSet.size() > matchToInput.length )
			matchToInput = new int[ dataSet.size() ];

		pruneList.clear();
		inliers.clear();
		for( int i = 0; i < dataSet.size(); i++ )
			pruneList.add(new PointIndex<>(dataSet.get(i), i));
		inliers.clear();

		errorAlg.init(modelError, pruneList);

		oldCenter = Double.MAX_VALUE;
		boolean converged = false;

		// iterate until it converges or the maximum number of iterations has been exceeded
		int i = 0;
		for (; i < maxIterations && !converged && pruneList.size() >= minFitPoints; i++) {

			inliers.clear();
			for( PointIndex<Point> p : pruneList) {
				inliers.add(p.data);
			}

			if (!modelFitter.generate(inliers, currParam)) {
				// failed to fit the model, so stop before it screws things up
				break;
			}

			modelError.setModel(currParam);
			errorAlg.computeStatistics();
			centerError = errorAlg.getErrorMetric();

			// see if the error is so small that it no longer needs to run
			if (centerError < exitCenterError)
				converged = true;
				// if the model did not significantly change then stop iterating
			else if (computeDiff(currParam, param) <= minChange) {
				converged = true;
			}
			Model temp = param;
			param = currParam;
			currParam = temp;

			if (!converged) {
				errorAlg.prune();
				oldCenter = centerError;
			}
		}

		boolean ret = centerError < failError && pruneList.size() >= minFitPoints;

		if( ret ) {
			inliers.clear();
			int index = 0;
			for( PointIndex<Point> p : pruneList) {
				inliers.add(p.data);
				matchToInput[index++] = p.index;
			}
		}

		return ret;
	}

	/**
	 * Computes the difference between the two parameters.
	 */
	protected double computeDiff(Model modelA, Model modelB ) {

		double[] paramA = new double[ codec.getParamLength() ];
		double[] paramB = new double[ codec.getParamLength() ];

		codec.encode(modelA,paramA);
		codec.encode(modelB,paramB);

		double total = 0;

		for (int i = 0; i < paramA.length; i++) {
			total += Math.abs(paramA[i] - paramB[i]);
		}

		return total / paramA.length;
	}

	@Override
	public Model getModelParameters() {
		return param;
	}

	@Override
	public List<Point> getMatchSet() {
		return inliers;
	}

	@Override
	public int getInputIndex(int matchIndex) {
		return matchToInput[matchIndex];
	}

	@Override
	public double getFitQuality() {
		return centerError;
	}

	@Override
	public int getMinimumSize() {
		return minFitPoints;
	}

	@Override
	public void reset() {}

	@Override
	public Class<Point> getPointType() {
		return modelError.getPointType();
	}

	@Override
	public Class<Model> getModelType() {
		return modelError.getModelType();
	}
}
