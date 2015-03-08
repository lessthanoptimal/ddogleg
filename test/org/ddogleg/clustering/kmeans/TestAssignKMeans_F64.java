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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestAssignKMeans_F64 {

	@Test
	public void assign() {

		List<double[]> clusters = new ArrayList<double[]>();

		clusters.add( new double[]{10,0,0});
		clusters.add( new double[]{0,0,10});

		AssignKMeans_F64 alg = new AssignKMeans_F64(clusters);

		assertEquals(1,alg.assign(new double[]{0,0,9}));
		assertEquals(0,alg.assign(new double[]{12,0,0}));
	}

	@Test
	public void copy() {
		List<double[]> clusters = new ArrayList<double[]>();

		clusters.add( new double[]{10,0,0});
		clusters.add( new double[]{0,0,10});

		AssignKMeans_F64 original = new AssignKMeans_F64(clusters);
		AssignKMeans_F64 copy = (AssignKMeans_F64)original.copy();

		assertEquals(original.clusters.size(),copy.clusters.size());

		for (int i = 0; i < original.clusters.size(); i++) {
			double[] o = original.clusters.get(i);
			double[] c = copy.clusters.get(i);

			assertTrue(o!=c);

			for (int j = 0; j < o.length; j++) {
				assertEquals(o[j],c[j],1e-8);
			}
		}

	}
}
