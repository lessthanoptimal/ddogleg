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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestCircularArray_I32 {
	@Test void popHead() {
		var alg = new CircularArray_I32();

		alg.add(1);
		alg.add(2);
		assertEquals(1, alg.popHead());
		assertEquals(1, alg.size());

		assertEquals(2, alg.popHead());
		assertEquals(0, alg.size());
	}

	@Test void popTail() {
		var alg = new CircularArray_I32();

		alg.add(1);
		alg.add(2);
		assertEquals(2, alg.popTail());
		assertEquals(1, alg.size());

		assertEquals(1, alg.popTail());
		assertEquals(0, alg.size());
	}

	@Test void head() {
		var alg = new CircularArray_I32();

		alg.add(1);
		assertEquals(1, alg.head());
		alg.add(3);
		assertEquals(1, alg.head());
	}

	@Test void head_offset() {
		var alg = new CircularArray_I32(3);

		alg.start = 2;
		alg.size = 0;

		alg.add(1);
		assertEquals(1, alg.head());
		alg.add(3);
		assertEquals(1, alg.head());
	}

	@Test void tail() {
		var alg = new CircularArray_I32();

		alg.add(1);
		assertEquals(1, alg.tail());
		alg.add(3);
		assertEquals(3, alg.tail());
	}

	@Test void tail_offset() {
		var alg = new CircularArray_I32(3);

		alg.start = 2;
		alg.size = 0;

		alg.add(1);
		assertEquals(1, alg.tail());
		alg.add(3);
		assertEquals(3, alg.tail());
	}

	@Test void removeHead() {
		var alg = new CircularArray_I32();

		alg.add(1);
		alg.add(2);
		alg.removeHead();
		assertEquals(2, alg.head());
		assertEquals(1, alg.size());

		alg.removeHead();
		assertEquals(0, alg.size());
	}

	@Test void removeTail() {
		var alg = new CircularArray_I32();

		alg.add(1);
		alg.add(2);
		alg.removeTail();
		assertEquals(1, alg.head());
		assertEquals(1, alg.size());

		alg.removeTail();
		assertEquals(0, alg.size());
	}

	@Test void remove() {
		var alg = new CircularArray_I32(5);
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
		var alg = new CircularArray_I32(2);
		assertEquals(2, alg.data.length);

		// easy case
		alg.add(1);
		alg.add(2);

		assertEquals(1, alg.get(0));
		assertEquals(2, alg.get(1));

		// make there be an offset
		alg.removeHead();
		alg.add(3);
		assertEquals(2, alg.data.length); // sanity check
		assertEquals(2, alg.get(0));
		assertEquals(3, alg.get(1));
	}

	@Test void add() {
		var alg = new CircularArray_I32(3);
		assertEquals(3, alg.data.length);

		alg.add(1);
		assertEquals(1, alg.data[0]);
		assertEquals(1, alg.size);

		alg.add(2);
		assertEquals(1, alg.data[0]);
		assertEquals(2, alg.data[1]);
		assertEquals(2, alg.size);

		// see if it grows
		alg.add(3);
		alg.add(4);
		assertEquals(1, alg.data[0]);
		assertEquals(2, alg.data[1]);
		assertEquals(3, alg.data[2]);
		assertEquals(4, alg.data[3]);
		assertEquals(4, alg.size);

		// grows with offset
		alg.start = 1;
		alg.data = new int[]{1, 2, 3};
		alg.size = 3;
		alg.add(4);
		assertEquals(2, alg.data[0]);
		assertEquals(3, alg.data[1]);
		assertEquals(1, alg.data[2]);
		assertEquals(4, alg.data[3]);
		assertEquals(4, alg.size);

		// wrap around case
		alg.start = 1;
		alg.size = 2;
		alg.data = new int[3];
		alg.add(10);
		assertEquals(10, alg.data[0]);
		assertEquals(10, alg.data[0]);
		assertEquals(3, alg.size);
	}

	@Test void addW() {
		var alg = new CircularArray_I32(3);
		assertEquals(3, alg.data.length);

		alg.addW(1);
		assertEquals(1, alg.data[0]);
		assertEquals(1, alg.size);

		alg.addW(2);
		assertEquals(1, alg.data[0]);
		assertEquals(2, alg.data[1]);
		assertEquals(2, alg.size);

		// see if it over writes
		alg.addW(3);
		alg.addW(4);
		assertEquals(4, alg.data[0]);
		assertEquals(2, alg.data[1]);
		assertEquals(3, alg.data[2]);
		assertEquals(3, alg.size);
		assertEquals(1, alg.start);

		// wrap around case
		alg.start = 1;
		alg.size = 2;
		alg.data = new int[3];
		alg.addW(10);
		assertEquals(10, alg.data[0]);
		assertEquals(3, alg.size);
	}


	@Test void isEmpty() {
		var alg = new CircularArray_I32(3);

		assertTrue(alg.isEmpty());
		alg.add(5);
		assertFalse(alg.isEmpty());
		alg.removeTail();
		assertTrue(alg.isEmpty());
	}

	@Test void reset() {
		var alg = new CircularArray_I32(3);

		alg.start = 2;
		alg.size = 5;

		alg.reset();

		assertEquals(0, alg.size);
		assertEquals(0, alg.start);
	}
}
