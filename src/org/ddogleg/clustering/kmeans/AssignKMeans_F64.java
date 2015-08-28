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

import org.ddogleg.clustering.AssignCluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link org.ddogleg.clustering.AssignCluster} for K-Means.  Euclidean distance squared is
 * used to select the best fit clusters to a point.  This distance metric works well for hard assignment but can
 * produce undesirable results for soft assignment, see JavaDoc.
 *
 * @author Peter Abeles
 */
public class AssignKMeans_F64 implements AssignCluster<double[]> {

	List<double[]> clusters;

	public AssignKMeans_F64( List<double[]> clusters ) {
		this.clusters = clusters;
	}

	public AssignKMeans_F64( AssignKMeans_F64 original ) {
		clusters = new ArrayList<double[]>();
		for (int i = 0; i < original.clusters.size(); i++) {
			clusters.add( original.clusters.get(i).clone());
		}
	}

	@Override
	public int assign(double[] point) {

		int best = -1;
		double bestScore = Double.MAX_VALUE;

		for (int i = 0; i < clusters.size(); i++) {
			double score = StandardKMeans_F64.distanceSq(point,clusters.get(i));
			if( score < bestScore ) {
				bestScore = score;
				best = i;
			}
		}

		return best;
	}

	/**
	 * <p>Soft assignment is done by summing the total distance of the point from each cluster. Then for each cluster
	 * its value is set to total minus its distance.  The output array is then normalized by dividing each element
	 * by the sum.</p>
	 *
	 * <p>
	 * When all clusters are approximately the same distance or one is clearly the closest this produces reasonable
	 * results.  When multiple clusters are much closer than at least on other cluster then it effectively ignores
	 * the relative difference in distances between the closest points.  There are several obvious heuristic "fixes"
	 * to this issue, but the best way to solve it is to simply use {@link org.ddogleg.clustering.gmm.AssignGmm_F64}
	 * instead.
	 * </p>
	 */
	@Override
	public void assign(double[] point, double[] fit) {
		Arrays.fill(fit,0);

		// compute and save distance to each cluster
		double max = 0;
		for (int i = 0; i < clusters.size(); i++) {
			double d = StandardKMeans_F64.distanceSq(point,clusters.get(i));
			fit[i] = d;
			if( d > max ) {
				max = d;
			}
		}

		// normalize to reduce overflow
		double total = 0;
		for (int i = 0; i < clusters.size(); i++) {
			total += fit[i] /= max;
		}

		// ensure that the closer clusters are weighted more
		double total2 = 0;
		for (int i = 0; i < clusters.size(); i++) {
			total2 += fit[i] = total - fit[i];
		}

		// normalize so that the sum is 1
		for (int i = 0; i < clusters.size(); i++) {
			fit[i] /= total2;
		}

	}

	@Override
	public int getNumberOfClusters() {
		return clusters.size();
	}

	@Override
	public AssignCluster<double[]> copy() {
		return new AssignKMeans_F64(this);
	}

	public List<double[]> getClusters() {
		return clusters;
	}

	public void setClusters(List<double[]> clusters) {
		this.clusters = clusters;
	}
}
