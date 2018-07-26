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
import org.ejml.data.DGrowArray;
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
 * <p>
 *     Scaling can be optionally turned on. By default it is off. If scaling is turned on then a non symmetric
 *     trust region is used. The length of each axis is determined by the absolute value of diagonal elements in
 *     the hessian. Minimum and maximum possible scaling values are an important tuning parameter.
 * </p>
 *
 * <ul>
 * <li>[1] Jorge Nocedal,and Stephen J. Wright "Numerical Optimization" 2nd Ed. Springer 2006</li>
 * <li>[2] Peter Abeles, "DDogleg Technical Report: Nonlinear Optimization R1", July 2018</li>
 * </ul>
 *
 * @see ConfigTrustRegion
 *
 * @author Peter Abeles
 */
public abstract class TrustRegionBase_F64<S extends DMatrix> {

	// TODO consider moving prediction to update so that it can avoid duplicate calculations

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
	 * Storage for the Hessian. Update algorithms should not modify the Hessian
	 */
	protected S hessian;

	// Number of parameters being optimized
	int numberOfParameters;

	// Current parameter state
	protected DMatrixRMaj x = new DMatrixRMaj(1,1);
	// proposed next state of parameters
	protected DMatrixRMaj x_next = new DMatrixRMaj(1,1);
	// proposed relative change in parameter's state
	protected DMatrixRMaj p = new DMatrixRMaj(1,1);

	// Scaling used to compensate for poorly scaled variables
	protected DGrowArray scaling = new DGrowArray();

	protected ConfigTrustRegion config = new ConfigTrustRegion();

	// error function at x
	protected double fx;

	// size of the current trust region
	double regionRadius;

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
	 * @param numberOfParameters Number many parameters are being optimized.
	 * @param minimumFunctionValue The minimum possible value that the function can output
	 */
	public void initialize(double initial[] , int numberOfParameters , double minimumFunctionValue ) {
		this.numberOfParameters = numberOfParameters;

		((ReshapeMatrix)hessian).reshape(numberOfParameters,numberOfParameters);
		math.setIdentity(hessian);

		x.reshape(numberOfParameters,1);
		x_next.reshape(numberOfParameters,1);
		p.reshape(numberOfParameters,1);
		gradient.reshape(numberOfParameters,1);

		// initialize scaling to 1, which is no scaling
//		scaling.reshape(numberOfParameters);
//		Arrays.fill(scaling.data,0,numberOfParameters,1);

		System.arraycopy(initial,0,x.data,0,numberOfParameters);
		fx = costFunction(x);

		totalFullSteps = 0;
		totalRetries = 0;

		regionRadius = config.regionInitial;

		// a perfect initial guess is a pathological case. easiest to handle it here
		if( fx <= minimumFunctionValue ) {
			mode = Mode.CONVERGED;
		} else {
			mode = Mode.FULL_STEP;
		}

		this.parameterUpdate.initialize(this,numberOfParameters, minimumFunctionValue);
	}

	/**
	 * Performs one iteration
	 *
	 * @return true if it has converged or false if not
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
	 * Computes all the derived data structures and attempts to update the parameters
	 * @return true if it has converged.
	 */
	protected boolean updateState() {
		updateDerivedState(x);
//		if( isScaling() ) {
//			computeScaling();
//			applyScaling();
//		}

		gradientNorm = NormOps_DDRM.normF(gradient);
		if(UtilEjml.isUncountable(gradientNorm))
			throw new OptimizationException("Uncountable. gradientNorm="+gradientNorm);

		if( CommonOps_DDRM.elementMaxAbs(gradient) <= config.gtol*1000000)
			return true;

		parameterUpdate.initializeUpdate();
		return false;
	}

	/**
	 * Grabs scaling from diagonal elements of the Hessian
	 */
	protected void computeScaling() {
		math.extractDiag(hessian,scaling.data);

		// massage the data just in case there's weird stuff going on in the Hessian that shouldn't be happening
		for (int i = 0; i < scaling.length; i++) {
			scaling.data[i] = Math.min(config.scalingMaximum,
					Math.max(config.scalingMinimum,Math.abs(scaling.data[i])));
		}
	}

