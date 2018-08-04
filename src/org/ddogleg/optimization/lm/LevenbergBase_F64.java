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

package org.ddogleg.optimization.lm;

import org.ddogleg.optimization.math.MatrixMath;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.ReshapeMatrix;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;

/**
 * <p>
 * Base class for Levenberg-Marquardt non-linear least squares optimization methods. At each iteration these techniques
 * solve a system of the form:<br>
 * (G[k] + v*I)x[k] = -g[k] with v &ge; 0 <br>
 * where G[k] = F[k]<sup>T</sup>F[k] is an approximation to the hessian and is positive semi-definite, F[k] is
 * the Jacobian, v is a tuning parameter that is either a scalar or a vector, x[k] is the step being estimated,
 * and g[k] is the gradient.
 * </p>
 *
 * <p>
 *     Levenberg-Marquardt is a trust-region method but was formulated before trust-region methods had been defined.
 *     At each iteration it adjusted the value of 'v''. For smaller values it is closer to a Gauss-Newton step
 *     and has super linear convergence while for large values it becomes a gradient step
 * </p>
 *
 * <ul>
 * <li>[1] JK. Madsen and H. B. Nielsen and O. Tingleff, "Methods for Non-Linear Least Squares Problems (2nd ed.)"
 * Informatics and Mathematical Modelling, Technical University of Denmark</li>
 * <li>[2] Peter Abeles, "DDogleg Technical Report: Nonlinear Optimization", Revision 1, August 2018</li>
 * </ul>
 *
 * @author Peter Abeles
 */
public abstract class LevenbergBase_F64<S extends DMatrix> {

	/**
	 * Configuration
	 */
	protected ConfigLevenbergMarquardt config;

	// Math for some matrix operations
	protected MatrixMath<S> math;

	/**
	 * Storage for Jacobian
	 */
	S Jacobian;

	DMatrixRMaj gradient = new DMatrixRMaj(1,1);
	DMatrixRMaj residuals = new DMatrixRMaj(1,1);

	// Current parameter state
	protected DMatrixRMaj x = new DMatrixRMaj(1,1);
	// proposed next state of parameters
	protected DMatrixRMaj x_next = new DMatrixRMaj(1,1);
	// proposed relative change in parameter's state
	protected DMatrixRMaj p = new DMatrixRMaj(1,1);

	// error function at x
	protected double fx;

	/**
	 * Dampening parameter. Scalar that's adjusted at every step. smaller values for a Gauss-Newton step and larger
	 * values for a gradient step
	 */
	protected double lambda;
	protected double nu;

	protected Mode mode = Mode.FULL_STEP;

	protected boolean verbose=false;

	// number of each type of step it has taken
	protected int totalFullSteps, totalRetries;

	public LevenbergBase_F64( MatrixMath<S> math ) {
		setConfiguration(new ConfigLevenbergMarquardt());
		this.math = math;

		Jacobian = math.createMatrix();
	}
	public void initialize(double initial[] , int numberOfParameters , int numberOfFunctions ) {
		lambda = config.lambdaInitial;
		nu = 2;

		totalFullSteps = 0;
		totalRetries = 0;

		((ReshapeMatrix)Jacobian).reshape(numberOfFunctions,numberOfParameters);
		gradient.reshape(numberOfParameters,1);
		x.reshape(numberOfParameters,1);
		x_next.reshape(numberOfParameters,1);
		p.reshape(numberOfParameters,1);

		computeResiduals(x_next,residuals);
		fx = cost(residuals);

		if( checkConvergenceFTest(residuals)) {
			mode = Mode.CONVERGED;
		} else {
			mode = Mode.FULL_STEP;
		}
	}

	public boolean iterate() {
		boolean converged;
		switch( mode ) {
			case FULL_STEP:
				totalFullSteps++;
				converged = updateState();
				if( !converged )
					converged = computeAndConsiderNew();
				break;

			case RETRY:
				totalRetries++;
				converged = computeAndConsiderNew();
				break;

			case CONVERGED:
				return true;

			default:
				throw new RuntimeException("BUG! mode="+mode);
		}

		if( converged ) {
			mode = Mode.CONVERGED;
			return true;
		} else {
			return false;
		}
	}

	protected boolean updateState() {
		computeJacobian(x,true,Jacobian);

		math.multTransA(Jacobian,residuals,gradient);

		if( checkConvergenceGTest(gradient) )
			return true;

		mode = Mode.RETRY;

		return false;
	}

	protected boolean computeAndConsiderNew() {
		double predictedReduction = predictedReduction(x,gradient,lambda);

		// compute the new location and it's score
		computeStep(lambda,Jacobian,residuals,p);
		CommonOps_DDRM.add(x,p,x_next);
		computeResiduals(x_next,residuals);
		double fx_candidate = cost(residuals);

		// compute model prediction accuracy
		double actualReduction = fx-fx_candidate;

		if( actualReduction == 0 || predictedReduction == 0 ) {
			if( verbose )
				System.out.println(totalFullSteps+" reduction of zero");
			return true;
		}

		double ratio = actualReduction/predictedReduction;
		boolean accepted;

		if( fx_candidate < fx && ratio > 0 ) {
			// Step has been accepted
			// TODO swap states
			mode = Mode.FULL_STEP;
			// reduce the amount of dampening.  Magic equation from [1].  My attempts to improve
			// upon it have failed.  It is truly magical.
			lambda = lambda*Math.max(1.0/3.0, 1.0-Math.pow(2.0*ratio-1.0,3.0));
			nu=2;
			accepted = true;
		} else {
			lambda *= nu;
			nu *= 2;
			accepted = false;
		}

		if( verbose )
			System.out.println(totalFullSteps+" fx_candidate="+fx_candidate+" ratio="+ratio+" lambda="+lambda);

		if( accepted )
			return checkConvergenceFTest(residuals);
		else
			return false;
	}

	/**
	 * <p>Checks for convergence using f-test. f-test is defined differently for different problems</p>
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	protected boolean checkConvergenceFTest( DMatrixRMaj residuals ) {
		for (int i = 0; i < residuals.numRows; i++) {
			if( Math.abs(residuals.data[i]) > config.ftol )
				return false;
		}
		return true;
	}

	/**
	 * <p>Checks for convergence using f-test:</p>
	 *
	 * g-test : gtol &le; ||g(x)||_inf
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	protected boolean checkConvergenceGTest( DMatrixRMaj g ) {
		return CommonOps_DDRM.elementMaxAbs(g) <= config.gtol;
	}

	public double cost( DMatrixRMaj residuals ) {
		return 0.5*SpecializedOps_DDRM.elementSumSq(residuals);
	}

	protected abstract void computeJacobian(DMatrixRMaj x , boolean sameStateAsResiduals , S jacobian );

	protected abstract void computeResiduals( DMatrixRMaj x , DMatrixRMaj residuals );

	protected abstract void computeStep( double lambda, S jacobian, DMatrixRMaj residuals , DMatrixRMaj p );

	protected abstract double predictedReduction( DMatrixRMaj param, DMatrixRMaj gradient , double lambda );

	public void setConfiguration( ConfigLevenbergMarquardt config ) {
		this.config = config.copy();
	}

	enum Mode {
		FULL_STEP,
		RETRY,
		CONVERGED
	}
}
