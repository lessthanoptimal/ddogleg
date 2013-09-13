/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

import java.util.List;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public class TestFastQueue {

	@Test
	public void checkDeclareInstance() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(10,DummyData.class,true);

		assertTrue(alg.getMaxSize()>0);
		assertTrue(alg.data[0] != null);

		alg = new FastQueue<DummyData>(10,DummyData.class,false);

		assertTrue(alg.getMaxSize()>0);
		assertTrue(alg.data[0] == null);
	}

	@Test
	public void toList() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(10,DummyData.class,true);

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
	public void removeTail() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(10,DummyData.class,true);

		alg.grow();
		assertEquals(1,alg.size);
		alg.removeTail();
		assertEquals(0,alg.size);
	}

	@Test
	public void getTail() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(10,DummyData.class,true);

		alg.grow();alg.grow();

		assertTrue(alg.data[1] == alg.getTail());
	}

	@Test
	public void get_pop() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(DummyData.class,true);

		// test a failure case
		try {
			alg.get(0);
			fail("Didn't fail");
		} catch( IllegalArgumentException e ) {}

		alg.grow();
		alg.get(0);
	}

	@Test
	public void size() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(DummyData.class,true);
		assertEquals(0,alg.size);
		alg.grow();
		assertEquals(1,alg.size);
	}

	@Test
	public void add() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(DummyData.class,false);

		DummyData a = new DummyData();
		alg.add(a);
		assertTrue(a==alg.data[0]);
	}

	@Test
	public void addAll() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(DummyData.class,true);
		alg.grow();
		alg.grow();

		FastQueue<DummyData> alg2 = new FastQueue<DummyData>(DummyData.class,false);

		alg2.addAll(alg);

		assertTrue(alg.get(0) == alg2.get(0));
		assertTrue(alg.get(1) == alg2.get(1));
	}

	/**
	 * Checks to see if pop automatically grows correctly
	 */
	@Test
	public void pop_grow() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(1,DummyData.class,true);

		int before = alg.getMaxSize();
		for( int i = 0; i < 20; i++ ) {
			alg.grow();
		}
		alg.get(19);
		int after = alg.getMaxSize();
		assertTrue(after>before);
	}

	@Test
	public void growArray() {
		FastQueue<DummyData> alg = new FastQueue<DummyData>(1,DummyData.class,true);

		alg.grow().value = 10;
		int before = alg.getMaxSize();
		alg.growArray(before+5);
		assertEquals(10,alg.get(0).value);
	}
}
