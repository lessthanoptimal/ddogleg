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

import java.util.List;

/**
 * Computes a model from a set of points and optionally an initial estimate.
 *
 * @author Peter Abeles
 */
public interface ModelFitter<Model,Point> {

	/**
	 * Creates a new instance of the model
	 *
	 * @return New model instance
	 */
	public Model createModelInstance();

	/**
	 * Fits a model to a set of points.
	 *
	 * @param dataSet Set of points the model is being fit to.
	 * @param initial Initial hypothesis
	 * @param found The found model.
	 * @return true if a model was found and false if one was not.
	 */
	public boolean fitModel(List<Point> dataSet, Model initial, Model found);
}
