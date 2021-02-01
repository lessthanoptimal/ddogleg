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

package org.ddogleg.clustering;

/**
 * Computes the distance between two points. Each point is a tuple.
 *
 * @author Peter Abeles
 */
public interface PointDistance<P> {
	/**
	 * Computes the distance between the two input points
	 * @param a point
	 * @param b point
	 * @return distance
	 */
	double distance(P a , P b );

	/**
	 * Creates a new instance which has the same configuration and can be run in parallel. Some components
	 * can be shared as long as they are read only and thread safe.
	 */
	PointDistance<P> newInstanceThread();
}
