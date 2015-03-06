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

import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.clustering.GenericClusterChecks_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestStandardKMeans_F64 extends GenericClusterChecks_F64 {


	@Test
	public void matchPointsToClusters() {
		fail("implement");
	}

	@Test
	public void updateClusterCenters() {
		fail("implement");
	}

	@Test
	public void distanceSq() {
		fail("implement");
	}

	@Test
	public void computeDistance() {
		fail("implement");
	}

	public static List<double[]> createPoints( int DOF , int total , boolean fillRandom ) {
		List<double[]> ret = new ArrayList<double[]>();

		Random random = new Random(23432+DOF+total);

		for (int i = 0; i < total; i++) {
			double[] a = new double[DOF];
			if( fillRandom ) {
				for (int j = 0; j < a.length; j++) {
					a[j] = random.nextGaussian();
				}
			}

			ret.add(a);
		}

		return ret;
	}

	@Override
	public ComputeClusters<double[]> createClustersAlg() {
		return new StandardKMeans_F64();
	}
}
