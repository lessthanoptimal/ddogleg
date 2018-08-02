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

package org.ddogleg.optimization.funcs;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.data.DMatrix;

/**
 *
 *
 * <p>
 * [1] J. More, B. Garbow, K. Hillstrom, "Testing Unconstrained Optimization Software"
 * 1981 ACM Transactions on Mathematical Software, Vol 7, No. 1, Match 1981, pages 17-41
 * </p>
 *
 * @author Peter Abeles
 */
public class EvalFuncHelicalValley<S extends DMatrix> implements EvalFuncLeastSquares<S> {
	@Override
	public FunctionNtoM getFunction() {
		return new Func();
	}


	@Override
	public FunctionNtoMxN<S> getJacobian() {
		return null;
	}

	@Override
	public double[] getInitial() {
		return new double[]{-1,0,0};
	}
	
	@Override
	public double[] getOptimal() {
		return new double[]{1,0,0};
	}

	public static class Func implements FunctionNtoM
	{
		@Override
		public int getNumOfInputsN() {
			return 3;
		}

		@Override
		public int getNumOfOutputsM() {
			return 3;
		}

		@Override
		public void process(double[] input, double[] output) {
			double x1 = input[0];
			double x2 = input[1];
			double x3 = input[2];
			
			output[0] = 10*(x3 - 10*phi(x1,x2));
			output[1] = 10*(Math.sqrt(x1*x1 + x2*x2)-1);
			output[2] = x3;
		}
		
		private double phi( double a , double b ) {
			double left = 1.0/(2*Math.PI);
			
			if( a > 0 ) {
				return left*Math.atan(b/a);
			} else {
				return left*Math.atan(b/a) + 0.5;
			}
		}
	}
}
