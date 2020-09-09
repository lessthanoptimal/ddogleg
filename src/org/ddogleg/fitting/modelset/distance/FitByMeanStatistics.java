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

import org.ddogleg.fitting.modelset.DistanceFromModel;

import java.util.ArrayDeque;
import java.util.Iterator;


/**
 * Computes the mean error and prunes points based on the number of standard deviations they are away.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class FitByMeanStatistics<Model, Point> implements StatisticalFit<Model,Point> {

	protected DistanceFromModel<Model,Point> modelError;
	protected ArrayDeque<PointIndex<Point>> allPoints = new ArrayDeque<>();

	// the number of standard deviations away that points are pruned
	private final double pruneThreshold;

	// the mean error
	private double meanError;
	// the standard deviation of the error
	private double stdError;

	/**
	 * @param pruneThreshold Number of standard deviations away that points will be pruned.
	 */
	public FitByMeanStatistics(double pruneThreshold) {
		this.pruneThreshold = pruneThreshold;
	}

	@Override
	public void init(DistanceFromModel<Model,Point> modelError, ArrayDeque<PointIndex<Point>> allPoints ) {

		this.modelError = modelError;
		this.allPoints = allPoints;
	}

	@Override
	public void computeStatistics() {
		computeMean();
		computeStandardDeviation();
	}

	@Override
	public void prune() {
		double thresh = stdError * pruneThreshold;

		Iterator<PointIndex<Point>> iter = allPoints.iterator();
		while( iter.hasNext() ) {
			Point pt = iter.next().data;

			// only prune points which are less accurate than the mean
			if (modelError.distance(pt) - meanError > thresh) {
				iter.remove();
			}
		}
	}

	@Override
	public double getErrorMetric() {
		return meanError;
	}

	/**
	 * Computes the mean and standard deviation of the points from the model
	 */
	private void computeMean() {
		meanError = 0;

		int size = allPoints.size();
		for (PointIndex<Point> inlier : allPoints) {
			Point pt = inlier.data;

			meanError += modelError.distance(pt);
		}

		meanError /= size;

	}

	private void computeStandardDeviation() {
		stdError = 0;
		int size = allPoints.size();
		for (PointIndex<Point> inlier : allPoints) {
			Point pt = inlier.data;

			double e = modelError.distance(pt) - meanError;
			stdError += e * e;
		}

		stdError = Math.sqrt(stdError / size);
	}
}
