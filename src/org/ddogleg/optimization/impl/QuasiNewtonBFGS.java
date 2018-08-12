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

import org.ddogleg.optimization.LineSearch;
import org.ddogleg.optimization.functions.GradientLineFunction;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import java.io.PrintStream;

/**
 * <p>
 * Quasi-Newton nonlinear optimization using BFGS update on the approximate inverse Hessian with
 * a line search.  The function and its gradient is required.  If no gradient is available then a numerical
 * gradient will be used.  The line search must meet the Wolfe or strong Wolfe condition.  This
 * technique is automatically scale invariant and no scale matrix is required.  In most situations
 * super-linear convergence can be expected. Based on the description provided in [1].
 * </p>
 *
 * <p>
 * The inverse Hessian update requires only a rank-2 making it efficient.  Stability requires that the
 * line search maintain the Wolfe or strong Wolfe condition or else the inverse Hessian matrix can stop
 * being symmetric positive definite.
 * </p>
 *
 * <p>
 * [1] Jorge Nocedal, Stephen J. Wright, "Numerical Optimization" 2nd Ed, 2006 Springer
 * </p>
 * @author Peter Abeles
 */
public class QuasiNewtonBFGS
{
	// number of inputs
	private int N;

	// convergence conditions for change in function value, relative
	private double ftol;
	// convergence condition based on gradient norm. absolute
	private double gtol;

	// function being minimized and its gradient
	private GradientLineFunction function;

	// ----- variables and classes related to line search
	// searches for a parameter that meets the Wolfe condition
	private LineSearch lineSearch;
	private double funcMinValue;
	// from wolfe condition.  Used to estimate max line search step
	// This must be the same as what's specified internally in 'lineSearch'
	private double lineGTol;
	// derivative at the start of the line search
	private double derivAtZero;

	// inverse of the Hessian approximation
	private DMatrixRMaj B;
	// search direction
	private DMatrixRMaj searchVector;
	// gradient
	private DMatrixRMaj g;
	// difference between current and previous x
	private DMatrixRMaj s;
	// difference between current and previous gradient
	private DMatrixRMaj y;
	
	// current set of parameters being considered
	private DMatrixRMaj x;
	// function value at x(k)
	private double fx;

	// storage
	private DMatrixRMaj temp0_Nx1;
	private DMatrixRMaj temp1_Nx1;

	// mode that the algorithm is in
	private int mode;

	// error message
	private PrintStream verbose;
	// if it converged to a solution or not
	private boolean hasConverged;

	// How many full processing cycles have there been
	private int iterations;
	// was 'x' update this iteration?
	private boolean updated;

	// used when selecting an initial step.
	double initialStep, maxStep;
	boolean firstStep;

	/**
	 * Configures the search.
	 *
	 * @param function Function being optimized
	 * @param lineSearch Line search that selects a solution that meets the Wolfe condition.
	 * @param funcMinValue Minimum possible function value. E.g. 0 for least squares.
	 */
	public QuasiNewtonBFGS( GradientLineFunction function ,
							LineSearch lineSearch ,
							double funcMinValue )
	{
		this.lineSearch = lineSearch;
		this.funcMinValue = funcMinValue;
		this.function = function;

		lineSearch.setFunction(function);

		N = function.getN();
		
		B = new DMatrixRMaj(N,N);
		searchVector = new DMatrixRMaj(N,1);
		g = new DMatrixRMaj(N,1);
		s = new DMatrixRMaj(N,1);
		y = new DMatrixRMaj(N,1);
		x = new DMatrixRMaj(N,1);

		temp0_Nx1 = new DMatrixRMaj(N,1);
		temp1_Nx1 = new DMatrixRMaj(N,1);
	}

	/**
	 * Specify convergence tolerances
	 *
	 * @param ftol Relative error tolerance for function value  0 {@code <=} ftol {@code <=} 1
	 * @param gtol Absolute convergence based on gradient norm  0 {@code <=} gtol
	 * @param lineGTol Slope coefficient for wolfe condition used in line search. 0 {@code <} lineGTol
	 */
	public void setConvergence( double ftol , double gtol , double lineGTol ) {
		if( ftol < 0 )
			throw new IllegalArgumentException("ftol < 0");
		if( gtol < 0 )
			throw new IllegalArgumentException("gtol < 0");
		if( lineGTol <= 0 )
			throw new IllegalArgumentException("lineGTol <= 0");

		this.ftol = ftol;
		this.gtol = gtol;
		this.lineGTol = lineGTol;
	}

	/**
	 * Manually specify the initial inverse hessian approximation.
	 * @param Hinverse Initial hessian approximation
	 */
	public void setInitialHInv( DMatrixRMaj Hinverse) {
		B.set(Hinverse);
	}

	public void initialize(double[] initial) {
		this.mode = 0;
		this.hasConverged = false;
		this.iterations = 0;

		// set the change in x to be zero
		s.zero();
		// default to an initial inverse Hessian approximation as
		// the identity matrix.  This can be overridden or improved by an heuristic below
		CommonOps_DDRM.setIdentity(B);

		// save the initial value of x
		System.arraycopy(initial, 0, x.data, 0, N);

		function.setInput(x.data);
		fx = function.computeFunction();
		updated = false;
	}


	public double[] getParameters() {
		return x.data;
	}


