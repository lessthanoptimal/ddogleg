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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ddogleg.optimization.impl.NumericalGradientForward;
import org.ddogleg.optimization.impl.TrivialFunctionNtoS;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests which make sure the {@link org.ddogleg.optimization.UnconstrainedMinimization} contract is followed.
 *
 * @author Peter Abeles
 */
public abstract class GenericUnconstrainedMinimizationTests {

	public abstract UnconstrainedMinimization createAlgorithm();

	/**
	 * Simple optimization which checks several different aspects of its behavior
	 */
	@Test
	public void basicTest() {
		FunctionNtoS residual = new TrivialFunctionNtoS();
		FunctionNtoN jacobian = new NumericalGradientForward(residual);

		UnconstrainedMinimization alg = createAlgorithm();

		alg.setFunction(residual,jacobian,0);
		alg.initialize(new double[]{1,1,1},1e-10,1e-10);

		double[] prev = new double[]{1,1,1};
		int i;
		for( i = 0; i < 200 && !alg.iterate(); i++ ) {
			double found[] = alg.getParameters();

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
					assertTrue(found[j]==prev[j]);
				}
			}

			prev = found.clone();
		}

		// should converge way before this
		assertTrue(i != 200);
		assertTrue(alg.isConverged());

		double found[] = alg.getParameters();

		assertEquals(0, found[0], 1e-4);
		assertEquals(0, found[1], 1e-4);
		assertEquals(1, found[2], 1e-4);  // no change expected in last parameter
	}

	/**
	 * Makes sure if the jacobian is null that the numerical differentiation is done and that it uses the expected
	 * one
	 */
	@Test
	public void checkNumerical() {
		FunctionNtoS residual = new TrivialFunctionNtoS();
		FunctionNtoN jacobian = new NumericalGradientForward(residual);

		UnconstrainedMinimization alg = createAlgorithm();

		alg.setFunction(residual,jacobian,0);
		alg.initialize(new double[]{1,1,1},1e-10,1e-10);

		for( int i = 0; i < 200 && !alg.iterate(); i++ ) {}

		double expected[] = alg.getParameters().clone();

		alg.setFunction(residual,null,0);
		alg.initialize(new double[]{1,1,1},1e-10,1e-10);

		for( int i = 0; i < 200 && !alg.iterate(); i++ ) {}

		double found[] = alg.getParameters().clone();

		for( int i = 0; i < found.length; i++ ) {
			assertTrue(found[i]==expected[i]);
		}
	}

	/**
	 * The function can modify the input parameters and
	 */
	@Test
	public void checkAcceptModified() {
		ModifyInputFunctions residuals = new ModifyInputFunctions();

		UnconstrainedMinimization alg = createAlgorithm();

		alg.setFunction(residuals,null,0);
		alg.initialize(new double[]{1,0.5,9.5},1e-10,1e-10);

		for( int i = 0; i < 200 && !alg.iterate(); i++ ) {}

		double found[] = alg.getParameters().clone();
		double expected[] = new double[]{1,2,3};

		for( int i = 0; i < found.length; i++ ) {
			assertTrue(found[i]==expected[i]);
		}
	}

	private class ModifyInputFunctions implements FunctionNtoS {

		@Override
		public int getNumOfInputsN() {
			return 3;
		}

		@Override
		public double process(double[] input ) {
			for( int i = 0; i < input.length; i++ ) {
				input[i] = 1+i;
			}
			return 0;
		}
	}
}
