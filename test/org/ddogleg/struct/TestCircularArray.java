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
public class TestCircularArray {

	@Test
	public void popHead() {
		CircularArray<A> alg = new CircularArray<A>(A.class);

		alg.grow().value = 1;
		alg.grow().value = 2;
		assertEquals(1,alg.popHead().value);
		assertEquals(1,alg.size());

		assertEquals(2,alg.popHead().value);
		assertEquals(0,alg.size());
	}

	@Test
	public void popTail() {
		CircularArray<A> alg = new CircularArray<A>(A.class);

		alg.grow().value = 1;
		alg.grow().value = 2;
		assertEquals(2,alg.popTail().value);
		assertEquals(1,alg.size());

		assertEquals(1,alg.popTail().value);
		assertEquals(0, alg.size());
	}

	@Test
	public void head() {
		CircularArray<A> alg = new CircularArray<A>(A.class);

		alg.grow().value = 1;
		assertEquals(1, alg.head().value);
		alg.grow().value = 3;
		assertEquals(1,alg.head().value);
	}

	@Test
	public void head_offset() {
		CircularArray<A> alg = new CircularArray<A>(A.class,3);

		alg.start = 2;
		alg.size = 0;

		alg.grow().value = 1;
		assertEquals(1, alg.head().value);
		alg.grow().value = 3;
		assertEquals(1,alg.head().value);
	}

	@Test
	public void tail() {
		CircularArray<A> alg = new CircularArray<A>(A.class);

		alg.grow().value = 1;
		assertEquals(1,alg.tail().value);
		alg.grow().value = 3;
		assertEquals(3, alg.tail().value);
	}

	@Test
	public void tail_offset() {
		CircularArray<A> alg = new CircularArray<A>(A.class);

		alg.start = 2;
		alg.size = 0;

		alg.grow().value = 1;
		assertEquals(1,alg.tail().value);
		alg.grow().value = 3;
		assertEquals(3, alg.tail().value);
	}

	@Test
	public void removeHead() {
		CircularArray<A> alg = new CircularArray<A>(A.class);

		alg.grow().value = 1;
		alg.grow().value = 2;
		alg.removeHead();
		assertEquals(2, alg.head().value);
		assertEquals(1, alg.size());

		alg.removeHead();
		assertEquals(0, alg.size());
	}

	@Test
	public void removeTail() {
		CircularArray<A> alg = new CircularArray<A>(A.class);

		alg.grow().value = 1;
		alg.grow().value = 2;
		alg.removeTail();
		assertEquals(1,alg.head().value);
		assertEquals(1,alg.size());

		alg.removeTail();
		assertEquals(0, alg.size());
	}

	@Test
	public void get() {
		CircularArray<A> alg = new CircularArray<A>(A.class,2);
		assertEquals(2,alg.data.length);

		// easy case
		alg.grow().value = 1;
		alg.grow().value = 2;

		assertEquals(1,alg.get(0).value);
		assertEquals(2,alg.get(1).value);

		// make there be an offset
		alg.removeHead();
		alg.grow().value = 3;
		assertEquals(2,alg.data.length); // sanity check
		assertEquals(2,alg.get(0).value);
		assertEquals(3,alg.get(1).value);
	}

	@Test
	public void add() {
		CircularArray<A> alg = new CircularArray<A>(A.class,3);
		assertEquals(3,alg.data.length);

		alg.add( new A(1));
		assertEquals(1,alg.data[0].value);
		assertEquals(1,alg.size);

		alg.add( new A(2));
		assertEquals(1,alg.data[0].value);
		assertEquals(2,alg.data[1].value);
		assertEquals(2,alg.size);

		// see if it grows
		alg.add( new A(3));
		alg.add( new A(4));
		assertEquals(1,alg.data[0].value);
		assertEquals(2,alg.data[1].value);
		assertEquals(3,alg.data[2].value);
		assertEquals(4,alg.data[3].value);
		assertEquals(4,alg.size);

		// grows with offset
		alg.start = 1;
		alg.data = new A[]{new A(1),new A(2), new A(3)};
		alg.size = 3;
		alg.add( new A(4));
		assertEquals(2,alg.data[0].value);
		assertEquals(3,alg.data[1].value);
		assertEquals(1,alg.data[2].value);
		assertEquals(4,alg.data[3].value);
		assertEquals(4,alg.size);

		// wrap around case
		alg.start = 1;
		alg.size = 2;
		alg.data = new A[3];
		alg.add( new A(10));
		assertEquals(10,alg.data[0].value);
		assertEquals(3,alg.size);

	}

	@Test
	public void addW() {
		CircularArray<A> alg = new CircularArray<A>(A.class,3);
		assertEquals(3,alg.data.length);

		alg.addW(new A(1));
		assertEquals(1,alg.data[0].value);
		assertEquals(1,alg.size);

		alg.addW(new A(2));
		assertEquals(1,alg.data[0].value);
		assertEquals(2,alg.data[1].value);
		assertEquals(2,alg.size);

		// see if it over writes
		alg.addW(new A(3));
		alg.addW(new A(4));
		assertEquals(4,alg.data[0].value);
		assertEquals(2,alg.data[1].value);
		assertEquals(3,alg.data[2].value);
		assertEquals(3,alg.size);
		assertEquals(1,alg.start);

		// wrap around case
		alg.start = 1;
		alg.size = 2;
		alg.data = new A[3];
		alg.addW(new A(10));
		assertEquals(10,alg.data[0].value);
		assertEquals(3,alg.size);
	}

	@Test
	public void isEmpty() {
		CircularArray<A> alg = new CircularArray<A>(A.class,3);

		assertTrue(alg.isEmpty());
		alg.add(new A(5));
		assertFalse(alg.isEmpty());
		alg.removeTail();
		assertTrue(alg.isEmpty());

	}

	@Test
	public void reset() {
		CircularArray<A> alg = new CircularArray<A>(A.class,3);

		alg.start = 2;
		alg.size = 5;

		alg.reset();

		assertEquals(0,alg.size);
		assertEquals(0,alg.start);
	}

	@Test
	public void grow() {
		CircularArray<A> alg = new CircularArray<A>(A.class,3);

		alg.grow().value = 1;
		assertEquals(1,alg.size);
		alg.grow().value = 2;
		alg.grow().value = 3;
		alg.grow().value = 4;
		assertEquals(1,alg.data[0].value);
		assertEquals(4,alg.data[3].value);
		assertEquals(4,alg.size);
		assertTrue(alg.data.length >= 4);

		// wrap around case
		alg = new CircularArray<A>(A.class,3);
		alg.size = 2;
		alg.start = 1;
		alg.grow().value = 1;
		assertEquals(1,alg.data[0].value);
		assertTrue(null == alg.data[1]);
		assertTrue(null == alg.data[2]);

	}

	@Test
	public void growW() {
		CircularArray<A> alg = new CircularArray<A>(A.class,3);

		alg.growW().value = 1;
		assertEquals(1,alg.size);
		alg.growW().value = 2;
		alg.growW().value = 3;
		alg.growW().value = 4;
		assertEquals(4,alg.data[0].value);
		assertEquals(3,alg.data[2].value);
		assertEquals(3,alg.size);
		assertTrue(alg.data.length == 3);
	}


	public static class A
	{
		public int value;

		public A() {
		}

		public A(int value) {
			this.value = value;
		}
	}
}
