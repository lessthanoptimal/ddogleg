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
import org.ddogleg.chainrule.CROperatorForward;
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac.JacobianCase;
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac;
import org.ddogleg.chainrule.CommonCROperatorForward;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class TestCRErrorVectorSW extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand).tests();
	}

	double[] arrayA = new double[]{1.0, 2.5, -0.3, 0.4};
	double[] arrayB = new double[]{2.0, 1.5, 1.3, 2.4};

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(new CRErrorVectorSW(4), new CRErrorVectorSW(4, 2.0),
				new CRErrorVectorSW(5, 3.0));
	}

	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var variants = new ArrayList<JacobianCase<double[]>>();

		variants.add(new JacobianCase<>(new CRErrorVectorSW(arrayA.length, 2.5)).in(1, -2, 3, 4).in(arrayA));
		variants.add(new JacobianCase<>(new CRErrorVectorSW(arrayA.length, 1.5)).in(arrayB).in(4, 0.5, -0.1, -0.2));
		return variants;
	}
}
