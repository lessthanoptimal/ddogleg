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

package org.ddogleg.clustering.misc;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Peter Abeles
 */
class TestListAccessor {
	/**
	 * A few simple tests combined into one function
	 */
	@Test void various() {
		var list = new ArrayList<Foo>();
		list.add(new Foo(1));
		list.add(new Foo(-1));
		var alg = new ListAccessor<>(list, (a,b)->a.a=b.a, Foo.class);

		assertEquals(2, alg.size());

		var tmp = new Foo(10);
		for (int i = 0; i < 2; i++) {
			assertSame(list.get(i), alg.getTemp(i));

			alg.getCopy(i, tmp);
			assertEquals(list.get(i).a, tmp.a);
		}

		tmp.a = 999;
		alg.copy(list.get(0), tmp);
		assertEquals(list.get(0).a, tmp.a);
	}

	public static class Foo {
		public int a;
		public Foo(int a) {
			this.a = a;
		}
	}
}