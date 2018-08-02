/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Compares two different techniques for computing the smallest between a point and a set of points.
 *
 * @author Peter Abeles
 */
public class BenchmarkBulkDistance {

	Random rand = new Random(234);

	int DOF = 100;
	int numPoints = 100000;
	int numClusters = 300;

	List<double[]> centers = new ArrayList<double[]>();
	List<double[]> points = new ArrayList<double[]>();

	public long standard( int[] assignments , int numIterations ) {

		long timeStart = System.currentTimeMillis();

		for (int i = 0; i < numIterations; i++) {
			for (int j = 0; j < points.size(); j++) {
				double[] p = points.get(j);

				int best=-1;
				double bestDistance = Double.MAX_VALUE;

				for (int k = 0; k < centers.size(); k++) {
					double[] c = centers.get(k);

					double d = 0;
					for (int l = 0; l < c.length; l++) {
						double x = c[l]-p[l];
						d += x*x;
					}

					if( d < bestDistance ) {
						bestDistance = d;
						best = k;
					}
				}

				assignments[best]++;
			}
		}

		long timeStop = System.currentTimeMillis();

		return timeStop - timeStart;
	}

	public long bulk( int[] assignments , int numIterations ) {

		final int N = points.get(0).length;

		// stick all the center into a single hunk of memory so that it is all continuous.
		double array[] = new double[ centers.size()*N ];
		for (int i = 0; i < centers.size(); i++) {
			double c[] = centers.get(i);
			System.arraycopy(c,0,array,i*N,N);
		}
		double distances[] = new double[centers.size()];

		long timeStart = System.currentTimeMillis();

		for (int i = 0; i < numIterations; i++) {
			for (int j = 0; j < points.size(); j++) {
				double[] p = points.get(j);

				int best=-1;
				double bestDistance = Double.MAX_VALUE;

				int index = 0;
				for (int k = 0; k < distances.length; k++) {
					double d = 0;
					for (int l = 0; l < N; l++) {
						double x = array[index++]-p[l];
						d += x*x;
					}
					if( d < bestDistance ) {
						bestDistance = d;
						best = k;
					}
				}

				assignments[best]++;
			}
		}

		long timeStop = System.currentTimeMillis();

		return timeStop - timeStart;

	}

	public void process() {
		randomSet(points, numPoints);
		randomSet(centers, numClusters);

		int numIterations = 1;

		int assignA[] = new int[numPoints];
		int assignB[] = new int[numPoints];

		long timeB = bulk(assignB, numIterations);
		long timeA = standard(assignA, numIterations);

		System.out.println("Time Standard = "+timeA);
		System.out.println("Time Bulk     = "+timeB);

		// sanity check
		for (int i = 0; i < numPoints; i++) {
			if( assignA[i] != assignB[i])
				throw new RuntimeException("Oh shit");
		}
	}

	private void randomSet(List<double[]> points, int numPoints) {
		for (int i = 0; i < numPoints; i++) {
			double p[] = new double[DOF];
			for (int j = 0; j < DOF; j++) {
				p[j] = rand.nextGaussian();
			}
			points.add(p);
		}
	}

	public static void main(String[] args) {
		BenchmarkBulkDistance benchmark = new BenchmarkBulkDistance();

		benchmark.process();
	}
}
