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

package org.ddogleg.chainrule.grid;

import org.ddogleg.DDoglegJUnit;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.ops.CROperatorBilinearND;
import org.ddogleg.chainrule.ops.CROperatorBilinearSplineND;
import org.junit.jupiter.api.Test;

import static org.ddogleg.chainrule.grid.ConfigGridCoefND.NUM_KNOTS;
import static org.junit.jupiter.api.Assertions.*;

public class TestGridNdHelper extends DDoglegJUnit {
	static final int W = 400, H = 300;

	static GridNdHelper create( int cols, int rows, int coefs, int numOut, int padding ) {
		var grid = new GridCoefND(new ConfigGridCoefND()
				.cols(cols).rows(rows).coefs(coefs).numOut(numOut).padding(padding));
		var helper = new GridNdHelper();
		helper.configure(grid, W, H);
		return helper;
	}

	/// Puts the helper on the cell containing a point, which is what a caller does before blending
	static void locate( GridNdHelper helper, double x, double y ) {
		helper.coorToGrid(x, y);
		helper.gridToWeights();
	}

	/// Bilinear weights are a partition of unity inside the grid, whatever the point
	@Test void weightsSumToOne() {
		var helper = create(5, 4, 1, 1, 0);

		for (double[] p : new double[][]{{0, 0}, {W, H}, {123.4, 45.6}, {W/2.0, H/2.0}, {399, 1}}) {
			locate(helper, p[0], p[1]);
			double total = helper.w0 + helper.w1 + helper.w2 + helper.w3;
			assertEquals(1.0, total, 1e-12, "at (" + p[0] + " , " + p[1] + ")");
		}
	}

	/// A point on a knot puts all the weight on it, which also pins the corner order
	@Test void onAKnotAllTheWeightIsThere() {
		final int cols = 3, rows = 3;
		var helper = create(cols, rows, 1, 1, 0);

		locate(helper, 0, 0);
		assertEquals(0, helper.col);
		assertEquals(0, helper.row);
		assertEquals(1.0, helper.w0, 1e-12);
		assertEquals(0.0, helper.w1 + helper.w2 + helper.w3, 1e-12);

		// the last knot starts no cell, so it is the far corner of the one before it
		locate(helper, W, H);
		assertEquals(cols - 2, helper.col);
		assertEquals(rows - 2, helper.row);
		assertEquals(1.0, helper.w3, 1e-12);
	}

	/// The weights are the outer product of the column and row fractions, in that order. A swap still
	/// sums to one, so only an asymmetric point can see it
	@Test void cornerOrderIsColumnThenRow() {
		var helper = create(3, 3, 1, 1, 0);
		locate(helper, 0.25*W/2.0, 0.75*H/2.0);

		double fc = 0.25, fr = 0.75;
		assertEquals((1 - fc)*(1 - fr), helper.w0, 1e-12);
		assertEquals(fc*(1 - fr), helper.w1, 1e-12);
		assertEquals((1 - fc)*fr, helper.w2, 1e-12);
		assertEquals(fc*fr, helper.w3, 1e-12);
	}

	/// A point off the edge clamps to the nearest cell and extrapolates, rather than being an error
	@Test void outsideTheGridExtrapolates() {
		var helper = create(3, 3, 1, 1, 0);
		locate(helper, -W, 0);

		assertEquals(0, helper.col);
		assertEquals(-2.0, helper.fracCol, 1e-12);
		assertEquals(3.0, helper.w0, 1e-12);
		assertEquals(-2.0, helper.w1, 1e-12);
		assertEquals(1.0, helper.w0 + helper.w1 + helper.w2 + helper.w3, 1e-12);
	}

	/// Padding moves the image inwards, so a corner pixel lands on an interior knot
	@Test void paddingMovesTheImageInwards() {
		var helper = create(4, 4, 1, 1, 1);

		locate(helper, 0, 0);
		assertEquals(1, helper.col);
		assertEquals(1, helper.row);
		assertEquals(1.0, helper.w0, 1e-12);
	}

	/// gridToWeights also works out where the knots are, so blend can be called per output without
	/// redoing it. Those locations have to be the cell's corners
	@Test void gridToWeightsFindsTheKnots() {
		final int cols = 5, rows = 4, coefs = 2, numOut = 3;
		var helper = create(cols, rows, coefs, numOut, 0);
		locate(helper, W/2.0 + 3, H/2.0 + 3);

		var expected = new int[NUM_KNOTS];
		helper.grid.config.knotOffsets(helper.col, helper.row, 0, expected);
		assertArrayEquals(expected, helper.knotOffsets);
	}

