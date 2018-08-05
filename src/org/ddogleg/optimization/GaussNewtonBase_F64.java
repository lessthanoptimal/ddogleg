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

package org.ddogleg.optimization;

import org.ddogleg.optimization.math.HessianMath;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

import java.util.Arrays;

import static java.lang.Math.*;

/**
 * Base class for Gauss-Newton based approaches for unconstrained optimization.
 *
 * @author Peter Abeles
 */
public abstract class GaussNewtonBase_F64<C extends ConfigGaussNewton,HM extends HessianMath>
{
	// Manipulating and extracting information from the Hessian
	public HM hessian;

	// Current parameter state
	public DMatrixRMaj x = new DMatrixRMaj(1,1);
	// proposed next state of parameters
	public DMatrixRMaj x_next = new DMatrixRMaj(1,1);
	// proposed relative change in parameter's state
	public DMatrixRMaj p = new DMatrixRMaj(1,1);

	// error function at x
	public double fx;

	/**
	 * Storage for the gradient
	 */
	public DMatrixRMaj gradient = new DMatrixRMaj(1,1);

	// Is the value of x being passed in for the hessian the same as the value of x used to compute the cost
	protected boolean sameStateAsCost;

	// Scaling used to compensate for poorly scaled variables
	protected DMatrixRMaj scaling = new DMatrixRMaj(1,1);

	// which processing step it's on
	protected Mode mode = Mode.FULL_STEP;

	// number of each type of step it has taken
	protected int totalFullSteps, totalRetries;

	// print additional debugging messages to standard out
	protected boolean verbose;

	// Optimization configuration
	public C config;

	public GaussNewtonBase_F64(HM hessian) {
		this.hessian = hessian;
	}

	protected GaussNewtonBase_F64() {
	}

	public void initialize(double initial[] , int numberOfParameters ) {
		x.reshape(numberOfParameters,1);
		x_next.reshape(numberOfParameters,1);
		p.reshape(numberOfParameters,1);
		gradient.reshape(numberOfParameters,1);

		// initialize scaling to 1, which is no scaling
		scaling.reshape(numberOfParameters,1);
		Arrays.fill(scaling.data,0,numberOfParameters,1);

		hessian.init(numberOfParameters);

		System.arraycopy(initial,0,x.data,0,numberOfParameters);
		sameStateAsCost = true;

		totalFullSteps = 0;
		totalRetries = 0;
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
	protected abstract boolean updateState();

	/**
	 * Selects the next step
	 *
	 * @return true if it has converged.
	 */
	protected abstract boolean computeAndConsiderNew();

	/**
	 * Sets scaling to the sqrt() of the diagonal elements in the Hessian matrix
	 */
	protected void computeScaling() {
		hessian.extractDiagonals(scaling);
		computeScaling(scaling, config.scalingMinimum, config.scalingMaximum);
	}

	/**
	 * Applies the standard formula for computing scaling. This is broken off into its own
	 * function so that it easily invoked if the function above is overriden
	 */
	public void computeScaling( DMatrixRMaj scaling , double minimum , double maximum ) {
		for (int i = 0; i < scaling.numRows; i++) {
			// mathematically it should never be negative but...
			double scale = sqrt(abs(scaling.data[i]));
			// clamp the scale factor
			scaling.data[i] = min(maximum, max(minimum, scale));
		}
	}

	/**
	 * Apply scaling to gradient and Hessian
	 */
	protected void applyScaling() {
		CommonOps_DDRM.elementDiv(gradient,scaling);

		hessian.divideRowsCols(scaling);
	}

	/**
	 * Undo scaling on estimated parameters
	 */
	protected void undoScalingOnParameters( DMatrixRMaj p ) {
		CommonOps_DDRM.elementDiv(p,scaling);
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
	 * Computes the gradient and Hessian at 'x'. If sameStateAsCost is true then it can be assumed that 'x' has
	 * not changed since the cost was last computed.
	 * @param gradient (Input) x
	 * @param sameStateAsCost (Input) If true then when the cost (or residuals) was last called it had the same value of x
	 * @param gradient (Output) gradient
	 * @param hessian (Output) hessian
	 */
	protected abstract void functionGradientHessian(DMatrixRMaj x , boolean sameStateAsCost , DMatrixRMaj gradient , HM hessian);

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
	 * Optimization mode
	 */
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

	/**
	 * Toggles printing of status to standard out
	 * @param verbose true to print to standard out
	 */
	public void setVerbose( boolean verbose ) {
		this.verbose = verbose;
	}

}
