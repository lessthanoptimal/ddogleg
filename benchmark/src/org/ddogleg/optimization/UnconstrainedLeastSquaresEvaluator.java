/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.optimization.derivative.NumericalJacobianForward_DDRM;
import org.ddogleg.optimization.derivative.NumericalJacobianForward_DSCC;
import org.ddogleg.optimization.funcs.EvalFuncLeastSquares;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ejml.data.DMatrix;

public abstract class UnconstrainedLeastSquaresEvaluator<M extends DMatrix> {

	protected boolean verbose = true;
	protected boolean printSummary;
	protected int maxIteration = 500;
	protected boolean dense;

	protected UnconstrainedLeastSquaresEvaluator( boolean verbose, boolean printSummary,
												  boolean dense ) {
		this.verbose = verbose;
		this.printSummary = printSummary;
		this.dense = dense;
	}

	/**
	 * Creates a line search algorithm
	 *
	 * @return Line search algorithm
	 */
	protected abstract UnconstrainedLeastSquares<M> createSearch( double minimumValue );

	/**
	 * Run the line search algorithm on the two inputs and compute statistics
	 *
	 * @param func Function being searched
	 * @param deriv Derivative being searched
	 * @param initial Initial point
	 * @return Statistics
	 */
	private NonlinearResults performTest( FunctionNtoM func, FunctionNtoMxN<M> deriv,
										  double[] initial, double[] optimal, double minimValue ) {
		if (deriv == null) {
			if (dense)
				deriv = (FunctionNtoMxN)new NumericalJacobianForward_DDRM(func);
			else
				deriv = (FunctionNtoMxN)new NumericalJacobianForward_DSCC(func);
		}

		var f = new CallCounterNtoM(func);
		var d = new CallCounterNtoMxN<M>(deriv);
		var loss = new CallCounterLossSquared();

		UnconstrainedLeastSquares<M> alg = createSearch(minimValue);
		alg.setFunction(f, d);
		alg.setLoss(loss, null);

		alg.initialize(initial, 1e-10, 1e-6);
		double initialError = alg.getFunctionValue();
		int iter;
		// number of times the parameters have been updated
		int updateCounter = 0;
		for (iter = 0; iter < maxIteration && !alg.iterate(); iter++) {
			if (alg.isUpdated())
				updateCounter++;

			if (verbose && alg.isUpdated()) {
				double error = alg.getFunctionValue();
				System.out.println("  error = " + error);
			}
		}
		if (verbose)
			System.out.println("*** total iterations = " + iter);
		double[] found = alg.getParameters();
		double finalError = alg.getFunctionValue();

		if (printSummary) {
			// compute distance from optimal solution if one is provided
			double dist = Double.NaN;
			if (optimal != null) {
				dist = 0;
				for (int i = 0; i < func.getNumOfInputsN(); i++) {
					dist += Math.pow(found[i] - optimal[i], 2);
				}
				dist = Math.sqrt(dist);
			}

			System.out.printf("value{ init %7.1e final = %7.2e} optimal %7.1e count f = %2d d = %2d\n",
					initialError, finalError, dist, f.count, d.count);
		}

		// Everytime the state is updated the Loss's fixate should be called.
		// Tolerance of 2 comes from initialization and how it converges. Can't be bothered to take it all in account
		if (Math.abs(updateCounter - loss.countFixate) > 2)
			throw new RuntimeException("fixate in loss hasn't been called enough");

		var ret = new NonlinearResults();
		ret.numFunction = f.count;
		ret.numGradient = d.count;
		ret.f = finalError;
		ret.x = found;

		return ret;
	}

	NonlinearResults performTest( EvalFuncLeastSquares<M> func ) {
		double[] initial = func.getInitial();

		return performTest(func.getFunction(), func.getJacobian(), initial, func.getOptimal(), 0);
	}
}
