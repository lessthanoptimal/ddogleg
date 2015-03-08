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

package org.ddogleg.clustering;

import org.ddogleg.clustering.kmeans.TestStandardKMeans_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class GenericClusterChecks_F64 {
	public abstract ComputeClusters<double[]> createClustersAlg();

	/**
	 * Very simple and obvious clustering problem
	 */
	@Test
	public void simpleCluster() {
		List<double[]> points = new ArrayList<double[]>();

		for (int i = 0; i < 20; i++) {
			points.add( new double[]{    i});
			points.add( new double[]{100+i});
			points.add( new double[]{200+i});
		}

		ComputeClusters<double[]> alg = createClustersAlg();

		alg.init(1,243234);

		alg.process(points,3);

		AssignCluster<double[]> ass = alg.getAssignment();

		// test assignment
		int cluster0 = ass.assign(points.get(0));
		int cluster1 = ass.assign(points.get(1));
		int cluster2 = ass.assign(points.get(2));

		// make sure the clusters are unique
		assertTrue(cluster0!=cluster1);
		assertTrue(cluster0!=cluster2);
		assertTrue(cluster1!=cluster2);

		// see if it correctly assigns the inputs
		int index = 0;
		for (int i = 0; i < 20; i++) {
			assertEquals(cluster0,ass.assign(points.get(index++)),1e-8);
			assertEquals(cluster1,ass.assign(points.get(index++)),1e-8);
			assertEquals(cluster2,ass.assign(points.get(index++)),1e-8);
		}
	}

	@Test
	public void computeDistance() {
		int DOF = 5;

		List<double[]> points = TestStandardKMeans_F64.createPoints(DOF,200,true);

		ComputeClusters<double[]> alg = createClustersAlg();

		alg.init(DOF,243234);

		alg.process(points,3);
		double first = alg.getDistanceMeasure();
		alg.process(points,10);
		double second = alg.getDistanceMeasure();

		// since there are more clusters the distance should be shorter
		assertTrue(second<first);
	}
}
