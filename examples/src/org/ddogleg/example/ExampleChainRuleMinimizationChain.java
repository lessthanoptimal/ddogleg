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
import org.ddogleg.chainrule.JacobianSerialChain;
import org.ddogleg.chainrule.MathCRMatrixD;
import org.ddogleg.chainrule.ops.CRErrorVectorSW;
import org.ddogleg.chainrule.ops.CROperatorMult;
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
/// Each residual is a serial chain of operators. A chain is a line, where each operator's output is the next
/// one's input, so a value used in more than one place is the caller's problem: x1 is read three times, and
/// the derivative from each place it's read is added to its column by hand.
/// [ExampleChainRuleMinimizationGraph] declares those connections and lets the library add them up, and
/// [ExampleChainRuleMinimizationSym] writes the same model as an expression.
public class ExampleChainRuleMinimizationChain {
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
		final JacobianSerialChain<double[]> chains = new JacobianSerialChain<>(new MathCRMatrixD());

		// x1²
		final CROperatorMult opSquare = new CROperatorMult(1);
		// r0 = 10·(x2 − x1²)
		final CRErrorVectorSW opR0 = new CRErrorVectorSW(1, 10.0);
		// r1 = 1 − x1
		final CRErrorVectorSW opR1 = new CRErrorVectorSW(1, 1.0);

		final int chainR0, chainR1;

		final CRArray<double[]> x1 = new CRArray<>(new double[1], 0);
		final CRArray<double[]> x2 = new CRArray<>(new double[1], 0);
		final CRArray<double[]> output = new CRArray<>(new double[1], 0);

		DMatrixRMaj jacobian = new DMatrixRMaj(2, 2);
		/// Which residual's row is being differentiated
		int row;

		public Residuals() {
			// x1² = x1·x1. The chain's input is the first x1, and the second is parameter column 0
			int blockSquare = chains.add(opSquare).in(CROperatorMult.IN_B, 0).lastBlockId();
			// r0 = 10·(x2 − x1²). x1² comes from the block before, and x2 is parameter column 1
			int blockR0 = chains.add(opR0).in(1, 1).lastBlockId();
			// r1 = 1 − x1. The chain's input is x1, and the constant 1 is not a parameter
			int blockR1 = chains.add(opR1).lastBlockId();
			opR1.setObserved(new double[]{1.0}, 0);

			chainR0 = chains.chain(blockSquare, blockR0);
			chainR1 = chains.chain(blockR1);

			// ∂r/∂x for the inputs mapped to parameters
			chains.setPublishJac(( col, jac ) -> add(col, jac));

			// A chain's input isn't a parameter as far as the chain knows, but here it's x1. Its derivative is
			// captured and added to column 0, on top of what the second x1 in x1·x1 already added there:
			//     ∂r0/∂x1 = ∂r0/∂(x1²)·(x1 + x1)
			chains.setCapture(blockSquare, CROperatorMult.IN_A, ( jac ) -> add(0, jac));
			//     ∂r1/∂x1
			chains.setCapture(blockR1, 0, ( jac ) -> add(0, jac));
		}

		/// r(x)
		@Override public void process( double[] input, double[] output ) {
			setInputs(input);
			chains.forward(chainR0, x1, this.output.wrap(output, 0));
			chains.forward(chainR1, x1, this.output.wrap(output, 1));
		}

		/// J(x) = ∂r/∂x
		@Override public void process( double[] input, DMatrixRMaj output ) {
			jacobian = output;
			jacobian.zero();
			setInputs(input);

			row = 0;
			chains.backwardsF(chainR0, x1);
			row = 1;
			chains.backwardsF(chainR1, x1);
		}

		/// Inputs that aren't a chain's input are handed to their operator by the caller
		void setInputs( double[] input ) {
			x1.a[0] = input[0];
			x2.a[0] = input[1];
			opSquare.setInput(CROperatorMult.IN_B, x1);
			opR0.setInput(1, x2);
		}

		/// Adds a derivative into the Jacobian. Derivatives of the same parameter from different places sum
		void add( int col, CRMatrix<double[]> jac ) {
			for (int j = 0; j < jac.cols; j++) {
				jacobian.data[row*2 + col + j] += jac.d(0, j);
			}
		}

		@Override public DMatrixRMaj declareMatrixMxN() {return new DMatrixRMaj(2, 2);}

		@Override public int getNumOfInputsN() {return 2;}

		@Override public int getNumOfOutputsM() {return 2;}
	}
}
