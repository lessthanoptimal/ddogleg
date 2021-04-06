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

package org.ddogleg.fitting.modelset;


/**
 * Used to convert a model to and from an array parameterized format.
 *
 * @author Peter Abeles
 */
public interface ModelCodec<T> {

	/**
	 * Converts the parameter array into a model.
	 *
	 * @param input input model parameters.
	 * @param outputModel Output. The decoded model..
	 */
	void decode( double[] input, T outputModel );

	/**
	 * Converts the provided model into the array format.
	 *
	 * @param inputModel Input model.
	 * @param output Output parameterized model
	 */
	void encode( T inputModel , double[] output );

	/**
	 * Number of elements in array encoded parameters.
	 */
	int getParamLength();
}
