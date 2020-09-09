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

/**
 * @author Peter Abeles
 */
public class TestFastAccess {
	@Test
	void forIdx() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);
		alg.grow();
		alg.grow();
		alg.grow();

		alg.forIdx((i, o)->o.value=i);

		assertEquals(0,alg.get(0).value);
		assertEquals(1,alg.get(1).value);
		assertEquals(2,alg.get(2).value);
	}

	@Test
	void forIdx_range() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);
		for (int i = 0; i < 10; i++) {
			alg.grow();
		}

		alg.forIdx(2,5,(i, o)->o.value=i);

		for (int i = 0; i < 10; i++) {
			if( i >= 2 && i < 5 ) {
				assertEquals(i,alg.get(i).value);
			} else {
				assertEquals(0,alg.get(i).value);
			}
		}
	}

	@Test
	void forEach() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);
		alg.grow();
		alg.grow();
		alg.grow();

		alg.forEach((o)->o.value=2);

		assertEquals(2,alg.get(0).value);
		assertEquals(2,alg.get(1).value);
		assertEquals(2,alg.get(2).value);
	}

	@Test
	void forEach_range() {
		FastQueue<DummyData> alg = new FastQueue<>(DummyData::new);
		for (int i = 0; i < 10; i++) {
			alg.grow();
		}

		alg.forEach(2,5,(o)->o.value=3);

		for (int i = 0; i < 10; i++) {
			if( i >= 2 && i < 5 ) {
				assertEquals(3,alg.get(i).value);
			} else {
				assertEquals(0,alg.get(i).value);
			}
		}
	}
}
