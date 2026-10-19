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

package org.ddogleg.chainrule.ops;

import lombok.Getter;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.ddogleg.chainrule.MathCRMatrixD;

import java.util.List;

/// Remaps parameters from an input format into an output format. Does not change their values but
/// can be configured to drop some elements. If there is no mapping to an element then it's value will
/// be set to 0. Output vector size can be larger than the number of remapped elects to allow you to represent
/// states. For example, if the consumer expected 12 states but you only need to popular 3 of them.
public class CROperatorRemapParams implements CROperatorBackwardsJac<double[]> {

	CRArray<double[]> src;

	int[] inputToOutput;

	// Default values assigned to output
	@Getter double[] outputDefault;

	private final MathCRMatrixD math = new MathCRMatrixD();
	private final CRMatrix<double[]> workJac = CRMatrix.createD(5, 5);

	/// Constructor which specifies how the remapping is done. A value of -1 in mappings means it should be skipped.
	///
	/// @param outputSize Size of output vector
	/// @param mappings How elements from the input vector map into the output vector
	public CROperatorRemapParams( int outputSize, int... mappings ) {
		this.inputToOutput = mappings;

		src = math.createArray(inputToOutput.length);
		outputDefault = new double[outputSize];
	}

	/// Assigns the default value for the array
	public CROperatorRemapParams setDefaultValues( double... values ) {
		if (values.length != outputDefault.length)
			throw new IllegalArgumentException("Unexpected length. " + values.length + " != " + outputDefault.length);
		System.arraycopy(values, 0, outputDefault, 0, outputDefault.length);
		return this;
	}

	/// Shorthand for [#setDefaultValues]
	public CROperatorRemapParams def( double... values ) {
		return setDefaultValues(values);
	}

	@Override public void forward( CRArray<double[]> output ) {
		System.arraycopy(outputDefault, 0, output.a, output.offset, outputDefault.length);

		for (int i = 0; i < inputToOutput.length; i++) {
			if (inputToOutput[i] < 0)
				continue;
			output.d(inputToOutput[i], src.d(i));
		}
	}

	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		if (jacobianInputs.get(0).isInvalid())
			return;

		workJac.reshape(getOutputSize(), getInputSize(0));
		math.zero(workJac);

		for (int i = 0; i < inputToOutput.length; i++) {
			if (inputToOutput[i] < 0)
				continue;
			workJac.d(inputToOutput[i], i, 1.0);
		}

//        System.out.println(workJac.toStringD("%.3f"));
		math.mult(dout, workJac, jacobianInputs.get(0));
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		if (which != 0)
			throw new IllegalArgumentException("Unknown input " + which);
		src.read(input.a, input.offset, inputToOutput.length);
	}

	@Override public int getInputSize( int which ) {
		if (which != 0)
			throw new IllegalArgumentException("Unknown input " + which);
		return inputToOutput.length;
	}

	@Override public int getInputCount() {return 1;}

	@Override public int getOutputSize() {return outputDefault.length;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
