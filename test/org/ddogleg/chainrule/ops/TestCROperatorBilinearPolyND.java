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
import org.ddogleg.chainrule.CROperatorForward;
import org.ddogleg.chainrule.CRTestHelpers;
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac.JacobianCase;
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac;
import org.ddogleg.chainrule.CommonCROperatorForward;
import org.ddogleg.chainrule.grid.ConfigGridCoefND;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.ddogleg.chainrule.grid.ConfigGridCoefND.NUM_KNOTS;
import static org.ddogleg.chainrule.ops.CROperatorBilinearPolyND.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCROperatorBilinearPolyND extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand) {
			@Override public CRArray<double[]> randomInput( int which, CROperatorForward<double[]> alg ) {
				return TestCROperatorBilinearPolyND.this.randomInput(which, alg);
			}
		}.tests();
	}

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(create(2, 2), create(1, 1), create(3, 2), create(2, 3));
	}

	static CROperatorBilinearPolyND create( int numCoefs, int outputDof ) {
		return new CROperatorBilinearPolyND().configure(config(numCoefs, outputDof));
	}

	/// cols and rows are no longer this operator's business, but the config still checks them
	static ConfigGridCoefND config( int numCoefs, int outputDof ) {
		return new ConfigGridCoefND().cols(2).rows(2).coefs(numCoefs).numOut(outputDof);
	}

	/// Weights are what a bilinear cell would produce, so they sum to one
	CRArray<double[]> randomInput( int which, CROperatorForward<double[]> alg ) {
		if (which == IN_WEIGHTS) {
			double fc = rand.nextDouble(), fr = rand.nextDouble();
			return new CRArray<>(new double[]{(1 - fc)*(1 - fr), fc*(1 - fr), (1 - fc)*fr, fc*fr}, 0);
		}
		if (which == IN_VALUE)
			return new CRArray<>(new double[]{0.5*uniform()}, 0);
		return CRTestHelpers.randomCRArray(alg.getInputSize(which), rand);
	}

	/// Feeds the weights and the four knots, which is everything except the value
	static void setCell( CROperatorBilinearPolyND alg, double[] knots, double... weights ) {
		alg.setInput(IN_WEIGHTS, new CRArray<>(weights, 0));
		int stride = alg.knotStride();
		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			alg.setInput(IN_KNOT_0 + knot, new CRArray<>(knots, knot*stride));
		}
	}

	/// Six inputs: the weights, the value, and one per knot
	@Test void inputShape() {
		var alg = create(3, 2);
		assertEquals(IN_KNOT_3 + 1, alg.getInputCount());
		assertEquals(NUM_KNOTS, alg.getInputSize(IN_WEIGHTS));
		assertEquals(1, alg.getInputSize(IN_VALUE));
		for (int knot = 0; knot < NUM_KNOTS; knot++)
			assertEquals(2*3, alg.getInputSize(IN_KNOT_0 + knot));
		assertEquals(2, alg.getOutputSize());
		assertEquals(6, alg.knotStride());
	}

	/// Nothing about pixels, images or grid size reaches this operator any more
	@Test void settingsAreOnlyThePolynomialShape() {
		assertEquals(java.util.Set.of("num_coefs", "output_dof"), create(3, 2).getSettings().keySet());

		var dst = create(1, 1);
		dst.setSettings(create(3, 2).getSettings());
		assertEquals(3, dst.numCoefs());
		assertEquals(2, dst.outputDof());
		assertEquals(6, dst.knotStride());
	}

	/// All the weight on one knot returns that knot's polynomials untouched, which is the one case
	/// interpolation cannot get wrong. Also pins the knot ordering to the input ordering
	@Test void oneWeightSelectsOneKnot() {
		var alg = create(1, 2);
		// knot k holds (10k, -10k)
		var knots = new double[NUM_KNOTS*2];
		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			knots[knot*2] = 10*knot;
			knots[knot*2 + 1] = -10*knot;
		}
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{0.3}, 0));

		var found = new CRArray<>(new double[2], 0);
		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			var weights = new double[NUM_KNOTS];
			weights[knot] = 1;
			setCell(alg, knots, weights);
			alg.forward(found);
			assertEquals(10*knot, found.d(0), 1e-12, "knot " + knot);
			assertEquals(-10*knot, found.d(1), 1e-12, "knot " + knot);
		}
	}

	/// Every knot holding the same polynomial gives that polynomial back for any weights that sum to
	/// one, whatever they are
	@Test void identicalKnotsIgnoreTheWeights() {
		var alg = create(1, 1);
		var knots = new double[]{2.5, 2.5, 2.5, 2.5};

		var found = new CRArray<>(new double[1], 0);
		for (double[] w : new double[][]{{1, 0, 0, 0}, {0.25, 0.25, 0.25, 0.25}, {0.1, 0.2, 0.3, 0.4}}) {
			setCell(alg, knots, w);
			alg.setInput(IN_VALUE, new CRArray<>(new double[]{0}, 0));
			alg.forward(found);
			assertEquals(2.5, found.d(0), 1e-12);
		}
	}

	/// The blend is linear in the weights, so half of two knots is the average of them
	@Test void blendIsLinearInTheWeights() {
		var alg = create(1, 1);
		var knots = new double[]{1, 5, 0, 0};
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{0}, 0));

		var found = new CRArray<>(new double[1], 0);
		setCell(alg, knots, 0.5, 0.5, 0, 0);
		alg.forward(found);
		assertEquals(3.0, found.d(0), 1e-12);
	}

	/// Weights outside [0,1] extrapolate rather than being rejected. This is how a pixel outside its
	/// cell is handled, so it has to be an ordinary case and not an error
	@Test void weightsOutsideTheCellExtrapolate() {
		var alg = create(1, 1);
		var knots = new double[]{1, 5, 0, 0};
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{0}, 0));

		var found = new CRArray<>(new double[1], 0);
		// fracCol = 2, one cell past the far knot: (1-2)*1 + 2*5
		setCell(alg, knots, -1, 2, 0, 0);
		alg.forward(found);
		assertEquals(9.0, found.d(0), 1e-12);
	}

	/// The polynomial is in `value` and every coefficient has to be used. Each output also owns its own
	/// run of `numCoefs`, so this fails if the runs are indexed by output rather than output*numCoefs
	@Test void polynomialUsesEveryCoefficient() {
		var alg = create(3, 2);
		// same polynomial at every knot so the blend is a no-op: p0 = 1 + 2v + 3v^2, p1 = -1 + 0 + 4v^2
		var knots = new double[NUM_KNOTS*6];
		for (int i = 0; i < knots.length; i += 6) {
			knots[i] = 1; knots[i + 1] = 2; knots[i + 2] = 3;
			knots[i + 3] = -1; knots[i + 4] = 0; knots[i + 5] = 4;
		}
		setCell(alg, knots, 0.1, 0.2, 0.3, 0.4);

		var found = new CRArray<>(new double[2], 0);
		for (double v : new double[]{0, 0.5, -0.5, 2}) {
			alg.setInput(IN_VALUE, new CRArray<>(new double[]{v}, 0));
			alg.forward(found);
			assertEquals(1 + 2*v + 3*v*v, found.d(0), 1e-9, "p0 at v=" + v);
			assertEquals(-1 + 4*v*v, found.d(1), 1e-9, "p1 at v=" + v);
		}
	}

	/// A single output is the aberration's shape. Nothing here may assume there are two of them
	@Test void singleOutputDimension() {
		var alg = create(2, 1);
		var knots = new double[NUM_KNOTS*2];
		for (int i = 0; i < knots.length; i += 2) {
			knots[i] = 0.5;
			knots[i + 1] = -2;
		}
		setCell(alg, knots, 0.25, 0.25, 0.25, 0.25);

		var found = new CRArray<>(new double[1], 0);
		for (double v : new double[]{0, 1.5, -0.75}) {
			alg.setInput(IN_VALUE, new CRArray<>(new double[]{v}, 0));
			alg.forward(found);
			assertEquals(0.5 - 2*v, found.d(0), 1e-9, "at v=" + v);
		}
	}

	/// One coefficient means the output does not depend on `value` at all
	@Test void oneCoefficientIgnoresValue() {
		var alg = create(1, 2);
		var knots = new double[NUM_KNOTS*2];
		for (int i = 0; i < knots.length; i++)
			knots[i] = 0.1*(i + 1);
		setCell(alg, knots, 0.1, 0.2, 0.3, 0.4);

		var a = new CRArray<>(new double[2], 0);
		var b = new CRArray<>(new double[2], 0);
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{0}, 0));
		alg.forward(a);
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{7.5}, 0));
		alg.forward(b);
		assertEquals(a.d(0), b.d(0));
		assertEquals(a.d(1), b.d(1));
	}

	/// Nothing is copied, so a caller can point a knot at a slice of a larger array and the values it
	/// reads are the ones sitting there when it runs
	@Test void knotsAreReadFromTheCallersArray() {
		final int base = 7;
		var alg = create(2, 2);

		var whole = new double[base + NUM_KNOTS*4];
		for (int i = 0; i < NUM_KNOTS*4; i++)
			whole[base + i] = 0.5*Math.sin(i*1.7);

		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			alg.setInput(IN_KNOT_0 + knot, new CRArray<>(whole, base + knot*4));
			assertEquals(base + knot*4, alg.knotOffset(knot));
		}
		alg.setInput(IN_WEIGHTS, new CRArray<>(new double[]{0.1, 0.2, 0.3, 0.4}, 0));
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{0.4}, 0));

		var before = new CRArray<>(new double[2], 0);
		alg.forward(before);

		// changing the array behind a knot changes what it reads, without another setInput
		whole[base] += 1.0;
		var after = new CRArray<>(new double[2], 0);
		alg.forward(after);
		assertEquals(before.d(0) + 0.1, after.d(0), 1e-12);
	}

	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var out = new ArrayList<JacobianCase<double[]>>();
		out.add(testCase(2, 2, 0.3, 0.7, 0.4));
		out.add(testCase(1, 2, 0.5, 0.5, 0.0));
		out.add(testCase(3, 2, 0.1, 0.9, -0.3));
		// a single output is the aberration's shape, where anything assuming two would read past
		// the end of a knot
		out.add(testCase(2, 1, 0.6, 0.2, 0.6));
		out.add(testCase(1, 1, 0.25, 0.75, -1.2));
		// more outputs than a pixel has axes, so nothing may tie the two together
		out.add(testCase(2, 3, 0.4, 0.35, 0.2));
		// outside the cell, which is an ordinary case here
		out.add(testCase(2, 2, 1.4, -0.3, 0.25));
		return out;
	}

	private JacobianCase<double[]> testCase( int numCoefs, int outputDof,
										double fracCol, double fracRow, double value ) {
		var alg = create(numCoefs, outputDof);
		int stride = numCoefs*outputDof;

		var knots = new double[NUM_KNOTS*stride];
		for (int i = 0; i < knots.length; i++)
			knots[i] = 0.5*Math.sin(i*1.7);        // deterministic, order one, mixed signs

		var testCase = new JacobianCase<double[]>(alg)
				.in((1 - fracCol)*(1 - fracRow), fracCol*(1 - fracRow),
						(1 - fracCol)*fracRow, fracCol*fracRow)
				.in(value);
		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			testCase.in(Arrays.copyOfRange(knots, knot*stride, (knot + 1)*stride));
		}
		return testCase;
	}
}
