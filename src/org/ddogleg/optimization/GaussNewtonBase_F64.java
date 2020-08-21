/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * Base class for Gauss-Newton based approaches for unconstrained optimization.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
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

	// Scaling applied to hessian matrix to improve it's condition
	protected DMatrixRMaj hessianScaling = new DMatrixRMaj(1,1);

	// which processing step it's on
	protected Mode mode = Mode.COMPUTE_DERIVATIVES;

	// number of each type of step it has taken
	protected int totalFullSteps, totalSelectSteps;

	// If not null then print additional information to this stream
	protected @Nullable PrintStream verbose;
	protected int verboseLevel=0;

	// Optimization configuration
	public C config;

	// value of convergence tests after the last test
	public double ftest_val,gtest_val;

	protected GaussNewtonBase_F64(HM hessian) {
		this.hessian = hessian;
	}

	public void initialize(double[] initial, int numberOfParameters ) {
		x.reshape(numberOfParameters,1);
		x_next.reshape(numberOfParameters,1);
		p.reshape(numberOfParameters,1);
		gradient.reshape(numberOfParameters,1);

		// initialize scaling to 1, which is no scaling
		hessianScaling.reshape(numberOfParameters,1);
		Arrays.fill(hessianScaling.data,0,numberOfParameters,1);

		hessian.init(numberOfParameters);

		System.arraycopy(initial,0,x.data,0,numberOfParameters);
		sameStateAsCost = true;

		totalFullSteps = 0;
		totalSelectSteps = 0;
	}

	/**
	 * Performs one iteration
	 *
	 * @return true if it has converged or false if not
	 */
	public boolean iterate() {
		boolean converged;
		switch( mode ) {
			case COMPUTE_DERIVATIVES:
				totalFullSteps++;
				converged = updateDerivates();
				if( !converged ) {
					totalSelectSteps++;
					converged = computeStep();
				}
				break;

			case DETERMINE_STEP:
				totalSelectSteps++;
				converged = computeStep();
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
	protected abstract boolean updateDerivates();

	/**
	 * Selects the next step
	 *
	 * @return true if it has converged.
	 */
	protected abstract boolean computeStep();

	/**
	 * Sets scaling to the sqrt() of the diagonal elements in the Hessian matrix
	 */
	protected void computeHessianScaling() {
		hessian.extractDiagonals(hessianScaling);
		computeHessianScaling(hessianScaling);
	}

	/**
	 * Applies the standard formula for computing scaling. This is broken off into its own
	 * function so that it easily invoked if the function above is overridden
	 *
	 * @param scaling Vector containing scaling information
	 */
	public void computeHessianScaling(DMatrixRMaj scaling ) {

		double max = 0;
		for (int i = 0; i < scaling.numRows; i++) {
			// mathematically it should never be negative but...
			double v = scaling.data[i] = sqrt(abs(scaling.data[i]));
			if( v > max )
				max = v;
		}

		// Add this number to avoid divide by zero.
		// Ceres solver just uses 1 no matter what. That's probably not the best approach because if you're doing
		// with very small numbers scaling will be washed out
		max *= 1e-12;
		for (int i = 0; i < scaling.numRows; i++) {
			scaling.data[i] += max;
		}
	}

	/**
	 * Apply scaling to gradient and Hessian
	 */
	protected void applyHessianScaling() {
		CommonOps_DDRM.elementDiv(gradient, hessianScaling);

		hessian.divideRowsCols(hessianScaling);
	}

	/**
	 * Undo scaling on estimated parameters
	 */
	protected void undoHessianScalingOnParameters(DMatrixRMaj p ) {
		CommonOps_DDRM.elementDiv(p, hessianScaling);
	}

	/**
	 * <p>Checks for convergence using f-test:</p>
	 *
	 * g-test : gtol &le; ||g(x)||_inf
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	protected boolean checkConvergenceGTest( DMatrixRMaj g ) {
		gtest_val = 0;
		for (int i = 0; i < g.numRows; i++) {
			double v = Math.abs(g.data[i]);
			if( v > gtest_val ) {
				gtest_val = v;
				if( gtest_val > config.gtol )
					return false;
			}
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
	public enum Mode {
		COMPUTE_DERIVATIVES,
		DETERMINE_STEP,
		CONVERGED
	}

	public Mode mode() {
		return mode;
	}

	/**
	 * If set to a non-null output then extra information will be printed to the specified stream.
	 *
	 * @param out Stream that is printed to. Set to null to disable
	 * @param level (Future use) Parameter which can be used to specify level of verbose output. Set to zero for now.
	 */
	public void setVerbose(@Nullable PrintStream out , int level ) {
		this.verbose = out;
		this.verboseLevel = level;
	}

}