	/**
	 * Perform one iteration in the optimization.
	 *
	 * @return true if the optimization has stopped.
	 */
	public boolean iterate() {
		updated = false;
//		System.out.println("QN iterations "+iterations);
		if( mode == 0 ) {
			return computeSearchDirection();
		} else {
			return performLineSearch();
		}
	}

	/**
	 * Computes the next search direction using BFGS
	 */
	private boolean computeSearchDirection() {
		// Compute the function's gradient
		function.computeGradient(temp0_Nx1.data);

		// compute the change in gradient
		for( int i = 0; i < N; i++ ) {
			y.data[i] = temp0_Nx1.data[i] - g.data[i];
			g.data[i] = temp0_Nx1.data[i];
		}

		// Update the inverse Hessian matrix
		if( iterations != 0 ) {
			EquationsBFGS.inverseUpdate(B, s, y, temp0_Nx1, temp1_Nx1);
		}

		// compute the search direction
		CommonOps_DDRM.mult(-1,B,g, searchVector);

		// use the line search to find the next x
		if( !setupLineSearch(fx, x.data, g.data, searchVector.data) ) {
			// the search direction has a positive derivative, meaning the B matrix is
			// no longer SPD.  Attempt to fix the situation by resetting the matrix
			resetMatrixB();
			// do the search again, it can't fail this time
			CommonOps_DDRM.mult(-1,B,g, searchVector);
			setupLineSearch(fx, x.data, g.data, searchVector.data);
		} else if(Math.abs(derivAtZero) <= gtol ) {
			// the input might have been modified by the function.  So copy it
			System.arraycopy(function.getCurrentState(),0,x.data,0,N);
			return terminateSearch(true);
		}

		mode = 1;
		iterations++;
		return false;
	}

	/**
	 * This is a total hack.  Set B to a diagonal matrix where each diagonal element
	 * is the value of the largest absolute value in B.  This will be SPD and hopefully
	 * not screw up the search.
	 */
	private void resetMatrixB() {
		// find the magnitude of the largest diagonal element
		double maxDiag = 0;
		for( int i = 0; i < N; i++ ) {
			double d = Math.abs(B.get(i,i));
			if( d > maxDiag )
				maxDiag = d;
		}

		B.zero();
		for( int i = 0; i < N; i++ ) {
			B.set(i,i,maxDiag);
		}
	}

	private boolean setupLineSearch( double funcAtStart , double[] startPoint , double[] startDeriv,
									 double[] direction ) {
		// derivative of the line search is the dot product of the gradient and search direction
		derivAtZero = 0;
		for( int i = 0; i < N; i++ ) {
			derivAtZero += startDeriv[i]*direction[i];
		}

		// degenerate case
		if( derivAtZero > 0 )
			return false;
		else if( derivAtZero == 0 )
			return true;

		// setup line functions
		function.setLine(startPoint, direction);

		// use wolfe condition to set the maximum step size
		maxStep = (funcMinValue-funcAtStart)/(lineGTol*derivAtZero);
		initialStep = 1 < maxStep ? 1 : maxStep;
		invokeLineInitialize(funcAtStart,maxStep);

		return true;
	}

	private void invokeLineInitialize(double funcAtStart, double maxStep) {
		function.setInput(initialStep);
		double funcAtInit = function.computeFunction();
		lineSearch.init(funcAtStart,derivAtZero,funcAtInit,initialStep,0,maxStep);
		firstStep = true;
	}

	/**
	 * Performs a 1-D line search along the chosen direction until the Wolfe conditions
	 * have been meet.
	 *
	 * @return true if the search has terminated.
	 */
	private boolean performLineSearch() {
		// if true then it can't iterate any more
		if( lineSearch.iterate() ) {
			// see if the line search failed
			if( !lineSearch.isConverged() ) {
				if( firstStep ) {
					// if it failed on the very first step then it might have been too large
					// try halving the step size
					initialStep /= 2;
					if( initialStep != 0 ) {
						invokeLineInitialize(fx, maxStep);
						return false;
					} else {
						if( verbose != null )
							verbose.println("Initial step reduced to zero");
						return terminateSearch(false);
					}
				} else {
					return terminateSearch(false);
				}
			} else {
				firstStep = false;
			}

			// update variables
			double step = lineSearch.getStep();

			// save the new x
			System.arraycopy(function.getCurrentState(),0,x.data,0,N);
			// compute the change in the x
			for( int i = 0; i < N; i++ )
				s.data[i] = step * searchVector.data[i];
			updated = true;

			// convergence tests
			// function value at end of line search
			double fstp = lineSearch.getFunction();

			// see if the actual different and predicted differences are smaller than the
			// error tolerance
			if( Math.abs(fstp-fx) <= ftol*Math.abs(fx) || Math.abs(derivAtZero) < gtol )
				return terminateSearch(true);

			if( fstp > fx ) {
				throw new RuntimeException("Worse results!");
			}

			// current function value is now the previous
			fx = fstp;

			// start the loop again
			mode = 0;
		}
		return false;
	}

	/**
	 * Helper function that lets converged and the final message bet set in one line
	 */
	private boolean terminateSearch( boolean converged ) {
		this.hasConverged = converged;
		return true;
	}

	/**
	 * True if the line search converged to a solution
	 */
	public boolean isConverged() {
		return hasConverged;
	}

	public double getFx() {
		return fx;
	}

	public boolean isUpdatedParameters() {
		return updated;
	}
}
