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

import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.math.HessianMath;
import org.ddogleg.optimization.math.MatrixMath;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;

/**
 * <p>
 * Implementation of Levenberg-Marquardt non-linear least squares optimization. At each iteration the following
 * is sovled for:<br>
 * (G[k] + v*()x[k] = -g[k] with v &ge; 0 <br>
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
 * <p>
 *     Levenberg's formulation is as follows:<br>
 *     (J<sup>T</sup>J + &lambda I) = J<sup>T</sup>r<br>
 *     where &lambda; is adjusted at each iteration.
 * </p>
 * <p>
 *     Marquardt's formulation is as follows:<br>
 *     (J<sup>T</sup>J + &lambda diag(J<sup>T</sup>J )) = J<sup>T</sup>r<br>
 *     where &lambda; is adjusted at each iteration.
 * </p>
 *
 * <ul>
 * <li>[1] JK. Madsen and H. B. Nielsen and O. Tingleff, "Methods for Non-Linear Least Squares Problems (2nd ed.)"
 * Informatics and Mathematical Modelling, Technical University of Denmark</li>
 * <li>[2] Peter Abeles, "DDogleg Technical Report: Nonlinear Optimization", Revision 1, August 2018</li>
 * <li>[3] Levenberg, Kenneth (1944). "A Method for the Solution of Certain Non-Linear Problems in Least Squares".
 *         Quarterly of Applied Mathematics. 2: 164–168.<</li>
 * </ul>
 *
 * @author Peter Abeles
 */
public abstract class LevenbergMarquardt_F64<S extends DMatrix, HM extends HessianMath>
{
	// TODO Add scaling
	/**
	 * Configuration
	 */
	protected ConfigLevenbergMarquardt config;

	// Math for some matrix operations
	protected MatrixMath<S> math;

	protected HM hessian;

	DMatrixRMaj gradient = new DMatrixRMaj(1,1);
	DMatrixRMaj residuals = new DMatrixRMaj(1,1);

	DMatrixRMaj diagOrig = new DMatrixRMaj(1,1);
	DMatrixRMaj diagStep = new DMatrixRMaj(1,1);

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

	// TODO comment
	protected double nu;
	private final static double NU_INITIAL = 2;

	protected Mode mode = Mode.FULL_STEP;

	protected boolean verbose=false;

	// number of each type of step it has taken
	protected int totalFullSteps, totalRetries;

	public LevenbergMarquardt_F64(MatrixMath<S> math , HM hessian ) {
		configure(new ConfigLevenbergMarquardt());
		this.math = math;
		this.hessian = hessian;
	}

	/**
	 * Initializes the search.
	 *
	 * @param initial Initial state
	 * @param numberOfParameters number of parameters
	 * @param numberOfFunctions number of functions
	 */
	public void initialize(double initial[] , int numberOfParameters , int numberOfFunctions ) {
		lambda = config.dampeningInitial;
		nu = NU_INITIAL;

		totalFullSteps = 0;
		totalRetries = 0;

		// Declare storage
		gradient.reshape(numberOfParameters,1);
		x.reshape(numberOfParameters,1);
		x_next.reshape(numberOfParameters,1);
		p.reshape(numberOfParameters,1);
		residuals.reshape(numberOfFunctions,1);

		diagOrig.reshape(numberOfParameters,1);
		diagStep.reshape(numberOfParameters,1);

		hessian.init(numberOfParameters);

		// Set the initial state
		System.arraycopy(initial,0,x.data,0,numberOfParameters);
		computeResiduals(x,residuals);
		fx = cost(residuals);

		if( checkConvergenceFTest(residuals)) {
			mode = Mode.CONVERGED;
		} else {
			mode = Mode.FULL_STEP;
		}
	}

	/**
	 * Performs a single step in the iteration
	 *
	 * @return returns true if it has converged and false otherwise
	 */
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

	/**
	 * Computes derived
	 * @return true if it has converged or false if it has not
	 */
	protected boolean updateState() {
		computeGradientHessian(x,true, gradient, hessian);
		hessian.extractDiagonals(diagOrig);

		if( checkConvergenceGTest(gradient) )
			return true;

		mode = Mode.RETRY;

		return false;
	}

