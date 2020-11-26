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

package org.ddogleg.nn.alg;

import org.ddogleg.nn.alg.distance.KdTreeEuclideanSq_F64;
import org.ddogleg.struct.DogArray_F64;
import org.ddogleg.struct.DogArray_I32;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestExhaustiveNeighbor {

	KdTreeEuclideanSq_F64 distance = new KdTreeEuclideanSq_F64(2);

	@Test
	public void findClosest_zero() {
		List<double[]> list = new ArrayList<double[]>();

		ExhaustiveNeighbor<double[]> alg = new ExhaustiveNeighbor<>(distance);
		alg.setPoints(list);

		assertEquals(alg.findClosest(new double[]{1, 2}, 10), -1);
	}

	@Test
	public void findClosest_one() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2);

		ExhaustiveNeighbor<double[]> alg = new ExhaustiveNeighbor<>(distance);
		alg.setPoints(list);

		assertEquals(0, alg.findClosest(new double[]{1, 2.1}, 10));
		assertNotEquals(0, alg.findClosest(new double[]{1, 200}, 10));
	}

	@Test
	public void findClosest_two() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4);

		ExhaustiveNeighbor<double[]> alg = new ExhaustiveNeighbor<>(distance);
		alg.setPoints(list);

		assertEquals(0, alg.findClosest(new double[]{1, 2.1}, 10));
	}

	@Test
	public void findClosest_three() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4,  6,7);

		ExhaustiveNeighbor<double[]> alg = new ExhaustiveNeighbor<>(distance);
		alg.setPoints(list);

		assertEquals(1, alg.findClosest(new double[]{3.1, 3.9}, 10));
	}

	@Test
	public void findClosestN_zero() {
		List<double[]> list = new ArrayList<double[]>();

		ExhaustiveNeighbor<double[]>  alg = new ExhaustiveNeighbor<>(distance);
		alg.setPoints(list);

		DogArray_I32 outputIndex = new DogArray_I32();
		DogArray_F64 outputDistance = new DogArray_F64();

		alg.findClosestN(new double[]{1, 2}, 10, 5, outputIndex, outputDistance);

		assertEquals(0,outputIndex.size);
		assertEquals(0,outputDistance.size);
	}

	/**
	 * Request more inliers than there are
	 */
	@Test
	public void findClosestN_toomany() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4);

		ExhaustiveNeighbor<double[]> alg = new ExhaustiveNeighbor<>(distance);
		alg.setPoints(list);

		DogArray_I32 outputIndex = new DogArray_I32();
		DogArray_F64 outputDistance = new DogArray_F64();

		alg.findClosestN(new double[]{1, 2}, 10, 5, outputIndex, outputDistance);

		assertEquals(2,outputIndex.size);
		assertEquals(2,outputDistance.size);

		assertEquals(0,outputIndex.get(0));
		assertEquals(1,outputIndex.get(1));
	}

	/**
	 * Request more inliers than there are within the allowed distance
	 */
	@Test
	public void findClosestN_toomany_distance() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4);

		ExhaustiveNeighbor<double[]> alg = new ExhaustiveNeighbor<>(distance);
		alg.setPoints(list);

		DogArray_I32 outputIndex = new DogArray_I32();
		DogArray_F64 outputDistance = new DogArray_F64();

		alg.findClosestN(new double[]{1, 2}, 0.1, 5, outputIndex, outputDistance);

		assertEquals(1,outputIndex.size);
		assertEquals(1,outputDistance.size);

		assertEquals(0,outputIndex.get(0));
		assertEquals(0,outputDistance.get(0),1e-8);
	}

	@Test
	public void findClosestN_standard() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4 , 4,5, 6,7 , 8,9 );

		ExhaustiveNeighbor<double[]> alg = new ExhaustiveNeighbor<>(distance);
		alg.setPoints(list);

		DogArray_I32 outputIndex = new DogArray_I32();
		DogArray_F64 outputDistance = new DogArray_F64();

		alg.findClosestN(new double[]{4.1, 4.9}, 10, 3, outputIndex, outputDistance);

		assertEquals(3,outputIndex.size);
		assertEquals(3,outputDistance.size);

		checkContains(1,outputIndex);
		checkContains(2,outputIndex);
		checkContains(3,outputIndex);
	}

	/**
	 * Make sure it works after multiple calls
	 */
	@Test
	public void findClosestN_multiple_calls() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4 , 4,5, 6,7 , 8,9 );

		ExhaustiveNeighbor<double[]> alg = new ExhaustiveNeighbor<>(distance);
		alg.setPoints(list);

		DogArray_I32 outputIndex = new DogArray_I32();
		DogArray_F64 outputDistance = new DogArray_F64();

		alg.findClosestN(new double[]{4.1, 4.9}, 10, 3, outputIndex, outputDistance);

		outputIndex.reset();
		outputDistance.reset();

		alg.findClosestN(new double[]{4.1, 4.9}, 10, 3, outputIndex, outputDistance);

		assertEquals(3,outputIndex.size);
		assertEquals(3,outputDistance.size);

		checkContains(1,outputIndex);
		checkContains(2,outputIndex);
		checkContains(3,outputIndex);
	}

	private void checkContains( int  value , DogArray_I32 list ) {
		for( int i = 0; i < list.size; i++ ) {
			if( list.data[i] == value ) {
				return;
			}
		}
		fail("couldn't find");
	}
}
