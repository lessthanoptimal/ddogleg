/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.optimization.GaussNewtonBase_F64;
import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.loss.LossFunction;
import org.ddogleg.optimization.loss.LossFunctionGradient;
import org.ddogleg.optimization.loss.LossSquared;
import org.ddogleg.optimization.math.HessianMath;
import org.ddogleg.optimization.math.MatrixMath;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.jetbrains.annotations.Nullable;

import static java.lang.Math.max;
import static java.lang.Math.min;

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
 * Levenberg-Marquardt is a trust-region method but was formulated before trust-region methods had been defined.
 * At each iteration it adjusted the value of 'v''. For smaller values it is closer to a Gauss-Newton step
 * and has super linear convergence while for large values it becomes a gradient step
 * </p>
 *
 * <p>
 * Levenberg's formulation is as follows:<br>
 * (J<sup>T</sup>J + &lambda; I) = J<sup>T</sup>r<br>
 * where &lambda; is adjusted at each iteration.
 * </p>
 * <p>
 * Marquardt's formulation is as follows:<br>
 * (J<sup>T</sup>J + &lambda; diag(J<sup>T</sup>J )) = J<sup>T</sup>r<br>
 * where &lambda; is adjusted at each iteration.
 * </p>
 *
 * <ul>
 * <li>[1] JK. Madsen and H. B. Nielsen and O. Tingleff, "Methods for Non-Linear Least Squares Problems (2nd ed.)"
 * Informatics and Mathematical Modelling, Technical University of Denmark</li>
 * <li>[2] Peter Abeles, "DDogleg Technical Report: Nonlinear Optimization", Revision 1, August 2018</li>
 * <li>[3] Levenberg, Kenneth (1944). "A Method for the Solution of Certain Non-Linear Problems in Least Squares".
 *         Quarterly of Applied Mathematics. 2: 164–168.</li>
 * </ul>
 *
 * @author Peter Abeles
 */
