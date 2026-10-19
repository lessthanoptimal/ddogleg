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
import org.ejml.UtilEjml;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.ddogleg.chainrule.ops.CROperatorDivideVSDamped.IN_A;
import static org.ddogleg.chainrule.ops.CROperatorDivideVSDamped.IN_B;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCROperatorDivideVSDamped extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand).tests();
	}

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(new CROperatorDivideVSDamped(1, 0.5), new CROperatorDivideVSDamped(2, 1e-3),
				new CROperatorDivideVSDamped(3, 0.0));
	}

	/// Compares found solutions to hand computed solutions
	@Test void compareToHandWritten() {
		var alg = new CROperatorDivideVSDamped(3, 0.5);
		alg.setInput(IN_A, new CRArray<>(new double[]{1, -2, 0.5}, 0));
		alg.setInput(IN_B, new CRArray<>(new double[]{1.5}, 0));

		var found = new CRArray<>(new double[3], 0);
		alg.forward(found);

		assertEquals(1/2.0, found.d(0), UtilEjml.TEST_F64);
		assertEquals(-2/2.0, found.d(1), UtilEjml.TEST_F64);
		assertEquals(0.5/2.0, found.d(2), UtilEjml.TEST_F64);
	}

	/// The whole point of the damping: a scalar of exactly zero still divides cleanly
	@Test void zeroScalarStaysFinite() {
		var alg = new CROperatorDivideVSDamped(2, 0.25);
		alg.setInput(IN_A, new CRArray<>(new double[]{1, -3}, 0));
		alg.setInput(IN_B, new CRArray<>(new double[]{0}, 0));

		var found = new CRArray<>(new double[2], 0);
		alg.forward(found);

		assertEquals(4.0, found.d(0), UtilEjml.TEST_F64);
		assertEquals(-12.0, found.d(1), UtilEjml.TEST_F64);
	}

	/// The second input is one scalar, whatever the vector's size
	@Test void sizes() {
		for (int dof : new int[]{1, 2, 5}) {
			var alg = new CROperatorDivideVSDamped(dof, 0.5);
			assertEquals(2, alg.getInputCount());
			assertEquals(dof, alg.getOutputSize());
			assertEquals(dof, alg.getInputSize(IN_A));
			assertEquals(1, alg.getInputSize(IN_B));
		}
	}

	/// Damping away from zero, so a Jacobian that forgot to add it to the denominator, in either
	/// the diagonal or the scalar's square, would be caught
	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var out = new ArrayList<JacobianCase<double[]>>();
		out.add(new JacobianCase<double[]>(new CROperatorDivideVSDamped(1, 0.5)).in(0.5).in(3.0));
		out.add(new JacobianCase<double[]>(new CROperatorDivideVSDamped(2, 0.25)).in(1e-2, -2e-3).in(0.0));
		out.add(new JacobianCase<double[]>(new CROperatorDivideVSDamped(3, 1.5)).in(1, -2, 3).in(0.75));
		return out;
	}
}
