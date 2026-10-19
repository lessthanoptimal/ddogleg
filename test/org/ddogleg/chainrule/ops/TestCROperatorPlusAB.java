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

import static org.ddogleg.chainrule.ops.CROperatorPlusAB.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCROperatorPlusAB extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand).tests();
	}

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(new CROperatorPlusAB(1), new CROperatorPlusAB(2), new CROperatorPlusAB(3));
	}

	/// Compares found solutions to hand computed solutions
	@Test void compareToHandWritten() {
		var alg = new CROperatorPlusAB(3);
		alg.setInput(IN_V0, new CRArray<>(new double[]{1, -2, 0.5}, 0));
		alg.setInput(IN_V1, new CRArray<>(new double[]{4, 0.25, -1}, 0));
		alg.setInput(IN_ALPHA, new CRArray<>(new double[]{2}, 0));
		alg.setInput(IN_BETA, new CRArray<>(new double[]{-0.5}, 0));

		var found = new CRArray<>(new double[3], 0);
		alg.forward(found);

		// 2*V0 - 0.5*V1, element by element
		assertEquals(2*1 - 0.5*4, found.d(0), UtilEjml.TEST_F64);
		assertEquals(2*-2 - 0.5*0.25, found.d(1), UtilEjml.TEST_F64);
		assertEquals(2*0.5 - 0.5*-1, found.d(2), UtilEjml.TEST_F64);
	}

	/// A coefficient of zero drops its side of the sum, which is what makes this usable as a plain
	/// scale as well as a sum
	@Test void oneSidedSum() {
		var alg = new CROperatorPlusAB(2);
		alg.setInput(IN_V0, new CRArray<>(new double[]{3, -1}, 0));
		alg.setInput(IN_V1, new CRArray<>(new double[]{100, 100}, 0));
		alg.setInput(IN_ALPHA, new CRArray<>(new double[]{1.5}, 0));
		alg.setInput(IN_BETA, new CRArray<>(new double[]{0}, 0));

		var found = new CRArray<>(new double[2], 0);
		alg.forward(found);

		assertEquals(4.5, found.d(0), UtilEjml.TEST_F64);
		assertEquals(-1.5, found.d(1), UtilEjml.TEST_F64);
	}

	@Test void sizes() {
		for (int dof : new int[]{1, 2, 5}) {
			var alg = new CROperatorPlusAB(dof);
			assertEquals(4, alg.getInputCount());
			assertEquals(dof, alg.getOutputSize());
			assertEquals(dof, alg.getInputSize(IN_V0));
			assertEquals(dof, alg.getInputSize(IN_V1));
			assertEquals(1, alg.getInputSize(IN_ALPHA));
			assertEquals(1, alg.getInputSize(IN_BETA));
		}
	}

	/// Coefficients away from one, so a Jacobian that ignored them would be caught
	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var out = new ArrayList<JacobianCase<double[]>>();
		out.add(new JacobianCase<double[]>(new CROperatorPlusAB(1)).in(0.5).in(-3.0).in(2.0).in(-0.5));
		out.add(new JacobianCase<double[]>(new CROperatorPlusAB(2))
				.in(1e-2, -2e-3).in(0.5, -0.25).in(-1.5).in(0.75));
		out.add(new JacobianCase<double[]>(new CROperatorPlusAB(3))
				.in(1, -2, 3).in(0.25, 0.5, -1).in(0.3).in(1.2));
		return out;
	}
}
