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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestDogLinkedList {
	@Test
	public void reset() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		alg.pushHead(1);
		alg.pushHead(2);
		alg.reset();
		assertEquals(2,alg.available.size());
		assertEquals(0,alg.size);
		assertNull(alg.first);
		assertNull(alg.last);

		checkList(alg);
	}

	@Test
	public void isEmpty() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		assertTrue(alg.isEmpty());
		alg.pushHead(1);
		assertFalse(alg.isEmpty());
		alg.reset();
		assertTrue(alg.isEmpty());
	}

	@Test
	public void getElement() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		DogLinkedList.Element<Integer> e0 = alg.pushTail(1);
		DogLinkedList.Element<Integer> e1 = alg.pushTail(2);
		DogLinkedList.Element<Integer> e2 = alg.pushTail(2);

		assertSame(e0, alg.getElement(0, true));
		assertSame(e1, alg.getElement(1, true));
		assertSame(e2, alg.getElement(2, true));

		assertSame(e2, alg.getElement(0, false));
		assertSame(e1, alg.getElement(1, false));
		assertSame(e0, alg.getElement(2, false));
	}

	@Test
	public void pushHead() {

		DogLinkedList<Integer> alg = new DogLinkedList<>();

		DogLinkedList.Element<Integer> e0 = alg.pushHead(1);
		assertEquals(1,alg.size);
		checkList(alg);
		DogLinkedList.Element<Integer> e1 = alg.pushHead(2);
		assertEquals(2,alg.size);
		assertSame(e1, alg.first);
		assertSame(e0, alg.last);
		checkList(alg);

		DogLinkedList.Element<Integer> e2 = alg.pushHead(3);
		assertEquals(3,alg.size);
		assertSame(e2, alg.first);
		assertSame(e0, alg.last);
		checkList(alg);
	}

	@Test
	public void pushTail() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		DogLinkedList.Element<Integer> e0 = alg.pushTail(1);
		assertEquals(1,alg.size);
		checkList(alg);

		DogLinkedList.Element<Integer> e1 = alg.pushTail(2);
		assertEquals(2,alg.size);
		assertSame(e0, alg.first);
		assertSame(e1, alg.last);
		checkList(alg);

		DogLinkedList.Element<Integer> e2 = alg.pushTail(3);
		assertEquals(3,alg.size);
		assertSame(e0, alg.first);
		assertSame(e2, alg.last);
		checkList(alg);
	}

	@Test
	public void insertAfter() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		DogLinkedList.Element<Integer> e0 = alg.pushHead(1);
		DogLinkedList.Element<Integer> e1 = alg.insertAfter(e0, 2);
		assertSame(e0.next, e1);
		assertSame(e1.previous, e0);
		assertEquals(2,alg.size);
		checkList(alg);

		DogLinkedList.Element<Integer> e2 = alg.insertAfter(e1, 2);
		assertSame(e1.next, e2);
		assertSame(e2.previous, e1);
		assertSame(e2, alg.last);
		assertEquals(3,alg.size);
		checkList(alg);
	}

	@Test
	public void insertBefore() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		DogLinkedList.Element<Integer> e0 = alg.pushHead(1);
		DogLinkedList.Element<Integer> e1 = alg.insertBefore(e0, 2);
		assertSame(e0.previous, e1);
		assertSame(e1.next, e0);
		assertEquals(2,alg.size);
		checkList(alg);

		DogLinkedList.Element<Integer> e2 = alg.insertBefore(e1, 2);
		assertSame(e1.previous, e2);
		assertSame(e2.next, e1);
		assertSame(e2, alg.first);
		assertEquals(3,alg.size);
		checkList(alg);
	}

	@Test
	public void swap() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		DogLinkedList.Element<Integer> e0 = alg.pushTail(1);
		DogLinkedList.Element<Integer> e1 = alg.pushTail(2);

		alg.swap(e0,e1);
		assertSame(alg.first, e1);
		assertSame(alg.last, e0);
		checkList(alg);
		alg.swap(e0,e1);
		assertSame(alg.first, e0);
		assertSame(alg.last, e1);
		checkList(alg);

		DogLinkedList.Element<Integer> e2 = alg.pushTail(3);
		alg.swap(e0,e1);
		assertSame(alg.first, e1);
		assertSame(alg.last, e2);
		checkList(alg);
		alg.swap(e0,e1);
		assertSame(alg.first, e0);
		checkList(alg);
		alg.swap(e1,e2);
		assertSame(alg.last, e1);
		checkList(alg);
		alg.swap(e1,e2);
		assertSame(alg.last, e2);
		checkList(alg);

		DogLinkedList.Element<Integer> e3 = alg.pushTail(4);
		alg.swap(e0,e2);
		assertSame(alg.first, e2);
		assertSame(alg.last, e3);
		checkList(alg);
		alg.swap(e0,e2);
		checkList(alg);
		assertSame(alg.first, e0);
		assertSame(alg.last, e3);

	}

	@Test
	public void remove() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		DogLinkedList.Element<Integer> e0,e1,e2;

		e0 = alg.pushTail(1);
		alg.remove(e0);
		assertEquals(0,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		e0 = alg.pushTail(1);
		e1 = alg.pushTail(2);
		alg.remove(e1);
		assertSame(e0, alg.first);
		assertEquals(1,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		e1 = alg.pushTail(2);
		alg.remove(e0);
		assertSame(e1, alg.first);
		assertEquals(1,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		e0 = alg.pushHead(1);
		e2 = alg.pushTail(3);
		alg.remove(e1);
		assertSame(e0, alg.first);
		assertEquals(2,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);
	}

	@Test
	public void removeHead() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		alg.pushHead(1);
		assertEquals((int) alg.removeHead(), 1);
		assertEquals(0,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		alg.pushTail(1);
		alg.pushTail(2);
		assertEquals((int) alg.removeHead(), 1);
		assertEquals(1,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);
	}

	@Test
	public void removeTail() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		alg.pushHead(1);
		assertEquals(alg.removeTail(), 1);
		assertEquals(0,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		alg.pushTail(1);
		alg.pushTail(2);
		assertEquals(alg.removeTail(), 2);
		assertEquals(1,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);
	}

	@Test
	public void find() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		assertNull(alg.find(1));

		DogLinkedList.Element<Integer> e1 = alg.pushHead(1);
		assertNull(alg.find(4));
		assertSame(e1, alg.find(e1.object));

		DogLinkedList.Element<Integer> e2 = alg.pushHead(2);
		assertNull(alg.find(4));
		assertSame(e2, alg.find(e2.object));
		assertSame(e1, alg.find(e1.object));
	}

	@Test
	public void getHead() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		assertNull(alg.getHead());
		DogLinkedList.Element<Integer> e = alg.pushHead(1);
		assertSame(e, alg.getHead());

	}

	@Test
	public void getTail() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		assertNull(alg.getHead());
		DogLinkedList.Element<Integer> e = alg.pushHead(1);
		assertSame(e, alg.getTail());
		e = alg.pushTail(2);
		assertSame(e, alg.getTail());
	}

	@Test
	public void addAll_List() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		List<Integer> list0 = new ArrayList<>();
		List<Integer> list1 = new ArrayList<>();


		alg.addAll(list0);
		assertEquals(0,alg.size);
		checkList(alg);

		list0.add(1);
		alg.addAll(list0);
		assertEquals(1,alg.size);
		checkList(alg);

		alg.addAll(list1);
		assertEquals(1,alg.size);
		checkList(alg);

		list1.add(2);
		list1.add(3);
		alg.addAll(list1);
		assertEquals(3,alg.size);
		checkList(alg);
		assertSame(Integer.valueOf(1), alg.getHead().object);
		assertSame(Integer.valueOf(3), alg.getTail().object);

	}

	@Test
	public void addAll_array() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		Integer[] array0 = new Integer[0];

		alg.addAll(array0,0,0);
		assertEquals(0,alg.size);
		checkList(alg);

		array0 = new Integer[3];
		for (int i = 0; i < array0.length; i++) {
			array0[i] = i;
		}

		alg.addAll(array0,1,1);
		assertEquals(1,alg.size);
		assertSame(Integer.valueOf(1), alg.getHead().object);
		checkList(alg);

		alg.addAll(array0,0,3);
		assertEquals(4,alg.size);
		assertSame(Integer.valueOf(1), alg.getHead().object);
		assertSame(Integer.valueOf(0), alg.getElement(1, true).object);
		assertSame(Integer.valueOf(1), alg.getElement(2, true).object);
		assertSame(Integer.valueOf(2), alg.getElement(3, true).object);
		checkList(alg);
	}

	@Test
	public void requestNew() {
		DogLinkedList<Integer> alg = new DogLinkedList<>();

		DogLinkedList.Element<Integer> e0 = alg.requestNew();
		assertNotNull(e0);

		DogLinkedList.Element<Integer> e1 = alg.requestNew();
		assertNotNull(e1);
		assertNotSame(e1, e0);

		alg.available.add(e0);
		DogLinkedList.Element<Integer> e2 = alg.requestNew();
		assertSame(e2, e0);

		DogLinkedList.Element<Integer> e3 = alg.requestNew();
		assertNotNull(e3);
		assertNotSame(e3, e0);
		assertNotSame(e3, e1);
		assertNotSame(e3, e2);
	}

	/**
	 * Performs checks on the lists preconditions
	 */
	protected void checkList( DogLinkedList<Integer> queue ) {
		for( var e : queue.available ) {
			assertNull(e.previous);
			assertNull(e.next);
			assertNull(e.object);
		}

		if( queue.size == 0 ) {
			assertNull(queue.first);
			assertNull(queue.last);
		} else {
			List<DogLinkedList.Element<Integer>> forwards = new ArrayList<>();
			List<DogLinkedList.Element<Integer>> backwards = new ArrayList<>();

			DogLinkedList.Element<Integer> e = queue.first;
			while( e != null ) {
				forwards.add(e);
				e = e.next;
				if( forwards.size() > queue.size() )
					fail("too many elements in forward direction");
			}

			e = queue.last;
			while( e != null ) {
				backwards.add(e);
				e = e.previous;
				if( backwards.size() > queue.size() )
					fail("too many elements in forward direction");
			}

			assertEquals(forwards.size(),backwards.size());
			assertEquals(forwards.size(),queue.size());

			for (int i = 0; i < forwards.size(); i++) {
				int j = forwards.size()-1-i;
				assertSame(forwards.get(i), backwards.get(j));
			}
		}

	}
}