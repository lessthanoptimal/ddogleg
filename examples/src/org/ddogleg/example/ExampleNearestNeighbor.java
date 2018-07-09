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

package org.ddogleg.example;

import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.nn.alg.distance.KdTreeEuclideanSq_F64;

import java.util.ArrayList;
import java.util.List;

/**
 * Nearest-Neighbor search looks for the closest match to a point from a set of points based upon some distance metric.
 * In low dimension it can be done very efficiently using K-D Trees, but in high dimension approximate methods perform
 * better.
 *
 * @author Peter Abeles
 */
public class ExampleNearestNeighbor {

	public static void main( String args[] ) {
		// Easiest way to create a NN algorithm is using the factory below
		// KdTreeEuclideanSq_F64 tells it what type of data structure is being searched
		// F64 standards for double and means double[]. You can easily specify your own
		// data type by changing what you pass in here
		NearestNeighbor<double[]> nn = FactoryNearestNeighbor.kdtree(new KdTreeEuclideanSq_F64(2));

		// Create data that's going to be searched
		List<double[]> points = new ArrayList<>();

		// For sake of demonstration add a set of points along the line
		for( int i = 0; i < 10; i++ ) {
			double[] p = new double[]{i,i*2};
			points.add(p);
		}

		// Pass the points and associated data.  Internally a data structure is constructed that enables fast lookup.
		// This can be one of the more expensive operations, depending on which implementation is used.
		nn.setPoints(points,true);

		// declare storage for where to store the result
		NnData<double[]> result = new NnData<>();

		// It will look for the closest point to [1.1,2.2] which will be [1,2]
		// The second parameter specifies the maximum distance away that it will consider for a neighbor
		// set to -1 to set to the largest possible value
		if( nn.findNearest(new double[]{1.1,2.2},-1,result) ) {
			System.out.println("Best match:");
			System.out.println("   point     = "+result.point[0]+" "+result.point[1]);
			System.out.println("   data      = "+result.index);
			System.out.println("   distance  = "+result.distance);
		} else {
			System.out.println("No match found");
		}
	}
}
