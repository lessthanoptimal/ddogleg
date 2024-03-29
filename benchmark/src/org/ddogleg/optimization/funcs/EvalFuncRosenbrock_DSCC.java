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
import org.ejml.data.DMatrixSparseCSC;

/**
 * <p>
 * [1] J. More, B. Garbow, K. Hillstrom, "Testing Unconstrained Optimization Software"
 * 1981 ACM Transactions on Mathematical Software, Vol 7, No. 1, Match 1981, pages 17-41
 * </p>
 *
 * @author Peter Abeles
 */
public class EvalFuncRosenbrock_DSCC implements EvalFuncLeastSquares<DMatrixSparseCSC> {
	@Override public FunctionNtoM getFunction() {
		return new Func();
	}

	@Override public FunctionNtoMxN<DMatrixSparseCSC> getJacobian() {
		return new Deriv();
	}

	@Override public double[] getInitial() {
		return new double[]{-1.2, 1};
	}

	@Override public double[] getOptimal() {
		return new double[]{1, 1};
	}

	public static class Func implements FunctionNtoM {
		@Override public int getNumOfInputsN() {
			return 2;
		}

		@Override public int getNumOfOutputsM() {
			return 2;
		}

		@Override public void process( double[] input, double[] output ) {
			double x1 = input[0];
			double x2 = input[1];

			output[0] = 10.0*(x2 - x1*x1);
			output[1] = 1.0 - x1;
		}
	}

	public static class Deriv implements FunctionNtoMxN<DMatrixSparseCSC> {
		@Override public int getNumOfInputsN() {
			return 2;
		}

		@Override public int getNumOfOutputsM() {
			return 2;
		}

		@Override public void process( double[] input, DMatrixSparseCSC J ) {
			double x1 = input[0];

			J.zero();
			J.set(0, 0, -20*x1);
			J.set(0, 1, 10);
			J.set(1, 0, -1);
		}

		@Override public DMatrixSparseCSC declareMatrixMxN() {
			return new DMatrixSparseCSC(2, 2);
		}
	}
}
