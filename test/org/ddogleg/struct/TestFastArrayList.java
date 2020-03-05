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
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
class TestFastArrayList {

	@Test
	void size() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);

		List<Double> list = queue.toList();

		assertEquals(0, list.size());

		queue.add( 1.0 );

		assertEquals(1, list.size());
	}

	@Test
	void isEmpty() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);

		List<Double> list = queue.toList();

		assertTrue( list.isEmpty() );

		queue.add( 1.0 );

		assertFalse(list.isEmpty());
	}

	@Test
	void contains() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);
		Double d = 1.0;

		List<Double> list = queue.toList();

		assertFalse(list.contains(d));

		queue.add( d );

		assertTrue(list.contains(d));
	}

	@Test
	void iterator() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);
		queue.add(1.0);
		queue.add(2.0);
		queue.add(3.0);

		Iterator<Double> iterator = queue.toList().iterator();
		assertTrue(iterator.hasNext());
		assertEquals(1.0, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(2.0, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(3.0, iterator.next());
		assertFalse(iterator.hasNext());

	}

	@Test
	void toArray() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);
		queue.add(1.0);
		queue.add(2.0);
		queue.add(3.0);

		Object[] array = queue.toList().toArray();
		assertEquals(3, array.length);
		assertEquals(1.0, (Double) array[0]);
		assertEquals(2.0, (Double) array[1]);
		assertEquals(3.0, (Double) array[2]);

		// remove an element from the queue to make sure it isn't using array length
		queue.removeTail();
		array = queue.toList().toArray();
		assertEquals(2, array.length);
	}

	@Test
	void add() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);
		List<Double> list = queue.toList();

		list.add( 5.0 );

		assertEquals(1,queue.size());
		assertEquals(5.0,queue.get(0),UtilEjml.TEST_F64);
	}

	@Test
	void containsAll() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);
		queue.add(1.0);
		queue.add(2.0);
		queue.add(3.0);

		List<Double> list = new ArrayList<>();
		list.add(1.0);
		list.add(2.0);

		assertTrue(queue.toList().containsAll(list));

		list.add(5.0);
		assertFalse(queue.toList().containsAll(list));
	}

	@Test
	void addAll() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);
		List<Double> list = queue.toList();
		List<Double> stuff = new ArrayList<>();

		stuff.add(5.0);
		stuff.add(10.0);

		assertTrue(list.addAll(stuff));

		assertEquals(2,queue.size());
		assertEquals(5.0,queue.get(0), UtilEjml.TEST_F64);
		assertEquals(10.0, queue.get(1), UtilEjml.TEST_F64);
	}

	@Test
	void get() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);
		queue.add(1.0);
		queue.add(2.0);

		assertEquals(2.0, queue.toList().get(1));
	}

	@Test
	void set() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);
		queue.add(1.0);
		queue.add(2.0);

		List<Double> list = queue.toList();
		list.set(0,3.0);

		assertEquals(3.0, list.get(0));
	}

	@Test
	void indexOf_lastIndexOf() {
		FastArray<Double> queue = new FastArray<>(Double.class,100);
		queue.add(1.0);
		queue.add(2.0);
		queue.add(2.0);
		queue.add(3.0);

		assertEquals(1, queue.toList().indexOf(2.0));
		assertEquals(2, queue.toList().lastIndexOf(2.0));
	}
}
