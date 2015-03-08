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

import java.util.List;
import java.util.Random;

/**
 * Seeds are selects by randomly picking points.  This is the standard way to initialize k-means
 *
 * @author Peter Abeles
 */
public class StandardSeeds_F64 implements InitializeKMeans_F64 {

	Random rand;

	@Override
	public void init(int pointDimension, long randomSeed) {
		rand = new Random(randomSeed);
	}

	@Override
	public void selectSeeds(List<double[]> points, List<double[]> seeds) {

		for (int i = 0; i < seeds.size(); i++) {
			double[] s = seeds.get(i);
			double[] p = points.get(rand.nextInt(points.size()));

			System.arraycopy(p,0,s,0,s.length);
		}
	}
}
