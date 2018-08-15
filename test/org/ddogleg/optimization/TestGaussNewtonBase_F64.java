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

package org.ddogleg.optimization;

import org.ddogleg.optimization.math.HessianMath;
import org.ddogleg.optimization.math.HessianMath_DDRM;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.equation.Equation;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestGaussNewtonBase_F64 {

	Random rand = new Random(2345);

	@Test
	public void initialize() {
		MockGaussNewtonBase_F64 alg = new MockGaussNewtonBase_F64();

		alg.totalFullSteps = 10;
		alg.totalSelectSteps = 11;

		double x[] = new double[]{1,2};
		alg.initialize(x,2);

		assertEquals(2,alg.x.numRows);
		assertEquals(2,alg.x_next.numRows);
		assertEquals(2,alg.gradient.numRows);

		assertEquals(0,alg.totalFullSteps);
		assertEquals(0,alg.totalSelectSteps);


		assertEquals(GaussNewtonBase_F64.Mode.COMPUTE_DERIVATIVES,alg.mode);
	}

	@Test
	public void computePredictedReduction() {
		MockGaussNewtonBase_F64 alg = new MockGaussNewtonBase_F64();
		DMatrixRMaj H = ((HessianMath_DDRM)alg.hessian).getHessian();
		H.reshape(2,2);

		double x[] = new double[]{1,2};
		alg.initialize(x,2);

		RandomMatrices_DDRM.fillUniform(alg.gradient,-1,1,rand);
		RandomMatrices_DDRM.fillUniform(H,-1,1,rand);

		Equation eq = new Equation();
		eq.alias(alg.p,"p",alg.gradient,"g",H,"H");

		eq.process("reduction = -g'*p - 0.5*p'*H*p");
		double expected = eq.lookupDouble("reduction");
		double found = alg.computePredictedReduction(alg.p);

		assertEquals(expected, found, UtilEjml.TEST_F64);
	}

	@Test
	public void checkConvergenceGTest() {
		MockGaussNewtonBase_F64 alg = new MockGaussNewtonBase_F64();

		alg.config.gtol = 1e-5;

		DMatrixRMaj g = new DMatrixRMaj(2,1);
		g.data = new double[]{-0.1,0.1};
		assertFalse(alg.checkConvergenceGTest(g));
		g.data[0] = -2e-5;
		assertFalse(alg.checkConvergenceGTest(g));
		g.data[1] = 2e-5;
		assertFalse(alg.checkConvergenceGTest(g));
		g.data[1] = 1e-5;
		assertFalse(alg.checkConvergenceGTest(g));
		g.data[0] = 1e-5;
		assertTrue(alg.checkConvergenceGTest(g));
	}

	@Test
	public void computeHessianScaling() {
		double d[] = {1,2,3,-0.001,0};
		DMatrixRMaj m = new DMatrixRMaj(d);

		MockGaussNewtonBase_F64 alg = new MockGaussNewtonBase_F64();
		alg.computeHessianScaling(m);

		// make sure it handled edge cases
		for (int i = 0; i < m.numRows; i++) {
			assertFalse( UtilEjml.isUncountable(m.get(i)));
			assertTrue( m.get(i) > 0 );

			// looser tolerance because it will have a small number added to it
			assertEquals(Math.sqrt(Math.abs(d[i])+1e-12), m.get(i) , UtilEjml.TEST_F64_SQ);
		}
	}

	@Test
	public void applyHessianScaling() {
		MockGaussNewtonBase_F64 alg = new MockGaussNewtonBase_F64();

		DMatrixRMaj H = ((HessianMath_DDRM)alg.hessian).getHessian();
		H.reshape(4,4);
		alg.gradient = RandomMatrices_DDRM.rectangle(4,1,-1,2,rand);
		RandomMatrices_DDRM.fillUniform(H,rand);

		DMatrixRMaj _g = alg.gradient.copy();
		DMatrixRMaj _H = H.copy();

		alg.hessianScaling = new DMatrixRMaj(new double[][]{{1},{0.1},{2},{0.6}});

		alg.applyHessianScaling();

		for (int row = 0; row < 4; row++) {
			double expected = _g.get(row,0)/alg.hessianScaling.get(row);
			assertEquals(expected, alg.gradient.get(row), UtilEjml.TEST_F64);

			for (int col = 0; col < 4; col++) {
				expected = _H.get(row,col)/(alg.hessianScaling.get(row)*alg.hessianScaling.get(col));
				assertEquals(expected, H.get(row,col), UtilEjml.TEST_F64);
			}
		}
	}

	@Test
	public void undoHessianScalingOnParameters() {
		MockGaussNewtonBase_F64 alg = new MockGaussNewtonBase_F64();
		alg.hessianScaling = new DMatrixRMaj(new double[][]{{1},{0.1},{2},{0.6}});

		DMatrixRMaj p = RandomMatrices_DDRM.rectangle(4,1,-1,2,rand);
		DMatrixRMaj o = p.copy();

		alg.undoHessianScalingOnParameters(p);

		for (int row = 0; row < 4; row++) {
			double expected = o.get(row, 0) / alg.hessianScaling.get(row);
			assertEquals(expected, p.get(row), UtilEjml.TEST_F64);
		}
	}

	protected static class MockGaussNewtonBase_F64
			extends GaussNewtonBase_F64<ConfigGaussNewton,HessianMath> {

		public MockGaussNewtonBase_F64() {
			super(new HessianMath_DDRM());
			config = new ConfigGaussNewton();
		}

		@Override
		protected boolean updateDerivates() {
			return false;
		}

		@Override
		protected boolean computeStep() {
			return false;
		}

		@Override
		protected void functionGradientHessian(DMatrixRMaj x, boolean sameStateAsCost, DMatrixRMaj gradient, HessianMath hessian) {

		}
	}
}