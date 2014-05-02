/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.nn.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contains algorithms for generating possibly poisonous data for nn-search algorithms.
 */
public class NnDataGenerator {
	private static Random random = new Random(42);

	public static List<double[]> createLine(int dimensions, int points) {
		final double[] dir = new double[dimensions], origin = new double[dimensions];
		for (int i = 0; i < dimensions; i++) {
			dir[i] = random.nextDouble();
			origin[i] = random.nextDouble();
		}

		ArrayList<double[]> res = new ArrayList<double[]>(points);
		for (int i = 0; i < points; i++) {
			double[] pt = new double[dimensions];
			double k = random.nextInt(points); // The line is approx. "points" long, 1 point per unit length on average
			for (int j = 0; j < dimensions; j++) {
				pt[i] = dir[i] * k + origin[i];
			}
			res.add(pt);
		}
		return res;
	}

	public static List<double[]> createCircle(int points) {
		double[] origin = {random.nextInt(100), random.nextInt(100) };
		double r = random.nextDouble() * 100;
		ArrayList<double[]> res = new ArrayList<double[]>(points);
		for (int i = 0; i < points; i++) {
			double phi = i / (2 * Math.PI * points);
			res.add(new double[] { r * Math.cos(phi) + origin[0], r * Math.sin(phi) + origin[1] });
		}
		return res;
	}
}
