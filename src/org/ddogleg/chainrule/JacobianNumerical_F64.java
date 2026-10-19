/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.chainrule;

import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/// Implementation of [JacobianNumerical]
public class JacobianNumerical_F64 implements JacobianNumerical<double[]> {
	@SuppressWarnings("NullAway.Init") CROperatorForward<double[]> function;
	// sampling distance
	double T = 1e-6;

	// Output after adding plus T
	@SuppressWarnings("NullAway.Init") CRArray<double[]> outputP;
	// Output after adding minus T
	@SuppressWarnings("NullAway.Init") CRArray<double[]> outputM;

	// Jacobian for the function before multiplying by dout
	@SuppressWarnings("NullAway.Init") CRMatrix<double[]> jacobianFunc;

	// Values of inputs to the operator
	List<CRArray<double[]>> inputs = new ArrayList<>();

	MathCRMatrixD math = new MathCRMatrixD();

	/// print out partials before multiplying by dout
	@Setter protected boolean printPartials = false;

	@Override
	public void configure( double T ) {
		if (T <= 0)
			throw new IllegalArgumentException("T must be > 0");

		this.T = T;
	}

	@Override
	public void setFunction( List<CRArray<double[]>> inputs, CROperatorForward<double[]> function ) {
		this.function = function;
		this.inputs = inputs;

		outputP = new CRArray<>(new double[function.getOutputSize()], 0);
		outputM = new CRArray<>(new double[function.getOutputSize()], 0);

		// largest input vector size
		int largestN = 0;
		for (int i = 0; i < function.getInputCount(); i++) {
			largestN = Math.max(largestN, function.getInputSize(i));
		}

		int jacSize = largestN*function.getOutputSize();
		jacobianFunc = new CRMatrix<>(new double[jacSize], 0, 0, 0);
	}

	@Override public void differentiate( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacInputs ) {
		for (int idxInput = 0; idxInput < function.getInputCount(); idxInput++) {
			diff(inputs.get(idxInput), function.getInputSize(idxInput), jacobianFunc);
			if (printPartials) {
				System.out.println("partial[" + idxInput + "]:");
				System.out.println(jacobianFunc.toStringD("%.3f"));
			}
			math.mult(dout, jacobianFunc, jacInputs.get(idxInput));
		}
	}

	/// Computes the gradient for a specific tensor
	private void diff( CRArray<double[]> vector, int length, CRMatrix<double[]> jacobianTarget ) {
		jacobianTarget.rows = function.getOutputSize();
		jacobianTarget.cols = length;

		for (int idxValue = 0; idxValue < length; idxValue++) {
			int indexTarget = vector.offset + idxValue;
			double v = vector.a[indexTarget];

			// value in forward direction
			vector.a[indexTarget] = v + T;
			callForward(outputP);

			// value in backwards direction
			vector.a[indexTarget] = v - T;
			callForward(outputM);

			// undo the changes
			vector.a[indexTarget] = v;

			// Jacobian is defined as @output[i] / @ param[j], where i = row and j = column
			for (int idxOut = 0; idxOut < jacobianTarget.rows; idxOut++) {
				double derivative = (outputP.d(idxOut) - outputM.d(idxOut))/(2.0*T);
				jacobianTarget.d(idxOut, idxValue, derivative);
			}
		}
	}

	private void callForward( CRArray<double[]> output ) {
		for (int idxInput = 0; idxInput < function.getInputCount(); idxInput++) {
			function.setInput(idxInput, inputs.get(idxInput));
		}
		function.forward(output);
	}
}
