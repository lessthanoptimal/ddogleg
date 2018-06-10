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

package org.ddogleg.optimization.impl;

import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.wrap.Individual_to_CoupledJacobian;
import org.ddogleg.optimization.wrap.LevenbergDampened_to_UnconstrainedLeastSquares;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestLevenbergMarquardtDampened_DDRM extends CommonChecksUnconstrainedLeastSquares_DDRM {

	@Test
	public void basicTest() {
		double a=2,b=0.1;

		LevenbergMarquardtDampened_DDRM alg = createAlg(a,b);
		
		alg.initialize(new double[]{1,0.5});
		
		int i;
		for( i = 0; i < 200 && !alg.iterate(); i++ ) { }

		// should converge way before this
		assertTrue(i != 200);
		assertTrue(alg.isConverged());

		double found[] = alg.getParameters();

		assertEquals(a, found[0], 1e-4);
		assertEquals(b, found[1], 1e-4);
	}

	private LevenbergMarquardtDampened_DDRM createAlg(double a, double b ) {

		FunctionNtoM residual = new TrivialLeastSquaresResidual(a,b);
		FunctionNtoMxN<DMatrixRMaj> jacobian = new NumericalJacobianForward_DDRM(residual);

		LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.pseudoInverse(true);

		LevenbergMarquardtDampened_DDRM alg = new LevenbergMarquardtDampened_DDRM(solver,1e-3);

		alg.setConvergence(1e-6,1e-6);
		alg.setFunction(new Individual_to_CoupledJacobian<>(residual,jacobian));

		return alg;
	}

	@Override
	protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch(double minimumValue) {
		LevenbergMarquardtDampened_DDRM alg = new LevenbergMarquardtDampened_DDRM(1e-3);
		return new LevenbergDampened_to_UnconstrainedLeastSquares<>(alg);
	}
}
