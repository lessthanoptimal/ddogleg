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

import org.ddogleg.DDoglegJUnit;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.grid.ConfigGridCoefND;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.ddogleg.chainrule.grid.ConfigGridCoefND.NUM_KNOTS;
import static org.ddogleg.chainrule.ops.CROperatorBilinearND.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Checks the blending and Jacobian plumbing every subclass shares. A stand-in function is used rather
/// than a real one, so a change to the polynomial or the spline cannot make these pass or fail.
public class TestCROperatorBilinearND extends DDoglegJUnit {
	/// `f(c) = sum_k c_k*(1 + k*value)`. Distinct per coefficient and dependent on the value, which is
	/// all the base class needs, and it matches neither subclass
	static class Stub extends CROperatorBilinearND {
		@Override protected double evaluate( int knot, int polyOffset ) {
			double total = 0;
			for (int k = 0; k < numCoefs(); k++)
				total += knots[knot].d(polyOffset + k)*(1 + k*value);
			return total;
		}

		@Override protected double evaluateDerivative( int knot, int polyOffset ) {
			double total = 0;
			for (int k = 0; k < numCoefs(); k++)
				total += knots[knot].d(polyOffset + k)*k;
			return total;
		}

		@Override protected void functionDerivatives( double[] derivatives ) {
			for (int k = 0; k < numCoefs(); k++)
				derivatives[k] = 1 + k*value;
		}
	}

	static Stub create( int numCoefs, int outputDof ) {
		var stub = new Stub();
		stub.configureGrid(new ConfigGridCoefND().cols(2).rows(2).coefs(numCoefs).numOut(outputDof));
		return stub;
	}

	/// Every slot holds a value no other slot has, so a misread lands somewhere visible
	static double[] knotValues( int numCoefs, int outputDof ) {
		var knots = new double[NUM_KNOTS*numCoefs*outputDof];
		for (int i = 0; i < knots.length; i++)
			knots[i] = 0.5*Math.sin(i*1.7);
		return knots;
	}

