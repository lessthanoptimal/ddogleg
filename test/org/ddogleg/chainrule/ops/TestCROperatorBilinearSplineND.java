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
import org.ddogleg.chainrule.CROperatorForward;
import org.ddogleg.chainrule.CRTestHelpers;
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac.JacobianCase;
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac;
import org.ddogleg.chainrule.CommonCROperatorForward;
import org.ddogleg.chainrule.grid.ConfigGridSplineND;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.ddogleg.chainrule.grid.ConfigGridCoefND.NUM_KNOTS;
import static org.ddogleg.chainrule.ops.CROperatorBilinearND.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestCROperatorBilinearSplineND extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand) {
			@Override public CRArray<double[]> randomInput( int which, CROperatorForward<double[]> alg ) {
				return TestCROperatorBilinearSplineND.this.randomInput(which, alg);
			}
		}.tests();
	}

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(create(2, 2, 0, 1), create(3, 1, -2, 2), create(4, 2, 3.35, 9.99), create(2, 3, 0, 10));
	}

	static CROperatorBilinearSplineND create( int numCoefs, int outputDof, double min, double max ) {
		return new CROperatorBilinearSplineND().configure(config(numCoefs, outputDof, min, max));
	}

	static ConfigGridSplineND config( int numCoefs, int outputDof, double min, double max ) {
		var config = new ConfigGridSplineND();
		config.grid.cols(2).rows(2).coefs(numCoefs).numOut(outputDof);
		return config.min(min).max(max);
	}

	/// Weights are what a bilinear cell would produce, so they sum to one. The value has to land inside
	/// the configured range, since past the clamp every sample reads the same bucket
	CRArray<double[]> randomInput( int which, CROperatorForward<double[]> alg ) {
		if (which == IN_WEIGHTS) {
			double fc = rand.nextDouble(), fr = rand.nextDouble();
			return new CRArray<>(new double[]{(1 - fc)*(1 - fr), fc*(1 - fr), (1 - fc)*fr, fc*fr}, 0);
		}
		if (which == IN_VALUE) {
			var spline = (CROperatorBilinearSplineND)alg;
			double f = 0.1 + 0.8*rand.nextDouble();
			return new CRArray<>(new double[]{spline.valueMin() + f*(spline.valueMax() - spline.valueMin())}, 0);
		}
		return CRTestHelpers.randomCRArray(alg.getInputSize(which), rand);
	}

	static void setCell( CROperatorBilinearSplineND alg, double[] knots, double... weights ) {
		alg.setInput(IN_WEIGHTS, new CRArray<>(weights, 0));
		for (int knot = 0; knot < NUM_KNOTS; knot++)
			alg.setInput(IN_KNOT_0 + knot, new CRArray<>(knots, knot*alg.knotStride()));
	}

	/// All four knots holding the same samples, so the blend is a no-op and only the interpolation in
	/// `value` is under test
	static CROperatorBilinearSplineND withSamples( double min, double max, double... samples ) {
		var alg = create(samples.length, 1, min, max);
		var knots = new double[NUM_KNOTS*samples.length];
		for (int knot = 0; knot < NUM_KNOTS; knot++)
			System.arraycopy(samples, 0, knots, knot*samples.length, samples.length);
		setCell(alg, knots, 0.25, 0.25, 0.25, 0.25);
		return alg;
	}

	static double at( CROperatorBilinearSplineND alg, double value ) {
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{value}, 0));
		var found = new CRArray<>(new double[alg.getOutputSize()], 0);
		alg.forward(found);
		return found.d(0);
	}

	/// The samples are evenly spaced across the range and every one has to be hit exactly. The last one
	/// is the one at risk: scale the coordinate by coefs-2 instead of coefs-1 and it starts no bucket,
	/// always carries weight zero, and becomes a parameter nothing constrains
	@Test void everySampleIsReachedAtFullWeight() {
		for (int numCoefs = 2; numCoefs <= 5; numCoefs++) {
			var samples = new double[numCoefs];
			for (int i = 0; i < numCoefs; i++)
				samples[i] = 10*(i + 1);           // distinct, so reading the wrong one is obvious
			var alg = withSamples(3.35, 9.99, samples);

			for (int i = 0; i < numCoefs; i++) {
				double v = 3.35 + (9.99 - 3.35)*i/(numCoefs - 1.0);
				assertEquals(samples[i], at(alg, v), 1e-9, "coefs " + numCoefs + " sample " + i);
			}
		}
	}

	/// Halfway between two samples is their average, which is the whole point of the scheme
	@Test void interpolatesLinearlyBetweenSamples() {
		var alg = withSamples(0, 1, 0, 10, 30);       // buckets [0,0.5] and [0.5,1]

		assertEquals(5.0, at(alg, 0.25), 1e-9);
		assertEquals(10.0, at(alg, 0.5), 1e-9);
		assertEquals(20.0, at(alg, 0.75), 1e-9);
		assertEquals(14.0, at(alg, 0.6), 1e-9);
	}

	/// Outside the range it clamps rather than extrapolating. That is what bounds the output no matter
	/// what it is handed, where a polynomial runs away
	@Test void clampsOutsideTheRange() {
		var alg = withSamples(0, 1, 0, 10, 30);

		assertEquals(0.0, at(alg, -5), 1e-9);
		assertEquals(0.0, at(alg, -1e9), 1e-9);
		assertEquals(30.0, at(alg, 6), 1e-9);
		assertEquals(30.0, at(alg, 1e9), 1e-9);
	}

	/// Two samples is the minimum and makes the whole range one bucket, so it is plain linear
	@Test void twoSamplesIsLinear() {
		var alg = withSamples(2, 6, 1, 5);
		assertEquals(1.0, at(alg, 2), 1e-9);
		assertEquals(3.0, at(alg, 4), 1e-9);
		assertEquals(5.0, at(alg, 6), 1e-9);
	}

	/// One sample describes a function between nothing, so it is rejected rather than quietly behaving
	/// like a constant
	@Test void rejectsFewerThanTwoSamples() {
		var config = config(1, 1, 0, 1);
		assertThrows(IllegalArgumentException.class, () -> new CROperatorBilinearSplineND().configure(config));
	}

	/// A backwards range has no buckets to put a value in. Nothing checks this once values are
	/// arriving, so it has to be caught while configuring
	@Test void configureRejectsABackwardsRange() {
		assertThrows(RuntimeException.class,
				() -> new CROperatorBilinearSplineND().configure(config(3, 1, 5, 1)));
	}

	/// Only the two samples bracketing the value move the output, so the rest of the knot has no
	/// derivative. That locality is what stops one capture's data reaching every sample
	@Test void onlyTheBracketingSamplesHaveADerivative() {
		var alg = create(4, 1, 0, 3);
		var knots = new double[NUM_KNOTS*4];
		for (int i = 0; i < knots.length; i++)
			knots[i] = 0.5*Math.sin(i*1.7);
		setCell(alg, knots, 0.1, 0.2, 0.3, 0.4);
		// 1.25 sits a quarter of the way through the bucket between samples 1 and 2
		alg.setInput(IN_VALUE, new CRArray<>(new double[]{1.25}, 0));

		var identity = CRMatrix.createD(1, 1);
		identity.d(0, 0, 1);
		var out = CRMatrix.createD(1, 1);
		alg.jacobianKnot(identity, out, 2);

		assertEquals(0.0, out.d(0, 0), 1e-12, "sample 0");
		assertEquals(0.3*0.75, out.d(0, 1), 1e-12, "sample 1");
		assertEquals(0.3*0.25, out.d(0, 2), 1e-12, "sample 2");
		assertEquals(0.0, out.d(0, 3), 1e-12, "sample 3");
	}

	/// The bucket range has to survive a settings round trip, or a restored model reads its samples at
	/// the wrong places and is silently a different function
	@Test void settingsCarryTheRange() {
		var src = create(4, 2, 3.35, 9.99);
		var dst = create(2, 1, 0, 1);
		dst.setSettings(src.getSettings());

		assertEquals(4, dst.numCoefs());
		assertEquals(2, dst.outputDof());
		assertEquals(3.35, dst.valueMin(), 1e-12);
		assertEquals(9.99, dst.valueMax(), 1e-12);
		assertEquals(src.getSettings(), dst.getSettings());
	}

	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var out = new ArrayList<JacobianCase<double[]>>();
		out.add(testCase(2, 2, 0.3, 0.7, 0.4, 0, 1));
		out.add(testCase(3, 2, 0.1, 0.9, 0.25, 0, 1));
		// the focus axis this is meant for, in diopters
		out.add(testCase(4, 2, 0.5, 0.5, 6.0, 3.35, 9.99));
		out.add(testCase(5, 2, 0.2, 0.8, 8.4, 3.35, 9.99));
		// a single output is the aberration's shape, where anything assuming two reads past the end
		out.add(testCase(3, 1, 0.6, 0.2, -0.6, -2, 2));
		// more outputs than a pixel has axes, so nothing may tie the two together
		out.add(testCase(2, 3, 0.4, 0.35, 4.0, 0, 10));
		// outside the cell, which is an ordinary case for the spatial weights
		out.add(testCase(3, 2, 1.4, -0.3, 0.6, 0, 1));
		return out;
	}

	/// Values sit inside a bucket, away from its edges. At an edge the piecewise linear derivative
	/// is one sided and a central difference reports the average of the two slopes, which is a
	/// property of the function rather than a defect in the Jacobian
	private JacobianCase<double[]> testCase( int numCoefs, int outputDof, double fracCol, double fracRow,
										 double value, double min, double max ) {
		var alg = create(numCoefs, outputDof, min, max);
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
