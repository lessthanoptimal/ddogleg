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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestGrowQueue_F64 extends ChecksGrowQueue<GrowQueue_F64> {

	@Test
	public void addAll_queue() {
		GrowQueue_F64 queue0 = new GrowQueue_F64(2);
		GrowQueue_F64 queue1 = new GrowQueue_F64(3);

		queue0.add(1);
		queue0.add(2);

		queue1.add(3);
		queue1.add(4);
		queue1.add(5);

		assertEquals(2,queue0.size);
		queue0.addAll(queue1);
		assertEquals(5,queue0.size);
		for( int i = 0; i < queue0.size; i++ ) {
			assertEquals(queue0.get(i),i+1,1e-8);
		}

		queue0.reset();
		queue0.addAll(queue1);
		assertEquals(3,queue0.size);
		for( int i = 0; i < queue0.size; i++ ) {
			assertEquals(queue0.get(i),i+3,1e-8);
		}
	}

	@Test
	public void addAll_array() {
		GrowQueue_F64 queue0 = new GrowQueue_F64(2);
		double[] array = new double[]{3,4,5};

		queue0.add(1);
		queue0.add(2);

		assertEquals(2,queue0.size);
		queue0.addAll(array,0,3);
		assertEquals(5,queue0.size);
		for( int i = 0; i < queue0.size; i++ ) {
			assertEquals(queue0.get(i),i+1,1e-8);
		}

		queue0.reset();
		queue0.addAll(array,1,3);
		assertEquals(2,queue0.size);
		for( int i = 0; i < queue0.size; i++ ) {
			assertEquals(queue0.get(i),i+4,1e-8);
		}
	}

	@Test
	public void auto_grow() {
		GrowQueue_F64 alg = new GrowQueue_F64(3);

		assertEquals(3,alg.data.length);

		for( int i = 0; i < 10; i++ )
			alg.push(i);

		assertEquals(10,alg.size);

		for( int i = 0; i < 10; i++ )
			assertEquals(i,alg.get(i),1e-8);
	}

	@Test
	public void reset() {
		GrowQueue_F64 alg = new GrowQueue_F64(10);

		alg.push(1);
		alg.push(3);
		alg.push(-2);

		assertTrue(1.0 == alg.get(0));
		assertEquals(3,alg.size);

		alg.reset();

		assertEquals(0,alg.size);
	}

	@Test
	public void push_pop() {
		GrowQueue_F64 alg = new GrowQueue_F64(10);

		alg.push(1);
		alg.push(3);

		assertEquals(2,alg.size);
		assertTrue(3==alg.pop());
		assertTrue(1==alg.pop());
		assertEquals(0, alg.size);
	}

	@Test
	public void remove() {

		GrowQueue_F64 alg = new GrowQueue_F64(10);

		alg.push(1);
		alg.push(3);
		alg.push(4);
		alg.push(5);

		alg.remove(1);
		assertEquals(3,alg.size);
		assertEquals(1,alg.get(0),1e-8);
		assertEquals(4,alg.get(1),1e-8);
		assertEquals(5,alg.get(2),1e-8);
	}

	@Override
	public GrowQueue_F64 declare(int maxsize) {
		return new GrowQueue_F64(maxsize);
	}

	@Override
	public void push(GrowQueue_F64 queue, double value) {
		queue.push(value);
	}

	@Override
	public void insert(GrowQueue_F64 queue, int index, double value) {
		queue.insert(index,value);
	}

	@Override
	public void check(GrowQueue_F64 queue, int index, double value) {
		assertEquals(value,queue.get(index),1e-8);
	}

	@Test
	public void indexOf() {
		GrowQueue_F64 alg = new GrowQueue_F64(10);

		alg.push(1);
		alg.push(3);
		alg.push(4);
		alg.push(5);

		assertEquals(1,alg.indexOf(3));
		assertEquals(-1,alg.indexOf(8));
	}

	@Test
	public void sort() {
		GrowQueue_F64 alg = new GrowQueue_F64(6);

		alg.push(8);
		alg.push(2);
		alg.push(4);
		alg.push(3);

		alg.sort();

		assertEquals(4,alg.size);
		assertEquals(2,alg.get(0),1e-8);
		assertEquals(3,alg.get(1),1e-8);
		assertEquals(4,alg.get(2),1e-8);
		assertEquals(8,alg.get(3),1e-8);
	}

	@Test
	public void getFraction() {
		GrowQueue_F64 alg = new GrowQueue_F64(20);

		for (int i = 0; i < 20; i++) {
			alg.add(i);
		}

		assertEquals(0,alg.getFraction(0.0), UtilEjml.TEST_F64);
		assertEquals(0,alg.getFraction(0.02), UtilEjml.TEST_F64);
		assertEquals(0,alg.getFraction(0.03), UtilEjml.TEST_F64);
		assertEquals(1,alg.getFraction(1.0/19.0), UtilEjml.TEST_F64);
		assertEquals(1,alg.getFraction(1.7/19.0), UtilEjml.TEST_F64);
		assertEquals(19/2,alg.getFraction(0.5), UtilEjml.TEST_F64);
		assertEquals(19,alg.getFraction(1.0), UtilEjml.TEST_F64);
	}
}
