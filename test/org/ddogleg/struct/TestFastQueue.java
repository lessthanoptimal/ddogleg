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

import org.ejml.UtilEjml;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
class TestFastQueue {

	/**
	 * makes sure reset function is called when the grow command is used
	 */
	@Test
	void reset_grow() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new,(d)->d.value=2);

		for (int i = 0; i < 5; i++) {
			DummyData d = alg.grow();
			assertEquals(2,d.value);
		}
		assertEquals(5,alg.size);
	}

	/**
	 * makes sure reset function is called when the grow command is used
	 */
	@Test
	void reset_resize() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new,(d)->d.value=2);

		alg.resize(3);
		for (int i = 0; i < alg.size; i++) {
			alg.get(i).value = 100;
		}

		// resize again and make sure it doesn't reset elements already in the list
		alg.resize(10);
		for (int i = 0; i < 3; i++) {
			assertEquals(100, alg.get(i).value);
		}
		for (int i = 3; i < 10; i++) {
			assertEquals(2, alg.get(i).value);
		}
	}

	@Test
	void checkDeclareInstance() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);

		assertTrue(alg.getMaxSize()>0);
		assertNotNull(alg.data[0]);
	}

	@Test
	void toList() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);

		List<DummyData> l = alg.toList();
		assertEquals(0,l.size());
		
		alg.grow().value = 1;
		alg.grow().value = 1;
		alg.grow().value = 2;
		alg.removeTail();

		l = alg.toList();
		assertEquals(2,l.size());
		assertEquals(1,l.get(0).value);
		assertEquals(1,l.get(1).value);
	}

	@Test
	void remove_indexes() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData::new);
		for (int i = 0; i < 10; i++) {
			alg.grow().value = i;
		}

		int[] indexes = new int[]{0,1,4,2,6,8,9};
		alg.remove(indexes,2,6,null);
		assertEquals(6,alg.size());
		assertEquals(0,alg.get(0).value);
		assertEquals(1,alg.get(1).value);
		assertEquals(3,alg.get(2).value);
		assertEquals(5,alg.get(3).value);
		assertEquals(7,alg.get(4).value);
		assertEquals(9,alg.get(5).value);

		// make sure original objects were recycled properly
		for (int i = 0; i < 10; i++) {
			for (int j = i+1; j < 10; j++) {
				assertNotEquals(alg.data[i].value,alg.data[j].value);
			}
		}
	}

	@Test
	void remove_indexes_RemoveNothing() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData::new);
		for (int i = 0; i < 10; i++) {
			alg.grow().value = i;
		}
		int[] indexes = new int[]{};
		alg.remove(indexes,2,2,null);
		assertEquals(10,alg.size());
	}

	@Test
	void removeTail() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData::new);

		alg.grow();
		assertEquals(1,alg.size);
		alg.removeTail();
		assertEquals(0,alg.size);
	}

	@Test
	void copyAll() {
		List<DummyData> data = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			DummyData d = new DummyData();
			d.value = i;
			data.add(d);
		}
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);
		alg.copyAll(data,(src, dst)-> dst.value=src.value);

		for (int i = 0; i < 10; i++) {
			assertNotEquals(data.get(i), alg.get(i));
			assertEquals(i, alg.get(i).value);
		}
	}

	@Test
	void remove_index() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData::new);

		List<DummyData> l = alg.toList();
		assertEquals(0,l.size());

		alg.grow().value = 1;
		alg.grow().value = 2;
		alg.grow().value = 3;

		alg.remove(1);

		assertEquals(2,alg.size());
		assertEquals(1,alg.get(0).value);
		assertEquals(3,alg.get(1).value);
		// make sure the data was shifted to the end
		assertEquals(2,alg.data[2].value);

		alg.remove(1);
		assertEquals(1,alg.size());
		assertEquals(1,alg.get(0).value);
		assertEquals(3,alg.data[1].value);
		assertEquals(2,alg.data[2].value);

		alg.remove(0);
		assertEquals(0,alg.size());
		assertEquals(1,alg.data[0].value);
		assertEquals(3,alg.data[1].value);
		assertEquals(2,alg.data[2].value);
	}

	@Test
	void remove_object() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);

		alg.grow().value = 10;
		alg.grow().value = 11;
		alg.grow().value = 12;

		assertFalse(alg.remove(new DummyData()));

		assertTrue(alg.remove(alg.get(1)));
		assertEquals(2,alg.size);
		assertEquals(10,alg.get(0).value);
		assertEquals(12,alg.get(1).value);
	}

	@Test
	void removeSwap() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData::new);

		List<DummyData> l = alg.toList();
		assertEquals(0,l.size());

		alg.grow().value = 1;
		DummyData d = alg.get(0);
		assertSame(d,alg.removeSwap(0));
		assertEquals(0,alg.size());

		alg.grow().value = 1;
		alg.grow().value = 2;
		alg.grow().value = 3;
		alg.grow().value = 4;

		alg.removeSwap(1);

		assertEquals(3,alg.size());
		assertEquals(1,alg.get(0).value);
		assertEquals(4,alg.get(1).value);
		assertEquals(3,alg.get(2).value);
		// Make sure the removed element is at the tail
		assertEquals(2,alg.data[3].value);
	}

	@Test
	void getTail() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData::new);

		alg.grow();alg.grow();

		assertSame(alg.data[1], alg.getTail());
	}

	@Test
	void getTail_index() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData::new);

		alg.grow();alg.grow();

		for (int i = 0; i < alg.size(); i++) {
			assertSame(alg.data[i], alg.getTail(alg.size - i - 1));
		}
	}

	@Test
	void get_pop() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);

		// test a failure case
		try {
			alg.get(0);
			fail("Didn't fail");
		} catch( IllegalArgumentException ignore ) {}

		alg.grow();
		alg.get(0);
	}

	@Test
	void size() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);
		assertEquals(0,alg.size);
		alg.grow();
		assertEquals(1,alg.size);
	}

	/**
	 * Checks to see if pop automatically grows correctly
	 */
	@Test
	void pop_grow() {
		FastQueue<DummyData> alg = new FastQueue<>(1,DummyData::new);

		int before = alg.getMaxSize();
		for( int i = 0; i < 20; i++ ) {
			alg.grow();
		}
		alg.get(19);
		int after = alg.getMaxSize();
		assertTrue(after>before);
	}

	@Test
	void growArray() {
		FastQueue<DummyData> alg = new FastQueue<>(1,DummyData::new);

		alg.grow().value = 10;
		int before = alg.getMaxSize();
		alg.growArray(before+5);
		assertEquals(10,alg.get(0).value);
	}

	@Test
	void contains() {
		FastQueue<DummyData> queue = new FastQueue<>(DummyData::new);
		queue.grow();

		assertFalse(queue.contains(new DummyData()));

		assertTrue(queue.contains(queue.get(0)));
	}

	@Test
	void indexOf() {
		FastQueue<DummyData> queue = new FastQueue<>(100,DummyData::new);
		queue.grow().value = 1;
		queue.grow().value = 3;
		queue.grow().value = 2;

		assertEquals(-1,queue.indexOf(new DummyData()), UtilEjml.TEST_F64);
		assertEquals(0,queue.indexOf(queue.get(0)), UtilEjml.TEST_F64);
		assertEquals(1,queue.indexOf(queue.get(1)), UtilEjml.TEST_F64);
		assertEquals(2,queue.indexOf(queue.get(2)), UtilEjml.TEST_F64);
	}


	@Test
	void swap() {
		FastQueue<DummyData> queue = new FastQueue<>(100,DummyData::new);
		queue.grow().value = 1;
		queue.grow().value = 2;
		queue.grow().value = 3;
		queue.grow().value = 4;

		queue.swap(0,3);
		queue.swap(0,1);

		assertEquals(2,queue.get(0).value);
		assertEquals(4,queue.get(1).value);
		assertEquals(3,queue.get(2).value);
		assertEquals(1,queue.get(3).value);
	}
}
