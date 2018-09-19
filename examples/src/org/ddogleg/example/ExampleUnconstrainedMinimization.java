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

package org.ddogleg.example;

import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.UtilOptimize;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;

/**
 * Example of unconstrained minimization using Quasi Newton BFGS. The function being optimized is from [1]
 * a function used to test optimization routines.
 *
 * [1] ROSENBROCK, H.H. An automatm method for finding the greatest or least value of a function.
 * Comput. J. 3 (1960), 175-184.
 *
 * @author Peter Abeles
 */
public class ExampleUnconstrainedMinimization {
	public static void main(String[] args) {

		UnconstrainedMinimization optimizer = FactoryOptimization.quasiNewtonBfgs(null);

		// Send to standard out progress information
		optimizer.setVerbose(System.out,0);

		// Provide an analytical gradient to the Rosenbrock function.
		optimizer.setFunction(new Rosenbrock(),new Gradient(),0);

		// [-1.2,  1] is the recommended starting point for testing
		optimizer.initialize(new double[]{-1.2,1},1e-12,1e-12);

		// iterate 500 times or until it converges.
		// Manually iteration is possible too if more control over is required
		UtilOptimize.process(optimizer,500);

		double found[] = optimizer.getParameters();

		// see how accurately it found the solution
		System.out.println("Final Error = "+optimizer.getFunctionValue());

		// Compare the actual parameters to the found parameters
		System.out.printf("x[0]: expected=1.00  found=%5.2f\n",found[0]);
		System.out.printf("x[1]: expected=1.00  found=%5.2f\n",found[1]);
	}

	/**
	 * A classic function used to test optimization routines
	 */
	public static class Rosenbrock implements FunctionNtoS {

		@Override
		public int getNumOfInputsN() { return 2; }

		@Override
		public double process(double[] input) {
			double x1 = input[0];
			double x2 = input[1];
			double f1 = 10*(x2 - x1*x1);
			double f2 = 1-x1;
			return f1*f1 + f2*f2;
		}
	}

	/**
	 * Gradient of the Rosenbrock function. You can check analytical gradients using the DerivativeChecker class.
	 */
	public static class Gradient implements FunctionNtoN {

		@Override
		public int getN() { return 2; }

		@Override
		public void process(double[] input, double[] output) {
			double x1 = input[0];
			double x2 = input[1];

			output[0] = -400*(x2-x1*x1)*x1 - 2*(1-x1);
			output[1] = 200*(x2-x1*x1);
		}
	}
}
