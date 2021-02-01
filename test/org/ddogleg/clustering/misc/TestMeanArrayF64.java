/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.clustering.misc;

import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.DogArray_I32;
import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
class TestMeanArrayF64 {
	/**
	 * Create a situation that's easy to hand compute the solution and see if it's right
	 */
	@Test void easy() {
		int dof = 2;
		List<double[]> list = new ArrayList<>();
		var assignments = new DogArray_I32();
		for (int i = 0; i < 9; i++) {
			list.add(new double[]{i+1,i});

			// only the first element will be assigned to 0, all the others are 1
			assignments.add(i==0?0:1);
		}
		var accessor = new ListAccessor<>(list, (src, dst) -> System.arraycopy(src, 0, dst, 0, dof));

		var means = new DogArray<>(()->new double[dof]);
		means.resize(2);

		var alg = new MeanArrayF64(dof);
		alg.process(accessor, assignments, means);

		// make sure it didn't mess with the size
		assertEquals(2, means.size);

		assertArrayEquals(list.get(0), means.get(0), UtilEjml.TEST_F64);

		assertEquals(5.5, means.get(1)[0], UtilEjml.TEST_F64);
		assertEquals(4.5, means.get(1)[1], UtilEjml.TEST_F64);
	}
}