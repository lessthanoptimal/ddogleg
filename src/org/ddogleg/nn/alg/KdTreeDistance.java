/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.nn.alg;

/**
 * Computes the distance between two points. Each point is a tuple.
 *
 * @author Peter Abeles
 */
public interface KdTreeDistance<P> {
	/**
	 * Computes the distance between the two input points
	 * @param a point
	 * @param b point
	 * @return distance
	 */
	double distance(P a , P b );

	/**
	 * Returns the value of an element in the point
	 * @param point (Input) the point
	 * @param index Which element in the point is to be read
	 * @return The value of the element in the point.
	 */
	double valueAt( P point , int index );

	/**
	 * Number of elements in the point
	 */
	int length();
}
