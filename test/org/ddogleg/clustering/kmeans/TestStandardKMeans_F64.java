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

import static junit.framework.TestCase.assertEquals;

/**
 *
 * Can;t use generic checks because he seeds might suck and the sets will be poorly chosen.
 *
 * @author Peter Abeles
 */
public class TestStandardKMeans_F64 extends GenericClusterChecks_F64{

	@Test
	public void matchPointsToClusters() {
		StandardKMeans_F64 alg = new StandardKMeans_F64(100,1,new InitializeStandard_F64());

		alg.init(4, 123);

		alg.clusters.resize(3);
		alg.workClusters.resize(3);
		alg.memberCount.resize(3);

		alg.clusters.data[0] = new double[]{20,0,0,0};
		alg.clusters.data[1] = new double[]{0,20,0,0};
		alg.clusters.data[2] = new double[]{0,0,20,0};

		List<double[]> points = new ArrayList<double[]>();
		points.add( new double[]{20,5,0,0});
		points.add( new double[]{25,-4,0,0});
		points.add( new double[]{0,0,22,0});

		alg.matchPointsToClusters(points);

		assertEquals(2,alg.memberCount.get(0));
		assertEquals(0,alg.memberCount.get(1));
		assertEquals(1,alg.memberCount.get(2));

		assertEquals(45,alg.workClusters.data[0][0],1e-8);
		assertEquals(1 ,alg.workClusters.data[0][1],1e-8);
		assertEquals(0 ,alg.workClusters.data[0][2],1e-8);
		assertEquals(0 ,alg.workClusters.data[0][3],1e-8);

		assertEquals(0 ,alg.workClusters.data[1][1],1e-8);

		assertEquals(22,alg.workClusters.data[2][2],1e-8);
	}

	@Test
	public void updateClusterCenters() {

		StandardKMeans_F64 alg = new StandardKMeans_F64(100,1,new InitializeStandard_F64());

		alg.init(4,123);

		alg.clusters.resize(3);
		alg.workClusters.resize(3);
		alg.memberCount.resize(3);

		double orig[][] = new double[3][4];
		orig[0] = new double[]{10,20,30,20};
		orig[1] = new double[]{20,10,30,40};
		orig[2] = new double[]{3,9,1,12};

		alg.workClusters.data[0] = orig[0].clone();
		alg.workClusters.data[1] = orig[1].clone();
		alg.workClusters.data[2] = orig[2].clone();

		alg.memberCount.data[0] = 10;
		alg.memberCount.data[1] = 1;
		alg.memberCount.data[2] = 3;

		// previous clusters will be near zero
		alg.updateClusterCenters();

		for (int i = 0; i < 4; i++) {
			assertEquals(alg.clusters.data[0][i],orig[0][i]/10);
			assertEquals(alg.clusters.data[1][i],orig[1][i]/1);
			assertEquals(alg.clusters.data[2][i],orig[2][i]/3);
		}
	}

	@Test
	public void distanceSq() {
		double a[] = new double[]{1,2,3,4,5};
		double b[] = new double[]{4,6,3,1,-1};

		double found = StandardKMeans_F64.distanceSq(a,b);
		assertEquals(70.0,found,1e-8);
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
	public ComputeClusters<double[]> createClustersAlg( boolean hint ) {
		if( hint ) {
			// assume the first 3 are in different groups for the seeds
			return new StandardKMeans_F64(1000, 1e-8, new FixedSeeds());
		} else {
			InitializeStandard_F64 seeds = new InitializeStandard_F64();
			return new StandardKMeans_F64(1000, 1e-8, seeds );
		}
	}

	public static class FixedSeeds implements InitializeKMeans_F64 {

		@Override
		public void init(int pointDimension, long randomSeed) {}

		@Override
		public void selectSeeds(List<double[]> points, List<double[]> seeds) {
			int N = seeds.get(0).length;
			for (int i = 0; i < 3; i++) {
				System.arraycopy(points.get(i), 0, seeds.get(i), 0, N);
			}
		}
	}
}
