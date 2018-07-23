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

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestQuickSortComparator_F64 {
	Random rand = new Random(0xfeed4);

	Comparator<Double> comparator = new Comparator<Double>() {
		@Override
		public int compare(Double o1, Double o2) {
			if( o1 > o2 )
				return 1;
			else if( o1 < o2 )
				return -1;
			else
				return 0;
		}
	};

	@Test
	public void testSortingRandom() {
		Double[] ret = createRandom(rand, 200);

		double preTotal = sum(ret);

		QuickSortComparator<Double> sorter = new QuickSortComparator<Double>(comparator);

		sorter.sort(ret, ret.length);

		double postTotal = sum(ret);

		// make sure it didn't modify the list, in an unexpected way
		assertEquals(preTotal,postTotal,1e-8);

		double prev = ret[0];
		for( int i = 1; i < ret.length; i++ ) {
			if( ret[i] < prev )
				fail("Not ascending");
			prev = ret[i];
		}
	}

	@Test
	public void testSortingRandom_indexes() {
		for( int a = 0; a < 20; a++ ) {
			Double[] normal = createRandom(rand,20);
			Double[] original = normal.clone();
			Double[] withIndexes = normal.clone();
			int[] indexes = new int[ normal.length ];

			QuickSortComparator<Double> sorter = new QuickSortComparator<Double>(comparator);

			sorter.sort(normal,normal.length);
			sorter.sort(withIndexes,normal.length,indexes);

			for( int i = 0; i < normal.length; i++ ) {
				// make sure the original hasn't been modified
				assertEquals(original[i],withIndexes[i],1e-8);
				// see if it produced the same results as the normal one
				assertEquals(normal[i],withIndexes[indexes[i]],1e-8);
			}
		}
	}

	public static double sum( Double[] list ) {
		double total = 0;
		for (int i = 0; i < list.length; i++) {
			total += list[i];
		}
		return total;
	}

	public static Double[] createRandom( Random rand , final int num ) {
		Double[] ret = new Double[ num ];

		for( int i = 0; i < num; i++ ) {
			ret[i] = (rand.nextDouble()-0.5)*2000.0;
		}

		return ret;
	}
}