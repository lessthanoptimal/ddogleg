/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestLinkedList {
	@Test
	public void reset() {
		LinkedList alg = new LinkedList();

		alg.pushHead(1);
		alg.pushHead(2);
		alg.reset();
		assertEquals(2,alg.available.size());
		assertEquals(0,alg.size);
		assertTrue(null==alg.first);
		assertTrue(null==alg.last);

		checkList(alg);
	}

	@Test
	public void isEmpty() {
		LinkedList alg = new LinkedList();

		assertTrue(alg.isEmpty());
		alg.pushHead(1);
		assertFalse(alg.isEmpty());
		alg.reset();
		assertTrue(alg.isEmpty());
	}

	@Test
	public void getElement() {
		LinkedList alg = new LinkedList();

		LinkedList.Element e0 = alg.pushTail(1);
		LinkedList.Element e1 = alg.pushTail(2);
		LinkedList.Element e2 = alg.pushTail(2);

		assertTrue(e0 == alg.getElement(0,true));
		assertTrue(e1 == alg.getElement(1,true));
		assertTrue(e2 == alg.getElement(2,true));

		assertTrue(e2 == alg.getElement(0,false));
		assertTrue(e1 == alg.getElement(1,false));
		assertTrue(e0 == alg.getElement(2,false));
	}

	@Test
	public void pushHead() {

		LinkedList alg = new LinkedList();

		LinkedList.Element e0 = alg.pushHead(1);
		assertEquals(1,alg.size);
		checkList(alg);
		LinkedList.Element e1 = alg.pushHead(2);
		assertEquals(2,alg.size);
		assertTrue(e1 == alg.first);
		assertTrue(e0 == alg.last);
		checkList(alg);

		LinkedList.Element e2 = alg.pushHead(3);
		assertEquals(3,alg.size);
		assertTrue(e2 == alg.first);
		assertTrue(e0 == alg.last);
		checkList(alg);
	}

	@Test
	public void pushTail() {
		LinkedList alg = new LinkedList();

		LinkedList.Element e0 = alg.pushTail(1);
		assertEquals(1,alg.size);
		checkList(alg);

		LinkedList.Element e1 = alg.pushTail(2);
		assertEquals(2,alg.size);
		assertTrue(e0 == alg.first);
		assertTrue(e1 == alg.last);
		checkList(alg);

		LinkedList.Element e2 = alg.pushTail(3);
		assertEquals(3,alg.size);
		assertTrue(e0 == alg.first);
		assertTrue(e2 == alg.last);
		checkList(alg);
	}

	@Test
	public void insertAfter() {
		LinkedList alg = new LinkedList();

		LinkedList.Element e0 = alg.pushHead(1);
		LinkedList.Element e1 = alg.insertAfter(e0, 2);
		assertTrue(e0.next==e1);
		assertTrue(e1.previous==e0);
		assertEquals(2,alg.size);
		checkList(alg);

		LinkedList.Element e2 = alg.insertAfter(e1, 2);
		assertTrue(e1.next==e2);
		assertTrue(e2.previous==e1);
		assertTrue(e2 == alg.last);
		assertEquals(3,alg.size);
		checkList(alg);
	}

	@Test
	public void insertBefore() {
		LinkedList alg = new LinkedList();

		LinkedList.Element e0 = alg.pushHead(1);
		LinkedList.Element e1 = alg.insertBefore(e0, 2);
		assertTrue(e0.previous==e1);
		assertTrue(e1.next==e0);
		assertEquals(2,alg.size);
		checkList(alg);

		LinkedList.Element e2 = alg.insertBefore(e1, 2);
		assertTrue(e1.previous==e2);
		assertTrue(e2.next==e1);
		assertTrue(e2 == alg.first);
		assertEquals(3,alg.size);
		checkList(alg);
	}

	@Test
	public void swap() {
		LinkedList alg = new LinkedList();

		LinkedList.Element e0 = alg.pushTail(1);
		LinkedList.Element e1 = alg.pushTail(2);

		alg.swap(e0,e1);
		assertTrue(alg.first == e1);
		assertTrue(alg.last  == e0);
		checkList(alg);
		alg.swap(e0,e1);
		assertTrue(alg.first == e0);
		assertTrue(alg.last  == e1);
		checkList(alg);

		LinkedList.Element e2 = alg.pushTail(3);
		alg.swap(e0,e1);
		assertTrue(alg.first == e1);
		assertTrue(alg.last  == e2);
		checkList(alg);
		alg.swap(e0,e1);
		assertTrue(alg.first == e0);
		checkList(alg);
		alg.swap(e1,e2);
		assertTrue(alg.last  == e1);
		checkList(alg);
		alg.swap(e1,e2);
		assertTrue(alg.last  == e2);
		checkList(alg);

		LinkedList.Element e3 = alg.pushTail(4);
		alg.swap(e0,e2);
		assertTrue(alg.first == e2);
		assertTrue(alg.last  == e3);
		checkList(alg);
		alg.swap(e0,e2);
		checkList(alg);
		assertTrue(alg.first == e0);
		assertTrue(alg.last  == e3);

	}

	@Test
	public void remove() {
		LinkedList alg = new LinkedList();

		LinkedList.Element e0,e1,e2;

		e0 = alg.pushTail(1);
		alg.remove(e0);
		assertEquals(0,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		e0 = alg.pushTail(1);
		e1 = alg.pushTail(2);
		alg.remove(e1);
		assertTrue(e0==alg.first);
		assertEquals(1,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		e1 = alg.pushTail(2);
		alg.remove(e0);
		assertTrue(e1==alg.first);
		assertEquals(1,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		e0 = alg.pushHead(1);
		e2 = alg.pushTail(3);
		alg.remove(e1);
		assertTrue(e0==alg.first);
		assertEquals(2,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);
	}

	@Test
	public void removeHead() {
		LinkedList alg = new LinkedList();

		alg.pushHead(1);
		assertTrue(alg.removeHead().equals(1));
		assertEquals(0,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		alg.pushTail(1);
		alg.pushTail(2);
		assertTrue(alg.removeHead().equals(1));
		assertEquals(1,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);
	}

	@Test
	public void removeTail() {
		LinkedList alg = new LinkedList();

		alg.pushHead(1);
		assertTrue(alg.removeTail().equals(1));
		assertEquals(0,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);

		alg.pushTail(1);
		alg.pushTail(2);
		assertTrue(alg.removeTail().equals(2));
		assertEquals(1,alg.size);
		assertEquals(1,alg.available.size());
		checkList(alg);
	}

	@Test
	public void find() {
		LinkedList alg = new LinkedList();

		assertTrue(null==alg.find(1));

		LinkedList.Element e1 = alg.pushHead(1);
		assertTrue(null==alg.find(4));
		assertTrue(e1==alg.find(e1.object));

		LinkedList.Element e2 = alg.pushHead(2);
		assertTrue(null==alg.find(4));
		assertTrue(e2==alg.find(e2.object));
		assertTrue(e1==alg.find(e1.object));
	}

	@Test
	public void getHead() {
		LinkedList alg = new LinkedList();

		assertTrue(null==alg.getHead());
		LinkedList.Element e = alg.pushHead(1);
		assertTrue(e==alg.getHead());

	}

	@Test
	public void getTail() {
		LinkedList alg = new LinkedList();

		assertTrue(null==alg.getHead());
		LinkedList.Element e = alg.pushHead(1);
		assertTrue(e == alg.getTail());
		e = alg.pushTail(2);
		assertTrue(e == alg.getTail());
	}

	@Test
	public void addAll_List() {
		LinkedList alg = new LinkedList();

		List<Integer> list0 = new ArrayList<Integer>();
		List<Integer> list1 = new ArrayList<Integer>();


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
		assertTrue(Integer.valueOf(1)==alg.getHead().object);
		assertTrue(Integer.valueOf(3)==alg.getTail().object);

	}

	@Test
	public void addAll_array() {
		LinkedList alg = new LinkedList();

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
		assertTrue(Integer.valueOf(1)==alg.getHead().object);
		checkList(alg);

		alg.addAll(array0,0,3);
		assertEquals(4,alg.size);
		assertTrue(Integer.valueOf(1)==alg.getHead().object);
		assertTrue(Integer.valueOf(0)==alg.getElement(1, true).object);
		assertTrue(Integer.valueOf(1)==alg.getElement(2, true).object);
		assertTrue(Integer.valueOf(2)==alg.getElement(3, true).object);
		checkList(alg);
	}

	@Test
	public void requestNew() {
		LinkedList alg = new LinkedList();

		LinkedList.Element e0 = alg.requestNew();
		assertTrue(e0 != null);

		LinkedList.Element e1 = alg.requestNew();
		assertTrue(e1 != null);
		assertTrue(e1 != e0);

		alg.available.add(e0);
		LinkedList.Element e2 = alg.requestNew();
		assertTrue(e2 == e0);

		LinkedList.Element e3 = alg.requestNew();
		assertTrue(e3 != null);
		assertTrue(e3 != e0);
		assertTrue(e3 != e1);
		assertTrue(e3 != e2);
	}

	/**
	 * Performs checks on the lists preconditions
	 */
	protected void checkList( LinkedList list ) {
		for (int i = 0; i < list.available.size(); i++) {
			LinkedList.Element e = (LinkedList.Element)list.available.get(i);
			assertTrue(null==e.previous);
			assertTrue(null==e.next);
			assertTrue(null==e.object);
		}

		if( list.size == 0 ) {
			assertTrue(null==list.first);
			assertTrue(null==list.last);
		} else {
			List<LinkedList.Element> forwards = new ArrayList<LinkedList.Element>();
			List<LinkedList.Element> backwards = new ArrayList<LinkedList.Element>();

			LinkedList.Element e = list.first;
			while( e != null ) {
				forwards.add(e);
				e = e.next;
				if( forwards.size() > list.size() )
					fail("too many elements in forward direction");
			}

			e = list.last;
			while( e != null ) {
				backwards.add(e);
				e = e.previous;
				if( backwards.size() > list.size() )
					fail("too many elements in forward direction");
			}

			assertEquals(forwards.size(),backwards.size());
			assertEquals(forwards.size(),list.size());

			for (int i = 0; i < forwards.size(); i++) {
				int j = forwards.size()-1-i;
				assertTrue(forwards.get(i)==backwards.get(j));
			}
		}

	}
}