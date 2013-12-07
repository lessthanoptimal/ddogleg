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

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoS;

/**
 * Converts a least squares function into a nonlinear optimization function.
 *
 * F(x) = sum f_i(x)^2
 *
 * @author Peter Abeles
 */
public class LsToNonLinear implements FunctionNtoS {

	FunctionNtoM func;
	
	double output[];

	public LsToNonLinear(FunctionNtoM func) {
		this.func = func;
		output = new double[ func.getNumOfOutputsM() ];
	}

	@Override
	public int getNumOfInputsN() {
		return func.getNumOfInputsN();
	}

	@Override
	public double process(double[] input) {
		func.process(input,output);

		double result = 0;
		for( int i = 0; i < output.length; i++ ) {
			result += output[i]*output[i];
		}
		return result;
	}
}
