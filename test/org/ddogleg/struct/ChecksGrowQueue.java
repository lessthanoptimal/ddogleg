/*
 * Copyright (c) 2012-2017, Peter Abeles. All Rights Reserved.
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

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public abstract class ChecksGrowQueue<T extends GrowQueue<T>> {

	public abstract T declare( int maxsize );

	public abstract void push( T queue , double value );
	public abstract void insert( T queue , int index, double value );
	public abstract void check( T queue , int index , double value );


	@Test
	public void insert() {
		T alg = declare(10);

		// insert with no array resize
		push(alg, 1);
		push(alg, 3);
		push(alg, 4);
		push(alg, 5);

		insert(alg, 2, 6);

		assertEquals(5,alg.size());
		check(alg,0,1);
		check(alg,1,3);
		check(alg,2,6);
		check(alg,3,4);
		check(alg,4,5);

		// insert with array resize

		alg = declare(4);
		push(alg, 1);
		push(alg, 3);
		push(alg, 4);
		push(alg, 5);

		insert(alg, 2, 6);

		assertEquals(5,alg.size());
		check(alg,0,1);
		check(alg,1,3);
		check(alg,2,6);
		check(alg,3,4);
		check(alg,4,5);
	}

	@Test
	public void extend() {
		T alg = declare(2);
		push(alg, 1);
		push(alg, 3);

		alg.extend(4);
		check(alg,0,1);
		check(alg,1,3);
		assertEquals(4,alg.size());
	}
}
