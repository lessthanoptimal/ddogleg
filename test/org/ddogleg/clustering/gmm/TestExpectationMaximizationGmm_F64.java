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

package org.ddogleg.clustering.gmm;

import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.clustering.GenericClusterChecks_F64;
import org.ddogleg.clustering.kmeans.StandardKMeans_F64;
import org.ddogleg.clustering.kmeans.TestStandardKMeans_F64;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestExpectationMaximizationGmm_F64 extends GenericClusterChecks_F64 {
	@Test
	public void stuff() {
		fail("Implement");
	}

	@Override
	public ComputeClusters<double[]> createClustersAlg() {
		StandardKMeans_F64 kmeans = new StandardKMeans_F64(1000,1e-8,new TestStandardKMeans_F64.FixedSeeds());;
		SeedFromKMeans_F64 seeds = new SeedFromKMeans_F64(kmeans);
		return new ExpectationMaximizationGmm_F64(1000,1e-8,seeds);
	}
}