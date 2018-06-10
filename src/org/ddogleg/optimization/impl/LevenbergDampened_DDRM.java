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

import org.ddogleg.optimization.functions.CoupledJacobian;
import org.ejml.LinearSolverSafe;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.dense.row.mult.MatrixMultProduct_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

/**
 * <p>
 * Implementation of Levenberg's algorithm which explicitly computes the J(x)'J(x) matrix and iteratively
 * adjusts the dampening parameter.  If a candidate sample point decreases the score then it is accepted
 * ad the dampening parameter adjusted according to [1].  Because it immediately accepts any decrease
 * it will tend to require more function and gradient calculations and less linear solutions. Explicitly
 * computing J(x)'J(x) improves the linear solver's speed at the cost of some numerical precision.
 * </p>
 *
 * <p>
 * The step 'x' is computed using the following formula:
 * (J(k)'*J(k) + &mu;*I)x = -g = -J'*f<br>
 * where J is the Jacobian, &mu; is the damping coefficient, I is an identify matrix, g is the gradient,
 * f is the functions output.
 * </p>
 *
 * <p>
 * Unlike some implementations, the option for a scaling matrix is not provided.  Scaling can be done inside
 * the function itself and would add even more complexity to the code. The dampening parameter is updated
 * using the equation below from [1]: <br>
 * damp = damp * max( 1/3 , 1 - (2*ratio-1)^3 )<br>
 * where ratio is the actual reduction over the predicted reduction.
 * </p>
 *
 * <p>
 * [1] K. Madsen and H. B. Nielsen and O. Tingleff, "Methods for Non-Linear Least Squares Problems (2nd ed.)"
 * Informatics and Mathematical Modelling, Technical University of Denmark
 * </p>
 *
 * @author Peter Abeles
 */
// After some minor modifications it was compared against Matlab code in [1] and produced identical results
// in each step.  Stopping conditions and initialization is a bit different.
public class LevenbergDampened_DDRM extends LevenbergBase_DDRM {

	// solver used to compute (A + mu*diag(A))d = g
	protected LinearSolverDense<DMatrixRMaj> solver;

	/**
	 * Specifies termination condition and dampening parameter
	 *
	 * @param initialDampParam Initial value of the dampening parameter.  Tune.. try 1e-3;
	 */
	public LevenbergDampened_DDRM(double initialDampParam) {
		super(initialDampParam);
	}


	@Override
	protected void computeJacobian( DMatrixRMaj residuals , DMatrixRMaj gradient) {
		// calculate the Jacobian values at the current sample point
		function.computeJacobian(jacobianVals);

		// compute helper matrices
		// B = J'*J;   g = J'*r
		// Take advantage of symmetry when computing B and only compute the upper triangular
		// portion used by cholesky decomposition
		MatrixMultProduct_DDRM.inner_reorder_upper(jacobianVals, B);
		CommonOps_DDRM.multTransA(jacobianVals, residuals, gradient);

		// extract diagonal elements from B
		CommonOps_DDRM.extractDiag(B, Bdiag);
	}

	@Override
	protected boolean computeStep(double lambda, DMatrixRMaj gradientNegative , DMatrixRMaj step) {
		// add dampening parameter
		for( int i = 0; i < N; i++ ) {
			int index = B.getIndex(i,i);
			B.data[index] = Bdiag.data[i] + lambda;
		}

		// compute the change in step.
		if( !solver.setA(B) ) {
			return false;
		}
		// solve for change in x
		solver.solve(gradientNegative, step);
		return true;
	}

	/**
	 * Specifies function being optimized.
	 *
	 * @param function Computes residuals and Jacobian.
	 */
	@Override
	public void setFunction( CoupledJacobian<DMatrixRMaj> function ) {
		super.setFunction(function);

		solver = LinearSolverFactory_DDRM.symmPosDef(N);
		this.solver = new LinearSolverSafe<>(solver);
	}

	/**
	 * compute the change predicted by the model
	 *
	 * m_k(0) - m_k(p_k) = -g_k'*p - 0.5*p'*B*p
	 * (J'*J+mu*I)*p = -J'*r = -g
	 *
	 * @return predicted reduction
	 */
	@Override
	protected double predictedReduction( DMatrixRMaj param, DMatrixRMaj gradientNegative , double mu ) {
		double p_dot_p = VectorVectorMult_DDRM.innerProd(param,param);
		double p_dot_g = VectorVectorMult_DDRM.innerProd(param,gradientNegative);
		return 0.5*(mu*p_dot_p + p_dot_g);
	}
}
