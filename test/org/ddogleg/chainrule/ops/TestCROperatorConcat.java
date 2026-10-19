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
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac.JacobianCase;
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac;
import org.ddogleg.chainrule.CommonCROperatorForward;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestCROperatorConcat extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand).tests();
	}

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(new CROperatorConcat(2, 1), new CROperatorConcat(3),
				new CROperatorConcat(1, 1, 1), new CROperatorConcat(2, 4));
	}

	/// The inputs land end to end in the order they were declared
	@Test void forward_isTheInputsEndToEnd() {
		var alg = new CROperatorConcat(2, 1);
		alg.setInput(0, new CRArray<>(new double[]{1, -2}, 0));
		alg.setInput(1, new CRArray<>(new double[]{7}, 0));

		var found = new CRArray<>(new double[3], 0);
		alg.forward(found);
		assertEquals(1.0, found.d(0));
		assertEquals(-2.0, found.d(1));
		assertEquals(7.0, found.d(2));
	}

	/// Order matters, unlike a sum. Two inputs of the same length must not be interchangeable
	@Test void forward_keepsTheInputsInOrder() {
		var alg = new CROperatorConcat(2, 2);
		alg.setInput(0, new CRArray<>(new double[]{1, 2}, 0));
		alg.setInput(1, new CRArray<>(new double[]{3, 4}, 0));

		var found = new CRArray<>(new double[4], 0);
		alg.forward(found);
		assertArrayEquals(new double[]{1, 2, 3, 4}, found.a);
	}

	/// Writing one input must not disturb another, since they arrive at different times
	@Test void inputsAreIndependent() {
		var alg = new CROperatorConcat(2, 1);
		alg.setInput(0, new CRArray<>(new double[]{1, -2}, 0));
		alg.setInput(1, new CRArray<>(new double[]{7}, 0));
		alg.setInput(1, new CRArray<>(new double[]{9}, 0));

		var found = new CRArray<>(new double[3], 0);
		alg.forward(found);
		assertArrayEquals(new double[]{1, -2, 9}, found.a);
	}

	/// The output is as long as its inputs together, and each input keeps the length it was given
	@Test void sizes() {
		var alg = new CROperatorConcat(2, 1, 4);
		assertEquals(3, alg.getInputCount());
		assertEquals(7, alg.getOutputSize());
		assertEquals(2, alg.getInputSize(0));
		assertEquals(1, alg.getInputSize(1));
		assertEquals(4, alg.getInputSize(2));

		assertEquals(0, alg.outputOffset(0));
		assertEquals(2, alg.outputOffset(1));
		assertEquals(3, alg.outputOffset(2));
	}

	/// An input has to have something in it, and there has to be an input, otherwise the output is
	/// empty and the graph it belongs to has no residuals
	@Test void degenerateShapesThrow() {
		assertThrows(RuntimeException.class, CROperatorConcat::new);
		assertThrows(RuntimeException.class, () -> new CROperatorConcat(2, 0));
		assertThrows(RuntimeException.class, () -> new CROperatorConcat(-1));
	}

	/// The shape has to survive the round trip, including how the total is split up. Two operators
	/// with the same output length but a different split are not the same operator
	@Test void settingsRoundTrip() {
		var src = new CROperatorConcat(2, 1, 4);
		var dst = new CROperatorConcat(3);
		dst.setSettings(src.getSettings());

		assertEquals(src.getSettings(), dst.getSettings());
		assertEquals(3, dst.getInputCount());
		assertEquals(7, dst.getOutputSize());
		assertEquals(3, dst.outputOffset(2));

		// same total, different split
		assertNotEquals(new CROperatorConcat(2, 1).getSettings(), new CROperatorConcat(1, 2).getSettings());
	}

	/// Where each part lands in the output is concat's own question, so it is asked here rather
	/// than by the generic checks
	@Test void outputOffset_unexpectedThrows() {
		var alg = new CROperatorConcat(2, 1);
		assertThrows(IndexOutOfBoundsException.class, () -> alg.outputOffset(2));
	}

	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var out = new ArrayList<JacobianCase<double[]>>();
		// the shape the calibration graph needs, a pixel residual and a disparity residual
		out.add(new JacobianCase<double[]>(new CROperatorConcat(2, 1)).in(0.5, -0.25).in(3.0));
		out.add(new JacobianCase<double[]>(new CROperatorConcat(3)).in(1, -2, 3));
		out.add(new JacobianCase<double[]>(new CROperatorConcat(1, 1, 1)).in(0.5).in(-3.0).in(1e-2));
		out.add(new JacobianCase<double[]>(new CROperatorConcat(2, 4))
				.in(1e-2, -2e-3).in(0.5, -0.25, 1, 2));
		return out;
	}
}
