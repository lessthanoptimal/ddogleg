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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestGrowQueue_I8 extends ChecksGrowQueue<GrowQueue_I8> {

	@Test
	void count() {
		GrowQueue_I8 alg = GrowQueue_I8.array(0,0,1,1,1);

		assertEquals(2,alg.count(0));
		assertEquals(3,alg.count(1));
	}

	@Test
	void addAll_queue() {
		GrowQueue_I8 queue0 = new GrowQueue_I8(2);
		GrowQueue_I8 queue1 = new GrowQueue_I8(3);

		queue0.add(1);
		queue0.add(2);

		queue1.add(3);
		queue1.add(4);
		queue1.add(5);

		assertEquals(2,queue0.size);
		queue0.addAll(queue1);
		assertEquals(5,queue0.size);
		for( int i = 0; i < queue0.size; i++ ) {
			assertEquals(queue0.get(i),i+1);
		}

		queue0.reset();
		queue0.addAll(queue1);
		assertEquals(3,queue0.size);
		for( int i = 0; i < queue0.size; i++ ) {
			assertEquals(queue0.get(i),i+3);
		}
	}

	@Test
	void addAll_array() {
		GrowQueue_I8 queue0 = new GrowQueue_I8(2);
		byte[] array = new byte[]{3,4,5};

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
	void auto_grow() {
		GrowQueue_I8 alg = new GrowQueue_I8(3);

		assertEquals(3,alg.data.length);

		for( int i = 0; i < 10; i++ )
			alg.push(i);

		assertEquals(10,alg.size);

		for( int i = 0; i < 10; i++ )
			assertEquals(i,alg.get(i),1e-8);
	}

	@Test
	void reset() {
		GrowQueue_I8 alg = new GrowQueue_I8(10);

		alg.push(1);
		alg.push(3);
		alg.push(-2);

		assertEquals(1.0, alg.get(0));
		assertEquals(3,alg.size);

		alg.reset();

		assertEquals(0,alg.size);
	}

	@Test
	void resize() {
		GrowQueue_I8 alg = new GrowQueue_I8(2);
		assertEquals(0,alg.size);
		alg.resize(12);
		assertTrue(alg.data.length >= 12);
		assertEquals(12,alg.size);
		// Make sure it doesn't declare a new array since it doesn't have to
		alg.data[2] = 5;
		alg.resize(10);
		assertTrue(alg.data.length >= 10);
		assertEquals(10,alg.size);
		assertEquals(5,alg.get(2));
	}

	@Test
	void resize_default() {
		GrowQueue_I8 alg = new GrowQueue_I8(2);
		assertEquals(0,alg.size);
		alg.resize(12, (byte)1);
		assertTrue(alg.data.length >= 12);
		assertEquals(12,alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertEquals(1,alg.get(i));
		}
		// The array isn't redeclared but the value should still change
		alg.resize(10,(byte)2);
		assertTrue(alg.data.length >= 10);
		assertEquals(10,alg.size);
		for (int i = 0; i < alg.size; i++) {
			assertEquals(2,alg.get(i));
		}
		// it shouldn't change the entire array's value since that's wasteful
		for (int i = alg.size; i < alg.data.length; i++) {
			assertEquals(1,alg.data[i]);
		}
	}

	@Test
	void push_pop() {
		GrowQueue_I8 alg = new GrowQueue_I8(10);

		alg.push(1);
		alg.push(3);

		assertEquals(2,alg.size);
		assertEquals(3, alg.pop());
		assertEquals(1, alg.pop());
		assertEquals(0, alg.size);
	}

	@Test
	void setTo_array() {
		GrowQueue_I8 alg = new GrowQueue_I8(10);

		byte[] foo = new byte[]{1,3,4,5,7};
		alg.setTo(foo,1,3);
		assertEquals(3,alg.size);
		for (int i = 0; i < 3; i++) {
			assertEquals(alg.get(i),foo[i+1]);
		}
	}

	@Test
	void remove_two() {
		GrowQueue_I8 alg = new GrowQueue_I8(10);

		alg.push(1);
		alg.push(3);
		alg.push(4);
		alg.push(5);
		alg.push(6);

		alg.remove(1,1);
		assertEquals(4,alg.size);
		assertEquals(1,alg.get(0));
		assertEquals(4,alg.get(1));
		assertEquals(5,alg.get(2));
		assertEquals(6,alg.get(3));
		alg.remove(0,1);
		assertEquals(2,alg.size);
		assertEquals(5,alg.get(0));
		assertEquals(6,alg.get(1));
	}

	@Override
	public GrowQueue_I8 declare(int maxsize) {
		return new GrowQueue_I8(maxsize);
	}

	@Override
	public void push(GrowQueue_I8 queue, double value) {
		queue.push((int)value);
	}

	@Override
	public void insert(GrowQueue_I8 queue, int index, double value) {
		queue.insert(index,(int)value);
	}

	@Override
	public void check(GrowQueue_I8 queue, int index, double value) {
		assertEquals((int)value,queue.get(index));
	}

	@Test
	void indexOf() {
		GrowQueue_I8 alg = new GrowQueue_I8(10);

		alg.push(1);
		alg.push(3);
		alg.push(4);
		alg.push(5);

		assertEquals(1,alg.indexOf((byte)3));
		assertEquals(-1,alg.indexOf((byte)8));
	}

	@Test
	void sort() {
		GrowQueue_I8 alg = new GrowQueue_I8(6);

		alg.push(8);
		alg.push(2);
		alg.push(4);
		alg.push(3);
		alg.push(-1);

		alg.sort();

		assertEquals(5,alg.size);
		assertEquals(-1,alg.get(0));
		assertEquals(2,alg.get(1));
		assertEquals(3,alg.get(2));
		assertEquals(4,alg.get(3));
		assertEquals(8,alg.get(4));
	}

	@Test
	void getFraction() {
		GrowQueue_I8 alg = new GrowQueue_I8(20);

		for (int i = 0; i < 20; i++) {
			alg.add(i);
		}

		assertEquals(0,alg.getFraction(0.0));
		assertEquals(0,alg.getFraction(0.02));
		assertEquals(0,alg.getFraction(0.03));
		assertEquals(1,alg.getFraction(1.0/19.0));
		assertEquals(1,alg.getFraction(1.7/19.0));
		assertEquals(19/2,alg.getFraction(0.5));
		assertEquals(19,alg.getFraction(1.0));
	}

	@Test
	void getTail() {
		GrowQueue_I8 alg = new GrowQueue_I8(20);

		for (int i = 0; i < 20; i++) {
			alg.add(i);
		}

		for (int i = 0; i < 20; i++) {
			assertEquals(20-i-1,alg.getTail(i));
		}
	}
}
