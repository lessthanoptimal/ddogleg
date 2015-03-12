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

import org.ddogleg.struct.GrowQueue_I32;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class BenchmarkStabilityInitialization {

	Random rand = new Random(234);
	List<double[]> points = new ArrayList<double[]>();
	List<double[]> centers = new ArrayList<double[]>();
	GrowQueue_I32 membership = new GrowQueue_I32();
	GrowQueue_I32 clusterSize = new GrowQueue_I32();
	int totalClusters = 0;

	public void addCluster( double x , double y , double sigmaX , double sigmaY , int N ) {
		centers.add( new double[]{x,y});

		for (int i = 0; i < N; i++) {
			double p[] = new double[2];

			p[0] = x + rand.nextGaussian()*sigmaX;
			p[1] = y + rand.nextGaussian()*sigmaY;

			points.add(p);
			membership.add(totalClusters);
		}
		totalClusters++;
		clusterSize.add(N);
	}

	public void bencharkAll() {
		addCluster(10,12,1,2,200);
		addCluster(-1,5,0.5,0.2,500);
		addCluster(5,7,0.3,0.3,100);

		System.out.println("Lower errors the better....\n");
		evaluate(FactoryClustering.kMeans_F64(KMeansInitializers.STANDARD, 1000, 1e-8));
		evaluate(FactoryClustering.kMeans_F64(KMeansInitializers.PLUS_PLUS,1000,1e-8));
		evaluate(FactoryClustering.gaussianMixtureModelEM_F64(1000,1e-8));
	}

	public void evaluate( ComputeClusters<double[]> clusterer ) {
		clusterer.init(2,32454325);

		int numTrials = 500;
		double totalSizeError = 0;
		for (int i = 0; i < numTrials; i++) {
			clusterer.process(points,3);
			AssignCluster<double[]> assign = clusterer.getAssignment();

			int counts[] = new int[totalClusters];

			for (int j = 0; j < points.size(); j++) {
				int found = assign.assign(points.get(j));
				counts[found]++;
			}

			totalSizeError += computeSizeError(assign,counts);
		}

		System.out.println("average size error = "+(totalSizeError/numTrials));
	}

	private double computeSizeError( AssignCluster<double[]> assign , int counts[] ) {
		double error = 0;
		for (int i = 0; i < totalClusters; i++) {
			int closest = assign.assign(centers.get(i));
			int foundSize = counts[closest];
			int expectedSize = clusterSize.get(i);

			error += Math.abs(foundSize-expectedSize);
		}

		return error/totalClusters;
	}



	public static void main(String[] args) {
		BenchmarkStabilityInitialization app = new BenchmarkStabilityInitialization();
		app.bencharkAll();
	}
}
