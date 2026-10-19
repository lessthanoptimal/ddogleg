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

/// Adds two vectors with scalar coefficients: 'output = alpha*V0 + beta*V1'
///
/// * input[0] = V0
/// * input[1] = V1
/// * input[2] = alpha
/// * input[3] = beta
public class CROperatorPlusAB implements CROperatorBackwardsJac<double[]> {
	public static final int IN_V0 = 0;
	public static final int IN_V1 = 1;
	public static final int IN_ALPHA = 2;
	public static final int IN_BETA = 3;

	@Getter private final int dof;

	// two vectors
	final double[] v0;
	final double[] v1;

	// scalar coefficients for the two vectors
	double alpha, beta;

	private final CRMatrix<double[]> workJac = CRMatrix.createD(1, 1);
	private final MathCRMatrixD math = new MathCRMatrixD();

	public CROperatorPlusAB( int dof ) {
		this.dof = dof;
		this.v0 = new double[dof];
		this.v1 = new double[dof];
	}

	@Override public void forward( CRArray<double[]> output ) {
		for (int i = 0; i < dof; i++) {
			output.d(i, alpha*v0[i] + beta*v1[i]);
		}
	}

	/// Each vector enters scaled by its own coefficient, and each coefficient enters as the vector
	/// it scales
	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		for (int which = 0; which < 2; which++) {
			CRMatrix<double[]> output = jacobianInputs.get(which);
			if (output.isInvalid())
				continue;
			workJac.reshape(dof, dof);
			math.zero(workJac);
			math.diag(which == IN_V0 ? alpha : beta, workJac);
			math.mult(dout, workJac, output);
		}

		for (int which = IN_ALPHA; which <= IN_BETA; which++) {
			CRMatrix<double[]> output = jacobianInputs.get(which);
			if (output.isInvalid())
				continue;
			workJac.reshape(dof, 1);
			double[] vector = which == IN_ALPHA ? v0 : v1;
			for (int i = 0; i < dof; i++) {
				workJac.d(i, 0, vector[i]);
			}
			math.mult(dout, workJac, output);
		}
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		switch (which) {
			case IN_V0 -> input.write(v0, 0, dof);
			case IN_V1 -> input.write(v1, 0, dof);
			case IN_ALPHA -> alpha = input.d(0);
			case IN_BETA -> beta = input.d(0);
			default -> throw new IndexOutOfBoundsException("Unexpected input " + which);
		}
	}

	@Override public int getInputSize( int which ) {
		if (which == IN_V0 || which == IN_V1)
			return dof;
		else if (which == IN_ALPHA || which == IN_BETA)
			return 1;
		throw new IndexOutOfBoundsException("Unknown input " + which);
	}

	@Override public int getInputCount() {return 4;}

	@Override public int getOutputSize() {return dof;}

	@Override public Class<double[]> getArrayType() {
		return double[].class;
	}
}
