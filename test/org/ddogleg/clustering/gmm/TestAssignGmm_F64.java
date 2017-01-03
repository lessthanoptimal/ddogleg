/*
 * Copyright (c) 2012-2017, Peter Abeles. All Rights Reserved.
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

import org.ejml.ops.MatrixFeatures_D64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestAssignGmm_F64 {
	@Test
	public void assign() {
		List<GaussianGmm_F64> clusters = new ArrayList<GaussianGmm_F64>();

		clusters.add( createGaussian(2,1));
		clusters.add( createGaussian(10,2) );

		AssignGmm_F64 alg = new AssignGmm_F64(clusters);

		assertEquals(0, alg.assign(new double[]{3}));
		assertEquals(1, alg.assign(new double[]{9}));
	}

	@Test
	public void assign_soft() {
		List<GaussianGmm_F64> clusters = new ArrayList<GaussianGmm_F64>();

		clusters.add(createGaussian(2, 1));
		clusters.add(createGaussian(4, 1));

		AssignGmm_F64 alg = new AssignGmm_F64(clusters);

		double[] fit = new double[2];
		alg.assign(new double[]{3}, fit);
		assertEquals(0.5, fit[0], 1e-8);
		assertEquals(0.5, fit[1], 1e-8);

		alg.assign(new double[]{3.5}, fit);
		assertTrue(fit[0] < fit[1]);
	}

	@Test
	public void copy() {
		List<GaussianGmm_F64> clusters = new ArrayList<GaussianGmm_F64>();

		clusters.add(createGaussian(2, 1));
		clusters.add(createGaussian(10, 2));

		AssignGmm_F64 original = new AssignGmm_F64(clusters);
		AssignGmm_F64 copy = (AssignGmm_F64)original.copy();

		assertEquals(original.getNumberOfClusters(),copy.getNumberOfClusters());

		for (int i = 0; i < original.getNumberOfClusters(); i++) {
			GaussianGmm_F64 o = original.mixture.get(i);
			GaussianGmm_F64 c = copy.mixture.get(i);

			assertTrue(o!=c);
			assertTrue(MatrixFeatures_D64.isIdentical(o.mean,c.mean,1e-8));
			assertTrue(MatrixFeatures_D64.isIdentical(o.covariance,c.covariance,1e-8));
			assertTrue(o.weight == c.weight);
		}
	}

	public static GaussianGmm_F64 createGaussian( double mean , double var ) {
		GaussianGmm_F64 ret = new GaussianGmm_F64(1);

		ret.mean.set(0,0,mean);
		ret.covariance.set(0,0,var);
		ret.weight = 2;

		return ret;
	}
}
