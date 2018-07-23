/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestQuickSort_S32 {
	Random rand = new Random(0xfeed4);

	@Test
	public void testSortingRandom() {
		int[] ret = BenchMarkSort.createRandom_S32(rand,200);

		int preTotal = sum(ret);

		QuickSort_S32 sorter = new QuickSort_S32();

		sorter.sort(ret,ret.length);

		int postTotal = sum(ret);

		// make sure it didn't modify the list, in an unexpected way
		assertEquals(preTotal,postTotal,1e-8);

		double prev = ret[0];
		for( int i = 1; i < ret.length; i++ ) {
			if( ret[i] < prev )
				fail("Not ascending");
			prev = ret[i];
		}
	}

	private int sum( int a[] ) {
		int total = 0;
		for( int i = 0; i < a.length; i++ ) {
			total += a[i];
		}
		return total;
	}

	@Test
	public void testSortingRandom_indexes() {
		int offset = 10;
		for( int a = 0; a < 20; a++ ) {
			int[] normal = BenchMarkSort.createRandom_S32(rand,20);
			int[] original = normal.clone();
			int[] withIndexes = new int[offset+normal.length];;
			int[] indexes = new int[ withIndexes.length ];

			System.arraycopy(normal,0,withIndexes,offset,normal.length);

			QuickSort_S32 sorter = new QuickSort_S32();

			sorter.sort(normal,normal.length);
			sorter.sort(withIndexes,offset,normal.length,indexes);

			for( int i = 0; i < normal.length; i++ ) {
				// make sure the original hasn't been modified
				assertEquals(original[i],withIndexes[i+offset], UtilEjml.TEST_F64);
				// see if it produced the same results as the normal one
				assertEquals(normal[i],withIndexes[indexes[i]],UtilEjml.TEST_F64);
			}
		}
	}
}