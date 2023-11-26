/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization;

import org.ddogleg.optimization.functions.SchurJacobian;
import org.ejml.data.DMatrix;

/**
 * Wraps around a function and counts the number of times it processes an input.
 *
 * @author Peter Abeles
 */
public class CallCounterSchurJacobian<S extends DMatrix> implements SchurJacobian<S> {

	public int count;
	public SchurJacobian<S> func;

	public CallCounterSchurJacobian( SchurJacobian<S> func ) {
		this.func = func;
	}

	@Override public int getNumOfInputsN() {
		return func.getNumOfInputsN();
	}

	@Override public int getNumOfOutputsM() {
		return func.getNumOfOutputsM();
	}

	@Override public void process( double[] input, S left, S right ) {
		count++;
		func.process(input, left, right);
	}
}
