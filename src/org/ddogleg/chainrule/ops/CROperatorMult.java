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

/// Element-wise multiplication of two vectors
public class CROperatorMult implements CROperatorBackwardsJac<double[]> {
	public static final int IN_A = 0;
	public static final int IN_B = 1;

	@Getter private final int dof;

	final double[] a;
	final double[] b;

	private final CRMatrix<double[]> workJac = CRMatrix.createD(1, 1);
	private final MathCRMatrixD math = new MathCRMatrixD();

	public CROperatorMult( int dof ) {
		this.dof = dof;
		this.a = new double[dof];
		this.b = new double[dof];
	}

	@Override public void forward( CRArray<double[]> output ) {
		for (int i = 0; i < dof; i++) {
			output.d(i, a[i] * b[i]);
		}
	}

	/// The product rule: each side's Jacobian is a diagonal holding the other side's values
	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		for (int which = 0; which < 2; which++) {
			CRMatrix<double[]> output = jacobianInputs.get(which);
			if (output.isInvalid())
				continue;
			double[] other = which == IN_A ? b : a;
			workJac.reshape(dof, dof);
			math.zero(workJac);
			for (int i = 0; i < dof; i++) {
				workJac.d(i, i, other[i]);
			}
			math.mult(dout, workJac, output);
		}
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		switch (which) {
			case IN_A -> input.write(a, 0, dof);
			case IN_B -> input.write(b, 0, dof);
			default -> throw new IndexOutOfBoundsException("Unexpected input " + which);
		}
	}

	@Override public int getInputSize( int which ) {
		if (which != IN_A && which != IN_B)
			throw new IndexOutOfBoundsException("Unexpected input " + which);
		return dof;
	}

	@Override public int getInputCount() {
		return 2;
	}

	@Override public int getOutputSize() {
		return dof;
	}

	@Override public Class<double[]> getArrayType() {
		return double[].class;
	}
}
