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

package org.ddogleg.optimization.trustregion;

import org.ddogleg.optimization.OptimizationException;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.ReshapeMatrix;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;

/**
 * <p>Base class for all trust region implementations. The Trust Region approach assumes that a quadratic model is valid
 * within the trust region. At each iteration the Trust Region's subproblem is solved for and a new state is selected.
 * Depending on how accurately the quadratic model predicted the new score the size of the region will be increased
 * or decreased. This implementation is primarily based on [1] and is fully described in the DDogleg Technical
 * Report [2].</p>
 *
 * <ul>
 * <li>[1] Jorge Nocedal,and Stephen J. Wright "Numerical Optimization" 2nd Ed. Springer 2006</li>
 * <li>[2] Peter Abeles, "DDogleg Technical Report: Nonlinear Optimization R1", July 2018</li>
 * </ul>
 *
 * @author Peter Abeles
 */
public abstract class TrustRegionBase_F64<S extends DMatrix> {

	// Technique used to compute the change in parameters
	private ParameterUpdate<S> parameterUpdate;

	// Math for some matrix operations
	protected MatrixMath<S> math;

	/**
	 * Storage for the gradient
	 */
	protected DMatrixRMaj gradient = new DMatrixRMaj(1,1);
	/**
	 * F-norm of the gradient
	 */
	protected double gradientNorm;

	/**
	 * Storage for the Hessian
	 */
	protected S hessian;

	// Number of parameters being optimized
	int numberOfParameters;

	// Curren parameter state
	protected DMatrixRMaj x = new DMatrixRMaj(1,1);
	// proposed next state of parameters
	protected DMatrixRMaj x_next = new DMatrixRMaj(1,1);
	// proposed relative change in parameter's state
	protected DMatrixRMaj p = new DMatrixRMaj(1,1);

	// error function at x
	protected double fx;
	// the previous error
	protected double fx_prev;

	// if the prediction ratio his higher than this threshold it is accepted
	private double candidateAcceptThreshold = 0.05;

	// initial size of the trust region
	double regionRadiusInitial=1;

	// size of the current trust region
	double regionRadius;

	// maximum size of the trust region
	double regionRadiusMax = Double.MAX_VALUE;

	// tolerance for termination. magnitude of gradient. absolute
	double gtol;
	// tolerance for termination, change in function value.  relative
	double ftol;

	// which processing step it's on
	protected Mode mode = Mode.FULL_STEP;

	// number of each type of step it has taken
	protected int totalFullSteps, totalRetries;

	public TrustRegionBase_F64(ParameterUpdate parameterUpdate, MatrixMath<S> math ) {
		this.parameterUpdate = parameterUpdate;
		this.math = math;
		this.hessian = math.createMatrix();
	}

	/**
	 * Specifies initial state of the search and completion criteria
	 *
	 * @param initial Initial parameter state
	 * @param ftol ftol completion
	 * @param gtol gtol completion
	 * @param numberOfParameters Number many parameters are being optimized.
	 * @param minimumFunctionValue The minimum possible value that the function can output
	 */
	public void initialize(double initial[] , double ftol , double gtol ,
						   int numberOfParameters , double minimumFunctionValue) {
		this.numberOfParameters = numberOfParameters;
		this.ftol = ftol;
		this.gtol = gtol;

		this.parameterUpdate.initialize(this,numberOfParameters, minimumFunctionValue);

		((ReshapeMatrix)hessian).reshape(numberOfParameters,numberOfParameters);

		x.reshape(numberOfParameters,1);
		x_next.reshape(numberOfParameters,1);
		p.reshape(numberOfParameters,1);
		gradient.reshape(numberOfParameters,1);

		System.arraycopy(initial,0,x.data,0,numberOfParameters);
		fx = costFunction(x);

		totalFullSteps = 0;
		totalRetries = 0;

		regionRadius = regionRadiusInitial;

		// a perfect initial guess is a pathological case. easiest to handle it here
		if( fx <= minimumFunctionValue ) {
			mode = Mode.CONVERGED;
		} else {
			mode = Mode.FULL_STEP;
		}
	}

	/**
	 * Performs one iteration
	 *
	 * @return true if it has converged or false if not
	 */
	public boolean iterate() {
		switch( mode ) {
			case FULL_STEP:
				totalFullSteps++;
				updateState();
				return computeAndConsiderNew();

			case RETRY:
				totalRetries++;
				return computeAndConsiderNew();

			case CONVERGED:
				return true;

			default:
				throw new RuntimeException("BUG! mode="+mode);
		}
	}

	/**
	 * Computes all the derived data structures and attempts to update the parameters
	 * @return true if it has converged.
	 */
	protected void updateState() {
		updateDerivedState(x);
		gradientNorm = NormOps_DDRM.normF(gradient);
		if(UtilEjml.isUncountable(gradientNorm))
			throw new OptimizationException("Uncountable. gradientNorm="+gradientNorm);
		fx_prev = fx;
		parameterUpdate.initializeUpdate();
	}

	/**
	 * Changes the trust region's size and attempts to do a step again
	 * @return true if it has converged.
	 */
	protected boolean computeAndConsiderNew() {
		boolean hitBoundary = parameterUpdate.computeUpdate(p, regionRadius);
		if (considerUpdate(p, hitBoundary)) {
			swapOldAndNewParameters();
			mode = Mode.FULL_STEP;
			if( checkConvergence() ) {
				mode = Mode.CONVERGED;
				return true;
			}
		} else {
			mode = Mode.RETRY;
		}
		return false;
	}

	/**
	 * Instead of doing a memory copy just swap the references
	 */
	void swapOldAndNewParameters() {
		DMatrixRMaj tmp = x;
		x = x_next;
		x_next = tmp;
	}

