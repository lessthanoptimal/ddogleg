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

package org.ddogleg.optimization.trustregion;

import org.ddogleg.optimization.GaussNewtonBase_F64;
import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.math.HessianMath;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;

import static java.lang.Math.max;
import static java.lang.Math.min;

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
 * <li>[2] JK. Madsen and H. B. Nielsen and O. Tingleff, "Methods for Non-Linear Least Squares Problems (2nd ed.)"
 * Informatics and Mathematical Modelling, Technical University of Denmark</li>
 * <li>[3] Peter Abeles, "DDogleg Technical Report: Nonlinear Optimization R1", August 2018</li>
 * </ul>
 *
 * @see ConfigTrustRegion
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public abstract class TrustRegionBase_F64<S extends DMatrix, HM extends HessianMath>
		extends GaussNewtonBase_F64<ConfigTrustRegion,HM>
{
	// Technique used to compute the change in parameters
	protected ParameterUpdate<S> parameterUpdate;

	// Workspace for step
	protected DMatrixRMaj tmp_p = new DMatrixRMaj(1,1);

	// size of the current trust region
	double regionRadius;

	/**
	 * F-norm of the gradient
	 */
	public double gradientNorm;

	protected TrustRegionBase_F64(ParameterUpdate<S> parameterUpdate, HM hessian ) {
		super(hessian);
		configure(new ConfigTrustRegion());
		this.parameterUpdate = parameterUpdate;
		this.hessian = hessian;
	}

	/**
	 * Specifies initial state of the search and completion criteria
	 *
	 * @param initial Initial parameter state
	 * @param numberOfParameters Number many parameters are being optimized.
	 * @param minimumFunctionValue The minimum possible value that the function can output
	 */
	public void initialize(double[] initial, int numberOfParameters , double minimumFunctionValue ) {
		super.initialize(initial,numberOfParameters);

		tmp_p.reshape(numberOfParameters,1);

		regionRadius = config.regionInitial;

		fx = cost(x);

		if( verbose != null ) {
			verbose.println("Steps     fx        change      |step|   f-test     g-test    tr-ratio  region ");
			verbose.printf("%-4d  %9.3E  %10.3E  %9.3E  %9.3E  %9.3E  %6.2f   %6.2E\n",
					totalSelectSteps, fx, 0.0,0.0,0.0,0.0, 0.0, regionRadius);
		}

		this.parameterUpdate.initialize(this,numberOfParameters, minimumFunctionValue);

		// a perfect initial guess is a pathological case. easiest to handle it here
		if( fx <= minimumFunctionValue ) {
			if( verbose != null ) {
				verbose.println("Converged minimum value");
			}
			mode = TrustRegionBase_F64.Mode.CONVERGED;
		} else {
			mode = TrustRegionBase_F64.Mode.COMPUTE_DERIVATIVES;
		}
	}

	/**
	 * Computes all the derived data structures and attempts to update the parameters
	 * @return true if it has converged.
	 */
	@Override
	protected boolean updateDerivates() {
		functionGradientHessian(x,sameStateAsCost,gradient,hessian);

		if( config.hessianScaling ) {
			computeHessianScaling();
			applyHessianScaling();
		}

		// Convergence should be tested on scaled variables to remove their arbitrary natural scale
		// from influencing convergence
		if( checkConvergenceGTest(gradient)) {
			if( verbose != null ) {
				verbose.println("Converged g-test");
			}
			return true;
		}

		gradientNorm = NormOps_DDRM.normF(gradient);
		if(UtilEjml.isUncountable(gradientNorm))
			throw new OptimizationException("Uncountable. gradientNorm="+gradientNorm);

		parameterUpdate.initializeUpdate();
		return false;
	}

	/**
	 * Changes the trust region's size and attempts to do a step again
	 * @return true if it has converged.
	 */
	@Override
	protected boolean computeStep() {
		// If first iteration and automatic
		if( regionRadius == -1 ) {
			// user has selected unconstrained method for initial step size
			parameterUpdate.computeUpdate(p, Double.MAX_VALUE);
			regionRadius = parameterUpdate.getStepLength();

			if( regionRadius == Double.MAX_VALUE || UtilEjml.isUncountable(regionRadius)) {
				if( verbose != null )
					verbose.println("unconstrained initialization failed. Using Cauchy initialization instead.");
				regionRadius = -2;
			} else {
				if( verbose != null )
					verbose.println("unconstrained initialization radius="+regionRadius);
			}
		}
		if( regionRadius == -2 ) {
			// User has selected Cauchy method for initial step size
			regionRadius = solveCauchyStepLength()*10;
			parameterUpdate.computeUpdate(p, regionRadius);
			if( verbose != null )
				verbose.println("cauchy initialization radius="+regionRadius);

		} else {
			parameterUpdate.computeUpdate(p, regionRadius);
		}

		if(  config.hessianScaling )
			undoHessianScalingOnParameters(p);
		CommonOps_DDRM.add(x,p,x_next);
		double fx_candidate = cost(x_next);

		if( UtilEjml.isUncountable(fx_candidate)) {
			throw new OptimizationException("Uncountable candidate cost. "+fx_candidate);
		}

		// this notes that the cost was computed at x_next for the Hessian calculation.
		// This is a relic from a variant on this implementation where another candidate might be considered. I'm
		// leaving this code where since it might be useful in the future and doesn't add much complexity
		sameStateAsCost = true;

		// NOTE: step length was computed using the weighted/scaled version of 'p', which is correct
		return considerCandidate(fx_candidate,fx,
				parameterUpdate.getPredictedReduction(),
				parameterUpdate.getStepLength());
	}

	protected boolean acceptNewState(boolean converged , double fx_candidate) {
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
			mode = Mode.COMPUTE_DERIVATIVES;
			return false;
		}
	}

	protected double solveCauchyStepLength() {
		double gBg = hessian.innerVectorHessian(gradient);

		return gradientNorm*gradientNorm/gBg;
	}

	/**
	 * Consider updating the system with the change in state p. The update will never
	 * be accepted if the cost function increases.
	 *
	 * @param fx_candidate Actual score at the candidate 'x'
	 * @param fx_prev  Score at the current 'x'
	 * @param predictedReduction Reduction in score predicted by quadratic model
	 * @param stepLength The length of the step, i.e. |p|
	 * @return true if it should update the state or false if it should try agian
	 */
	protected boolean considerCandidate(double fx_candidate, double fx_prev,
											double predictedReduction, double stepLength ) {

		// compute model prediction accuracy
		double actualReduction = fx_prev-fx_candidate;

		if( actualReduction == 0 || predictedReduction == 0 ) {
			if( verbose != null )
				verbose.println(totalFullSteps+" reduction of zero");
			return true;
		}

		double ratio = actualReduction/predictedReduction;

		if( fx_candidate > fx_prev || ratio < 0.25 ) {
			// if the improvement is too small (or not an improvement) reduce the region size
			regionRadius = 0.5*regionRadius;
		} else {
			if( ratio > 0.75 ) {
				regionRadius = min(max(3*stepLength,regionRadius),config.regionMaximum);
			}
		}

		// The new state has been accepted. See if it has converged and change the candidate state to the actual state
		if( fx_candidate < fx_prev && ratio > 0 ) {
			boolean converged = checkConvergenceFTest(fx_candidate,fx_prev);
			if( verbose != null ) {
				verbose.printf("%-4d  %9.3E  %10.3E  %9.3E  %9.3E  %9.3E  %6.2f   %6.2E\n",
						totalSelectSteps, fx_candidate, fx_candidate-fx_prev,stepLength,ftest_val,gtest_val, ratio, regionRadius);
				if( converged ) {
					System.out.println("Converged f-test");
				}
			}

			return acceptNewState(converged,fx_candidate);
		} else {
			mode = Mode.DETERMINE_STEP;
			return false;
		}
	}

	/**
	 * Computes the function's value at x
	 * @param x parameters
	 * @return function value
	 */
	protected abstract double cost( DMatrixRMaj x );

	/**
	 * <p>Checks for convergence using f-test:</p>
	 *
	 * f-test : ftol &le; 1.0-f(x+p)/f(x)
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	protected boolean checkConvergenceFTest(double fx, double fx_prev ) {
		// something really bad has happened if this gets triggered before it thinks it converged
		if( UtilEjml.isUncountable(regionRadius) || regionRadius <= 0 )
			throw new OptimizationException("Failing to converge. Region size hit a wall. r="+regionRadius);

		if( fx > fx_prev )
			throw new RuntimeException("BUG! Shouldn't have gotten this far");

		// f-test. avoid potential divide by zero errors
		ftest_val = 1.0 - fx/fx_prev;
		return config.ftol * fx_prev >= fx_prev - fx;
	}

	public interface ParameterUpdate<S extends DMatrix> {

		/**
		 * Must call this function first. Specifies the number of parameters that are being optimized.
		 *
		 * @param minimumFunctionValue The minimum possible value that the function can output
		 */
		void initialize ( TrustRegionBase_F64<S,?> base , int numberOfParameters,
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
		 */
		void initializeUpdate();

		/**
		 * Compute the value of p given a new parameter state x and the region radius.
		 *
		 * @param p (Output) change in state
		 * @param regionRadius (Input) Radius of the region
		 */
		void computeUpdate(DMatrixRMaj p , double regionRadius );

		/**
		 * <p>
		 *     Returns the predicted reduction from the quadratic model.<br><br>
		 * 	   reduction = m(0) - m(p) = -g(0)*p - 0.5*p<sup>T</sup>*H(0)*p
		 * </p>
		 *
		 * <p>This computation is done inside the update because it can often be done more
		 * efficiently without repeating previous computations</p>
		 *
		 */
		double getPredictedReduction();

		/**
		 * This function returns ||p||.
		 *
		 * <p>This computation is done inside the update because it can often be done more
		 * efficiently without repeating previous computations</p>
		 * @return step length
		 */
		double getStepLength();

		void setVerbose( @Nullable PrintStream out , int level );
	}

	/**
	 * Turns on printing of debug messages.
	 * @param out Stream that is printed to. Set to null to disable
	 * @param level 0=Debug from solver. {@code > 0} is debug from solver and update
	 */
	@Override
	public void setVerbose(@Nullable PrintStream out, int level) {
		super.setVerbose(out, level);
		if( level > 0 )
			this.parameterUpdate.setVerbose(verbose,level);
	}


	public void configure(ConfigTrustRegion config) {
		if( config.regionInitial <= 0 && (config.regionInitial != -1 && config.regionInitial != -2 ))
			throw new IllegalArgumentException("Invalid regionInitial. Read javadoc and try again.");
		this.config = config.copy();
	}

	public ConfigTrustRegion getConfig() {
		return config;
	}
}
