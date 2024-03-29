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
import org.ejml.data.DMatrixRMaj;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * <p>
 * [1] J. More, B. Garbow, K. Hillstrom, "Testing Unconstrained Optimization Software"
 * 1981 ACM Transactions on Mathematical Software, Vol 7, No. 1, Match 1981, pages 17-41
 * </p>
 *
 * @author Peter Abeles
 */
public class EvalFuncPowellSingular_DDRM implements EvalFuncLeastSquares<DMatrixRMaj> {
	@Override public FunctionNtoM getFunction() {
		return new Func();
	}

	@Override public FunctionNtoMxN<DMatrixRMaj> getJacobian() {
		return new Deriv();
	}

	@Override public double[] getInitial() {
		return new double[]{3, -1, 0, 1};
	}

	@Override public double[] getOptimal() {
		return new double[]{0, 0, 0, 0};
	}

	public static class Func implements FunctionNtoM {
		@Override public int getNumOfInputsN() {
			return 4;
		}

		@Override public int getNumOfOutputsM() {
			return 4;
		}

		@Override public void process( double[] input, double[] output ) {
			double x1 = input[0];
			double x2 = input[1];
			double x3 = input[2];
			double x4 = input[3];

			output[0] = x1 + 10.0*x2;
			output[1] = sqrt(5)*(x3 - x4);
			output[2] = pow(x2 - 2*x3, 2.0);
			output[3] = sqrt(10)*pow(x1 - x4, 2.0);
		}
	}

	public static class Deriv implements FunctionNtoMxN<DMatrixRMaj> {
		@Override public int getNumOfInputsN() {
			return 4;
		}

		@Override public int getNumOfOutputsM() {
			return 4;
		}

		@Override public void process( double[] input, DMatrixRMaj J ) {
			double x1 = input[0];
			double x2 = input[1];
			double x3 = input[2];
			double x4 = input[3];

			J.set(0, 0, 1);
			J.set(0, 1, 10);
			J.set(1, 2, sqrt(5));
			J.set(1, 3, -sqrt(5));
			J.set(2, 1, 2.0*(x2 - 2*x3));
			J.set(2, 2, -4.0*(x2 - 2*x3));
			J.set(3, 0, 2.0*sqrt(10)*(x1 - x4));
			J.set(3, 3, -2.0*sqrt(10)*(x1 - x4));
		}

		@Override public DMatrixRMaj declareMatrixMxN() {
			return new DMatrixRMaj(getNumOfOutputsM(), getNumOfInputsN());
		}
	}
}
