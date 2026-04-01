/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class TestCircularArray_F32 {
	@Test void popHead() {
		var alg = new CircularArray_F32();

		alg.add(1);
		alg.add(2);
		assertEquals(1, alg.popHead(), UtilEjml.TEST_F64);
		assertEquals(1, alg.size());

		assertEquals(2, alg.popHead(), UtilEjml.TEST_F64);
		assertEquals(0, alg.size());
	}

	@Test void popTail() {
		var alg = new CircularArray_F32();

		alg.add(1);
		alg.add(2);
		assertEquals(2, alg.popTail(), UtilEjml.TEST_F64);
		assertEquals(1, alg.size());

		assertEquals(1, alg.popTail(), UtilEjml.TEST_F64);
		assertEquals(0, alg.size());
	}

	@Test void head() {
		var alg = new CircularArray_F32();

		alg.add(1);
		assertEquals(1, alg.head(), UtilEjml.TEST_F64);
		alg.add(3);
		assertEquals(1, alg.head(), UtilEjml.TEST_F64);
	}

	@Test void head_offset() {
		var alg = new CircularArray_F32(3);

		alg.start = 2;
		alg.size = 0;

		alg.add(1);
		assertEquals(1, alg.head(), UtilEjml.TEST_F64);
		alg.add(3);
		assertEquals(1, alg.head(), UtilEjml.TEST_F64);
	}

	@Test void tail() {
		var alg = new CircularArray_F32();

		alg.add(1);
		assertEquals(1, alg.tail(), UtilEjml.TEST_F64);
		alg.add(3);
		assertEquals(3, alg.tail(), UtilEjml.TEST_F64);
	}

	@Test void tail_offset() {
		var alg = new CircularArray_F32(3);

		alg.start = 2;
		alg.size = 0;

		alg.add(1);
		assertEquals(1, alg.tail(), UtilEjml.TEST_F64);
		alg.add(3);
		assertEquals(3, alg.tail(), UtilEjml.TEST_F64);
	}

	@Test void removeHead() {
		var alg = new CircularArray_F32();

		alg.add(1);
		alg.add(2);
		alg.removeHead();
		assertEquals(2, alg.head(), UtilEjml.TEST_F64);
		assertEquals(1, alg.size());

		alg.removeHead();
		assertEquals(0, alg.size());
	}

	@Test void removeTail() {
		var alg = new CircularArray_F32();

		alg.add(1);
		alg.add(2);
		alg.removeTail();
		assertEquals(1, alg.head(), UtilEjml.TEST_F64);
		assertEquals(1, alg.size());

		alg.removeTail();
		assertEquals(0, alg.size());
	}

	@Test void remove() {
		var alg = new CircularArray_F32(5);
		// make it more interesting via wrapping around
		alg.start = 3;
		alg.add(1);
		alg.remove(0);
		assertEquals(0, alg.size);

		for (int i = 0; i < 3; i++) {
			alg.add(i+1);
		}

		alg.remove(1);
		assertEquals(2, alg.size);
		assertEquals(1, alg.get(0));
		assertEquals(3, alg.get(1));

		// Remove the tail. There's special logic for that
		alg.remove(1);
		assertEquals(1, alg.size);
		assertEquals(1, alg.get(0));
	}

	@Test void get() {
		var alg = new CircularArray_F32(2);
		assertEquals(2, alg.data.length);

		// easy case
		alg.add(1);
		alg.add(2);

		assertEquals(1, alg.get(0), UtilEjml.TEST_F64);
		assertEquals(2, alg.get(1), UtilEjml.TEST_F64);

		// make there be an offset
		alg.removeHead();
		alg.add(3);
		assertEquals(2, alg.data.length); // sanity check
		assertEquals(2, alg.get(0), UtilEjml.TEST_F64);
		assertEquals(3, alg.get(1), UtilEjml.TEST_F64);
	}

	@Test void add() {
		var alg = new CircularArray_F32(3);
		assertEquals(3, alg.data.length);

		alg.add(1);
		assertEquals(1, alg.data[0], UtilEjml.TEST_F64);
		assertEquals(1, alg.size);

		alg.add(2);
		assertEquals(1, alg.data[0], UtilEjml.TEST_F64);
		assertEquals(2, alg.data[1], UtilEjml.TEST_F64);
		assertEquals(2, alg.size);

		// see if it overwrites
		alg.add(3);
		alg.add(4);
		assertEquals(4, alg.data[0], UtilEjml.TEST_F64);
		assertEquals(2, alg.data[1], UtilEjml.TEST_F64);
		assertEquals(3, alg.data[2], UtilEjml.TEST_F64);
		assertEquals(3, alg.size);
		assertEquals(1, alg.start);

		// wrap around case
		alg.start = 1;
		alg.size = 2;
		alg.data = new float[3];
		alg.add(10);
		assertEquals(10, alg.data[0], UtilEjml.TEST_F64);
		assertEquals(3, alg.size);
	}

	@Test void isEmpty() {
		var alg = new CircularArray_F32(3);

		assertTrue(alg.isEmpty());
		alg.add(5);
		assertFalse(alg.isEmpty());
		alg.removeTail();
		assertTrue(alg.isEmpty());
	}

	@Test void reset() {
		var alg = new CircularArray_F32(3);

		alg.start = 2;
		alg.size = 5;

		alg.reset();

		assertEquals(0, alg.size);
		assertEquals(0, alg.start);
	}

	@Test void set_queue() {
		CircularArray_F32 a = new CircularArray_F32(3);

		for (int i = 0; i < 4; i++) {
			a.add(i);
		}

		CircularArray_F32 b = new CircularArray_F32(10);
		b.setTo(a);

		assertEquals(3, b.getMaxSize());
		for (int i = 0; i < a.data.length; i++) {
			assertEquals(a.data[i], b.data[i], UtilEjml.TEST_F64);
		}
		assertEquals(a.size, b.size);
		assertEquals(a.start, b.start);
	}
}
