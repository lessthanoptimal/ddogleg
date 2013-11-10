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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.functions.CoupledDerivative;
import org.ddogleg.optimization.functions.FunctionStoS;

/**
 * Takes two functions which independently computes the function's value and derivative and allows them
 * to be used in a coupled function.
 *
 * @author Peter Abeles
 */
public class Individual_to_CoupledDerivative implements CoupledDerivative {

	double input;
	FunctionStoS function;
	FunctionStoS derivative;

	public Individual_to_CoupledDerivative(FunctionStoS function, FunctionStoS derivative) {
		this.function = function;
		this.derivative = derivative;
	}

	@Override
	public void setInput(double x) {
		input = x;
	}

	@Override
	public double computeFunction() {
		return function.process(input);
	}

	@Override
	public double computeDerivative() {
		return derivative.process(input);
	}
}
