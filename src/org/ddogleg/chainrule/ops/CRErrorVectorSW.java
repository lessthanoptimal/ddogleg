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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Computes the error between a known prior and an estimated value. A single weight is applied to all elements
public class CRErrorVectorSW implements CROperatorBackwardsJac<double[]> {
	/// input[0] the estimated value
	@Getter protected double[] estimated;
	/// input[1] the observed value. Often this is known no Jacobian is needed.
	@Getter protected double[] observed;

	/// Error weight applied to all elements
	@Getter @Setter protected double weight;

	protected final MathCRMatrixD math = new MathCRMatrixD();
	protected final CRMatrix<double[]> workJac = CRMatrix.createD(5, 5);

	public CRErrorVectorSW( int length ) {
		this.estimated = new double[length];
		this.observed = new double[length];
	}

	public CRErrorVectorSW( int length, double weight ) {
		this(length);
		this.weight = weight;
	}

	/// Sets the observed vector to zero. This is a common assumption for parameter priors
	public void zeroObserved() {
		Arrays.fill(observed, 0.0);
	}

	/// Convenient way to set observed values without using CRArray
	public void setObserved( double[] values, int offset ) {
		System.arraycopy(values, offset, observed, 0, observed.length);
	}

	/// Convenient way to set estimated values without using CRArray
	public void setEstimated( double[] values, int offset ) {
		System.arraycopy(values, offset, estimated, 0, estimated.length);
	}

	@Override public void forward( CRArray<double[]> output ) {
		for (int i = 0; i < estimated.length; i++) {
			output.d(i, weight*(observed[i] - estimated[i]));
		}
	}

	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		if (!jacobianInputs.get(0).isInvalid()) {
			workJac.reshape(getOutputSize(), getInputSize(0));
			math.zero(workJac);

			for (int i = 0; i < estimated.length; i++) {
				workJac.d(i, i, -weight);
			}
			math.mult(dout, workJac, jacobianInputs.get(0));
		}

		if (!jacobianInputs.get(1).isInvalid()) {
			workJac.reshape(getOutputSize(), getInputSize(1));
			math.zero(workJac);

			for (int i = 0; i < observed.length; i++) {
				workJac.d(i, i, weight);
			}
			math.mult(dout, workJac, jacobianInputs.get(1));
		}
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		switch (which) {
			case 0 -> System.arraycopy(input.a, input.offset, estimated, 0, estimated.length);
			case 1 -> System.arraycopy(input.a, input.offset, observed, 0, observed.length);
			default -> throw new IllegalArgumentException("Unknown input " + which);
		}
	}

	@Override public int getInputSize( int which ) {
		if (which != 0 && which != 1)
			throw new IndexOutOfBoundsException("Unexpected input " + which);
		return estimated.length;
	}

	@Override public int getInputCount() {return 2;}

	@Override public void setSettings( Map<String, String> settings ) {
		int length = Integer.parseInt(settings.get("length"));
		if (estimated.length != length) {
			this.estimated = new double[length];
			this.observed = new double[length];
		}

		// put weights into their default value
		this.weight = Double.parseDouble(settings.getOrDefault("weight", "1.0"));
	}

	/** Encodes all weights that are not 1.0 */
	@Override public Map<String, String> getSettings() {
		var map = new HashMap<String, String>();

		map.put("length", Integer.toString(estimated.length));

		if (weight != 1.0)
			map.put("weight", Double.toString(weight));

		return map;
	}

	@Override public int getOutputSize() {return estimated.length;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
