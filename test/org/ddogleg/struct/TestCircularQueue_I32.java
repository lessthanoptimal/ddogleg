package org.ddogleg.struct;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestCircularQueue_I32 {

	@Test
	public void popHead() {
		CircularQueue_I32 alg = new CircularQueue_I32();

		alg.add(1);
		alg.add(2);
		assertEquals(1,alg.popHead());
		assertEquals(1,alg.size());

		assertEquals(2,alg.popHead());
		assertEquals(0,alg.size());
	}

	@Test
	public void popTail() {
		CircularQueue_I32 alg = new CircularQueue_I32();

		alg.add(1);
		alg.add(2);
		assertEquals(2,alg.popTail());
		assertEquals(1,alg.size());

		assertEquals(1,alg.popTail());
		assertEquals(0, alg.size());
	}

	@Test
	public void head() {
		CircularQueue_I32 alg = new CircularQueue_I32();

		alg.add(1);
		assertEquals(1, alg.head());
		alg.add(3);
		assertEquals(1,alg.head());
	}

	@Test
	public void head_offset() {
		CircularQueue_I32 alg = new CircularQueue_I32(3);

		alg.start = 2;
		alg.size = 0;

		alg.add(1);
		assertEquals(1, alg.head());
		alg.add(3);
		assertEquals(1,alg.head());
	}

	@Test
	public void tail() {
		CircularQueue_I32 alg = new CircularQueue_I32();

		alg.add(1);
		assertEquals(1,alg.tail());
		alg.add(3);
		assertEquals(3, alg.tail());
	}

	@Test
	public void tail_offset() {
		CircularQueue_I32 alg = new CircularQueue_I32(3);

		alg.start = 2;
		alg.size = 0;

		alg.add(1);
		assertEquals(1,alg.tail());
		alg.add(3);
		assertEquals(3, alg.tail());
	}

	@Test
	public void removeHead() {
		CircularQueue_I32 alg = new CircularQueue_I32();

		alg.add(1);
		alg.add(2);
		alg.removeHead();
		assertEquals(2, alg.head());
		assertEquals(1, alg.size());

		alg.removeHead();
		assertEquals(0, alg.size());
	}

	@Test
	public void removeTail() {
		CircularQueue_I32 alg = new CircularQueue_I32();

		alg.add(1);
		alg.add(2);
		alg.removeTail();
		assertEquals(1,alg.head());
		assertEquals(1,alg.size());

		alg.removeTail();
		assertEquals(0, alg.size());
	}

	@Test
	public void get() {
		CircularQueue_I32 alg = new CircularQueue_I32(2);
		assertEquals(2,alg.data.length);

		// easy case
		alg.add(1);
		alg.add(2);

		assertEquals(1,alg.get(0));
		assertEquals(2,alg.get(1));

		// make there be an offset
		alg.removeHead();
		alg.add(3);
		assertEquals(2,alg.data.length); // sanity check
		assertEquals(2,alg.get(0));
		assertEquals(3,alg.get(1));
	}

	@Test
	public void add() {
		CircularQueue_I32 alg = new CircularQueue_I32(3);
		assertEquals(3,alg.data.length);

		alg.add(1);
		assertEquals(1,alg.data[0]);
		assertEquals(1,alg.size);

		alg.add(2);
		assertEquals(1,alg.data[0]);
		assertEquals(2,alg.data[1]);
		assertEquals(2,alg.size);

		// see if it grows
		alg.add(3);
		alg.add(4);
		assertEquals(1,alg.data[0]);
		assertEquals(2,alg.data[1]);
		assertEquals(3,alg.data[2]);
		assertEquals(4,alg.data[3]);
		assertEquals(4,alg.size);

		// wrap around case
		alg.start = 1;
		alg.size = 2;
		alg.data = new int[3];
		alg.add(10);
		assertEquals(10,alg.data[0]);
		assertEquals(3,alg.size);

	}

	@Test
	public void addW() {
		CircularQueue_I32 alg = new CircularQueue_I32(3);
		assertEquals(3,alg.data.length);

		alg.addW(1);
		assertEquals(1,alg.data[0]);
		assertEquals(1,alg.size);

		alg.addW(2);
		assertEquals(1,alg.data[0]);
		assertEquals(2,alg.data[1]);
		assertEquals(2,alg.size);

		// see if it over writes
		alg.addW(3);
		alg.addW(4);
		assertEquals(4,alg.data[0]);
		assertEquals(2,alg.data[1]);
		assertEquals(3,alg.data[2]);
		assertEquals(3,alg.size);
		assertEquals(1,alg.start);

		// wrap around case
		alg.start = 1;
		alg.size = 2;
		alg.data = new int[3];
		alg.addW(10);
		assertEquals(10,alg.data[0]);
		assertEquals(3,alg.size);
	}

	@Test
	public void isEmpty() {
		CircularQueue_I32 alg = new CircularQueue_I32(3);

		assertTrue(alg.isEmpty());
		alg.add(5);
		assertFalse(alg.isEmpty());
		alg.removeTail();
		assertTrue(alg.isEmpty());

	}

	@Test
	public void reset() {
		CircularQueue_I32 alg = new CircularQueue_I32(3);

		alg.start = 2;
		alg.size = 5;

		alg.reset();

		assertEquals(0,alg.size);
		assertEquals(0,alg.start);
	}


}
