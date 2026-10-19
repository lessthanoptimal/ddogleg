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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCRPenaltyL2 extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand).tests();
	}

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(create(4, 0.5), create(2, 1.0), create(1, 2.0), create(6, 0.25));
	}

	static CRPenaltyL2 create( int length, double weight ) {
		return new CRPenaltyL2(length).weight(weight);
	}

	/// Each residual is its own parameter times the weight, in the order they arrived
	@Test void forward_isWeightTimesValue() {
		var alg = create(4, 0.5);
		alg.setInput(0, new CRArray<>(new double[]{1, -2, 3, -4}, 0));

		var found = new CRArray<>(new double[4], 0);
		alg.forward(found);
		for (int i = 0; i < 4; i++)
			assertEquals(0.5*new double[]{1, -2, 3, -4}[i], found.d(i));
	}

	/// The input is read from the offset it was given, so a caller can point this at a slice of a
	/// larger parameter vector without copying it out first
	@Test void readsFromTheOffset() {
		var alg = create(2, 1.0);
		var whole = new double[]{9, 9, 9, 7, -8, 9};
		alg.setInput(0, new CRArray<>(whole, 3));

		var found = new CRArray<>(new double[2], 0);
		alg.forward(found);
		assertEquals(7.0, found.d(0));
		assertEquals(-8.0, found.d(1));
	}

	/// Rows and columns correspond one to one, whatever the weight. A group that should not be
	/// penalized is left out by the caller rather than weighted zero
	@Test void sizesAreTheBlockLength() {
		for (int length : new int[]{0, 1, 5}) {
			var alg = create(length, 0.5);
			assertEquals(length, alg.getOutputSize());
			assertEquals(length, alg.getInputSize(0));
		}
		// a zero weight still emits its rows; it does not silently reshape the residual vector
		assertEquals(3, create(3, 0.0).getOutputSize());
	}

	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var out = new ArrayList<JacobianCase<double[]>>();
		out.add(new JacobianCase<>(create(4, 0.5)).in(new double[]{1e-2, -2e-3, 0.5, -0.25}));
		out.add(new JacobianCase<>(create(2, 1.0)).in(new double[]{0.5, -3.0}));
		out.add(new JacobianCase<>(create(1, 2.0)).in(new double[]{1e-4}));
		return out;
	}
}
