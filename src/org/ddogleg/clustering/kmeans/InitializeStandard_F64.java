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

import org.ddogleg.struct.GrowQueue_B;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds are selects by randomly picking points.  This is the standard way to initialize k-means
 *
 * @author Peter Abeles
 */
public class InitializeStandard_F64 implements InitializeKMeans_F64 {

	Random rand;

	// used to ensure that the same point isn't returned twice
	GrowQueue_B marked = new GrowQueue_B();
	List<double[]> unused = new ArrayList<double[]>();

	@Override
	public void init(int pointDimension, long randomSeed) {
		rand = new Random(randomSeed);
	}

	@Override
	public void selectSeeds(List<double[]> points, List<double[]> seeds) {

		if( seeds.size() > points.size() )
			throw new IllegalArgumentException("More seeds requested than points!");

		if( seeds.size()*2 > points.size() ) {
			// If the probability of a collision is too great use a list which is modified
			// this way each time a draw is made its going to not be used.  However removing items
			// from a list is an expensive opeartion

			unused.addAll(points);
			for (int i = 0; i < seeds.size(); i++) {
				double[] s = seeds.get(i);
				double[] p = unused.remove(rand.nextInt(unused.size()));

				System.arraycopy(p, 0, s, 0, s.length);
			}
			unused.clear();
		} else {
			// mark which points have been used.  If it draws the same one twice just do another random draw
			marked.resize(points.size());
			marked.fill(false);
			int numSelected = 0;
			while( numSelected < seeds.size() ) {
				int drawn = rand.nextInt(points.size());

				// see if has already been selected
				if( marked.get(drawn))
					continue;

				marked.set(drawn,true);
				double[] s = seeds.get(numSelected);
				double[] p = points.get(drawn);

				System.arraycopy(p, 0, s, 0, s.length);
				numSelected++;
			}
		}
	}
}
