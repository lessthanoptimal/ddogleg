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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestCountingSort {
	@Test
	public void sort_inplace() {
		int a[] = new int[]{66,4,2,9,5,4,5,72,20,4,56,94,53,2,55,5,5,5,5,5,7,89,4,3,2};
		int copy[] = a.clone();

		Arrays.sort(copy);

		CountingSort alg = new CountingSort(0,100);
		alg.sort(a,0,a.length);

		for( int i = 0; i < a.length; i++ ) {
			assertEquals(copy[i], a[i]);
		}
	}

	@Test
	public void sort_in_out() {
		int a[] = new int[]{66,4,2,9,5,4,5,72,20,4,56,94,53,2,55,5,5,5,5,5,7,89,4,3,2};
		int b[] = new int[a.length];

		int copy[] = a.clone();

		Arrays.sort(copy);

		CountingSort alg = new CountingSort(0,100);
		alg.sort(a,0,b,0,a.length);

		for( int i = 0; i < a.length; i++ ) {
			assertEquals(copy[i], b[i]);
		}
	}

	@Test
	public void sortIndex() {
		int a[] = new int[]{123,66,4,2,9,5,4,5,72,20,4,56,94,53,2,55,5,5,5,5,5,7,89,4,3,2};
		int indexes[] = new int[a.length-1];

		int copy[] = a.clone();

		Arrays.sort(copy,1,a.length);

		CountingSort alg = new CountingSort(0,100);
		alg.sortIndex(a, 1, indexes.length, indexes);

		for( int i = 0; i < indexes.length; i++ ) {
			assertEquals(copy[i+1], a[indexes[i]]);
		}
	}
}
