/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.sym.Sym;
import org.ddogleg.chainrule.sym.SymFunction;
import org.ddogleg.chainrule.sym.SymGraphBuilder;
import org.ddogleg.chainrule.sym.SymOps;
import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.UtilOptimize;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.wrap.LsToNonLinear;
import org.ddogleg.optimization.wrap.LsToNonLinearDeriv;
import org.ejml.data.DMatrixRMaj;

/// Minimizes the Rosenbrock function, as [ExampleUnconstrainedMinimization] does, but the gradient comes from
/// the chain rule instead of being derived by hand. The function is written as a sum of squared residuals
///
///     f(x) = r0² + r1²,   r0 = 10·(x2 − x1²),   r1 = 1 − x1
///
/// and the residuals are written as an expression. The library works out how the operators connect and how
/// derivatives flow between them. Compare with [ExampleChainRuleMinimizationChain], which strings the same
/// operators together by hand, and [ExampleChainRuleMinimizationGraph], which declares the connections itself.
public class ExampleChainRuleMinimizationSym {
	public static void main( String[] args ) {
		var residuals = new Residuals();

		UnconstrainedMinimization optimizer = FactoryOptimization.quasiNewtonBfgs(null);
		optimizer.setVerbose(System.out, 0);

		// f(x) = Σ r_i², and its gradient ∇f = 2·Jᵀ·r, from the residuals and their Jacobian
		optimizer.setFunction(new LsToNonLinear(residuals), new LsToNonLinearDeriv<>(residuals, residuals), 0);

		// [-1.2,  1] is the recommended starting point for testing
		optimizer.initialize(new double[]{-1.2, 1}, 1e-12, 1e-12);
		UtilOptimize.process(optimizer, 500);

		double[] found = optimizer.getParameters();
		System.out.println("Final Error = " + optimizer.getFunctionValue());
		System.out.printf("x[0]: expected=1.00  found=%5.2f\n", found[0]);
		System.out.printf("x[1]: expected=1.00  found=%5.2f\n", found[1]);
	}

	/// The two Rosenbrock residuals and their Jacobian
	public static class Residuals implements FunctionNtoM, FunctionNtoMxN<DMatrixRMaj> {
		final Sym symX = Sym.param("x", 2);
		final SymFunction function;
		final CRArray<double[]> output = new CRArray<>(new double[2], 0);

		DMatrixRMaj jacobian = new DMatrixRMaj(2, 2);

		public Residuals() {
			// x1 = x[0],  x2 = x[1]
			Sym x1 = SymOps.remap(1, symX, 0, -1);
			Sym x2 = SymOps.remap(1, symX, -1, 0);

			// r0 = 10·(x2 − x1²)
			Sym r0 = SymOps.residual(10.0, SymOps.mult(x1, x1), x2);

			// r1 = 1 − x1
			Sym r1 = SymOps.residual(1.0, x1, Sym.constant(1.0));

			// r = [r0, r1]
			function = new SymGraphBuilder().build(SymOps.concat(r0, r1));
			function.feed(symX, new double[2]);
			function.mapParamColumns();

			// every derivative is added into the Jacobian, so it's zeroed before each evaluation
			function.jacobianTo(( row, col, values, numRows, numCols ) -> {
				for (int i = 0; i < numRows; i++) {
					for (int j = 0; j < numCols; j++) {
						jacobian.data[(row + i)*2 + col + j] += values[i*numCols + j];
					}
				}
			});
		}

		/// r(x)
		@Override public void process( double[] input, double[] output ) {
			function.feed(symX, input);
			this.output.wrap(output, 0);
			function.run(this.output);
		}

		/// J(x) = ∂r/∂x
		@Override public void process( double[] input, DMatrixRMaj output ) {
			jacobian = output;
			jacobian.zero();
			function.feed(symX, input);
			function.jacobianRow = 0;
			function.runJacobian();
		}

		@Override public DMatrixRMaj declareMatrixMxN() {return new DMatrixRMaj(2, 2);}

		@Override public int getNumOfInputsN() {return 2;}

		@Override public int getNumOfOutputsM() {return 2;}
	}
}
