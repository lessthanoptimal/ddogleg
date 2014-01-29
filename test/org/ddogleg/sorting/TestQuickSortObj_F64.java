/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestQuickSortObj_F64 {

	Random rand = new Random(0xfeed4);

	@Test
	public void testSortingRandom() {
		SortableParameter_F64[] ret = createRandom(rand,200);

		double preTotal = sum(ret);

		QuickSortObj_F64 sorter = new QuickSortObj_F64();

		sorter.sort(ret,ret.length);

		double postTotal = sum(ret);

		// make sure it didn't modify the list, in an unexpected way
		assertEquals(preTotal,postTotal,1e-2);

		SortableParameter_F64 prev = ret[0];
		for( int i = 1; i < ret.length; i++ ) {
			if( ret[i].sortValue < prev.sortValue )
				fail("Not ascending");
			prev = ret[i];
		}
	}

	@Test
	public void testSortingRandom_indexes() {
		for( int a = 0; a < 20; a++ ) {
			SortableParameter_F64[] normal = createRandom(rand,20);
			SortableParameter_F64[] original = copy(normal);
			SortableParameter_F64[] withIndexes = copy(normal);
			int[] indexes = new int[ normal.length ];

			QuickSortObj_F64 sorter = new QuickSortObj_F64();

			sorter.sort(normal,normal.length);
			sorter.sort(withIndexes,normal.length,indexes);

			for( int i = 0; i < normal.length; i++ ) {
				// make sure the original hasn't been modified
				assertEquals(original[i].sortValue,withIndexes[i].sortValue,1e-8);
				// see if it produced the same results as the normal one
				assertEquals(normal[i].sortValue,withIndexes[indexes[i]].sortValue,1e-8);
			}
		}
	}

	public static SortableParameter_F64[] copy( SortableParameter_F64[] list ) {
		SortableParameter_F64[] ret = new SortableParameter_F64[ list.length ];
		for( int i = 0; i < list.length; i++ ) {
			ret[i] = new SortableParameter_F64();
			ret[i].sortValue = list[i].sortValue;
		}
		return ret;
	}

	public static double sum( SortableParameter_F64[] list ) {
		double total = 0;
		for( int i = 0; i < list.length; i++ ) {
			total += list[i].sortValue;
		}
		return total;
	}

	public static SortableParameter_F64[] createRandom( Random rand , final int num ) {
		SortableParameter_F64[] ret = new SortableParameter_F64[ num ];

		for( int i = 0; i < num; i++ ) {
			ret[i] = new SortableParameter_F64();
			ret[i].sortValue = (rand.nextDouble()-0.5)*2000.0;
		}

		return ret;
	}

}
