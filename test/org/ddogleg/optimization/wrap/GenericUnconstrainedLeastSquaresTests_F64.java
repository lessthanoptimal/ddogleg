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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.TrivialLeastSquaresResidual;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.derivative.NumericalJacobianForward_DDRM;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests which make sure the {@link UnconstrainedLeastSquares} contract is followed.
 *
 * @author Peter Abeles
 */
public abstract class GenericUnconstrainedLeastSquaresTests_F64 {

	public abstract UnconstrainedLeastSquares<DMatrixRMaj> createAlgorithm();

	/**
	 * Simple optimization which checks several different aspects of its behavior
	 */
	@Test
	public void basicTest() {
		double a = 2;
		double b = 0.1;
		FunctionNtoM residual = new TrivialLeastSquaresResidual(a,b);
		FunctionNtoMxN<DMatrixRMaj> jacobian = new NumericalJacobianForward_DDRM(residual);

		UnconstrainedLeastSquares alg = createAlgorithm();

		alg.setFunction(residual,jacobian);
		alg.initialize(new double[]{1,0.5},1e-10,1e-10);

		double[] prev = new double[]{1,0.5};
		int i;
		for( i = 0; i < 200 && !alg.iterate(); i++ ) {
			double[] found = alg.getParameters();

			// check updated flag
			if( alg.isUpdated() ) {
				boolean changed = false;
				for( int j = 0; j < found.length; j++ ) {
					if( found[j] != prev[j] )
						changed = true;
				}
				assertTrue(changed);
			} else {
				for( int j = 0; j < found.length; j++ ) {
					assertEquals(prev[j], found[j]);
				}
			}

			prev = found.clone();
		}

		// should converge way before this
		assertTrue(i != 200);
		assertTrue(alg.isConverged());

		double[] found = alg.getParameters();

		assertEquals(a, found[0], UtilEjml.TEST_F64_SQ);
		assertEquals(b, found[1], UtilEjml.TEST_F64_SQ);
	}

	/**
	 * Makes sure if the jacobian is null that the numerical differentiation is done and that it uses the expected
	 * one
	 */
	@Test
	public void checkNumerical() {
		double a = 2;
		double b = 0.1;
		FunctionNtoM residual = new TrivialLeastSquaresResidual(a,b);
		FunctionNtoMxN<DMatrixRMaj> jacobian = new NumericalJacobianForward_DDRM(residual);

		UnconstrainedLeastSquares<DMatrixRMaj> alg = createAlgorithm();

		alg.setFunction(residual,jacobian);
		alg.initialize(new double[]{1,0.5},1e-10,1e-10);

		for( int i = 0; i < 200 && !alg.iterate(); i++ ) {}

		double[] expected = alg.getParameters().clone();

		alg.setFunction(residual,null);
		alg.initialize(new double[]{1,0.5},1e-10,1e-10);

		for( int i = 0; i < 200 && !alg.iterate(); i++ ) {}

		double[] found = alg.getParameters().clone();

		for( int i = 0; i < found.length; i++ ) {
			assertEquals(expected[i], found[i]);
		}
	}

	/**
	 * The function can modify the input parameters and
	 */
	@Test
	public void checkAcceptModified() {
		ModifyInputFunctions residuals = new ModifyInputFunctions();

		UnconstrainedLeastSquares<DMatrixRMaj> alg = createAlgorithm();

		alg.setFunction(residuals,null);
		alg.initialize(new double[]{1,0.5,9.5},1e-10,1e-10);

		for( int i = 0; i < 200 && !alg.iterate(); i++ ) {}

		double[] found = alg.getParameters().clone();
		double[] expected = new double[]{1,2,3};

		for( int i = 0; i < found.length; i++ ) {
			assertEquals(expected[i], found[i]);
		}

		// This will modify it on the first process().  Should make test more robust by having it modify it
		// later on too
	}

	private static class ModifyInputFunctions implements FunctionNtoM {

		@Override
		public int getNumOfInputsN() {
			return 3;
		}

		@Override
		public int getNumOfOutputsM() {
			return 2;
		}

		@Override
		public void process(double[] input, double[] output) {
			for( int i = 0; i < input.length; i++ ) {
				input[i] = 1+i;
			}
			// no error
			for( int i = 0; i < output.length; i++ ) {
				output[i] = 0;
			}
		}
	}
}
