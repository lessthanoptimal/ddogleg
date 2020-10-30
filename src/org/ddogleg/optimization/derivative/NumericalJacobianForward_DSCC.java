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

package org.ddogleg.optimization.derivative;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.DConvertMatrixStruct;

/**
 * Finite difference numerical gradient calculation using forward equation. Forward
 * difference equation, f'(x) = f(x+h)-f(x)/h.  Scaling is taken in account by h based
 * upon the magnitude of the elements in variable x.
 *
 * <p>
 * NOTE: If multiple input parameters are modified by the function when a single one is changed numerical
 * derivatives aren't reliable.
 * </p>
 *
 * @author Peter Abeles
 */
public class NumericalJacobianForward_DSCC implements FunctionNtoMxN<DMatrixSparseCSC>
{
	// number of input variables
	private final int N;
	// number of functions
	private final int M;

	// function being differentiated
	private final FunctionNtoM function;

	// scaling of the difference parameter
	private final double differenceScale;

	private final double[] output0;
	private final double[] output1;

	// used to decide if a variable is a zero
	private double zeroTolerance = UtilEjml.EPS;

	public NumericalJacobianForward_DSCC(FunctionNtoM function, double differenceScale) {
		this.function = function;
		this.differenceScale = differenceScale;
		this.N = function.getNumOfInputsN();
		this.M = function.getNumOfOutputsM();
		output0 = new double[M];
		output1 = new double[M];
	}

	public NumericalJacobianForward_DSCC(FunctionNtoM function) {
		this(function,Math.sqrt(UtilEjml.EPS));
	}

	@Override
	public int getNumOfInputsN() {
		return N;
	}

	@Override
	public int getNumOfOutputsM() {
		return M;
	}

	@Override
	public void process(double[] input, DMatrixSparseCSC jacobian) {
		jacobian.reshape(M,N,N);

		function.process(input,output0);

		// Use a triplet initially because it is less expensive to grow
		DMatrixSparseTriplet tmp = new DMatrixSparseTriplet(M,N,N);

		for( int i = 0; i < N; i++ ) {
			double x = input[i];
			double h = x != 0 ? differenceScale*Math.abs(x) : differenceScale;

			// takes in account round off error
			double temp = x+h;
			h = temp-x;

			input[i] = temp;
			function.process(input,output1);
			for( int j = 0; j < M; j++ ) {
				double value = (output1[j] - output0[j])/h;
				if( Math.abs(value) > zeroTolerance )
					tmp.set(j,i,value);
			}
			input[i] = x;
		}

		DConvertMatrixStruct.convert(tmp,jacobian);
	}

	@Override
	public DMatrixSparseCSC declareMatrixMxN() {
		return new DMatrixSparseCSC(M,N);
	}

	public void setZeroTolerance(double zeroTolerance) {
		this.zeroTolerance = zeroTolerance;
	}
}
