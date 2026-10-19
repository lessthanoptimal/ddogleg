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

import static org.ddogleg.chainrule.CRArray.arrayD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCRErrorHuberNorm extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand).tests();
	}

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(new CRErrorHuberNorm(2, 0.2), (new CRErrorHuberNorm(3, 0.5)));
	}

	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var variants = new ArrayList<JacobianCase<double[]>>();

		// Threshold is above
		variants.add(new JacobianCase<>(new CRErrorHuberNorm(3,20)).
				in(arrayD(-0.2, -1.1, 2.4).a));

		// Threshold is below
		variants.add(new JacobianCase<>(new CRErrorHuberNorm(3,0.5)).
				in(arrayD(-0.2, -1.1, 2.4).a));

		return variants;
	}

	@Test void errorProperties() {
		var alg = new CRErrorHuberNorm(2, 3.0);

		var output = new CRArray<>(new double[alg.getOutputSize()], 0);

		// No change. Below the threshold
		double[] vector = {0.5, -0.8};
		alg.setInput(0, new CRArray<>(vector.clone(), 0));
		alg.forward(output);
		assertEquals(vector[0], output.d(0), 5e-7);
		assertEquals(vector[1], output.d(1), 5e-7);

		// At the threshold, no change
		scaleTo(vector, 3.0);
		alg.setInput(0, new CRArray<>(vector.clone(), 0));
		alg.forward(output);
		assertEquals(vector[0], output.d(0), 5e-7);
		assertEquals(vector[1], output.d(1), 5e-7);

		// Above the threshold. It should have a smaller value
		scaleTo(vector, 10.0);
		alg.setInput(0, new CRArray<>(vector.clone(), 0));
		alg.forward(output);
		assertTrue(Math.abs(vector[0]) > output.d(0));
		assertTrue(Math.abs(vector[1]) > output.d(1));

		double norm = Math.sqrt(output.d(0)*output.d(0) + output.d(1)*output.d(1));
		assertTrue(Math.hypot(vector[0], vector[1]) > norm);
	}

	/** Scales the vector so its length is 'length' */
	private static void scaleTo( double[] vector, double length ) {
		double scale = length/Math.hypot(vector[0], vector[1]);
		vector[0] *= scale;
		vector[1] *= scale;
	}
}