	/**
	 * Compute a new possible state and determine if it should be accepted or not. If accepted update the state
	 * @return true if it has converged or false if it has not
	 */
	protected boolean computeAndConsiderNew() {

		// compute the new location and it's score
		if( !computeStep(lambda,gradient,p) ) {
			if( config.mixture == 0.0 ) {
				throw new OptimizationException("Singular matrix encountered. Try setting mixture to a non-zero value");
			}
			lambda *= 4;
			if( verbose )
				System.out.println(totalFullSteps+" Step computation failed. Increasing lambda");
			return false;
		}

		// compute the potential new state
		CommonOps_DDRM.add(x,p,x_next);
		computeResiduals(x_next,residuals);
		double fx_candidate = cost(residuals);

		if(UtilEjml.isUncountable(fx_candidate) )
			throw new OptimizationException("Uncountable candidate score: "+fx_candidate);

		// compute model prediction accuracy
		double actualReduction = fx-fx_candidate;
		double predictedReduction = computePredictedReduction(p);

		if( actualReduction == 0 || predictedReduction == 0 ) {
			if( verbose )
				System.out.println(totalFullSteps+" reduction of zero");
			return true;
		}

		double ratio = actualReduction/predictedReduction;
		boolean accepted;

		// Accept the new state if the score improved
		if( fx_candidate < fx ) {
			// reduce the amount of dampening.  Magic equation from [1].  My attempts to improve
			// upon it have failed.  It is truly magical.
			lambda = lambda*Math.max(1.0/3.0, 1.0-Math.pow(2.0*ratio-1.0,3.0));
			nu=NU_INITIAL;
			accepted = true;
		} else {
			lambda *= nu;
			nu *= 2;
			accepted = false;
		}

		if( UtilEjml.isUncountable(lambda) || UtilEjml.isUncountable(nu) )
			throw new OptimizationException("lambda="+lambda+"  nu="+nu);

		if( verbose )
			System.out.println(totalFullSteps+" fx_delta="+(fx_candidate-fx)+" ratio="+ratio+" lambda="+lambda);

		if( accepted ) {
			acceptNewState(fx_candidate);
			return checkConvergenceFTest(residuals);
		} else
			return false;
	}

	/**
	 * The new state has been accepted. Switch the internal state over to this
	 * @param fx_candidate
	 */
	private void acceptNewState( double fx_candidate ) {
		DMatrixRMaj tmp = x;
		x = x_next;
		x_next = tmp;

		fx = fx_candidate;

		mode = Mode.FULL_STEP;
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
		for (int i = 0; i < g.numRows; i++) {
			if( Math.abs(g.data[i]) > config.gtol )
				return false;
		}
		return true;
	}

	/**
	 * Given the residuals compute the cost
	 * @param residuals (Input) residuals/errors
	 * @return Least squares cost
	 */
	public double cost( DMatrixRMaj residuals ) {
		return 0.5*SpecializedOps_DDRM.elementSumSq(residuals);
	}

	/**
	 * Adjusts the Hessian's diagonal elements value and computes the next step
	 *
	 * @param lambda (Input) tuning
	 * @param gradient (Input) gradient
	 * @param step (Output) step
	 * @return true if solver could compute the next step
	 */
	protected boolean computeStep( double lambda, DMatrixRMaj gradient , DMatrixRMaj step ) {

		final double mixture = config.mixture;
		for (int i = 0; i < diagOrig.numRows; i++) {
			double v = Math.abs(diagOrig.data[i]);
			diagStep.data[i] = v + lambda*(mixture + (1.0-mixture)*v);
		}
		hessian.setDiagonals( diagStep );

		if( !hessian.initializeSolver()) {
			return false;
		}

		if( hessian.solve(gradient,step) ) {
			CommonOps_DDRM.scale(-1, step);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Computes predicted reduction for step 'p'
	 *
	 * @param p Change in state or the step
	 * @return predicted reduction in quadratic model
	 */
	public double computePredictedReduction( DMatrixRMaj p ) {
		return -CommonOps_DDRM.dot(gradient,p) - 0.5*hessian.innerVectorHessian(p);
	}

	/**
	 * Computes the gradient and updates Hessian given the current state
	 * @param x (Input) Current state
	 * @param sameStateAsResiduals If true then this is the same value of x when {@link #computeResiduals} was called last
	 * @param gradient (Output) gradient at x
	 * @param hessian (Output) hessian at x
	 */
	protected abstract void computeGradientHessian(DMatrixRMaj x , boolean sameStateAsResiduals , DMatrixRMaj gradient , HM hessian );

	/**
	 * Computes the residuals at state 'x'
	 * @param x (Input) state
	 * @param residuals (Output) residuals F(x) - Y
	 */
	protected abstract void computeResiduals( DMatrixRMaj x , DMatrixRMaj residuals );

	/**
	 * Changes the optimization's configuration
	 * @param config New configuration
	 */
	public void configure(ConfigLevenbergMarquardt config ) {
		this.config = config.copy();
	}

	enum Mode {
		FULL_STEP,
		RETRY,
		CONVERGED
	}
}
