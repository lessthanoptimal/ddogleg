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

package org.ddogleg.solver.impl;

import org.ddogleg.solver.Polynomial;
import org.ddogleg.solver.PolynomialOps;
import org.ddogleg.solver.PolynomialRoots;
import org.ejml.data.Complex_F64;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestRootFinderCompanion {

	Random rand = new Random(234);

	@Test
	public void basicTest() {
		for( int numCoef = 2; numCoef < 6; numCoef++ ) {
			Polynomial poly = new Polynomial(numCoef);
			for( int i = 0; i < numCoef; i++ ) {
				poly.c[i] = 10*(rand.nextDouble()-0.5);
			}

			PolynomialRoots alg = new RootFinderCompanion();

			assertTrue(alg.process(poly));

			List<Complex_F64> roots = alg.getRoots();

			int numReal = 0;
			for( Complex_F64 c : roots ) {
				if( c.isReal() ) {
					assertEquals(0,poly.evaluate(c.real),1e-8);
					numReal++;
				}
			}

			int expectedRoots = PolynomialOps.countRealRoots(poly);
			assertTrue(numReal == expectedRoots);
		}
	}
}
