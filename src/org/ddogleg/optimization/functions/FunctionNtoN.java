/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.functions;

/**
 * Function with N inputs and N outputs.  The gradient of a function with N inputs and 1 output is the typical usage.
 *
 * @author Peter Abeles
 */
public interface FunctionNtoN {

	/**
	 * Returns the number of inputs and outputs for this function.
	 */
	public int getN();

	/**
	 * Processes the function.
	 *
	 * @param input Array with the inputs of length N.  Not modified.
	 * @param output Array for storing the output of length N.  Modified.
	 */
	public void process( double input[] , double[] output );
}
