/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.clustering.kmeans;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestInitializePlusPlus extends StandardInitializeKMeansChecks{

	Random rand = new Random(234);

	/**
	 * In this situation there are not enough unique points which can act as unique seeds.
	 *
	 * This is a stricter version of generic test
	 */
	@Test
	public void notEnoughUniquePoints_strict() {
		int DOF = 20;

		List<double[]> points = TestStandardKMeans_F64.createPoints(DOF,30,true);
		for (int i = 1; i < points.size(); i += 2) {
			System.arraycopy(points.get(i-1),0,points.get(i),0,DOF);
		}
		List<double[]> seeds = TestStandardKMeans_F64.createPoints(DOF,20,false);

		InitializeKMeans_F64 alg = createAlg();
		alg.init(DOF,0xBEEF);

		alg.selectSeeds(points, seeds);

		int hits[] = new int[15];
		for( double[] a : seeds ) {
			int match = findMatch( a , points )/2;
			hits[match]++;
		}

		// make sure each one was selected at least once
		for (int i = 0; i < hits.length; i++) {
			assertTrue(hits[i] > 0);
		}
	}

	/**
	 * Test seed selection by seeing if it has the expected distribution.
	 */
	@Test
	public void selectNextSeed() {
		InitializePlusPlus alg = new InitializePlusPlus();
		alg.init(1,123);

		alg.distance.resize(3);
		alg.distance.data = new double[]{3,6,1};
		alg.totalDistance = 10.0;

		List<double[]> points = new ArrayList<double[]>();
		for (int i = 0; i < 3; i++) {
			points.add(new double[1]);
		}

		double histogram[] = new double[3];

		for (int i = 0; i < 1000; i++) {
			double[] seed = alg.selectNextSeed(points,rand.nextDouble());
			int which = -1;
			for (int j = 0; j < points.size(); j++) {
				if( points.get(j) == seed ) {
					which = j;
					break;
				}
			}
			histogram[which]++;
		}
		assertEquals(0.3,histogram[0]/1000.0,0.02);
		assertEquals(0.6,histogram[1]/1000.0,0.02);
		assertEquals(0.1,histogram[2]/1000.0,0.02);
	}

	@Test
	public void updateDistances() {
		InitializePlusPlus alg = new InitializePlusPlus();
		alg.init(1,123);

		alg.distance.resize(3);
		alg.distance.data = new double[]{3,6,1};
		List<double[]> points = new ArrayList<double[]>();
		for (int i = 0; i < 3; i++) {
			points.add(new double[]{i*i});
		}
		alg.updateDistances(points, new double[]{-1});
		assertEquals(1,alg.distance.get(0),1e-8);
		assertEquals(4,alg.distance.get(1),1e-8);
		assertEquals(1,alg.distance.get(2),1e-8);
		assertEquals(6,alg.totalDistance,1e-8);

	}

	@Override
	public InitializeKMeans_F64 createAlg() {
		return new InitializePlusPlus();
	}
}