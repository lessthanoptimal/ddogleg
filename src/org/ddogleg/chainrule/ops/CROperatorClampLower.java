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
import java.util.Map;

/// Holds a value at or above a floor: `output = max(input, minimum)`.
///
/// Meant for a quantity the operators after it cannot accept, such as a depth that has gone negative
/// while a solver was stepping around. Below the floor the derivative is zero, so what has been
/// clamped stops asking to move and whatever else constrains it decides where it goes.
public class CROperatorClampLower implements CROperatorBackwardsJac<double[]> {
	@Getter int dof;

	@Getter double minimum;

	double[] input;

	private final CRMatrix<double[]> workJac = CRMatrix.createD(1, 1);
	private final MathCRMatrixD math = new MathCRMatrixD();

	public CROperatorClampLower( int dof, double minimum ) {
		this.dof = dof;
		this.minimum = minimum;
		this.input = new double[dof];
	}

	/// Changes how many values it holds, which the settings do when one operator is made to match
	/// another
	public void configure( int dof ) {
		if (this.dof == dof)
			return;
		this.dof = dof;
		this.input = new double[dof];
	}

	@Override public void forward( CRArray<double[]> output ) {
		for (int i = 0; i < dof; i++) {
			output.d(i, Math.max(input[i], minimum));
		}
	}

	/// One where the value passed through, zero where the floor decided it
	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		CRMatrix<double[]> output = jacobianInputs.get(0);
		if (output.isInvalid())
			return;

		workJac.reshape(dof, dof);
		math.zero(workJac);
		for (int i = 0; i < dof; i++) {
			workJac.d(i, i, input[i] > minimum ? 1.0 : 0.0);
		}
		math.mult(dout, workJac, output);
	}

	@Override public void setInput( int which, CRArray<double[]> src ) {
		if (which != 0)
			throw new IndexOutOfBoundsException("Unexpected input " + which);
		src.write(input, 0, dof);
	}

	@Override public Map<String, String> getSettings() {
		return Map.of("dof", "" + dof, "minimum", "" + minimum);
	}

	@Override public void setSettings( Map<String, String> settings ) {
		settings.forEach(( key, value ) -> {
			switch (key) {
				case "dof" -> configure(Integer.parseInt(value));
				case "minimum" -> minimum = Double.parseDouble(value);
				default -> throw new IllegalArgumentException("Unknown key: " + key);
			}
		});
	}

	@Override public int getInputSize( int which ) {
		if (which != 0)
			throw new IndexOutOfBoundsException("Unexpected input " + which);
		return dof;
	}

	@Override public int getInputCount() {return 1;}

	@Override public int getOutputSize() {return dof;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
