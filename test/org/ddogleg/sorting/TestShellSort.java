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

package org.ddogleg.sorting;

import org.ejml.UtilEjml;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestShellSort {
	Random rand = new Random(0xfeed);

	@Test
	public void test_F32() {
		float[] ret = BenchMarkSort.createRandom_F32(rand,200);

		ShellSort.sort(ret);

		float prev = ret[0];
		for( int i = 1; i < ret.length; i++ ) {
			if( ret[i] < prev )
				fail("Not ascending");
			prev = ret[i];
		}
	}

	@Test
	public void testSortingRandom_indexes_F32() {
		int offset = 10;
		for( int a = 0; a < 20; a++ ) {
			float[] normal = BenchMarkSort.createRandom_F32(rand,20);
			float[] original = normal.clone();
			float[] withIndexes = new float[offset+normal.length];;
			int[] indexes = new int[ withIndexes.length ];

			System.arraycopy(normal,0,withIndexes,offset,normal.length);


			ShellSort.sort(normal);
			ShellSort.sort(withIndexes,offset,normal.length,indexes);

			for( int i = 0; i < normal.length; i++ ) {
				// make sure the original hasn't been modified
				assertEquals(original[i],withIndexes[i+offset], UtilEjml.TEST_F32);
				// see if it produced the same results as the normal one
				assertEquals(normal[i],withIndexes[indexes[i]],UtilEjml.TEST_F32);
			}
		}
	}

	@Test
	public void test_F64() {
		double[] ret = BenchMarkSort.createRandom_F64(rand,200);

		ShellSort.sort(ret);

		double prev = ret[0];
		for( int i = 1; i < ret.length; i++ ) {
			if( ret[i] < prev )
				fail("Not ascending");
			prev = ret[i];
		}
	}

	@Test
	public void testSortingRandom_indexes_F64() {
		int offset = 10;
		for( int a = 0; a < 20; a++ ) {
			double[] normal = BenchMarkSort.createRandom_F64(rand,20);
			double[] original = normal.clone();
			double[] withIndexes = new double[offset+normal.length];;
			int[] indexes = new int[ withIndexes.length ];

			System.arraycopy(normal,0,withIndexes,offset,normal.length);


			ShellSort.sort(normal);
			ShellSort.sort(withIndexes,offset,normal.length,indexes);

			for( int i = 0; i < normal.length; i++ ) {
				// make sure the original hasn't been modified
				assertEquals(original[i],withIndexes[i+offset], UtilEjml.TEST_F64);
				// see if it produced the same results as the normal one
				assertEquals(normal[i],withIndexes[indexes[i]],UtilEjml.TEST_F64);
			}
		}
	}

	@Test
	public void test_S32() {
		int[] ret = BenchMarkSort.createRandom_S32(rand,200);

		ShellSort.sort(ret);

		double prev = ret[0];
		for( int i = 1; i < ret.length; i++ ) {
			if( ret[i] < prev )
				fail("Not ascending");
			prev = ret[i];
		}
	}

	@Test
	public void testSortingRandom_indexes_S32() {
		int offset = 10;
		for( int a = 0; a < 20; a++ ) {
			int[] normal = BenchMarkSort.createRandom_S32(rand,20);
			int[] original = normal.clone();
			int[] withIndexes = new int[offset+normal.length];;
			int[] indexes = new int[ withIndexes.length ];

			System.arraycopy(normal,0,withIndexes,offset,normal.length);


			ShellSort.sort(normal);
			ShellSort.sort(withIndexes,offset,normal.length,indexes);

			for( int i = 0; i < normal.length; i++ ) {
				// make sure the original hasn't been modified
				assertEquals(original[i],withIndexes[i+offset], UtilEjml.TEST_F64);
				// see if it produced the same results as the normal one
				assertEquals(normal[i],withIndexes[indexes[i]],UtilEjml.TEST_F64);
			}
		}
	}
}
