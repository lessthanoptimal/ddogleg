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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCROperatorClampLower extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand).tests();
	}

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(new CROperatorClampLower(1, 0.001),
				new CROperatorClampLower(2, -1.0),
				new CROperatorClampLower(3, 0.5));
	}

	@Test void passesThroughAboveTheFloor() {
		var alg = new CROperatorClampLower(3, 0.5);
		alg.setInput(0, new CRArray<>(new double[]{2, 0.75, 10}, 0));

		var found = new CRArray<>(new double[3], 0);
		alg.forward(found);

		assertEquals(2.0, found.d(0), UtilEjml.TEST_F64);
		assertEquals(0.75, found.d(1), UtilEjml.TEST_F64);
		assertEquals(10.0, found.d(2), UtilEjml.TEST_F64);
	}

	@Test void holdsAtTheFloorBelowIt() {
		var alg = new CROperatorClampLower(3, 0.5);
		alg.setInput(0, new CRArray<>(new double[]{0.4, -2, 0.5}, 0));

		var found = new CRArray<>(new double[3], 0);
		alg.forward(found);

		assertEquals(0.5, found.d(0), UtilEjml.TEST_F64);
		assertEquals(0.5, found.d(1), UtilEjml.TEST_F64);
		assertEquals(0.5, found.d(2), UtilEjml.TEST_F64, "at the floor exactly");
	}

	/// Values on both sides of the floor, so the numeric check sees the derivative switch off
	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var out = new ArrayList<JacobianCase<double[]>>();
		out.add(new JacobianCase<double[]>(new CROperatorClampLower(1, 0.001)).in(1.5));
		out.add(new JacobianCase<double[]>(new CROperatorClampLower(2, 0.5)).in(2.0, 3.0));
		return out;
	}
}
