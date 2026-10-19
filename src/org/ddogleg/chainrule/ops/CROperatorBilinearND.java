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
import lombok.experimental.Accessors;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.ddogleg.chainrule.MathCRMatrixD;
import org.ddogleg.chainrule.grid.ConfigGridCoefND;

import java.util.List;
import java.util.Map;

import static org.ddogleg.chainrule.grid.ConfigGridCoefND.NUM_KNOTS;

/// General purpose operator for blending 4 knots into an N-D output, each knot stores N-coefficients
/// for a yet to be defined function. The value the function is evaluated at is specified by 'value'.
///
/// Nothing here knows about pixels or grids. The weights are an input, so whatever produced them owns the
/// interpolation scheme and this only has to blend. The derivative with respect to whatever the weights
/// were computed from then follows through the chain.
///
/// Functions for each knot are externally specified to avoid a large sparse output Jacobian. It's up to
/// the user to make sure the correct 4 are passed in.
///
/// @see CROperatorGridBilinearWeights
/// @see CROperatorBilinearPolyND
/// @see CROperatorBilinearSplineND
@Accessors(chain = true, fluent = true)
public abstract class CROperatorBilinearND implements CROperatorBackwardsJac<double[]> {
	public static final int IN_WEIGHTS = 0;
	public static final int IN_VALUE = 1;
	public static final int IN_KNOT_0 = 2;
	public static final int IN_KNOT_1 = 3;
	public static final int IN_KNOT_2 = 4;
	public static final int IN_KNOT_3 = 5;

	/// Number of coefficients in the polynomial
	@Getter int numCoefs;

	/// How many parameters are stored in each knot
	@Getter private int knotStride;

	/// How many outputs each knot describes
	@Getter private int outputDof;

	/// Where each knot's polynomials live. Pointed at the caller's array rather than holding a copy
	protected final CRArray<double[]>[] knots = createKnots();

	/// value that the polynomial is being sampled at
	protected double value;

	/// How much each knot contributes, in the same order as the knot inputs
	final double[] weights = new double[NUM_KNOTS];

	/// Derivative with respect to each coefficient of a knot, filled by [#functionDerivatives]
	protected double[] coefDeriv = new double[0];

	/// Workspace for the jacobian
	protected final CRMatrix<double[]> workJac = CRMatrix.createD(2, 1);
	protected final MathCRMatrixD math = new MathCRMatrixD();

	@SuppressWarnings("unchecked")
	private static CRArray<double[]>[] createKnots() {
		var out = new CRArray[NUM_KNOTS];
		for (int i = 0; i < NUM_KNOTS; i++) {
			// invalid until a knot arrives, so using one that was never set blows up rather than reading zeros
			out[i] = new CRArray<>(new double[0], -1);
		}
		return out;
	}

	/// Copies grid into internal fields
	protected void configureGrid( ConfigGridCoefND grid ) {
		this.numCoefs = grid.coefs;
		this.outputDof = grid.numOut;
		this.knotStride = outputDof*numCoefs;
		this.coefDeriv = new double[numCoefs];
	}

	/// Where the specified knot's polynomials start in the array they came from, which is also the column
	/// its Jacobian belongs to when that array is the system's parameter vector
	public int knotOffset( int knot ) {
		return knots[knot].offset;
	}

	@Override public void forward( CRArray<double[]> output ) {
		for (int i = 0; i < outputDof; i++) {
			output.d(i, blendAt(i*numCoefs));
		}
	}

