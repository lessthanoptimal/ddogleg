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

package org.ddogleg.clustering.misc;

import org.ddogleg.clustering.ComputeMeanClusters;
import org.ddogleg.struct.DogArray_I32;
import org.ddogleg.struct.FastAccess;
import org.ddogleg.struct.LArrayAccessor;

import java.util.Arrays;

/**
 * Computes the mean for points composed of double[]
 *
 * @author Peter Abeles
 */
public class MeanArrayF64 implements ComputeMeanClusters<double[]> {

	final int length;

	DogArray_I32 counts = new DogArray_I32();

	public MeanArrayF64(int length) {
		this.length = length;
	}

	@Override
	public void process( LArrayAccessor<double[]> points,
						 DogArray_I32 assignments,
						 FastAccess<double[]> clusters) {

		if (assignments.size != points.size())
			throw new IllegalArgumentException("Points and assignments need to be the same size");

		// set the number of points in each cluster to zero and zero the clusters
		counts.resize(clusters.size, 0);
		for (int i = 0; i < clusters.size; i++) {
			Arrays.fill(clusters.get(i),0,length,0.0);
		}

		// Compute the sum of all points in each cluster
		for (int pointIdx = 0; pointIdx < points.size(); pointIdx++) {
			double[] point = points.getTemp(pointIdx);

			int clusterIdx = assignments.get(pointIdx);
			counts.data[clusterIdx]++;
			double[] cluster = clusters.get(clusterIdx);
			for (int i = 0; i < length; i++) {
				cluster[i] += point[i];
			}
		}

		// Divide to get the average value in each cluster
		for (int clusterIdx = 0; clusterIdx < clusters.size; clusterIdx++) {
			double[] cluster = clusters.get(clusterIdx);
			double divisor = counts.get(clusterIdx);
			for (int i = 0; i < length; i++) {
				cluster[i] /= divisor;
			}
		}
	}

	@Override public ComputeMeanClusters<double[]> newInstanceThread() {
		return new MeanArrayF64(length);
	}
}
