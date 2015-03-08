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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class StandardInitializeKMeansChecks {

	public abstract InitializeKMeans_F64 createAlg();

	@Test
	public void selectSeeds() {
		int DOF = 20;

		InitializeKMeans_F64 alg = createAlg();

		alg.init(DOF,0xBEEF);

		List<double[]> points = TestStandardKMeans_F64.createPoints(DOF,100,true);
		List<double[]> seeds = TestStandardKMeans_F64.createPoints(DOF,20,false);


		alg.selectSeeds(points,seeds);

		// make sure nothing was added to the list
		assertEquals(20,seeds.size());

		for (int i = 0; i < seeds.size(); i++) {
			double[] s = seeds.get(i);

			// make sure the seed was written to
			for (int j = 0; j < DOF; j++) {
				assertTrue(s[j]!=0);
			}

			// make sure it wasn't swapped with one of the points
			for (int j = 0; j < points.size(); j++) {
				assertTrue(points.get(j) != s);
			}
		}
	}
}
