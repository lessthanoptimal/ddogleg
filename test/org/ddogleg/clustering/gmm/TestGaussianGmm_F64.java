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

import org.ejml.ops.CommonOps;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestGaussianGmm_F64 {
	@Test
	public void zero() {
		GaussianGmm_F64 g = new GaussianGmm_F64(3);

		CommonOps.fill(g.mean,1);
		CommonOps.fill(g.covariance,2);
		g.weight = 4;

		g.zero();

		assertTrue(CommonOps.elementSumAbs(g.mean)==0);
		assertTrue(CommonOps.elementSumAbs(g.covariance)==0);
		assertTrue(g.weight==0);
	}

	@Test
	public void addMean() {
		GaussianGmm_F64 g = new GaussianGmm_F64(3);
		g.addMean(new double[]{2,3,-1},0.7);
		g.addMean(new double[]{4,1,0.5},1.2);

		assertEquals(2*0.7 + 4*1.2,g.mean.data[0],1e-8);
		assertEquals(3*0.7 + 1*1.2,g.mean.data[1],1e-8);
		assertEquals(-1*0.7 + 0.5*1.2,g.mean.data[2],1e-8);

		assertEquals(0.7+1.2,g.weight,1e-8);
	}

	@Test
	public void addCovariance() {
		fail("Implement");
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