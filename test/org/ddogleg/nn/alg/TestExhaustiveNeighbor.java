/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.nn.alg;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestExhaustiveNeighbor {

	@Test
	public void zero() {
		List<double[]> list = new ArrayList<double[]>();

		ExhaustiveNeighbor alg = new ExhaustiveNeighbor(2);
		alg.setPoints(list);

		assertTrue(alg.findClosest(new double[]{1,2},10) == -1);
	}

	@Test
	public void one() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2);

		ExhaustiveNeighbor alg = new ExhaustiveNeighbor(2);
		alg.setPoints(list);

		assertTrue(alg.findClosest(new double[]{1,2.1},10) == 0);
		assertFalse(alg.findClosest(new double[]{1, 200}, 10) == 0);
	}

	@Test
	public void two() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4);

		ExhaustiveNeighbor alg = new ExhaustiveNeighbor(2);
		alg.setPoints(list);

		assertTrue(alg.findClosest(new double[]{1, 2.1}, 10) == 0);
	}

	@Test
	public void three() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4,  6,7);

		ExhaustiveNeighbor alg = new ExhaustiveNeighbor(2);
		alg.setPoints(list);

		assertTrue(alg.findClosest(new double[]{3.1, 3.9}, 10) == 1);
	}

}
