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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
class TestFastArray {
	@Test
	void toList() {
		FastArray<DummyData> alg = new FastArray<>(DummyData.class,10);

		List<DummyData> l = alg.toList();
		assertEquals(0,l.size());

		alg.add( new DummyData(1) );
		alg.add( new DummyData(1) );
		alg.add( new DummyData(2) );
		alg.removeTail();

		l = alg.toList();
		assertEquals(2,l.size());
		assertEquals(1,l.get(0).value);
		assertEquals(1,l.get(1).value);
	}

	@Test
	void removeTail() {
		FastArray<DummyData> alg = new FastArray<>(DummyData.class,10);

		alg.add( new DummyData() );
		assertEquals(1,alg.size);
		alg.removeTail();
		assertEquals(0,alg.size);
	}

	@Test
	void remove() {
		FastArray<DummyData> alg = new FastArray<>(DummyData.class,10);

		List<DummyData> l = alg.toList();
		assertEquals(0,l.size());

		alg.add( new DummyData(1) );
		alg.add( new DummyData(2) );
		alg.add( new DummyData(3) );

		alg.remove(1);

		assertEquals(2,alg.size());
		assertEquals(1,alg.get(0).value);
		assertEquals(3,alg.get(1).value);
		// make sure the data was shifted to the end
		assertNull(alg.data[2]);

		alg.remove(1);
		assertEquals(1,alg.size());
		assertEquals(1,alg.get(0).value);
		assertNull(alg.data[1]);
		assertNull(alg.data[2]);

		alg.remove(0);
		assertEquals(0,alg.size());
		assertNull(alg.data[0]);
		assertNull(alg.data[1]);
		assertNull(alg.data[2]);
	}

	@Test
	void removeSwap() {
		FastArray<DummyData> alg = new FastArray<>(DummyData.class,10);

		List<DummyData> l = alg.toList();
		assertEquals(0,l.size());

		alg.add( new DummyData(1) );
		DummyData d = alg.get(0);
		assertSame(d,alg.removeSwap(0));
		assertEquals(0,alg.size());

		alg.add( new DummyData(1) );
		alg.add( new DummyData(2) );
		alg.add( new DummyData(3) );
		alg.add( new DummyData(4) );

		alg.removeSwap(1);

		assertEquals(3,alg.size());
		assertEquals(1,alg.get(0).value);
		assertEquals(4,alg.get(1).value);
		assertEquals(3,alg.get(2).value);
		assertNull(alg.data[3]);
	}

	@Test
	void add() {
		FastArray<DummyData> alg = new FastArray<>(DummyData.class);

		DummyData a = new DummyData();
		alg.add(a);
		assertSame(a, alg.data[0]);
	}

	@Test
	void addAll() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);
		alg.grow();
		alg.grow();

		FastArray<DummyData> alg2 = new FastArray<>(DummyData.class);

		alg2.addAll(alg);

		assertSame(alg.get(0), alg2.get(0));
		assertSame(alg.get(1), alg2.get(1));
	}

	@Test
	void forEach() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);
		alg.grow();
		alg.grow();
		alg.grow();

		alg.forEach((i,o)->o.value=i);

		assertEquals(0,alg.get(0).value);
		assertEquals(1,alg.get(1).value);
		assertEquals(2,alg.get(2).value);
	}
}