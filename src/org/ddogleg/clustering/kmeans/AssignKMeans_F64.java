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
import java.util.List;

/**
 * Implementation of {@link org.ddogleg.clustering.AssignCluster} for K-Means.
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

	@Override
	public int getNumberOfClusters() {
		return clusters.size();
	}

	@Override
	public AssignCluster<double[]> copy() {
		return new AssignKMeans_F64(this);
	}
}
