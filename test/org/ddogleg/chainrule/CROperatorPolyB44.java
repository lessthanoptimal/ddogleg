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

import java.util.List;

/** Implements a function with a simple polynomial that can be hand computed */
public class CROperatorPolyB44 extends DummyBackwards<double[]> {
	double c0, c1, c2;
	double x0, x1, x2, x3;

	// Workspace for jacobian calculation
	final CRMatrix<double[]> workJac = CRMatrix.createD(4, 4);

	MathCRMatrixD math = new MathCRMatrixD();

	@Override public void setInput( int which, CRArray<double[]> input ) {
		if (which == 0) {
			x0 = input.d(0);
			x1 = input.d(1);
			x2 = input.d(2);
			x3 = input.d(3);
		} else if (which == 1) {
			c0 = input.d(0);
			c1 = input.d(1);
			c2 = input.d(2);
		}
	}

	@Override public void forward( CRArray<double[]> output ) {
		output.d(0, c0*x0 - c2*x2 + x3);
		output.d(1, c0*c0*x0 + c1*c1*x1*x1);
		output.d(2, c2*x0*x1 + c0*c1*x1*x2);
		output.d(3, c2*x3);
	}

	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		// Partial relative to inputs
		if (!jacobianInputs.get(0).isInvalid()) {
			workJac.shape(4, 4);
			math.zero(workJac);
			workJac.d(0, 0, c0);
			workJac.d(0, 1, 0);
			workJac.d(0, 2, -c2);
			workJac.d(0, 3, 1);
			workJac.d(1, 0, c0*c0);
			workJac.d(1, 1, 2*c1*c1*x1);
			workJac.d(1, 2, 0);
			workJac.d(1, 3, 0);
			workJac.d(2, 0, c2*x1);
			workJac.d(2, 1, c2*x0 + c0*c1*x2);
			workJac.d(2, 2, c0*c1*x1);
			workJac.d(2, 3, 0);
			workJac.d(3, 3, c2);

			math.mult(dout, workJac, jacobianInputs.get(0));
		}

		// Partial relative to parameters
		if (!jacobianInputs.get(1).isInvalid()) {
			workJac.shape(4, 3);
			math.zero(workJac);
			workJac.d(0, 0, x0);
			workJac.d(0, 1, 0);
			workJac.d(0, 2, -x2);
			workJac.d(1, 0, 2*c0*x0);
			workJac.d(1, 1, 2*c1*x1*x1);
			workJac.d(1, 2, 0);
			workJac.d(2, 0, c1*x1*x2);
			workJac.d(2, 1, c0*x1*x2);
			workJac.d(2, 2, x0*x1);
			workJac.d(3, 2, x3);
			math.mult(dout, workJac, jacobianInputs.get(1));
		}
	}

	@Override public int getInputCount() {return 2;}

	@Override public int getInputSize( int which ) {
		return switch (which) {
			case 0 -> 4;
			case 1 -> 3;
			default -> throw new RuntimeException();
		};
	}

	@Override public int getOutputSize() {return 4;}

	@Override public Class<double[]> getArrayType() {
		return double[].class;
	}
}
