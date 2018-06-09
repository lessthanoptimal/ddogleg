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

package org.ddogleg.struct;

import org.ejml.UtilEjml;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestCircularQueue_F64 {

	@Test
	public void popHead() {
		CircularQueue_F64 alg = new CircularQueue_F64();

		alg.add(1);
		alg.add(2);
		assertEquals(1,alg.popHead(), UtilEjml.TEST_F64);
		assertEquals(1,alg.size());

		assertEquals(2,alg.popHead(), UtilEjml.TEST_F64);
		assertEquals(0,alg.size());
	}

	@Test
	public void popTail() {
		CircularQueue_F64 alg = new CircularQueue_F64();

		alg.add(1);
		alg.add(2);
		assertEquals(2,alg.popTail(), UtilEjml.TEST_F64);
		assertEquals(1,alg.size());

		assertEquals(1,alg.popTail(), UtilEjml.TEST_F64);
		assertEquals(0, alg.size());
	}

	@Test
	public void head() {
		CircularQueue_F64 alg = new CircularQueue_F64();

		alg.add(1);
		assertEquals(1, alg.head(), UtilEjml.TEST_F64);
		alg.add(3);
		assertEquals(1,alg.head(), UtilEjml.TEST_F64);
	}

	@Test
	public void head_offset() {
		CircularQueue_F64 alg = new CircularQueue_F64(3);

		alg.start = 2;
		alg.size = 0;

		alg.add(1);
		assertEquals(1, alg.head(), UtilEjml.TEST_F64);
		alg.add(3);
		assertEquals(1,alg.head(), UtilEjml.TEST_F64);
	}

	@Test
	public void tail() {
		CircularQueue_F64 alg = new CircularQueue_F64();

		alg.add(1);
		assertEquals(1,alg.tail(), UtilEjml.TEST_F64);
		alg.add(3);
		assertEquals(3, alg.tail(), UtilEjml.TEST_F64);
	}

	@Test
	public void tail_offset() {
		CircularQueue_F64 alg = new CircularQueue_F64(3);

		alg.start = 2;
		alg.size = 0;

		alg.add(1);
		assertEquals(1,alg.tail(), UtilEjml.TEST_F64);
		alg.add(3);
		assertEquals(3, alg.tail(), UtilEjml.TEST_F64);
	}

	@Test
	public void removeHead() {
		CircularQueue_F64 alg = new CircularQueue_F64();

		alg.add(1);
		alg.add(2);
		alg.removeHead();
		assertEquals(2, alg.head(), UtilEjml.TEST_F64);
		assertEquals(1, alg.size());

		alg.removeHead();
		assertEquals(0, alg.size());
	}

	@Test
	public void removeTail() {
		CircularQueue_F64 alg = new CircularQueue_F64();

		alg.add(1);
		alg.add(2);
		alg.removeTail();
		assertEquals(1,alg.head(), UtilEjml.TEST_F64);
		assertEquals(1,alg.size());

		alg.removeTail();
		assertEquals(0, alg.size());
	}

	@Test
	public void get() {
		CircularQueue_F64 alg = new CircularQueue_F64(2);
		assertEquals(2,alg.data.length);

		// easy case
		alg.add(1);
		alg.add(2);

		assertEquals(1,alg.get(0), UtilEjml.TEST_F64);
		assertEquals(2,alg.get(1), UtilEjml.TEST_F64);

		// make there be an offset
		alg.removeHead();
		alg.add(3);
		assertEquals(2,alg.data.length); // sanity check
		assertEquals(2,alg.get(0), UtilEjml.TEST_F64);
		assertEquals(3,alg.get(1), UtilEjml.TEST_F64);
	}

	@Test
	public void add() {
		CircularQueue_F64 alg = new CircularQueue_F64(3);
		assertEquals(3,alg.data.length);

		alg.add(1);
		assertEquals(1,alg.data[0], UtilEjml.TEST_F64);
		assertEquals(1,alg.size);

		alg.add(2);
		assertEquals(1,alg.data[0], UtilEjml.TEST_F64);
		assertEquals(2,alg.data[1], UtilEjml.TEST_F64);
		assertEquals(2,alg.size);

		// see if it over writes
		alg.add(3);
		alg.add(4);
		assertEquals(4,alg.data[0], UtilEjml.TEST_F64);
		assertEquals(2,alg.data[1], UtilEjml.TEST_F64);
		assertEquals(3,alg.data[2], UtilEjml.TEST_F64);
		assertEquals(3,alg.size);
		assertEquals(1,alg.start);

		// wrap around case
		alg.start = 1;
		alg.size = 2;
		alg.data = new  double[3];
		alg.add(10);
		assertEquals(10,alg.data[0], UtilEjml.TEST_F64);
		assertEquals(3,alg.size);
	}

	@Test
	public void isEmpty() {
		CircularQueue_F64 alg = new CircularQueue_F64(3);

		assertTrue(alg.isEmpty());
		alg.add(5);
		assertFalse(alg.isEmpty());
		alg.removeTail();
		assertTrue(alg.isEmpty());

	}

	@Test
	public void reset() {
		CircularQueue_F64 alg = new CircularQueue_F64(3);

		alg.start = 2;
		alg.size = 5;

		alg.reset();

		assertEquals(0,alg.size);
		assertEquals(0,alg.start);
	}

	@Test
	public void set_queue() {
		CircularQueue_F64 a = new CircularQueue_F64(3);

		for (int i = 0; i < 4; i++) {
			a.add(i);
		}

		CircularQueue_F64 b = new CircularQueue_F64(10);
		b.set(a);

		assertEquals(3,b.queueSize());
		for (int i = 0; i < a.data.length; i++) {
			assertEquals(a.data[i],b.data[i], UtilEjml.TEST_F64);
		}
		assertEquals(a.size,b.size);
		assertEquals(a.start,b.start);

	}
}
