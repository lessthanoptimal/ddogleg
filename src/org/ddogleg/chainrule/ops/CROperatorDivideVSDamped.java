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
import lombok.Setter;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.ddogleg.chainrule.MathCRMatrixD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Divides a vector by a damped scalar: `a[i]/(beta + b)`. input[0] is the vector and input[1] is
/// the scalar, which is expected to be at or above zero, so a `beta > 0` keeps the denominator away
/// from zero however small the scalar gets
public class CROperatorDivideVSDamped implements CROperatorBackwardsJac<double[]> {
	public static final int IN_A = 0;
	public static final int IN_B = 1;

	@Getter private int dof;

	/// The damping added to the scalar `b` before dividing.
	@Getter @Setter double beta;

	double[] a;
	double b;

	private final CRMatrix<double[]> workJac = CRMatrix.createD(1, 1);
	private final MathCRMatrixD math = new MathCRMatrixD();

	public CROperatorDivideVSDamped( int dof, double beta ) {
		this.dof = dof;
		this.beta = beta;
		this.a = new double[dof];
	}

	@Override public void forward( CRArray<double[]> output ) {
		for (int i = 0; i < dof; i++) {
			output.d(i, a[i]/(beta + b));
		}
	}

	/// The quotient rule with the damping riding along in the denominator: the vector's Jacobian is
	/// `1/(beta + b)` on the diagonal, and the scalar's is `-a/(beta + b)^2` as a single column
	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		double damped = beta + b;

		CRMatrix<double[]> outputA = jacobianInputs.get(IN_A);
		if (!outputA.isInvalid()) {
			workJac.reshape(dof, dof);
			math.zero(workJac);
			math.diag(1.0/damped, workJac);
			math.mult(dout, workJac, outputA);
		}

		CRMatrix<double[]> outputB = jacobianInputs.get(IN_B);
		if (!outputB.isInvalid()) {
			workJac.reshape(dof, 1);
			for (int i = 0; i < dof; i++) {
				workJac.d(i, 0, -a[i]/(damped*damped));
			}
			math.mult(dout, workJac, outputB);
		}
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		switch (which) {
			case IN_A -> input.write(a, 0, dof);
			case IN_B -> b = input.d(0);
			default -> throw new IndexOutOfBoundsException("Unexpected input " + which);
		}
	}

	@Override public int getInputSize( int which ) {
		return switch (which) {
			case IN_A -> dof;
			case IN_B -> 1;
			default -> throw new IndexOutOfBoundsException("Unexpected input " + which);
		};
	}

	@Override public int getInputCount() {
		return 2;
	}

	@Override public int getOutputSize() {
		return dof;
	}

	@Override public void setSettings( Map<String, String> settings ) {
		int dof = Integer.parseInt(settings.get("dof"));
		if (this.dof != dof) {
			this.dof = dof;
			this.a = new double[dof];
		}
		this.beta = Double.parseDouble(settings.getOrDefault("beta", "0.0"));
	}

	/// Encodes the damping only when it does something
	@Override public Map<String, String> getSettings() {
		var map = new HashMap<String, String>();
		map.put("dof", Integer.toString(dof));
		if (beta != 0.0)
			map.put("beta", Double.toString(beta));
		return map;
	}

	@Override public Class<double[]> getArrayType() {
		return double[].class;
	}
}
