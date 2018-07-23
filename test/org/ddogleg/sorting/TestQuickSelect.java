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
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestQuickSelect {
	Random rand = new Random(0xFF);

	/**
	 * Creates a random array then sorts it.  It then requests that the specified element be found
	 * and compares it against the sorted array.
	 */
	@Test
	public void testWithQuickSort() {
		Comparable orig[] = new Comparable[100];
		Comparable copy[] = new Comparable[orig.length];
		Comparable sorted[] = new Comparable[orig.length];

		for( int i = 0; i < orig.length; i++ ) {
			orig[i] = rand.nextDouble();
			sorted[i] = orig[i];
		}

		Arrays.sort(sorted);

		for( int i = 0; i < orig.length; i++ ) {
			System.arraycopy(orig,0,copy,0,orig.length);
			QuickSelect.select(copy,i,copy.length);

			assertEquals(sorted[i],copy[i]);
		}

	}

	@Test
	public void testWithQuickSortIndex() {
		Comparable orig[] = new Comparable[100];
		Comparable copy[] = new Comparable[orig.length];
		Comparable sorted[] = new Comparable[orig.length];
		int indexes[] = new int[ orig.length ];

		for( int i = 0; i < orig.length; i++ ) {
			orig[i] = rand.nextDouble();
			sorted[i] = orig[i];
		}

		Arrays.sort(sorted);

		for( int i = 0; i < orig.length; i++ ) {
			System.arraycopy(orig,0,copy,0,orig.length);
			QuickSelect.select(copy,i,copy.length,indexes);

			assertEquals(orig[i],copy[i]);
			assertEquals(sorted[i],copy[indexes[i]]);
		}
	}

	/**
	 * Creates a random array then sorts it.  It then requests that the specified element be found
	 * and compares it against the sorted array.
	 */
	@Test
	public void testWithQuickSort_F64() {
		double orig[] = new double[100];
		double copy[] = new double[orig.length];
		double sorted[] = new double[orig.length];

		for( int i = 0; i < orig.length; i++ ) {
			orig[i] = rand.nextDouble();
			sorted[i] = orig[i];
		}

		Arrays.sort(sorted);

		for( int i = 0; i < orig.length; i++ ) {
			System.arraycopy(orig,0,copy,0,orig.length);
			double val = QuickSelect.select(copy,i,copy.length);

			assertEquals(sorted[i],copy[i],1e-6);

			// make sure everything earlier in the list is less than the selected one
			for( int j = 0; j < i; j++ ) {
				assertTrue(copy[j]<=val);
			}
			// everything after it should be greater
			for( int j = i+1; j < copy.length; j++ ) {
				assertTrue(copy[j]>val);
			}
		}
	}

	@Test
	public void testWithQuickSort_I32() {
		int orig[] = new int[100];
		int copy[] = new int[orig.length];
		int sorted[] = new int[orig.length];

		for( int i = 0; i < orig.length; i++ ) {
			orig[i] = rand.nextInt(5000);
			sorted[i] = orig[i];
		}

		Arrays.sort(sorted);

		for( int i = 0; i < orig.length; i++ ) {
			System.arraycopy(orig,0,copy,0,orig.length);
			double val = QuickSelect.select(copy,i,copy.length);

			assertEquals(sorted[i],copy[i],1e-6);

			// make sure everything earlier in the list is less than the selected one
			for( int j = 0; j < i; j++ ) {
				assertTrue(copy[j]<=val);
			}
			// everything after it should be greater
			for( int j = i+1; j < copy.length; j++ ) {
				assertTrue(copy[j]>val);
			}
		}
	}

	@Test
	public void testWithQuickSort_I64() {
		long orig[] = new long[100];
		long copy[] = new long[orig.length];
		long sorted[] = new long[orig.length];

		for( int i = 0; i < orig.length; i++ ) {
			orig[i] = rand.nextInt(5000);
			sorted[i] = orig[i];
		}

		Arrays.sort(sorted);

		for( int i = 0; i < orig.length; i++ ) {
			System.arraycopy(orig,0,copy,0,orig.length);
			double val = QuickSelect.select(copy,i,copy.length);

			assertEquals(sorted[i],copy[i],1e-6);

			// make sure everything earlier in the list is less than the selected one
			for( int j = 0; j < i; j++ ) {
				assertTrue(copy[j]<=val);
			}
			// everything after it should be greater
			for( int j = i+1; j < copy.length; j++ ) {
				assertTrue(copy[j]>val);
			}
		}
	}

	/**
	 * Creates a random array then sorts it.  It then requests that the specified element be found
	 * and compares it against the sorted array.
	 */
	@Test
	public void testWithQuickSortIndex_F64() {
		double orig[] = new double[100];
		double copy[] = new double[orig.length];
		double sorted[] = new double[orig.length];
		int indexes[] = new int[orig.length];

		for( int i = 0; i < orig.length; i++ ) {
			orig[i] = rand.nextDouble();
			sorted[i] = orig[i];
		}

		Arrays.sort(sorted);

		for( int i = 0; i < orig.length; i++ ) {
			System.arraycopy(orig,0,copy,0,orig.length);
			int index = QuickSelect.selectIndex(copy,i,copy.length,indexes);

			assertEquals(sorted[i],orig[index],1e-6);

			double val = orig[index];

			// make sure the input hasn't been modified
			for (int j = 0; j < copy.length; j++) {
				assertEquals(orig[j],copy[j],1e-8);
			}

			// make sure everything earlier in the list is less than the selected one
			for( int j = 0; j < i; j++ ) {
				assertTrue(orig[indexes[j]]<=val);
			}

			// everything after it should be greater
			for( int j = i+1; j < copy.length; j++ ) {
				assertTrue(orig[indexes[j]]>val);
			}
		}
	}

	@Test
	public void testWithQuickSortIndex_I32() {
		int orig[] = new int[100];
		int copy[] = new int[orig.length];
		int sorted[] = new int[orig.length];
		int indexes[] = new int[orig.length];

		for( int i = 0; i < orig.length; i++ ) {
			orig[i] = rand.nextInt(6000);
			sorted[i] = orig[i];
		}

		Arrays.sort(sorted);

		for( int i = 0; i < orig.length; i++ ) {
			System.arraycopy(orig,0,copy,0,orig.length);
			int index = QuickSelect.selectIndex(copy,i,copy.length,indexes);

			assertEquals(sorted[i],orig[index],1e-6);

			double val = orig[index];

			// make sure the input hasn't been modified
			for (int j = 0; j < copy.length; j++) {
				assertEquals(orig[j],copy[j]);
			}

			// make sure everything earlier in the list is less than the selected one
			for( int j = 0; j < i; j++ ) {
				assertTrue(orig[indexes[j]]<=val);
			}

			// everything after it should be greater
			for( int j = i+1; j < copy.length; j++ ) {
				assertTrue(orig[indexes[j]]>val);
			}
		}
	}

	@Test
	public void testWithQuickSortIndex_I64() {
		long orig[] = new long[100];
		long copy[] = new long[orig.length];
		long sorted[] = new long[orig.length];
		int indexes[] = new int[orig.length];

		for( int i = 0; i < orig.length; i++ ) {
			orig[i] = rand.nextInt(6000);
			sorted[i] = orig[i];
		}

		Arrays.sort(sorted);

		for( int i = 0; i < orig.length; i++ ) {
			System.arraycopy(orig,0,copy,0,orig.length);
			int index = QuickSelect.selectIndex(copy,i,copy.length,indexes);

			assertEquals(sorted[i],orig[index],1e-6);

			double val = orig[index];

			// make sure the input hasn't been modified
			for (int j = 0; j < copy.length; j++) {
				assertEquals(orig[j], copy[j]);
			}

			// make sure everything earlier in the list is less than the selected one
			for( int j = 0; j < i; j++ ) {
				assertTrue(orig[indexes[j]]<=val);
			}

			// everything after it should be greater
			for( int j = i+1; j < copy.length; j++ ) {
				assertTrue(orig[indexes[j]]>val);
			}
		}
	}
}