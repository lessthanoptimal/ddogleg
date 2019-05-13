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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Peter Abeles
 */
class TestFastQueue {

	@Test
	void checkDeclareInstance() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData.class,true);

		assertTrue(alg.getMaxSize()>0);
		assertNotNull(alg.data[0]);

		alg = new FastQueue<>(10,DummyData.class,false);

		assertTrue(alg.getMaxSize()>0);
		assertNull(alg.data[0]);
	}

	@Test
	void toList() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData.class,true);

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
	void removeTail() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData.class,true);

		alg.grow();
		assertEquals(1,alg.size);
		alg.removeTail();
		assertEquals(0,alg.size);
	}

	@Test
	void remove() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData.class,true);

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
	void getTail() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData.class,true);

		alg.grow();alg.grow();

		assertSame(alg.data[1], alg.getTail());
	}

	@Test
	void getTail_index() {
		FastQueue<DummyData> alg = new FastQueue<>(10,DummyData.class,true);

		alg.grow();alg.grow();

		for (int i = 0; i < alg.size(); i++) {
			assertSame(alg.data[i], alg.getTail(alg.size - i - 1));
		}
	}

	@Test
	void get_pop() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData.class,true);

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
		FastQueue<DummyData> alg = new FastQueue<>(DummyData.class,true);
		assertEquals(0,alg.size);
		alg.grow();
		assertEquals(1,alg.size);
	}

	@Test
	void add() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData.class,false);

		DummyData a = new DummyData();
		alg.add(a);
		assertSame(a, alg.data[0]);
	}

	@Test
	void reverse() {
		FastQueue<DummyData> alg = new FastQueue<>(2,DummyData.class,true);

		// 0 items
		alg.reverse();
		assertEquals(0, alg.size());

		// 1 item
		alg.grow().value = 1;
		alg.reverse();

		assertEquals(1, alg.get(0).value);

		// 2 items
		alg.grow().value = 2;
		alg.reverse();

		assertEquals(2, alg.get(0).value);
		assertEquals(1, alg.get(1).value);

		// 3 items (odd)
		alg.reset();

		alg.grow().value = 1;
		alg.grow().value = 2;
		alg.grow().value = 3;

		alg.reverse();

		assertEquals(3, alg.get(0).value);
		assertEquals(2,alg.get(1).value);
		assertEquals(1,alg.get(2).value);

		// 4 items (even)
		alg.reset();

		alg.grow().value = 1;
		alg.grow().value = 2;
		alg.grow().value = 3;
		alg.grow().value = 4;

		alg.reverse();

		assertEquals(4,alg.get(0).value);
		assertEquals(3,alg.get(1).value);
		assertEquals(2,alg.get(2).value);
		assertEquals(1,alg.get(3).value);

		// double reverse = original
		alg.reverse();
		assertEquals(1,alg.get(0).value);
		assertEquals(2,alg.get(1).value);
		assertEquals(3,alg.get(2).value);
		assertEquals(4,alg.get(3).value);
	}

	@Test
	void addAll() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData.class,true);
		alg.grow();
		alg.grow();

		FastQueue<DummyData> alg2 = new FastQueue<>(DummyData.class,false);

		alg2.addAll(alg);

		assertSame(alg.get(0), alg2.get(0));
		assertSame(alg.get(1), alg2.get(1));
	}

	/**
	 * Checks to see if pop automatically grows correctly
	 */
	@Test
	void pop_grow() {
		FastQueue<DummyData> alg = new FastQueue<>(1,DummyData.class,true);

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
		FastQueue<DummyData> alg = new FastQueue<>(1,DummyData.class,true);

		alg.grow().value = 10;
		int before = alg.getMaxSize();
		alg.growArray(before+5);
		assertEquals(10,alg.get(0).value);
	}

	@Test
	void contains() {
		FastQueue<Double> queue = new FastQueue<>(100,Double.class,false);
		Double d = 1.0;

		assertFalse(queue.contains(d));

		queue.add( d );

		assertTrue(queue.contains(d));
	}

	@Test
	void indexOf() {
		FastQueue<Double> queue = new FastQueue<>(100,Double.class,false);
		queue.add(1.0);
		queue.add(3.0);
		queue.add(3.0);
		queue.add(2.0);

		assertEquals(-1,queue.indexOf(0.0), UtilEjml.TEST_F64);
		assertEquals(0,queue.indexOf(1.0), UtilEjml.TEST_F64);
		assertEquals(1,queue.indexOf(3.0), UtilEjml.TEST_F64);
		assertEquals(3,queue.indexOf(2.0), UtilEjml.TEST_F64);
	}

	@Test
	void flip() {
		FastQueue<Double> queue = new FastQueue<>(100,Double.class,false);
		queue.flip();

		queue.add(1.0);
		queue.flip();
		assertEquals(1,queue.get(0), UtilEjml.TEST_F64);

		queue.add(2.0);
		queue.flip();
		assertEquals(2,queue.get(0), UtilEjml.TEST_F64);
		assertEquals(1,queue.get(1), UtilEjml.TEST_F64);

		queue.add(3.0);
		queue.flip();
		assertEquals(3,queue.get(0), UtilEjml.TEST_F64);
		assertEquals(1,queue.get(1), UtilEjml.TEST_F64);
		assertEquals(2,queue.get(2), UtilEjml.TEST_F64);
	}

	@Test
	void swap() {
		FastQueue<Double> queue = new FastQueue<>(100,Double.class,false);
		queue.add(1.0);
		queue.add(2.0);
		queue.add(3.0);
		queue.add(4.0);

		queue.swap(0,3);
		queue.swap(0,1);

		assertEquals(2,queue.get(0), UtilEjml.TEST_F64);
		assertEquals(4,queue.get(1), UtilEjml.TEST_F64);
		assertEquals(3,queue.get(2), UtilEjml.TEST_F64);
		assertEquals(1,queue.get(3), UtilEjml.TEST_F64);
	}
}
