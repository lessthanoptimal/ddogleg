/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.trustregion;

import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.math.HessianBFGS_DDRM;
import org.ddogleg.optimization.wrap.GenericUnconstrainedMinimizationTests_F64;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Specific configurations on this class are tested inside of the TrustRegionUpdate implementations
 *
 * @author Peter Abeles
 */
public class TestUnconMinTrustRegionBFGS_F64 extends GenericUnconstrainedMinimizationTests_F64 {

	/**
	 * sees if it's checking the region radius for problems
	 */
	@Test
	public void checkConvergenceFTest_radius() {
		UnconMinTrustRegionBFGS_F64 alg = createAlg();

		alg.regionRadius = 0;
		try {
			alg.checkConvergenceFTest(-1,-1);
			fail("Should have thrown an exception");
		} catch( OptimizationException ignore){}

		alg.regionRadius = Double.NaN;
		try {
			alg.checkConvergenceFTest(-1,-1);
			fail("Should have thrown an exception");
		} catch( OptimizationException ignore){}
	}

	@Test
	public void checkConvergenceFTest() {
		UnconMinTrustRegionBFGS_F64 alg = createAlg();

		alg.regionRadius = 1;
		alg.config.ftol = 1e-4;

		assertTrue(alg.checkConvergenceFTest(2,2));
		assertTrue(alg.checkConvergenceFTest(2,2*(1+1e-5)));
		assertFalse(alg.checkConvergenceFTest(2,2*(1+9e-3)));
	}

	@Test
	public void wolfeCondition() {
		UnconMinTrustRegionBFGS_F64 alg = createAlg();

		alg.fx = 1;
		alg.f_prev = 1.1;

		DMatrixRMaj s = new DMatrixRMaj(new double[][]{{1},{2}});
		DMatrixRMaj y = new DMatrixRMaj(new double[][]{{2},{4}});
		DMatrixRMaj g = new DMatrixRMaj(new double[][]{{0.1},{0.4}});

		assertTrue(alg.wolfeCondition(s,y,g));

		// if the change in state is perpendicular to change in gradient it should fail
		s = new DMatrixRMaj(new double[][]{{1},{2}});
		y = new DMatrixRMaj(new double[][]{{2},{-4}});
		assertFalse(alg.wolfeCondition(s,y,g));
	}

	@Override
	public UnconstrainedMinimization createAlgorithm() {
		return createAlg();
	}

	protected UnconMinTrustRegionBFGS_F64 createAlg() {
		TrustRegionUpdateDogleg_F64 dogleg = new TrustRegionUpdateDogleg_F64();
		HessianBFGS_DDRM hessian = new HessianBFGS_DDRM(true);
		return new UnconMinTrustRegionBFGS_F64(dogleg,hessian);
	}
}