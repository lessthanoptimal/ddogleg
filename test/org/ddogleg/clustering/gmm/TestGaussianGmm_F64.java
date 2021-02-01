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

package org.ddogleg.clustering.gmm;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.equation.Equation;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestGaussianGmm_F64 {

	Random rand = new Random(234);

	@Test void zero() {
		var g = new GaussianGmm_F64(3);

		CommonOps_DDRM.fill(g.mean,1);
		CommonOps_DDRM.fill(g.covariance,2);
		g.weight = 4;

		g.zero();

		assertEquals(CommonOps_DDRM.elementSumAbs(g.mean), 0);
		assertEquals(CommonOps_DDRM.elementSumAbs(g.covariance), 0);
		assertEquals(g.weight, 0);
	}

	@Test void addMean() {
		var g = new GaussianGmm_F64(3);
		g.addMean(new double[]{2,3,-1},0.7);
		g.addMean(new double[]{4,1,0.5},1.2);

		assertEquals(2*0.7 + 4*1.2,g.mean.data[0],1e-8);
		assertEquals(3*0.7 + 1*1.2,g.mean.data[1],1e-8);
		assertEquals(-1*0.7 + 0.5*1.2,g.mean.data[2],1e-8);

		assertEquals(0.7 + 1.2, g.weight, 1e-8);
	}

	@Test void addCovariance() {
		var g = new GaussianGmm_F64(3);
		g.setMean(new double[]{4,3,6});

		var eq = new Equation();
		eq.process("Q = zeros(3,3)");
		for (int i = 0; i < 5; i++) {
			DMatrixRMaj x = RandomMatrices_DDRM.rectangle(3,1,rand);
			eq.alias(x,"x",0.4+i*0.1,"w");
			eq.process("Q = Q + w*x*x'");

			g.addCovariance(x.data,0.4+i*0.1);
		}
		DMatrixRMaj Q = eq.lookupDDRM("Q");

		assertTrue(MatrixFeatures_DDRM.isIdentical(Q, g.covariance, 1e-8));
	}

	@Test void setMean() {
		var g = new GaussianGmm_F64(3);

		g.setMean(new double[]{1,2,3});

		for (int i = 0; i < 3; i++) {
			assertEquals(i+1,g.mean.get(i,0),1e-8);
		}
	}

}