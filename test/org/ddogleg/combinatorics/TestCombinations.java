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

package org.ddogleg.combinatorics;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestCombinations {
	
	@Test
	public void next_1_1() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		
		Combinations<Integer> alg =new Combinations<Integer>(list,1);
		
		assertEquals(1, (int)alg.get(0));
		assertFalse(alg.next());
	}

	@Test
	public void next_1_2() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);

		Combinations<Integer> alg =new Combinations<Integer>(list,1);

		assertEquals(1, (int)alg.get(0));

		assertTrue(alg.next());
		assertEquals(2, (int) alg.get(0));
		assertFalse(alg.next());
	}

	@Test
	public void next_2_2() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);

		Combinations<Integer> alg = new Combinations<Integer>(list,2);

		assertEquals(1, (int)alg.get(0));
		assertEquals(2, (int)alg.get(1));

		assertFalse(alg.next());
	}

	@Test
	public void next_2_3() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);

		Combinations<Integer> alg = new Combinations<Integer>(list,2);

		assertEquals(1, (int)alg.get(0));
		assertEquals(2, (int)alg.get(1));

		assertTrue(alg.next());
		assertEquals(1, (int) alg.get(0));
		assertEquals(3, (int) alg.get(1));
		assertTrue(alg.next());
		assertEquals(2, (int) alg.get(0));
		assertEquals(3, (int) alg.get(1));
		assertFalse(alg.next());
	}

	@Test
	public void previous() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);

		Combinations<Integer> alg = new Combinations<Integer>(list,2);

		assertTrue(alg.next());
		// sanity check
		assertEquals(1, (int) alg.get(0));
		assertEquals(3, (int) alg.get(1));

		// check previous now
		assertTrue(alg.previous());
		assertEquals(1, (int)alg.get(0));
		assertEquals(2, (int)alg.get(1));

		assertFalse(alg.previous());

		// now force it past the end, see if previous still works
		assertTrue(alg.next());
		assertTrue(alg.next());
		// sanity check
		assertEquals(2, (int) alg.get(0));
		assertEquals(3, (int) alg.get(1));
		assertFalse(alg.next()); // <-- no change here
		assertTrue(alg.previous());
		assertEquals(1, (int)alg.get(0));
		assertEquals(3, (int)alg.get(1));
	}

	@Test
	public void getList_getOutside() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);

		Combinations<Integer> alg = new Combinations<Integer>(list,2);

		assertTrue(alg.next());

		// test getBucket() with and without storage
		List<Integer> a = new ArrayList<Integer>();
		alg.getBucket(a);
		List<Integer> b = alg.getBucket(null);

		assertEquals(2,a.size());
		assertEquals(2,b.size());
		assertEquals(1,(int)a.get(0));
		assertEquals(3,(int)a.get(1));
		assertEquals(1,(int)b.get(0));
		assertEquals(3,(int)b.get(1));

		// test getOutside() with and without storage
		a = new ArrayList<Integer>();
		alg.getOutside(a);
		b = alg.getOutside(null);

		assertEquals(1,a.size());
		assertEquals(1,b.size());
		assertEquals(2,(int)a.get(0));
		assertEquals(2,(int)b.get(0));
	}

	@Test
	public void computeNumShuffles() {
//		assertEquals(1,computeNumShuffles(1,1));
		assertEquals(2,computeNumShuffles(1,2));
		assertEquals(1,computeNumShuffles(2,2));
		assertEquals(3,computeNumShuffles(2,3));
		assertEquals(20,computeNumShuffles(3,6));
	}
	
	private long computeNumShuffles( int numBins , int numItems ) {
		List<Number> l = new ArrayList<Number>();
		for( int i = 0; i < numItems; i++ ) {
			l.add(i);
		}
		Combinations<Number> c = new Combinations<Number>(l,numBins);
		
		return c.computeTotalCombinations();
	}
}