	/// All the weight on one knot returns that knot's polynomial, for every output
	@Test void blend_oneKnot() {
		final int coefs = 2, numOut = 3;
		var helper = create(3, 3, coefs, numOut, 0);
		var grid = helper.grid;

		// knot k, output d holds the polynomial (10k + d) + 0*v
		for (int knot = 0; knot < grid.config.cols*grid.config.rows; knot++) {
			for (int d = 0; d < numOut; d++) {
				grid.parameters[knot*coefs*numOut + d*coefs] = 10*knot + d;
			}
		}

		locate(helper, 0, 0);
		for (int d = 0; d < numOut; d++) {
			assertEquals(d, helper.blendPoly(0.7, d), 1e-12, "output " + d);
		}
	}

	/// Every output reads its own run of coefficients, so this fails if the runs are indexed by output
	/// rather than output*coefs
	@Test void blend_outputsAreSeparate() {
		final int coefs = 3, numOut = 2;
		var helper = create(2, 2, coefs, numOut, 0);
		var grid = helper.grid;

		// the same polynomials at every knot, so the blend is a no-op
		for (int i = 0; i < grid.parameters.length; i += coefs*numOut) {
			grid.parameters[i] = 1; grid.parameters[i + 1] = 2; grid.parameters[i + 2] = 3;
			grid.parameters[i + 3] = -1; grid.parameters[i + 4] = 0; grid.parameters[i + 5] = 4;
		}

		locate(helper, 137, 88);
		for (double v : new double[]{0, 0.5, -0.5, 2}) {
			assertEquals(1 + 2*v + 3*v*v, helper.blendPoly(v, 0), 1e-9, "output 0 at v=" + v);
			assertEquals(-1 + 4*v*v, helper.blendPoly(v, 1), 1e-9, "output 1 at v=" + v);
		}
	}

