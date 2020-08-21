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
public class TestRecycleManagerL {
	@Test
	public void basic() {
		RecycleManagerL<Dummy> manager = new RecycleManagerL<Dummy>(Dummy.class);

		Dummy first = manager.requestInstance();
		Dummy second = manager.requestInstance();
		assertEquals(2,manager.getUsed().size());
		Dummy third = manager.requestInstance();
		assertEquals(3, manager.getUsed().size());
		manager.recycleAll();
		assertEquals(3, manager.getUnused().size());
		assertEquals(0, manager.getUsed().size());

		assertTrue(first != third);
		assertTrue(first != second);

		assertTrue(third == manager.requestInstance()); // just needs to be an element previously returned, really
		assertEquals(2, manager.getUnused().size());
		assertEquals(1, manager.getUsed().size());
	}

	public static class Dummy {

	}
}