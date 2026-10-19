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

import org.ejml.UtilEjml;

import java.util.ArrayList;
import java.util.List;

/// Implementation of [GradientNumerical] for double\[\]
public class GradientNumerical_F64 implements GradientNumerical<double[]> {
	@SuppressWarnings("NullAway.Init") CROperatorForward<double[]> function;
	// sampling distance
	double T = UtilEjml.TEST_F64_SQ;

	@SuppressWarnings("NullAway.Init") double[] output;
	@SuppressWarnings("NullAway.Init") CRArray<double[]> outputRef;

	// passed value of inputs
	List<CRArray<double[]>> inputs = new ArrayList<>();

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

		output = new double[function.getOutputSize()];
		outputRef = new CRArray<>(output, 0);
	}

	@Override
	public void differentiate( CRArray<double[]> dout, List<CRArray<double[]>> gradInput ) {
		for (int idxInput = 0; idxInput < function.getInputCount(); idxInput++) {
			diff(inputs.get(idxInput), function.getInputSize(idxInput), dout, gradInput.get(idxInput));
		}
	}

	/// Computes the gradient for a specific tensor
	private void diff( CRArray<double[]> vector, int length,
					   CRArray<double[]> dout, CRArray<double[]> gradientTarget ) {
		for (int i = 0; i < length; i++) {
			int indexTarget = vector.offset + i;
			double v = vector.a[indexTarget];

			// value in forward direction
			vector.a[indexTarget] = v + T;
			callForward(outputRef);
			elementMult(outputRef, dout, outputRef, function.getOutputSize());
			double plus_T = elementSum(outputRef, function.getOutputSize());

			// value in backwards direction
			vector.a[indexTarget] = v - T;
			callForward(outputRef);
			elementMult(outputRef, dout, outputRef, function.getOutputSize());
			double minus_T = elementSum(outputRef, function.getOutputSize());

			// undo the changes
			vector.a[indexTarget] = v;

			// compute derivative and save the results
			gradientTarget.d(i, (plus_T - minus_T)/(2.0*T));
		}
	}

	private void callForward( CRArray<double[]> output ) {
		for (int i = 0; i < function.getInputCount(); i++) {
			function.setInput(i, inputs.get(i));
		}
		function.forward(output);
	}

	private void elementMult( CRArray<double[]> a, CRArray<double[]> b, CRArray<double[]> out, int length ) {
		for (int i = 0; i < length; i++) {
			out.d(i, a.d(i)*b.d(i));
		}
	}

	private double elementSum( CRArray<double[]> a, int length ) {
		double total = 0;
		for (int i = 0; i < length; i++) {
			total += a.d(i);
		}
		return total;
	}
}
