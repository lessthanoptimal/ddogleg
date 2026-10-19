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
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.JacobianChainGraph;
import org.ddogleg.chainrule.MathCRMatrixD;
import org.ddogleg.chainrule.ops.CRErrorVectorSW;
import org.ddogleg.chainrule.ops.CROperatorConcat;
import org.ddogleg.chainrule.ops.CROperatorMult;
import org.ddogleg.chainrule.ops.CROperatorRemapParams;
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
/// The operators are the ones in [ExampleChainRuleMinimizationChain], but here x1 is computed once and each
/// place that reads it is declared as a dependency. The graph then adds up the derivatives arriving at x1 from
/// all three places it's read. [ExampleChainRuleMinimizationSym] writes the same model as an expression.
public class ExampleChainRuleMinimizationGraph {
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
		final JacobianChainGraph<double[]> graphs = new JacobianChainGraph<>(new MathCRMatrixD());

		// x1 = x[0]
		final CROperatorRemapParams opX1 = new CROperatorRemapParams(1, 0, -1);
		// x1²
		final CROperatorMult opSquare = new CROperatorMult(1);
		// r0 = 10·(x2 − x1²)
		final CRErrorVectorSW opR0 = new CRErrorVectorSW(1, 10.0);
		// r = [r0, r1]
		final CROperatorConcat opConcat = new CROperatorConcat(1, 1);
		// r1 = 1 − x1
		final CRErrorVectorSW opR1 = new CRErrorVectorSW(1, 1.0);

		final int chainX1, graph;

		final CRArray<double[]> x = new CRArray<>(new double[2], 0);
		final CRArray<double[]> x2 = new CRArray<>(new double[1], 0);
		final CRArray<double[]> output = new CRArray<>(new double[2], 0);

		DMatrixRMaj jacobian = new DMatrixRMaj(2, 2);

		public Residuals() {
			var chains = graphs.getChains();

			int blockX1 = chains.add(opX1).lastBlockId();
			int blockSquare = chains.add(opSquare).lastBlockId();
			// x2 is parameter column 1
			int blockR0 = chains.add(opR0).in(1, 1).lastBlockId();
			int blockConcat = chains.add(opConcat).lastBlockId();
			int blockR1 = chains.add(opR1).lastBlockId();
			opR1.setObserved(new double[]{1.0}, 0);

			// x → x1
			chainX1 = chains.chain(blockX1);
			// x1² → r0 → [r0, r1]
			int chainR0 = chains.chain(blockSquare, blockR0, blockConcat);
			// x1 → r1
			int chainR1 = chains.chain(blockR1);

			// ∂r/∂x for the inputs mapped to parameters
			chains.setPublishJac(( col, jac ) -> add(col, jac));
			// x is fed in by the caller. Its derivative is ∂r/∂x1 summed over every place x1 is read, times
			//     ∂x1/∂x = [1, 0]
			chains.setCapture(blockX1, 0, ( jac ) -> add(0, jac));

			graph = graphs.graph()
					// x1·x1 reads x1 twice
					.depMid(chainX1, 0, chainR0, 0, CROperatorMult.IN_A)
					.depMid(chainX1, 0, chainR0, 0, CROperatorMult.IN_B)
					// 1 − x1 reads it a third time
					.depMid(chainX1, 0, chainR1, 0, 0)
					// r1 is the second half of [r0, r1]
					.depMid(chainR1, 0, chainR0, 2, 1)
					.fixate();
		}

		/// r(x)
		@Override public void process( double[] input, double[] output ) {
			setInputs(input);
			graphs.forward(graph).input(chainX1, x).run(this.output.wrap(output, 0));
		}

		/// J(x) = ∂r/∂x
		@Override public void process( double[] input, DMatrixRMaj output ) {
			jacobian = output;
			jacobian.zero();
			setInputs(input);
			graphs.forward(graph).input(chainX1, x).run(null);
			graphs.backwards(graph);
		}

		/// Inputs that aren't part of the graph are handed to their operator by the caller
		void setInputs( double[] input ) {
			System.arraycopy(input, 0, x.a, 0, 2);
			x2.a[0] = input[1];
			opR0.setInput(1, x2);
		}

		/// Adds a derivative into the Jacobian. Both residuals are rows of the graph's single output
		void add( int col, CRMatrix<double[]> jac ) {
			for (int i = 0; i < jac.rows; i++) {
				for (int j = 0; j < jac.cols; j++) {
					jacobian.data[i*2 + col + j] += jac.d(i, j);
				}
			}
		}

		@Override public DMatrixRMaj declareMatrixMxN() {return new DMatrixRMaj(2, 2);}

		@Override public int getNumOfInputsN() {return 2;}

		@Override public int getNumOfOutputsM() {return 2;}
	}
}
