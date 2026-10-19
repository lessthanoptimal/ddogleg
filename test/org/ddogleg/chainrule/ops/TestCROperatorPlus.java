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

import static org.ddogleg.chainrule.ops.CROperatorPlus.IN_A;
import static org.ddogleg.chainrule.ops.CROperatorPlus.IN_B;
import static org.junit.jupiter.api.Assertions.*;

public class TestCROperatorPlus extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand).tests();
	}

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(new CROperatorPlus(1), new CROperatorPlus(2), new CROperatorPlus(3));
	}

	/// Element by element, in the order they arrived
	@Test void forward_isTheSum() {
		var alg = new CROperatorPlus(3);
		alg.setInput(IN_A, new CRArray<>(new double[]{1, -2, 3}, 0));
		alg.setInput(IN_B, new CRArray<>(new double[]{0.5, 0.25, -10}, 0));

		var found = new CRArray<>(new double[3], 0);
		alg.forward(found);
		assertEquals(1.5, found.d(0));
		assertEquals(-1.75, found.d(1));
		assertEquals(-7.0, found.d(2));
	}

	/// Both sides count the same, so swapping them cannot change the answer. Catches one side being
	/// dropped or read in place of the other
	@Test void forward_isSymmetric() {
		double[] x = {3, -1}, y = {-0.5, 7};

		var forward = new CRArray<>(new double[2], 0);
		var backward = new CRArray<>(new double[2], 0);

		var a = new CROperatorPlus(2);
		a.setInput(IN_A, new CRArray<>(x, 0));
		a.setInput(IN_B, new CRArray<>(y, 0));
		a.forward(forward);

		var b = new CROperatorPlus(2);
		b.setInput(IN_A, new CRArray<>(y, 0));
		b.setInput(IN_B, new CRArray<>(x, 0));
		b.forward(backward);

		assertEquals(forward.d(0), backward.d(0));
		assertEquals(forward.d(1), backward.d(1));
	}

	@Test void sizes() {
		for (int dof : new int[]{1, 2, 5}) {
			var alg = new CROperatorPlus(dof);
			assertEquals(2, alg.getInputCount());
			assertEquals(dof, alg.getOutputSize());
			assertEquals(dof, alg.getInputSize(IN_A));
			assertEquals(dof, alg.getInputSize(IN_B));
		}
	}

	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var out = new ArrayList<JacobianCase<double[]>>();
		out.add(new JacobianCase<double[]>(new CROperatorPlus(1)).in(0.5).in(-3.0));
		out.add(new JacobianCase<double[]>(new CROperatorPlus(2)).in(1e-2, -2e-3).in(0.5, -0.25));
		out.add(new JacobianCase<double[]>(new CROperatorPlus(3)).in(1, -2, 3).in(0.25, 0.5, -1));
		return out;
	}
}