	/// Weighted sum of one output's polynomial across the four knots
	double blendAt( int polyOffset ) {
		double total = 0;
		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			total += weights[knot]*evaluate(knot, polyOffset);
		}
		return total;
	}

	/// Values the specified polynomial encoded in the knot using `value`.
	protected abstract double evaluate( int knot, int polyOffset );

	/// Derivative of [#evaluate] with respect to `value`
	protected abstract double evaluateDerivative( int knot, int polyOffset );

	/// Derivative of [#evaluate] with respect to each of a knot's coefficients for one output. Every
	/// coefficient enters linearly and the same way in every knot and every output, so this is filled
	/// once and reused. Length is [#numCoefs]
	protected abstract void functionDerivatives( double[] derivatives );

	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		if (!jacobianInputs.get(IN_WEIGHTS).isInvalid())
			jacobianWeights(dout, jacobianInputs.get(IN_WEIGHTS));
		if (!jacobianInputs.get(IN_VALUE).isInvalid())
			jacobianValue(dout, jacobianInputs.get(IN_VALUE));

		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			CRMatrix<double[]> output = jacobianInputs.get(IN_KNOT_0 + knot);
			if (!output.isInvalid())
				jacobianKnot(dout, output, knot);
		}
	}

	/// A weight enters linearly, so its derivative is the polynomial it scales
	protected void jacobianWeights( CRMatrix<double[]> dout, CRMatrix<double[]> output ) {
		workJac.reshape(outputDof, NUM_KNOTS);
		for (int axis = 0; axis < outputDof; axis++) {
			for (int knot = 0; knot < NUM_KNOTS; knot++) {
				workJac.d(axis, knot, evaluate(knot, axis*numCoefs));
			}
		}
		math.mult(dout, workJac, output);
	}

	/// Each knot's polynomial moves with `value`, and the four of them are mixed by the same weights
	protected void jacobianValue( CRMatrix<double[]> dout, CRMatrix<double[]> output ) {
		workJac.reshape(outputDof, 1);
		for (int axis = 0; axis < outputDof; axis++) {
			int polyOffset = axis*numCoefs;
			double d = 0;
			for (int knot = 0; knot < NUM_KNOTS; knot++) {
				d += weights[knot]*evaluateDerivative(knot, polyOffset);
			}
			workJac.d(axis, 0, d);
		}
		math.mult(dout, workJac, output);
	}

	/// A coefficient enters linearly, so its derivative is its knot's weight times whatever the
	/// function makes of it. Each output reads its own run of the knot, so the rest stays zero
	protected void jacobianKnot( CRMatrix<double[]> dout, CRMatrix<double[]> output, int knot ) {
		workJac.reshape(outputDof, knotStride);
		math.zero(workJac);
		functionDerivatives(coefDeriv);
		for (int axis = 0; axis < outputDof; axis++) {
			for (int k = 0; k < numCoefs; k++) {
				workJac.d(axis, axis*numCoefs + k, weights[knot]*coefDeriv[k]);
			}
		}
		math.mult(dout, workJac, output);
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		if (which >= IN_KNOT_0 && which <= IN_KNOT_3) {
			// the array and offset are kept, not the values, so this costs nothing per observation
			knots[which - IN_KNOT_0].wrap(input.a, input.offset);
			return;
		}

		switch (which) {
			case IN_WEIGHTS -> input.write(weights, 0, NUM_KNOTS);
			case IN_VALUE -> setValue(input.d(0));
			default -> throw new IndexOutOfBoundsException("Unexpected input " + which);
		}
	}

	/// Assigns the value
	protected void setValue( double value ) {
		this.value = value;
	}

	@Override public int getInputSize( int which ) {
		if (which >= IN_KNOT_0 && which <= IN_KNOT_3)
			return knotStride;

		return switch (which) {
			case IN_WEIGHTS -> NUM_KNOTS;
			case IN_VALUE -> 1;
			default -> throw new IndexOutOfBoundsException("Unexpected input " + which);
		};
	}

	@Override public Map<String, String> getSettings() {
		return Map.of("num_coefs", "" + numCoefs, "output_dof", "" + outputDof);
	}

	@Override public void setSettings( Map<String, String> settings ) {
		int[] v = {numCoefs, outputDof};
		settings.forEach(( key, value ) -> {
			switch (key) {
				case "num_coefs" -> v[0] = Integer.parseInt(value);
				case "output_dof" -> v[1] = Integer.parseInt(value);
				default -> throw new IllegalArgumentException("Unknown key: " + key);
			}
		});
		// how the grid is laid out is not this operator's business, so those only have to pass checkValid
		configureGrid(new ConfigGridCoefND().cols(2).rows(2).coefs(v[0]).numOut(v[1]));
	}

	@Override public int getInputCount() {return IN_KNOT_3 + 1;}

	@Override public int getOutputSize() {return outputDof;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
