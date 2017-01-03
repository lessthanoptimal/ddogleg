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

import org.ejml.data.DenseMatrix64F;
import org.ejml.equation.Equation;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestGaussianGmm_F64 {

	Random rand = new Random(234);

	@Test
	public void zero() {
		GaussianGmm_F64 g = new GaussianGmm_F64(3);

		CommonOps_D64.fill(g.mean,1);
		CommonOps_D64.fill(g.covariance,2);
		g.weight = 4;

		g.zero();

		assertTrue(CommonOps_D64.elementSumAbs(g.mean)==0);
		assertTrue(CommonOps_D64.elementSumAbs(g.covariance)==0);
		assertTrue(g.weight == 0);
	}

	@Test
	public void addMean() {
		GaussianGmm_F64 g = new GaussianGmm_F64(3);
		g.addMean(new double[]{2,3,-1},0.7);
		g.addMean(new double[]{4,1,0.5},1.2);

		assertEquals(2*0.7 + 4*1.2,g.mean.data[0],1e-8);
		assertEquals(3*0.7 + 1*1.2,g.mean.data[1],1e-8);
		assertEquals(-1*0.7 + 0.5*1.2,g.mean.data[2],1e-8);

		assertEquals(0.7 + 1.2, g.weight, 1e-8);
	}

	@Test
	public void addCovariance() {

		GaussianGmm_F64 g = new GaussianGmm_F64(3);
		g.setMean(new double[]{4,3,6});

		Equation eq = new Equation();
		eq.process("Q = zeros(3,3)");
		for (int i = 0; i < 5; i++) {
			DenseMatrix64F x = RandomMatrices_D64.createRandom(3,1,rand);
			eq.alias(x,"x",0.4+i*0.1,"w");
			eq.process("Q = Q + w*x*x'");

			g.addCovariance(x.data,0.4+i*0.1);
		}
		DenseMatrix64F Q = eq.lookupMatrix("Q");

		assertTrue(MatrixFeatures_D64.isIdentical(Q, g.covariance, 1e-8));
	}

	@Test
	public void setMean() {
		GaussianGmm_F64 g = new GaussianGmm_F64(3);

		g.setMean(new double[]{1,2,3});

		for (int i = 0; i < 3; i++) {
			assertEquals(i+1,g.mean.get(i,0),1e-8);
		}
	}

}