	/**
	 * Computes derived data from the new current state x. At a minimum the following needs to be found:
	 *
	 * <ul>
	 *     <li>{@link #gradient}</li>
	 *     <li>{@link #hessian}</li>
	 * </ul>
	 */
	protected abstract void updateDerivedState(DMatrixRMaj x );

	/**
	 * Consider updating the system with the change in state p. The update will never
	 * be accepted if the cost function increases.
	 *
	 * @param p (Input) change in state vector
	 * @param hitBoundary (Input) true if it hits the region boundary
	 * @return true if it should update the state or false if it should try agian
	 */
	protected boolean considerUpdate( DMatrixRMaj p , boolean hitBoundary ) {
		// Compute the next possible parameter and the cost function's value
		CommonOps_DDRM.add(x,p,x_next);
		fx = costFunction(x_next);

		// compute model prediction accuracy
		double predictionAccuracy = computePredictionAccuracy(p);

		// if the improvement is too small (or not an improvement) reduce the region size
		if( fx > fx_prev || predictionAccuracy < 0.25 ) {
			regionRadius = 0.25*regionRadius;
		} else {
			if( predictionAccuracy > 0.75 && hitBoundary ) {
				regionRadius = Math.min(2.0*regionRadius,regionRadiusMax);
			}
		}

		return fx < fx_prev && predictionAccuracy > candidateAcceptThreshold;
	}

	/**
	 * Computes the prediction's accuracy
	 * @return prediction ratio
	 */
	protected double computePredictionAccuracy(DMatrixRMaj p) {

		// use quadratic model to predict new cost
		double m_k1 = fx_prev + CommonOps_DDRM.dot(gradient,p) + 0.5*math.innerProduct(p,hessian);
		// m_k0 = fx_prev

		// the model predicts no change. Something bad is going on
		if( m_k1 == fx_prev ) {
			return 0;
		} else {
			return (fx - fx_prev) / (m_k1 - fx_prev);
		}
	}

	/**
	 * <p>Checks for convergence using unconstrained minization f-test and g-test</p>
	 *
	 * f-test : ftol <= 1.0-f(x+p)/f(x)<br>
	 * g-test : gtol <= ||g(x)||_inf
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	protected boolean checkConvergence( ) {
		// something really bad has happened if this gets triggered before it thinks it converged
		if( UtilEjml.isUncountable(regionRadius) || regionRadius <= 0 )
			throw new OptimizationException("Failing to converge. Region size hit a wall. r="+regionRadius);

		if( fx > fx_prev )
			throw new RuntimeException("BUG! Shouldn't have gotten this far");

		// f-test
		double fscore = 1.0 - fx/fx_prev;
		if( ftol >= fscore )
			return true;

		// g-test
		// compute the infinity norm of g
		double max = 0;
		final int N = numberOfParameters;
		for (int i = 0; i < N; i++) {
			double v = gradient.data[i];
			if( v < 0 ) v = -v;
			if( v > max )
				max = v;
		}
		return gtol >= max;
	}

	/**
	 * Computes the function's value at x
	 * @param x parameters
	 * @return function value
	 */
	protected abstract double costFunction( DMatrixRMaj x );


	public interface MatrixMath<S extends DMatrix> {
		/**
		 * Returns v^T*M*v
		 * @param v vector
		 * @param M square matrix
		 */
		double innerProduct( DMatrixRMaj v , S M );

		/**
		 * Sets the provided matrix to identity
		 */
		void setIdentity( S matrix );

		/**
		 * output = A'*A
		 */
		void innerMatrixProduct( S A , S output );

		S createMatrix();
	}

	public interface ParameterUpdate<S extends DMatrix> {

		/**
		 * Must call this function first. Specifies the number of parameters that are being optimized.
		 *
		 * @param minimumFunctionValue The minimum possible value that the function can output
		 */
		void initialize ( TrustRegionBase_F64<S> base , int numberOfParameters,
						  double minimumFunctionValue );

		/**
		 * Initialize the parameter update. This is typically where all the expensive computations take place
		 *
		 * Useful internal class variables:
		 * <ul>
		 *     <li>{@link #x} current state</li>
		 *     <li>{@link #gradient} Gradient a x</li>
		 *     <li>{@link #hessian} Hessian at x</li>
		 * </ul>
		 *
		 * Inputs are not passed in explicitly since it varies by implementation which ones are needed.
		 *
		 * @return true if it hits the region boundary
		 */
		void initializeUpdate();

		/**
		 * Compute the value of p given a new parameter state x and the region radius.
		 *
		 * @param p (Output) change in state
		 * @param regionRadius (Input) Radius of the region
		 * @return true if it hits the region boundary
		 */
		boolean computeUpdate(DMatrixRMaj p , double regionRadius );
	}

	protected enum Mode {
		FULL_STEP,
		RETRY,
		CONVERGED
	}

	public double getRegionRadiusInitial() {
		return regionRadiusInitial;
	}

	public void setRegionRadiusInitial(double regionRadiusInitial) {
		this.regionRadiusInitial = regionRadiusInitial;
	}

	public double getRegionRadiusMax() {
		return regionRadiusMax;
	}

	public void setRegionRadiusMax(double regionRadiusMax) {
		this.regionRadiusMax = regionRadiusMax;
	}

	public double getCandidateAcceptThreshold() {
		return candidateAcceptThreshold;
	}

	public void setCandidateAcceptThreshold(double candidateAcceptThreshold) {
		this.candidateAcceptThreshold = candidateAcceptThreshold;
	}

	public double getGtol() {
		return gtol;
	}

	public void setGtol(double gtol) {
		this.gtol = gtol;
	}

	public double getFtol() {
		return ftol;
	}

	public void setFtol(double ftol) {
		this.ftol = ftol;
	}
}
