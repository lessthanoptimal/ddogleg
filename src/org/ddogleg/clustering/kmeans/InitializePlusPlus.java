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

import org.ddogleg.struct.GrowQueue_F64;

import java.util.List;
import java.util.Random;

/**
 * <p>
 * Implementation of the seeding strategy described in [1]. A point is randomly selected from the list as the first
 * seed.  The remaining seeds are selected randomly based on the distance of each seed from their closest
 * cluster.
 * </p>
 *
 * <p>
 * [1] David Arthur and Sergei Vassilvitskii. 2007. k-means++: the advantages of careful seeding.
 * In Proceedings of the eighteenth annual ACM-SIAM symposium on Discrete algorithms (SODA '07).
 * Society for Industrial and Applied Mathematics, Philadelphia, PA, USA, 1027-1035.
 * </p>
 *
 * @author Peter Abeles
 */
public class InitializePlusPlus implements InitializeKMeans_F64{

	Random rand;
	// the distance of each point to the cluster it is closest to
	GrowQueue_F64 distance = new GrowQueue_F64(1);
	double totalDistance;

	@Override
	public void init(int pointDimension, long randomSeed) {
		rand = new Random(randomSeed);
	}

	@Override
	public void selectSeeds(List<double[]> points, List<double[]> seeds) {
		if( seeds.size() > points.size() )
			throw new IllegalArgumentException("More seeds requested than points!");

		distance.resize(points.size());

		// the first seed is randomly selected from the list of points
		double[] seed = points.get( rand.nextInt(points.size()) );
		copyInto(seed,seeds.get(0));
		// compute the distance each points is from the seed
		totalDistance = 0;
		for (int i = 0; i < points.size(); i++) {
			double[] p = points.get(i);
			double d = StandardKMeans_F64.distanceSq(p,seed);
			distance.data[i] = d;
			totalDistance += d;
		}

		// iteratively select the next seed and update the list of point distances
		for (int i = 1; i < seeds.size(); i++) {
			if( totalDistance == 0 ) {
				// if the total distance is zero that means there are duplicate points and that
				// all the unique points have already been added as seeds.  just select a point
				// and copy it into rest of the seeds
				copyInto(seed, seeds.get(i));
			} else {
				double target = rand.nextDouble();
				copyInto(selectNextSeed(points, target), seeds.get(i));
				updateDistances(points, seeds.get(i));
			}
		}
	}

	/**
	 * Randomly selects the next seed.  The chance of a seed is based upon its distance
	 * from the closest cluster.  Larger distances mean more likely.
	 * @param points List of all the points
	 * @param target Number from 0 to 1, inclusive
	 * @return Index of the selected seed
	 */
	protected final double[] selectNextSeed( List<double[]> points , double target ) {
		// this won't select previously selected points because the distance will be zero
		// If the distance is zero it will simply skip over it
		double sum = 0;
		for (int i = 0; i < distance.size(); i++) {
			sum += distance.get(i);
			double fraction = sum/totalDistance;
			if( fraction >= target )
				return points.get(i);
		}
		throw new RuntimeException("This shouldn't happen");
	}

	/**
	 * Updates the list of distances from a point to the closest cluster.  Update list of total distances
	 */
	protected final void updateDistances( List<double[]> points , double []clusterNew ) {
		totalDistance = 0;
		for (int i = 0; i < distance.size(); i++) {
			double dOld = distance.get(i);
			double dNew = StandardKMeans_F64.distanceSq(points.get(i),clusterNew);
			if( dNew < dOld ) {
				distance.data[i] = dNew;
				totalDistance += dNew;
			} else {
				totalDistance += dOld;
			}
		}
	}

	private static void copyInto( double[] src, double[] dst) {
		System.arraycopy(src,0,dst,0,src.length);
	}
}