public abstract class LevenbergMarquardt_F64<S extends DMatrix, HM extends HessianMath>
		extends GaussNewtonBase_F64<ConfigLevenbergMarquardt, HM> {
	/**
	 * Maximum allowed value of lambda
	 */
	public static final double MAX_LAMBDA = 1e100;

	// Math for some matrix operations
	public MatrixMath<S> math;

	public DMatrixRMaj residuals = new DMatrixRMaj(1, 1);

	public DMatrixRMaj diagOrig = new DMatrixRMaj(1, 1);
	public DMatrixRMaj diagStep = new DMatrixRMaj(1, 1);

	/** Given the residuals it computes the "Loss" or cost */
	protected LossFunction lossFunc = new LossSquared();

	/** Gradient of the loss function. If null then squared error is assumed and this step can be skipped. */
	protected @Nullable LossFunctionGradient lossFuncGradient;

	// Storage for the loss gradient
	protected DMatrixRMaj storageLossGradient = new DMatrixRMaj();

	/**
	 * Dampening parameter. Scalar that's adjusted at every step. smaller values for a Gauss-Newton step and larger
	 * values for a gradient step
	 */
	protected double lambda;

	// TODO comment
	protected double nu;
	private final static double NU_INITIAL = 2;

	protected LevenbergMarquardt_F64( MatrixMath<S> math, HM hessian ) {
		super(hessian);
		configure(new ConfigLevenbergMarquardt());
		this.math = math;
	}

	/**
	 * Specifies the loss function.
	 */
	public void setLoss( LossFunction loss, LossFunctionGradient lossGradient ) {
		this.lossFunc = loss;
		this.lossFuncGradient = lossGradient;
	}

	/**
	 * Initializes the search.
	 *
	 * @param initial Initial state
	 * @param numberOfParameters number of parameters
	 * @param numberOfFunctions number of functions
	 */
	public void initialize( double[] initial, int numberOfParameters, int numberOfFunctions ) {
		super.initialize(initial, numberOfParameters);
		lambda = config.dampeningInitial;
		nu = NU_INITIAL;

		lossFunc.setNumberOfFunctions(numberOfFunctions);
		if (lossFuncGradient != null)
			lossFuncGradient.setNumberOfFunctions(numberOfFunctions);

		storageLossGradient.reshape(numberOfFunctions, 1);
		residuals.reshape(numberOfFunctions, 1);

		diagOrig.reshape(numberOfParameters, 1);
		diagStep.reshape(numberOfParameters, 1);

		computeResiduals(x, residuals);
		lossFunc.fixate(residuals.data);
		fx = costFromResiduals(residuals);

		mode = Mode.COMPUTE_DERIVATIVES;

		if (verbose != null) {
			verbose.println("Steps     fx        change      |step|   f-test     g-test    tr-ratio  lambda ");
			verbose.printf("%-4d  %9.3E  %10.3E  %9.3E  %9.3E  %9.3E  %6.2f   %6.2E\n",
					totalSelectSteps, fx, 0.0, 0.0, 0.0, 0.0, 0.0, lambda);
		}
	}

	/**
	 * Computes derived
	 *
	 * @return true if it has converged or false if it has not
	 */
	@Override protected boolean updateDerivates() {
		functionGradientHessian(x, true, gradient, hessian);

		if (config.hessianScaling) {
			computeHessianScaling();
			applyHessianScaling();
		}

		hessian.extractDiagonals(diagOrig);

		if (checkConvergenceGTest(gradient)) {
			if (verbose != null) {
				verbose.println("Converged g-test");
			}
			return true;
		}

		mode = Mode.DETERMINE_STEP;

		return false;
	}

	/**
	 * Compute a new possible state and determine if it should be accepted or not. If accepted update the state
	 *
	 * @return true if it has converged or false if it has not
	 */
	@Override protected boolean computeStep() {
		// compute the new location and it's score
		if (!computeStep(lambda, gradient, p)) {
			if (config.mixture == 0.0) {
				throw new OptimizationException("Singular matrix encountered. Try setting mixture to a non-zero value");
			}
			lambda *= 4;
			if (verbose != null)
				verbose.println(totalFullSteps + " Step computation failed. Increasing lambda");
			return maximumLambdaNu();
		}

		if (config.hessianScaling)
			undoHessianScalingOnParameters(p);

		// compute the potential new state
		CommonOps_DDRM.add(x, p, x_next);
		computeResiduals(x_next, residuals);
		double fx_candidate = costFromResiduals(residuals);

		if (UtilEjml.isUncountable(fx_candidate))
			throw new OptimizationException("Uncountable candidate score: " + fx_candidate);

		// compute model prediction accuracy
		double actualReduction = fx - fx_candidate;
		double predictedReduction = computePredictedReduction(p);

		if (actualReduction == 0 || predictedReduction == 0) {
			if (verbose != null)
				verbose.println(totalFullSteps + " reduction of zero");
			return true;
		}

		return processStepResults(fx_candidate, actualReduction, predictedReduction);
	}

	/**
	 * Sees if this is an improvement worth accepting. Adjust dampening parameter and change
	 * the state if accepted.
	 *
	 * @return true if it has converged or false if it has not
	 */
	private boolean processStepResults( double fx_candidate, double actualReduction, double predictedReduction ) {
		double ratio = actualReduction/predictedReduction;
		boolean accepted;

		// Accept the new state if the score improved
		if (fx_candidate < fx) {
			// reduce the amount of dampening.  Magic equation from [1].  My attempts to improve
			// upon it have failed.  It is truly magical.
			lambda = lambda*max(1.0/3.0, 1.0 - Math.pow(2.0*ratio - 1.0, 3.0));
			nu = NU_INITIAL;
			accepted = true;
		} else {
			lambda *= nu;
			nu *= 2;
			accepted = false;
		}

		if (UtilEjml.isUncountable(lambda) || UtilEjml.isUncountable(nu))
			throw new OptimizationException("BUG! lambda=" + lambda + "  nu=" + nu);

		if (accepted) {
			boolean converged = checkConvergenceFTest(fx_candidate, fx);
			if (verbose != null) {
				double length_p = NormOps_DDRM.normF(p); // TODO compute elsewhere ?
				verbose.printf("%-4d  %9.3E  %10.3E  %9.3E  %9.3E  %9.3E  %6.3f   %6.2E\n",
						totalSelectSteps, fx_candidate, fx_candidate - fx, length_p, ftest_val, gtest_val, ratio, lambda);
				if (converged) {
					verbose.println("Converged f-test");
				}
			}

			acceptNewState(fx_candidate);
			return converged || maximumLambdaNu();
		}
		return false;
	}

	/**
	 * The new state has been accepted. Switch the internal state over to this
	 *
	 * @param fx_candidate The new state
	 */
	protected void acceptNewState( double fx_candidate ) {
		DMatrixRMaj tmp = x;
		x = x_next;
		x_next = tmp;

		// Now that a new state has been excepted, pass in the new residuals to the loss function. If the cost
		// function has dynamically changed then recompute the cost as a result
		if (lossFunc.fixate(residuals.data)) {
			fx = lossFunc.process(residuals.data);
		} else {
			fx = fx_candidate;
		}

		mode = Mode.COMPUTE_DERIVATIVES;
	}

	/**
	 * <p>Checks for convergence using f-test. f-test is defined differently for different problems</p>
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	protected boolean checkConvergenceFTest( double fx, double fx_prev ) {
		if (fx_prev < fx)
			throw new OptimizationException("Score got worse. Shoul have been caught earlier!");

		// f-test. avoid potential divide by zero errors
		ftest_val = 1.0 - fx/fx_prev; // for print later on
		return config.ftol*fx_prev >= fx_prev - fx;
	}

	/**
	 * Given the residuals compute the cost
	 *
	 * @param residuals (Input) residuals/errors
	 * @return Least squares cost
	 */
	public double costFromResiduals( DMatrixRMaj residuals ) {
		return lossFunc.process(residuals.data);
	}

	/**
	 * Adjusts the Hessian's diagonal elements value and computes the next step
	 *
	 * @param lambda (Input) tuning
	 * @param gradient (Input) gradient
	 * @param step (Output) step
	 * @return true if solver could compute the next step
	 */
	protected boolean computeStep( double lambda, DMatrixRMaj gradient, DMatrixRMaj step ) {
		final double mixture = config.mixture;
		for (int i = 0; i < diagOrig.numRows; i++) {
			double v = min(config.diagonalMax, max(config.diagonalMin, diagOrig.data[i]));
			diagStep.data[i] = v + lambda*(mixture + (1.0 - mixture)*v);
		}
		hessian.setDiagonals(diagStep);

		if (!hessian.initializeSolver()) {
			return false;
		}

		// In the book formulation it solves something like (B + lambda*I)*p = -g
		// but we don't want to modify g, so we apply the negative to the step instead
		if (hessian.solve(gradient, step)) {
			CommonOps_DDRM.scale(-1, step);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * If the size of lambda or nu has grown so large it's reached a limit stop optimizing
	 */
	public boolean maximumLambdaNu() {
		return UtilEjml.isUncountable(lambda) || lambda >= MAX_LAMBDA || UtilEjml.isUncountable(nu);
	}

	/**
	 * Computes the residuals at state 'x'
	 *
	 * @param x (Input) state
	 * @param residuals (Output) residuals F(x) - Y
	 */
	protected abstract void computeResiduals( DMatrixRMaj x, DMatrixRMaj residuals );

	/**
	 * Changes the optimization's configuration
	 *
	 * @param config New configuration
	 */
	public void configure( ConfigLevenbergMarquardt config ) {
		this.config = config.copy();
	}
}
