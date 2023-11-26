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

package org.ddogleg.optimization.funcs;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.data.DMatrix;

/**
 * <p>
 * [1] J. More, B. Garbow, K. Hillstrom, "Testing Unconstrained Optimization Software"
 * 1981 ACM Transactions on Mathematical Software, Vol 7, No. 1, Match 1981, pages 17-41
 * </p>
 *
 * @author Peter Abeles
 */
public class EvalFuncTrigonometric<S extends DMatrix> implements EvalFuncLeastSquares<S> {

	int N;

	public EvalFuncTrigonometric( int n ) {
		N = n;
	}

	@Override public FunctionNtoM getFunction() {
		return new Func();
	}

	@Override public FunctionNtoMxN<S> getJacobian() {
		return null;
	}

	@Override public double[] getInitial() {
		double[] x = new double[N];
		for (int i = 0; i < N; i++) {
			x[i] = 1/(double)N;
		}
		return x;
	}

	@Override public double[] getOptimal() {
		return null;
	}

	public class Func implements FunctionNtoM {
		@Override public int getNumOfInputsN() {return N;}

		@Override public int getNumOfOutputsM() {return N;}

		@Override public void process( double[] input, double[] output ) {
			for (int i = 0; i < input.length; i++) {
				output[i] = F(input, i);
			}
		}

		public double F( double[] x, int degree ) {
			double total = N;
			for (int i = 0; i < N; i++) {
				total -= Math.cos(x[i]);
			}

			total += (degree + 1)*(1 - Math.cos(x[degree])) - Math.sin(x[degree]);

			return total;
		}
	}
}
