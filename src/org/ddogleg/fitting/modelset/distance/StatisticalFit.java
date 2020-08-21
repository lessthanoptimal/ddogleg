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


/**
 * Interface for computing error metrics and pruning features.
 *
 * @author Peter Abeles
 */
public interface StatisticalFit<Model, Point> {

	/**
	 * This is called once to provide access to internal data structures of the owner.
	 *
	 * @param modelDistance Computes the error between a point and the model
	 * @param allPoints	   Contains all the points which are currently considered part of the model.
	 */
	void init(DistanceFromModel<Model,Point> modelDistance, ArrayDeque<PointIndex<Point>> allPoints );

	/**
	 * Returns the computed statistical error.
	 *
	 * @return The error.
	 */
	double getErrorMetric();

	/**
	 * Computes the statistic error of the model to the data points.
	 */
	void computeStatistics();

	/**
	 * Prunes points based on the error and the computed statistics.
	 */
	void prune();
}
