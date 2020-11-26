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

package org.ddogleg.struct;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestGrowArray_B {

	@Test
	void auto_grow() {
		DogArray_B alg = new DogArray_B(3);

		assertEquals(3,alg.data.length);

		for( int i = 0; i < 10; i++ )
			alg.push((i%2)==0);

		assertEquals(10,alg.size);

		for( int i = 0; i < 10; i++ )
			assertEquals((i%2)==0,alg.get(i));
	}

	@Test
	void count() {
		DogArray_B alg = DogArray_B.array(0,0,1,1,1);

		assertEquals(2,alg.count(false));
		assertEquals(3,alg.count(true));
	}

	@Test
	void reset() {
		DogArray_B alg = new DogArray_B(10);

		alg.push(true);
		alg.push(false);
		alg.push(false);

		assertTrue(alg.get(0));
		assertEquals(3,alg.size);

		alg.reset();

		assertEquals(0, alg.size);
	}

	@Test
	void resize() {
		DogArray_B alg = new DogArray_B(2);
		assertEquals(0,alg.size);
		alg.resize(12);
		assertTrue(alg.data.length >= 12);
		assertEquals(12,alg.size);
		// Make sure it doesn't declare a new array since it doesn't have to
		alg.data[2] = true;
		alg.resize(10);
		assertTrue(alg.data.length >= 10);
		assertEquals(10,alg.size);
		assertTrue(alg.get(2));
	}

	@Test
	void resize_default() {
		DogArray_B alg = new DogArray_B(2);
		assertEquals(0,alg.size);
		alg.resize(12, true);
		assertTrue(alg.data.length >= 12);
		assertEquals(12,alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertTrue(alg.get(i));
		}
		// The array isn't redeclared but the value should still change
		alg.resize(10,false);
		assertTrue(alg.data.length >= 10);
		assertEquals(10,alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertFalse(alg.get(i));
		}
		// it shouldn't change the entire array's value since that's wasteful
		for (int i = alg.size; i < alg.data.length; i++) {
			assertTrue(alg.data[i]);
		}
	}

	@Test
	void push_pop() {
		DogArray_B alg = new DogArray_B(10);

		alg.push(false);
		alg.push(true);

		assertEquals(2,alg.size);
		assertTrue(alg.pop());
		assertFalse(alg.pop());
		assertEquals(0, alg.size);
	}

	@Test
	void setTo_array_off() {
		DogArray_B alg = new DogArray_B(10);

		boolean[] foo = new boolean[]{true,true,false,true,false};
		alg.setTo(foo,1,3);
		assertEquals(3,alg.size);
		for (int i = 0; i < 3; i++) {
			assertEquals(alg.get(i),foo[i+1]);
		}
	}

	@Test
	void setTo_array() {
		DogArray_B alg = new DogArray_B(10);

		boolean[] array = new boolean[]{true,true,false,true,false};

		assertSame(alg,alg.setTo(array));
		assertEquals(array.length,alg.size);

		for (int i = 0; i < array.length; i++) {
			assertEquals(alg.get(i),array[i]);
		}
	}

	@Test
	void remove_two() {
		DogArray_B alg = new DogArray_B(10);

		alg.push(true);
		alg.push(true);
		alg.push(false);
		alg.push(true);
		alg.push(false);

		alg.remove(1,1);
		assertEquals(4,alg.size);
		assertTrue(alg.get(0));
		assertFalse(alg.get(1));
		assertTrue(alg.get(2));
		assertFalse(alg.get(3));
		alg.remove(0,1);
		assertEquals(2,alg.size);
		assertTrue(alg.get(0));
		assertFalse(alg.get(1));
	}

	@Test
	void remove_swap() {
		DogArray_B alg = DogArray_B.array(0,0,0,0,1);
		alg.removeSwap(1);
		assertEquals(4,alg.size);
		alg.forIdx((i,v)-> assertEquals((i == 1), v));
	}

	@Test
	void indexOf() {
		DogArray_B alg = new DogArray_B(10);

		alg.push(true);
		alg.push(false);
		alg.push(false);
		alg.push(true);

		assertEquals(0,alg.indexOf(true));
		assertEquals(1,alg.indexOf(false));
	}

	@Test
	void getTail() {
		DogArray_B alg = new DogArray_B(20);

		for (int i = 0; i < 20; i++) {
			alg.add(i%2==0);
		}

		for (int i = 0; i < 20; i++) {
			assertEquals((20-i-1)%2==0,alg.getTail(i));
		}
	}

	@Test
	void forIdx() {
		DogArray_B alg = DogArray_B.array(true,false,true,false,true);
		alg.forIdx((idx,value)-> assertEquals(idx%2==0,value));
	}
}