	static void setCell( Stub alg, double[] knots, double value, double... weights ) {
		alg.setInput(IN_WEIGHTS, new CRArray<>(weights, 0));
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{value}, 0));
		for (int knot = 0; knot < NUM_KNOTS; knot++)
			alg.setInput(IN_KNOT_0 + knot, new CRArray<>(knots, knot*alg.knotStride()));
	}

	/// Identity in, so what comes back out of a jacobian* call is the block it built
	static CRMatrix<double[]> identity( int n ) {
		var m = CRMatrix.createD(n, n);
		for (int i = 0; i < n; i++)
			m.d(i, i, 1);
		return m;
	}

	/// The forward output is the weighted sum of each knot's function, one output at a time. Each output
	/// owns its own run of numCoefs inside the knot, so this fails if the runs are indexed by output
	@Test void blendAt() {
		int numCoefs = 3, outputDof = 2;
		var alg = create(numCoefs, outputDof);
		var knots = knotValues(numCoefs, outputDof);
		double value = 0.37;
		double[] w = {0.1, 0.2, 0.3, 0.4};
		setCell(alg, knots, value, w);

		var found = new CRArray<>(new double[outputDof], 0);
		alg.forward(found);

		for (int axis = 0; axis < outputDof; axis++) {
			double expected = 0;
			for (int knot = 0; knot < NUM_KNOTS; knot++) {
				for (int k = 0; k < numCoefs; k++) {
					expected += w[knot]*knots[knot*numCoefs*outputDof + axis*numCoefs + k]*(1 + k*value);
				}
			}
			assertEquals(expected, found.d(axis), 1e-12, "output " + axis);
		}
	}

	/// A weight scales its whole knot, so the derivative is that knot's function value. One column per
	/// knot, one row per output
	@Test void jacobianWeights() {
		int numCoefs = 2, outputDof = 2;
		var alg = create(numCoefs, outputDof);
		var knots = knotValues(numCoefs, outputDof);
		setCell(alg, knots, 0.4, 0.25, 0.25, 0.25, 0.25);

		var out = CRMatrix.createD(1, 1);
		alg.jacobianWeights(identity(outputDof), out);

		assertEquals(outputDof, out.rows);
		assertEquals(NUM_KNOTS, out.cols);
		for (int axis = 0; axis < outputDof; axis++) {
			for (int knot = 0; knot < NUM_KNOTS; knot++) {
				assertEquals(alg.evaluate(knot, axis*numCoefs), out.d(axis, knot), 1e-12,
						"output " + axis + " knot " + knot);
			}
		}
	}

	/// One column, holding the weighted sum of each knot's derivative. A knot left out of the sum is
	/// invisible in the forward pass and only shows up as a bad step
	@Test void jacobianValue() {
		int numCoefs = 3, outputDof = 2;
		var alg = create(numCoefs, outputDof);
		var knots = knotValues(numCoefs, outputDof);
		double[] w = {0.1, 0.2, 0.3, 0.4};
		setCell(alg, knots, -0.6, w);

		var out = CRMatrix.createD(1, 1);
		alg.jacobianValue(identity(outputDof), out);

		assertEquals(outputDof, out.rows);
		assertEquals(1, out.cols);
		for (int axis = 0; axis < outputDof; axis++) {
			double expected = 0;
			for (int knot = 0; knot < NUM_KNOTS; knot++)
				expected += w[knot]*alg.evaluateDerivative(knot, axis*numCoefs);
			assertEquals(expected, out.d(axis, 0), 1e-12, "output " + axis);
		}
	}

	/// A coefficient enters linearly, so its derivative is its knot's weight times what coefDerivatives
	/// reports. Every slot outside an output's own run stays zero, which is what keeps one output's
	/// derivative off another output's parameter
	@Test void jacobianKnot() {
		int numCoefs = 3, outputDof = 2;
		var alg = create(numCoefs, outputDof);
		var knots = knotValues(numCoefs, outputDof);
		double value = 0.8;
		double[] w = {0.1, 0.2, 0.3, 0.4};
		setCell(alg, knots, value, w);

		var out = CRMatrix.createD(1, 1);
		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			alg.jacobianKnot(identity(outputDof), out, knot);

			assertEquals(outputDof, out.rows);
			assertEquals(numCoefs*outputDof, out.cols);
			for (int axis = 0; axis < outputDof; axis++) {
				for (int col = 0; col < out.cols; col++) {
					int owner = col/numCoefs, k = col%numCoefs;
					double expected = owner == axis ? w[knot]*(1 + k*value) : 0;
					assertEquals(expected, out.d(axis, col), 1e-12,
							"knot " + knot + " output " + axis + " column " + col);
				}
			}
		}
	}

	/// Knots are pointed at the caller's array rather than copied, while the weights and value are read
	/// out of theirs
	@Test void setInput() {
		var alg = create(2, 2);
		assertEquals(IN_KNOT_3 + 1, alg.getInputCount());
		assertEquals(NUM_KNOTS, alg.getInputSize(IN_WEIGHTS));
		assertEquals(1, alg.getInputSize(IN_VALUE));
		for (int knot = 0; knot < NUM_KNOTS; knot++)
			assertEquals(4, alg.getInputSize(IN_KNOT_0 + knot));
		assertThrows(IndexOutOfBoundsException.class, () -> alg.getInputSize(IN_KNOT_3 + 1));
		assertThrows(IndexOutOfBoundsException.class,
				() -> alg.setInput(IN_KNOT_3 + 1, new CRArray<>(new double[1], 0)));

		// a knot keeps the array and offset it was given, so later edits are seen without setInput
		final int base = 5;
		var whole = new double[base + NUM_KNOTS*4];
		for (int i = 0; i < NUM_KNOTS*4; i++)
			whole[base + i] = 0.5*Math.sin(i*1.7);
		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			alg.setInput(IN_KNOT_0 + knot, new CRArray<>(whole, base + knot*4));
			assertEquals(base + knot*4, alg.knotOffset(knot));
		}
		alg.setInput(IN_WEIGHTS, new CRArray<>(new double[]{1, 0, 0, 0}, 0));
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{0}, 0));

		var before = new CRArray<>(new double[2], 0);
		alg.forward(before);
		whole[base] += 1.0;
		var after = new CRArray<>(new double[2], 0);
		alg.forward(after);
		assertEquals(before.d(0) + 1.0, after.d(0), 1e-12);
	}

	/// Only the function's shape crosses over. Where the grid sits and how big it is is not this
	/// operator's business, so none of it may appear here
	@Test void getSettings_setSettings() {
		assertEquals(Set.of("num_coefs", "output_dof"), create(3, 2).getSettings().keySet());

		var dst = create(1, 1);
		dst.setSettings(create(3, 2).getSettings());
		assertEquals(3, dst.numCoefs());
		assertEquals(2, dst.outputDof());
		assertEquals(6, dst.knotStride());
		// the workspace has to grow with it or the next Jacobian reads past its end
		assertEquals(3, dst.coefDeriv.length);

		assertThrows(IllegalArgumentException.class, () -> create(2, 2).setSettings(Map.of("nonsense", "1")));
	}
}
