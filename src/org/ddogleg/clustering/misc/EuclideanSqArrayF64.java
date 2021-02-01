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

import org.ddogleg.clustering.PointDistance;

/**
 * Returns Euclidean distance squared for double[] points.
 *
 * @author Peter Abeles
 */
public class EuclideanSqArrayF64 implements PointDistance<double[]> {
	// Number of elements in the array
	final int arrayLength;

	public EuclideanSqArrayF64(int arrayLength ) {
		this.arrayLength = arrayLength;
	}

	@Override
	public double distance(double[] a, double[] b) {
		double sum = 0.0;
		for (int i = 0; i < arrayLength; i++) {
			double d = a[i]-b[i];
			sum += d*d;
		}
		return sum;
	}

	@Override public PointDistance<double[]> newInstanceThread() {
		return this;
	}
}
