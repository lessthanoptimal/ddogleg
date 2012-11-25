/*
 * Copyright (c) 2012, Peter Abeles. All Rights Reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestCombinations {
	
	@Test
	public void shuffle_1_1() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		
		Combinations<Integer> alg =new Combinations<Integer>(list,1);
		
		assertEquals(1, (int)alg.get(0));
		try {
			alg.shuffle();
			fail("Should have thrown an exception");
		} catch (Combinations.ExhaustedException e) {}
	}

	@Test
	public void shuffle_1_2() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);

		Combinations<Integer> alg =new Combinations<Integer>(list,1);

		assertEquals(1, (int)alg.get(0));

		try {
			alg.shuffle();
			assertEquals(2, (int) alg.get(0));
			alg.shuffle();
			fail("Should have thrown an exception");
		} catch (Combinations.ExhaustedException e) {}
	}

	@Test
	public void shuffle_2_2() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);

		Combinations<Integer> alg = new Combinations<Integer>(list,2);

		assertEquals(1, (int)alg.get(0));
		assertEquals(2, (int)alg.get(1));

		try {
			alg.shuffle();
			fail("Should have thrown an exception");
		} catch (Combinations.ExhaustedException e) {}
	}

	@Test
	public void shuffle_2_3() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);

		Combinations<Integer> alg = new Combinations<Integer>(list,2);

		assertEquals(1, (int)alg.get(0));
		assertEquals(2, (int)alg.get(1));

		try {
			alg.shuffle();
			assertEquals(1, (int) alg.get(0));
			assertEquals(3, (int) alg.get(1));
			alg.shuffle();
			assertEquals(2, (int) alg.get(0));
			assertEquals(3, (int) alg.get(1));
			alg.shuffle();

			fail("Should have thrown an exception");
		} catch (Combinations.ExhaustedException e) {}
	}
	
	@Test
	public void unshuffle() {
		fail("implement");
	}

	@Test
	public void numShuffles() {
		assertEquals(1,computeNumShuffles(1,1));
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
		
		return c.numShuffles();
	}
}
