/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.fitting.modelset;

/**
 * <p>
 * Given a set of points and a set of models, it selects which model and model parameters best fits the points robustly.
 * Some of the points are assumed to be noise and should be pruned.  The set of points which fit the found
 * parameters and their index in the input list are returned.
 * </p>
 *
 * @param <Point> Type of data point being fitted.
 *
 * @author Peter Abeles
 */
public interface ModelMatcherMulti<Point> extends ModelMatcher<Object,Point> {

	/**
	 * Indicates which model was found to best fit the points.  The index is implementation specific and is likely
	 * to refer to the index inside a list.
	 * @return Index of selected model.
	 */
	public int getModelIndex();

}