	/**
	 * Apply scaling to gradient and Hessian
	 */
	protected void applyScaling() {
		for (int i = 0; i < scaling.length; i++) {
			gradient.data[i] /= scaling.data[i];
		}
		math.divideRows(scaling.data,hessian);
		math.divideColumns(scaling.data,hessian);
	}

	/**
	 * Undo scaling on estimated parameters
	 */
	protected void undoScalingOnParameters() {
		for (int i = 0; i < scaling.length; i++) {
			p.data[i] /= scaling.data[i];
		}
	}

	/**
	 * Changes the trust region's size and attempts to do a step again
	 * @return true if it has converged.
	 */
	protected boolean computeAndConsiderNew() {
		boolean hitBoundary = parameterUpdate.computeUpdate(p, regionRadius);

//		if( isScaling() )
//			undoScalingOnParameters();
		CommonOps_DDRM.add(x,p,x_next);
		double fx_candidate = costFunction(x_next);

		if (considerCandidate(fx_candidate,fx, p, hitBoundary)) {
			boolean converged = checkConvergence(fx_candidate,fx);
			// Assign values from candidate to current state
			fx = fx_candidate;

			DMatrixRMaj tmp = x;
			x = x_next;
			x_next = tmp;

			// Update the state
			if( converged ) {
				mode = Mode.CONVERGED;
				return true;
			} else {
				mode = Mode.FULL_STEP;
			}
		} else {
			mode = Mode.RETRY;
		}
		return false;
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
	 * @param x_delta (Input) change in state vector
	 * @param hitBoundary (Input) true if it hits the region boundary
	 * @return true if it should update the state or false if it should try agian
	 */
	protected boolean considerCandidate(double fx_candidate, double fx_prev, DMatrixRMaj x_delta , boolean hitBoundary ) {

		// compute model prediction accuracy
		double actualReduction = fx_prev-fx_candidate;
		double predictedReduction = -CommonOps_DDRM.dot(gradient,p) - 0.5*math.innerProduct(p,hessian);

		if( actualReduction == 0 || predictedReduction == 0 ) {
			return true;
		}

		double ratio = actualReduction/predictedReduction;

		if( fx_candidate > fx_prev || ratio < 0.25 ) {
			// if the improvement is too small (or not an improvement) reduce the region size
			// TODO 0.25 or 0.5 ?
			regionRadius = Math.max(config.regionMinimum,0.5*regionRadius);
		} else {
//			if( ratio > 0.75 && hitBoundary ) {
			if( ratio > 0.75 ) {
				double r = NormOps_DDRM.normF(x_delta);
				regionRadius = Math.min(Math.max(3*r,regionRadius),config.regionMaximum);
			}

		}
		return fx_candidate < fx_prev && ratio > 0;// config.candidateAcceptThreshold;
	}


	/**
	 * <p>Checks for convergence using unconstrained minization f-test and g-test</p>
	 *
	 * f-test : ftol <= 1.0-f(x+p)/f(x)<br>
	 * g-test : gtol <= ||g(x)||_inf
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	protected boolean checkConvergence( double fx, double fx_prev ) {
		// something really bad has happened if this gets triggered before it thinks it converged
		if( UtilEjml.isUncountable(regionRadius) || regionRadius <= 0 )
			throw new OptimizationException("Failing to converge. Region size hit a wall. r="+regionRadius);

		if( fx > fx_prev )
			throw new RuntimeException("BUG! Shouldn't have gotten this far");

		// f-test. avoid potential divide by zero errors
		if( config.ftol*fx_prev >= fx_prev - fx )
			return true;
//		if( Math.abs(fx-fx_prev) <= config.ftol*Math.max(fx,fx_prev) )
//			return true;


		// g-test:  compute the infinity norm of the gradient
		return false;//CommonOps_DDRM.elementMaxAbs(gradient) <= config.gtol;
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

		void extractDiag( S A , double diag[] );

		void divideRows(double scaling[] , S A );

		void divideColumns(double scaling[] , S A );

		void scaleRows(double scaling[] , S A );

		void scaleColumns(double scaling[] , S A );

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

	/**
	 * True if scaling is turned on
	 */
	public boolean isScaling() {
		return config.scalingMaximum > config.scalingMinimum;
	}


	public void configure(ConfigTrustRegion config) {
		this.config = config.copy();
	}

	public ConfigTrustRegion getConfig() {
		return config;
	}
}
