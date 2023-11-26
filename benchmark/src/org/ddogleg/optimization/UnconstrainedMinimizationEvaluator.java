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

import org.ddogleg.optimization.derivative.NumericalGradientForward;
import org.ddogleg.optimization.funcs.*;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ddogleg.optimization.wrap.LsToNonLinear;
import org.ddogleg.optimization.wrap.LsToNonLinearDeriv;

/**
 * @author Peter Abeles
 */
public abstract class UnconstrainedMinimizationEvaluator {

	protected boolean verbose;
	protected boolean printSummary;
	protected int maxIteration = 1000;

	protected double ftol = 1e-12;
	protected double gtol = 1e-12;

	protected UnconstrainedMinimizationEvaluator( boolean verbose, boolean printSummary ) {
		this.verbose = verbose;
		this.printSummary = printSummary;
	}

	/**
	 * Creates a line search algorithm
	 *
	 * @return Line search algorithm
	 */
	protected abstract UnconstrainedMinimization createSearch();

	/**
	 * Run the line search algorithm on the two inputs and compute statistics
	 *
	 * @param func Function being searched
	 * @param deriv Derivative being searched
	 * @param initial Initial point
	 * @return statics
	 */
	private NonlinearResults performTest( FunctionNtoS func, FunctionNtoN deriv,
										  double[] initial, double[] optimal, double minimValue ) {
		if (deriv == null) {
			deriv = new NumericalGradientForward(func);
		}

		CallCounterNtoS f = new CallCounterNtoS(func);
		CallCounterNtoN d = new CallCounterNtoN(deriv);

		UnconstrainedMinimization alg = createSearch();
		alg.setFunction(f, d, minimValue);
//		alg.setVerbose(System.out,0);

		double initialValue = func.process(initial);

		alg.initialize(initial, ftol, gtol);
		int iter;
		for (iter = 0; iter < maxIteration && !alg.iterate(); iter++) {
			printError(optimal, alg);
		}
		printError(optimal, alg);
		if (verbose)
			System.out.println("*** total iterations = " + iter);
		double[] found = alg.getParameters();

		double finalValue = func.process(found);

		if (printSummary) {
			System.out.printf("value{ init %4.1e final = %6.2e} count f = %2d d = %2d\n",
					initialValue, finalValue, f.count, d.count);
		}

		NonlinearResults ret = new NonlinearResults();
		ret.numFunction = f.count;
		ret.numGradient = d.count;
		ret.f = finalValue;
		ret.x = found;

		return ret;
	}

	private void printError( double[] optimal, UnconstrainedMinimization alg ) {
		if (optimal != null) {
			double[] x = alg.getParameters();
			double n = 0;
			for (int j = 0; j < x.length; j++) {
				double dx = x[j] - optimal[j];
				n += dx*dx;
			}
			if (verbose)
				System.out.println("||x(k)-x(*)|| = " + Math.sqrt(n));
		}
	}

	private NonlinearResults performTest( EvalFuncLeastSquares func ) {
		FunctionNtoS nl = new LsToNonLinear(func.getFunction());
		double[] initial = func.getInitial();

		FunctionNtoMxN jacobian = func.getJacobian();
		FunctionNtoN gradient = jacobian == null ? null : new LsToNonLinearDeriv(func.getFunction(), jacobian);

		if (verbose && func.getOptimal() != null)
			System.out.println("optimal = " + nl.process(func.getOptimal()));

		return performTest(nl, gradient, initial, func.getOptimal(), 0);
	}

	private NonlinearResults performTest( EvalFuncMinimization func ) {
		double[] initial = func.getInitial();

		FunctionNtoS nl = func.getFunction();

		if (verbose && func.getOptimal() != null)
			System.out.println("optimal = " + nl.process(func.getOptimal()));

		return performTest(nl, func.getGradient(), initial, func.getOptimal(), func.getMinimum());
	}

	public NonlinearResults helicalValley() {
		return performTest(new EvalFuncHelicalValley());
	}

	public NonlinearResults rosenbrock() {
		return performTest(new EvalFuncRosenbrock_DDRM());
	}

	public NonlinearResults rosenbrockMod( double lambda ) {
		return performTest(new EvalFuncRosenbrockMod_DDRM(lambda));
	}

	public NonlinearResults dodcfg() {
		return performTest(new EvalFuncDodcfg(50, 50, 8.0e-3));
	}

	public NonlinearResults variably() {
		return performTest(new EvalFuncVariablyDimensioned(10));
	}

	public NonlinearResults trigonometric() {
		return performTest(new EvalFuncTrigonometric(10));
	}

	public NonlinearResults badlyScaledBrown() {
		return performTest(new EvalFuncBadlyScaledBrown_DDRM());
	}

	public NonlinearResults powell() {
		return performTest(new EvalFuncPowell_DDRM());
	}

	public NonlinearResults powellSingular() {
		return performTest(new EvalFuncPowellSingular_DDRM());
	}

	public NonlinearResults badlyScalledPowell() {
		return performTest(new EvalFuncBadlyScaledPowell_DDRM());
	}
}
