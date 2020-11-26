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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestGrowArrayList {

	@Test
	public void size() {
		DogArray<Dummy> queue = new DogArray<>(100,()->new Dummy(0));

		List<Dummy> list = queue.toList();

		assertEquals(0, list.size());

		queue.grow().value = 1.0;

		assertEquals(1, list.size());
	}

	@Test
	public void isEmpty() {
		DogArray<Dummy> queue = new DogArray<>(100,()->new Dummy(0));

		List<Dummy> list = queue.toList();

		assertTrue( list.isEmpty() );

		queue.grow().value = 1.0;

		assertFalse(list.isEmpty());
	}

	@Test
	public void contains() {
		DogArray<Dummy> queue = new DogArray<>(100,()->new Dummy(0));
		List<Dummy> list = queue.toList();

		assertFalse(list.contains(new Dummy(1.0)));

		Dummy d = queue.grow();

		assertTrue(list.contains(d));
	}

	@Test
	public void iterator() {
		DogArray<Dummy> queue = new DogArray<>(100,()->new Dummy(0));
		queue.grow().value = 1.0;
		queue.grow().value = 2.0;
		queue.grow().value = 3.0;

		Iterator<Dummy> iterator = queue.toList().iterator();
		assertTrue(iterator.hasNext());
		assertEquals(1.0, iterator.next().value);
		assertTrue(iterator.hasNext());
		assertEquals(2.0, iterator.next().value);
		assertTrue(iterator.hasNext());
		assertEquals(3.0, iterator.next().value);
		assertFalse(iterator.hasNext());

	}

	@Test
	public void toArray() {
		DogArray<Dummy> queue = new DogArray<>(100,()->new Dummy(0));
		queue.grow().value = 1.0;
		queue.grow().value = 2.0;
		queue.grow().value = 3.0;

		Object[] array = queue.toList().toArray();
		assertEquals(3, array.length);
		assertEquals(1.0, ((Dummy) array[0]).value);
		assertEquals(2.0, ((Dummy) array[1]).value);
		assertEquals(3.0, ((Dummy) array[2]).value);

		// remove an element from the queue to make sure it isn't using array length
		queue.removeTail();
		array = queue.toList().toArray();
		assertEquals(2, array.length);
	}

	@Test
	public void containsAll() {
		DogArray<Dummy> queue = new DogArray<>(100,()->new Dummy(0));
		queue.grow().value = 1.0;
		queue.grow().value = 2.0;
		queue.grow().value = 3.0;

		List<Dummy> list = new ArrayList<>();
		list.add(queue.get(0));
		list.add(queue.get(1));

		assertTrue(queue.toList().containsAll(list));

		list.add(new Dummy(5.0));
		assertFalse(queue.toList().containsAll(list));
	}

	@Test
	public void get() {
		DogArray<Dummy> queue = new DogArray<>(100,()->new Dummy(0));
		queue.grow().value = 1.0;
		queue.grow().value = 2.0;

		assertEquals(2.0, queue.toList().get(1).value);
	}

	public static class Dummy {
		double value;

		public Dummy(double value) {
			this.value = value;
		}
	}
}
