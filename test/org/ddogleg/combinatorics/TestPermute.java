/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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
public class TestPermute {

	@Test
	public void testSetSize1() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);

		Permute<Integer> alg = new Permute<Integer>(l);

		assertEquals(1, alg.getTotalPermutations());

		assertFalse(alg.next());
	}

	@Test
	public void testSetSize2() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);
		l.add(2);

		Permute<Integer> alg = new Permute<Integer>(l);

		assertEquals(2, alg.getTotalPermutations());

		checkList(alg, 2);
	}

	@Test
	public void testSetSize3() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);
		l.add(2);
		l.add(3);

		Permute<Integer> alg = new Permute<Integer>(l);

		assertEquals(6, alg.getTotalPermutations());

		checkList(alg, 6);
	}


	@Test
	public void testSetSize4() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);
		l.add(2);
		l.add(3);
		l.add(4);

		Permute<Integer> alg = new Permute<Integer>(l);

		assertEquals(24, alg.getTotalPermutations());

		checkList(alg, 24);
	}

	@Test
	public void testSetSize5_to_7() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);
		l.add(2);
		l.add(3);
		l.add(4);
		l.add(5);

		int total = 24*5;
		for (int i = 0; i <= 2; i++) {

			Permute<Integer> alg = new Permute<Integer>(l);

			assertEquals(total, alg.getTotalPermutations());

			checkList(alg, total);

			l.add(6+i);
			total *= l.size();
		}
	}

	@Test
	public void previous() {
		List<Integer> l = new ArrayList<Integer>();
		for (int size = 0; size < 9; size++) {
			l.add(size);

			Permute<Integer> alg = new Permute<Integer>(l);

			List<List<Integer>> forward = new ArrayList<List<Integer>>();
			do {
				forward.add( alg.getPermutation(null));
			} while( alg.next() );

			int i = forward.size()-1;
			do {
				List<Integer> found = alg.getPermutation(null);
				List<Integer> expected = forward.get(i--);

				for( int j = 0; j < size; j++ ) {
					assertTrue(found.get(j) == expected.get(j));
				}
			} while( alg.previous() );
		}
	}

	private void checkList( Permute p , int expected ) {
		List<List> all = new ArrayList<List>();

		do {
			List l = p.getPermutation(null);
			// see if each object in the permutation is unique
			for (int i = 0; i < l.size(); i++) {
				Object o = l.get(i);
				for (int j = i+1; j < l.size(); j++) {
					if( o == l.get(j))
						fail("duplicate");
				}
			}
			all.add(l);
		} while( p.next() );

		assertEquals(expected,all.size());

		// now see if each permutation is unique in the set
		for( int i = 0; i < expected; i++ ) {
			List a = all.get(i);
			assertEquals(a.size(),p.size());
			for( int j = i+1; j < expected; j++) {
				List b = all.get(j);
				boolean match = true;
				for( int k = 0; k < a.size(); k++ ) {
					if( a.get(k) != b.get(k) ) {
						match = false;
						break;
					}
				}

				assertFalse(match);
			}
		}
	}
}
