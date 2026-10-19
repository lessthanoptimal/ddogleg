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

import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.ddogleg.chainrule.MathCRMatrixD;

import java.util.List;
import java.util.Map;

/// Mean of `count` values read at a fixed stride from one input vector. Exists so a soft
/// constraint can pull an average toward a target with a single residual row rather than
/// shrinking every element individually; the stride lets it average one coefficient across a
/// block of interleaved polynomials
public class CROperatorMeanVector implements CROperatorBackwardsJac<double[]> {
	/// How many values are averaged and how far apart they sit
	int count, stride;

	/// The mean, kept so the jacobian pass does not need the values again
	double mean;

	private final CRMatrix<double[]> workJac = CRMatrix.createD(1, 1);
	private final MathCRMatrixD math = new MathCRMatrixD();

	public CROperatorMeanVector( int count, int stride ) {
		this.count = count;
		this.stride = stride;
	}

	@Override public void forward( CRArray<double[]> output ) {
		output.d(0, mean);
	}

	/// Every strided element contributes equally, so the jacobian is `1/count` at each of them
	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		CRMatrix<double[]> output = jacobianInputs.get(0);
		if (output.isInvalid())
			return;

		workJac.reshape(1, getInputSize(0));
		math.zero(workJac);
		for (int i = 0; i < count; i++) {
			workJac.d(0, i*stride, 1.0/count);
		}
		math.mult(dout, workJac, output);
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		if (which != 0)
			throw new IndexOutOfBoundsException("Unexpected input " + which);
		double total = 0;
		for (int i = 0; i < count; i++) {
			total += input.d(i*stride);
		}
		mean = total/count;
	}

	/// Ends at the last element read rather than the block end, so a slice starting inside the
	/// last block of the parameter array stays in bounds
	@Override public int getInputSize( int which ) {
		if (which != 0)
			throw new IndexOutOfBoundsException("Unexpected input " + which);
		return (count - 1)*stride + 1;
	}

	@Override public Map<String, String> getSettings() {
		return Map.of("count", "" + count, "stride", "" + stride);
	}

	@Override public void setSettings( Map<String, String> settings ) {
		settings.forEach(( key, value ) -> {
			switch (key) {
				case "count" -> count = Integer.parseInt(value);
				case "stride" -> stride = Integer.parseInt(value);
				default -> throw new IllegalArgumentException("Unknown key: " + key);
			}
		});
	}

	@Override public int getInputCount() {return 1;}

	@Override public int getOutputSize() {return 1;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
