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

package org.ddogleg.clustering;

import org.ddogleg.clustering.kmeans.TestStandardKMeans;
import org.ddogleg.clustering.misc.ListAccessor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public abstract class GenericClusterChecks_F64 {

	protected Random rand = new Random(234);

	/**
	 * If hint is true then the first 3 elements are good initial seeds for clustering
	 */
	public abstract ComputeClusters<double[]> createClustersAlg(boolean seedHint, int dof);

	/**
	 * Selects the best cluster using internal model. Inspired by a bug
	 */
	protected abstract int selectBestCluster( ComputeClusters<double[]> alg, double[] p );

	/**
	 * Very simple and obvious clustering problem
	 */
	@Test void simpleCluster() {
		List<double[]> points = new ArrayList<>();
		ListAccessor<double[]> accessor = new ListAccessor<>(points,
				(src,dst)->System.arraycopy(src,0,dst,0,1), double[].class);

		for (int i = 0; i < 20; i++) {
			points.add( new double[]{    i});
			points.add( new double[]{100+i});
			points.add( new double[]{200+i});
		}

		ComputeClusters<double[]> alg = createClustersAlg(true,1);

		alg.initialize(243234);

		alg.process(accessor,3);

		AssignCluster<double[]> assignments = alg.getAssignment();

		// test assignment
		int cluster0 = assignments.assign(points.get(0));
		int cluster1 = assignments.assign(points.get(1));
		int cluster2 = assignments.assign(points.get(2));

		// make sure the clusters are unique
		assertTrue(cluster0!=cluster1);
		assertTrue(cluster0!=cluster2);
		assertTrue(cluster1!=cluster2);

		// see if it correctly assigns the inputs
		int index = 0;
		for (int i = 0; i < 20; i++) {
			assertEquals(cluster0,assignments.assign(points.get(index++)),1e-8);
			assertEquals(cluster1,assignments.assign(points.get(index++)),1e-8);
			assertEquals(cluster2,assignments.assign(points.get(index++)),1e-8);
		}
	}

	@Test void computeDistance() {
		int DOF = 5;

		List<double[]> points = TestStandardKMeans.createPoints(DOF,200,true);
		ListAccessor<double[]> accessor = new ListAccessor<>(points,
				(src,dst)->System.arraycopy(src,0,dst,0,src.length), double[].class);

		ComputeClusters<double[]> alg = createClustersAlg(false,5);

		alg.initialize(243234);

		alg.process(accessor,3);
		double first = alg.getDistanceMeasure();
		alg.process(accessor,10);
		double second = alg.getDistanceMeasure();

		// it's actually difficult to come up with meaningful tests for distance which don't make
		// assumptions about the algorithm.  So there's only these really simple tests
		assertTrue(first!=second);
		assertFalse(Double.isNaN(first));
		assertFalse(Double.isNaN(second));
		assertFalse(Double.isInfinite(first));
		assertFalse(Double.isInfinite(second));
	}

	/**
	 * Make sure the assigner matches the best assignment.
	 */
	@Test void consistentAssignments() {
		ComputeClusters<double[]> alg = createClustersAlg(false,1);

		for (int trial = 0; trial < 5; trial++) {
			List<double[]> points = new ArrayList<>();
			ListAccessor<double[]> accessor = new ListAccessor<>(points,
					(src,dst)->System.arraycopy(src,0,dst,0,1), double[].class);

			for (int i = 0; i < 100; i++) {
				points.add( new double[]{rand.nextDouble()});
			}

			alg.initialize(243234);

			alg.process(accessor, 7);

			AssignCluster<double[]> assigner = alg.getAssignment();

			for (int pointIdx = 0; pointIdx < points.size(); pointIdx++) {
				double[] p = points.get(pointIdx);
				assertEquals(selectBestCluster(alg,p), assigner.assign(p));
			}
		}

	}
}