	/// A linear ramp across the knots is reproduced exactly by bilinear interpolation, so the blend can
	/// be predicted in closed form anywhere. Catches an off by one in the cell scaling
	@Test void blend_linearRampIsExact() {
		final int cols = 3, rows = 2;
		var helper = create(cols, rows, 1, 1, 0);
		var grid = helper.grid;

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				grid.parameters[row*cols + col] = col;
			}
		}

		for (int i = 0; i <= 10; i++) {
			double x = W*i/10.0;
			locate(helper, x, 17);
			assertEquals((cols - 1)*x/W, helper.blendPoly(0, 0), 1e-9, "at x=" + x);
		}
	}

	/// evaluate walks the whole polynomial. Re-reading the constant term gives c0*(1+v+v^2)
	@Test void evaluate_usesEveryCoefficient() {
		var helper = create(2, 2, 3, 1, 0);
		var grid = helper.grid;
		grid.parameters[0] = 1; grid.parameters[1] = 2; grid.parameters[2] = 3;

		for (double v : new double[]{0, 0.5, -0.5, 2}) {
			assertEquals(1 + 2*v + 3*v*v, helper.evaluatePoly(v, 0), 1e-9, "at v=" + v);
		}
	}

	static GridNdHelper createSpline( int cols, int rows, int coefs, int numOut, double min, double max ) {
		var helper = create(cols, rows, coefs, numOut, 0);
		helper.configureSpline(min, max);
		return helper;
	}

	/// Every sample is reached at full weight, so none of them is a parameter nothing can constrain.
	/// The last one is the one at risk, since it starts no bucket of its own
	@Test void evaluateSpline_reachesEverySample() {
		var helper = createSpline(2, 2, 4, 1, 3.35, 9.99);
		double[] samples = {2, -7, 11, 4};
		System.arraycopy(samples, 0, helper.grid.parameters, 0, samples.length);

		for (int i = 0; i < samples.length; i++) {
			double v = 3.35 + (9.99 - 3.35)*i/(samples.length - 1.0);
			assertEquals(samples[i], helper.evaluateSpline(v, 0), 1e-9, "sample " + i);
		}
	}

	/// Linear between samples, and clamped outside the range rather than extrapolated. The clamp is
	/// what keeps a distortion grid bounded where a polynomial runs away
	@Test void evaluateSpline_interpolatesAndClamps() {
		var helper = createSpline(2, 2, 3, 1, 0, 1);
		double[] samples = {0, 10, 30};
		System.arraycopy(samples, 0, helper.grid.parameters, 0, samples.length);

		assertEquals(5.0, helper.evaluateSpline(0.25, 0), 1e-9);
		assertEquals(20.0, helper.evaluateSpline(0.75, 0), 1e-9);
		assertEquals(0.0, helper.evaluateSpline(-1e9, 0), 1e-9);
		assertEquals(30.0, helper.evaluateSpline(1e9, 0), 1e-9);
	}

	/// The model has to evaluate exactly what the fit optimized, so the helper and the operator have to
	/// be the same function. They are separate implementations, which is why this is checked rather
	/// than assumed
	@Test void evaluateSpline_matchesTheOperator() {
		final double min = 3.35, max = 9.99;
		for (int coefs = 2; coefs <= 5; coefs++) {
			var helper = createSpline(2, 2, coefs, 1, min, max);
			for (int i = 0; i < helper.grid.parameters.length; i++)
				helper.grid.parameters[i] = 0.5*Math.sin(i*1.7);

			var opConfig = new ConfigGridSplineND();
			opConfig.grid.cols(2).rows(2).coefs(coefs).numOut(1);
			var op = new org.ddogleg.chainrule.ops.CROperatorBilinearSplineND()
					.configure(opConfig.min(min).max(max));
			// all the weight on knot 0, so the blend is a no-op and only the sample lookup is compared
			op.setInput(org.ddogleg.chainrule.ops.CROperatorBilinearND.IN_WEIGHTS,
					new org.ddogleg.chainrule.CRArray<>(new double[]{1, 0, 0, 0}, 0));
			for (int knot = 0; knot < NUM_KNOTS; knot++)
				op.setInput(org.ddogleg.chainrule.ops.CROperatorBilinearND.IN_KNOT_0 + knot,
						new org.ddogleg.chainrule.CRArray<>(helper.grid.parameters, 0));

			var found = new org.ddogleg.chainrule.CRArray<>(new double[1], 0);
			for (double v : new double[]{min, 4.0, 6.0, 8.4, max, min - 5, max + 5}) {
				op.setInput(org.ddogleg.chainrule.ops.CROperatorBilinearND.IN_VALUE,
						new org.ddogleg.chainrule.CRArray<>(new double[]{v}, 0));
				op.forward(found);
				assertEquals(found.d(0), helper.evaluateSpline(v, 0), 1e-12, "coefs " + coefs + " v=" + v);
			}
		}
	}

	/// blendSpline mixes the four knots the same way blendPoly does, so identical knots give that
	/// sample back whatever the weights are
	@Test void blendSpline_mixesTheKnots() {
		var helper = createSpline(2, 2, 3, 1, 0, 1);
		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			helper.grid.parameters[knot*3] = 0;
			helper.grid.parameters[knot*3 + 1] = 10;
			helper.grid.parameters[knot*3 + 2] = 30;
		}
		locate(helper, 123.4, 45.6);
		assertEquals(5.0, helper.blendSpline(0.25, 0), 1e-9);

		// now make one knot differ, so a blend that ignored the weights would show up
		helper.grid.parameters[1] = 20;
		assertTrue(Math.abs(helper.blendSpline(0.25, 0) - 5.0) > 1e-6);
	}

	/// The analytic derivatives match differentiating the evaluations numerically. The tolerance is
	/// set by the central-difference truncation, not the double convention
	@Test void polyDerivsMatchNumerical() {
		var helper = create(5, 4, 3, 2, 0);
		for (int i = 0; i < helper.grid.parameters.length; i++) {
			helper.grid.parameters[i] = (rand.nextDouble() - 0.5)*2.0;
		}
		locate(helper, 123.4, 45.6);

		double h = 1e-6;
		for (double v : new double[]{-1.5, 0.0, 0.7, 2.0}) {
			double numeric = (helper.evaluatePoly(v + h, 0) - helper.evaluatePoly(v - h, 0))/(2*h);
			assertEquals(numeric, helper.evaluatePolyDeriv(v, 0), 1e-6, "evaluate at v=" + v);

			for (int out = 0; out < 2; out++) {
				numeric = (helper.blendPoly(v + h, out) - helper.blendPoly(v - h, out))/(2*h);
				assertEquals(numeric, helper.blendPolyDeriv(v, out), 1e-6, "blend output " + out + " at v=" + v);
			}
		}
	}

	/// A range with no width has no buckets to put a value in
	@Test void configureSpline_rejectsABadRange() {
		var helper = create(2, 2, 3, 1, 0);
		assertThrows(IllegalArgumentException.class, () -> helper.configureSpline(5, 5));
		assertThrows(IllegalArgumentException.class, () -> helper.configureSpline(5, 1));
	}
}
