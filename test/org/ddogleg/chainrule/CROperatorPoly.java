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

import org.ddogleg.struct.DogArray_F64;

import java.util.List;
import java.util.Map;

/** Function with a configurable number of inputs, outputs, and parameters */
public class CROperatorPoly extends DummyBackwards<double[]> {
	int numInput, numOutput;

	DogArray_F64 input = new DogArray_F64();
	DogArray_F64 params = new DogArray_F64();

	// Workspace for jacobian calculation
	final CRMatrix<double[]> workJac = CRMatrix.createD(2, 3);

	MathCRMatrixD math = new MathCRMatrixD();

	public CROperatorPoly() {}

	public CROperatorPoly( int numInput, int numOutput, int numParams ) {
		this.numInput = numInput;
		this.numOutput = numOutput;
		params.resize(numParams);
	}

	@Override public void setInput( int which, CRArray<double[]> src ) {
		if (which == 0) {
			input.setTo(src.a, src.offset, getInputSize(0));
		} else if (which == 1) {
			params.setTo(src.a, src.offset, getInputSize(1));
		}
	}

	@Override public void forward( CRArray<double[]> output ) {
		for (int idxOut = 0; idxOut < numOutput; idxOut++) {
			double sum = 0;
			for (int i = 0; i < numInput; i++) {
				for (int j = 0; j < params.size; j++) {
					sum += input.get(i)*params.get(j);
				}
			}
			output.d(idxOut, sum*(idxOut + 0.1));
		}
	}

	@Override public void gradient( CRArray<double[]> dout, List<CRArray<double[]>> gradientInputs ) {
		// gradient of inputs
		if (!gradientInputs.get(0).isInvalid()) {
			CRArray<double[]> gradient = gradientInputs.get(0);
			for (int i = 0; i < numInput; i++) {
				double sum = 0;
				for (int idxOut = 0; idxOut < numOutput; idxOut++) {
					double row = 0;
					for (int j = 0; j < params.size; j++) {
						row += params.get(j);
					}
					sum += row*(idxOut + 0.1)*dout.d(idxOut);
				}
				gradient.d(i, sum);
			}
		}

		// gradient of parameters
		if (!gradientInputs.get(1).isInvalid()) {
			CRArray<double[]> gradient = gradientInputs.get(1);
			for (int j = 0; j < params.size; j++) {
				double sum = 0;
				for (int idxOut = 0; idxOut < numOutput; idxOut++) {
					double row = 0;
					for (int i = 0; i < numInput; i++) {
						row += input.get(i);
					}
					sum += row*(idxOut + 0.1)*dout.d(idxOut);
				}
				gradient.d(j, sum);
			}
		}
	}

	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {

		if (!jacobianInputs.get(0).isInvalid()) {
			workJac.reshape(numOutput, numInput);
			for (int idxOut = 0; idxOut < numOutput; idxOut++) {
				for (int i = 0; i < numInput; i++) {
					double sum = 0;
					for (int j = 0; j < params.size; j++) {
						sum += params.get(j);
					}
					workJac.d(idxOut, i, sum*(idxOut + 0.1));
				}
			}
			math.mult(dout, workJac, jacobianInputs.get(0));
		}

		if (!jacobianInputs.get(1).isInvalid()) {
			workJac.reshape(numOutput, params.size);
			for (int idxOut = 0; idxOut < numOutput; idxOut++) {
				for (int j = 0; j < params.size; j++) {
					double sum = 0;
					for (int i = 0; i < numInput; i++) {
						sum += input.get(i);
					}
					workJac.d(idxOut, j, sum*(idxOut + 0.1));
				}
			}
			math.mult(dout, workJac, jacobianInputs.get(1));
		}
	}

	@Override public Map<String, String> getSettings() {
		return Map.of("input", Integer.toString(numInput), "output", Integer.toString(numOutput), "param", Integer.toString(params.size));
	}

	@Override public void setSettings( Map<String, String> settings ) {
		numInput = Integer.parseInt(settings.get("input"));
		numOutput = Integer.parseInt(settings.get("output"));
		params.resize(Integer.parseInt(settings.get("param")));
	}

	@Override public int getInputCount() {return 2;}

	@Override public int getInputSize( int which ) {
		return switch (which) {
			case 0 -> numInput;
			case 1 -> params.size;
			default -> throw new RuntimeException();
		};
	}

	@Override public int getOutputSize() {return numOutput;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
