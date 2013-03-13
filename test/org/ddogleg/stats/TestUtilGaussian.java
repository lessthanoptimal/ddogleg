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

package org.ddogleg.stats;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestUtilGaussian {

	/**
	 * Test the PDF based on some of its properties
	 */
	@Test
	public void computePDF() {
		double max = UtilGaussian.computePDF(2,1.5,2);

		assertTrue( UtilGaussian.computePDF(2,1.5,1) < max );
		assertTrue( UtilGaussian.computePDF(2,1.5,3) < max );
		assertEquals(UtilGaussian.computePDF(2, 1.5, 1),UtilGaussian.computePDF(2, 1.5, 3),1e-8 );

		assertTrue( UtilGaussian.computePDF(2,2,2) < max );
		assertTrue( UtilGaussian.computePDF(2,1,2) > max );
	}

	@Test
	public void derivative1() {
		fail("Implement");
	}

	@Test
	public void derivative2() {
		fail("Implement");
	}

	@Test
	public void derivative3() {
		fail("Implement");
	}

	@Test
	public void derivative4() {
		fail("Implement");
	}
}
