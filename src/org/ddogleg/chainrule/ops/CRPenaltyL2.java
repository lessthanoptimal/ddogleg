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
import lombok.experimental.Accessors;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.ddogleg.chainrule.MathCRMatrixD;
import org.ddogleg.struct.DogArray_F64;

import java.util.List;
import java.util.Map;

/// Penalizes a contiguous run of parameters, one residual per parameter, each `weight*value`. The cost
/// the optimizer sees is therefore the weight squared times the sum of squares, and the Jacobian is a
/// scaled identity.
///
/// Nothing here knows what the parameters mean. Point it at a slice of the encoded state by giving the
/// input that slice's offset, and use one instance per group that needs its own weight. A group with no
/// weight is left out rather than configured with zero, which is what keeps the row layout fixed: rows
/// and columns always correspond one to one.
///
/// The usual reason to want this is not a preference for small numbers, it is a parameter direction the
/// data does not determine. See the callers for which directions those are.
@Accessors(chain = true, fluent = true)
public class CRPenaltyL2 implements CROperatorBackwardsJac<double[]> {
	/// Applied to every parameter in the block
	@Getter @Setter double weight = 1.0;

	/// How many parameters are penalized
	@Getter int length;

	/// The parameters, copied in by [#setInput]
	final DogArray_F64 values = new DogArray_F64();

	final CRMatrix<double[]> workJac = CRMatrix.createD(1, 1);
	final MathCRMatrixD math = new MathCRMatrixD();

	public CRPenaltyL2( int length ) {
		configure(length);
	}

	public CRPenaltyL2() {
		this(0);
	}

	public CRPenaltyL2 configure( int length ) {
		if (length < 0)
			throw new IllegalArgumentException("length must be >= 0");
		this.length = length;
		this.values.resize(length);
		return this;
	}

	@Override public void forward( CRArray<double[]> output ) {
		for (int i = 0; i < length; i++) {
			output.d(i, weight*values.data[i]);
		}
	}

	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		if (jacobianInputs.get(0).isInvalid())
			return;

		workJac.reshape(length, length);
		math.zero(workJac);
		for (int i = 0; i < length; i++) {
			workJac.d(i, i, weight);
		}

		math.mult(dout, workJac, jacobianInputs.get(0));
	}

	@Override public Map<String, String> getSettings() {
		return Map.of("weight", Double.toString(weight), "length", "" + length);
	}

	@Override public void setSettings( Map<String, String> settings ) {
		this.weight = 1.0;
		int[] found = {length};

		settings.forEach(( key, value ) -> {
			switch (key) {
				case "weight" -> weight = Double.parseDouble(value);
				case "length" -> found[0] = Integer.parseInt(value);
				default -> throw new IllegalArgumentException("Unknown key: " + key);
			}
		});
		configure(found[0]);
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		if (which != 0)
			throw new IllegalArgumentException("Unexpected input " + which);
		for (int i = 0; i < length; i++) {
			values.data[i] = input.d(i);
		}
	}

	@Override public int getInputSize( int which ) {
		if (which != 0)
			throw new IllegalArgumentException("Unexpected input " + which);
		return length;
	}

	@Override public int getInputCount() {return 1;}

	@Override public int getOutputSize() {return length;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
