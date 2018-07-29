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
import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ddogleg.optimization.impl.EquationsBFGS;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 * Implementations of {@link TrustRegionUpdateCauchy_F64} for {@link UnconstrainedMinimization}. The hessian is approximated
 * using the {@link EquationsBFGS BFGS} method. This method exhibits poor convergence, probably due to the Hessian
 * being estimated poorly at times.
 *
 * @author Peter Abeles
 */
public class UnconMinTrustRegionBFGS_F64
		extends TrustRegionBase_F64<DMatrixRMaj>
		implements UnconstrainedMinimization
{
	// temp variable of length N
	private DMatrixRMaj y = new DMatrixRMaj(1,1);
	private DMatrixRMaj tmpN1 = new DMatrixRMaj(1,1);
	private DMatrixRMaj tmpN2 = new DMatrixRMaj(1,1);
	private DMatrixRMaj gradientPrevious = new DMatrixRMaj(1,1);
	private DMatrixRMaj xPrevious = new DMatrixRMaj(1,1);
	private DMatrixRMaj s = new DMatrixRMaj(1,1);

	protected DMatrixRMaj hessianInverse = new DMatrixRMaj(1,1);
	protected boolean computeInverse=false;

	double f_prev;

	private FunctionNtoS functionCost;
	private FunctionNtoN functionGradient;

	private double minimumFunctionValue;

	// true if it's the first iteration
	private boolean firstIteration;

	double c1=1e-4,c2=0.9;

	public UnconMinTrustRegionBFGS_F64(ParameterUpdate parameterUpdate) {
		super(parameterUpdate, new TrustRegionMath_DDRM());
	}

	@Override
	public void setFunction(FunctionNtoS function, FunctionNtoN gradient, double minFunctionValue) {
		this.functionCost = function;
		this.functionGradient = gradient;
		this.minimumFunctionValue = minFunctionValue;
	}

	@Override
	public void initialize(double[] initial, double ftol, double gtol) {
		this.initialize(initial,functionCost.getNumOfInputsN(), minimumFunctionValue);
		config.ftol = ftol;
		config.gtol = gtol;
	}

	/**
	 * <p>Checks for convergence using f-test:</p>
	 *
	 * f-test : ftol &le; 1.0-f(x+p)/f(x)
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	@Override
	protected boolean checkConvergenceFTest(double fx, double fx_prev ) {
		// something really bad has happened if this gets triggered before it thinks it converged
		if( UtilEjml.isUncountable(regionRadius) || regionRadius <= 0 )
			throw new OptimizationException("Failing to converge. Region size hit a wall. r="+regionRadius);

		if( fx > fx_prev )
			throw new RuntimeException("BUG! Shouldn't have gotten this far");

		// f-test. avoid potential divide by zero errors
		return config.ftol * fx_prev >= fx_prev - fx;
	}

	/**
	 * Override parent to initialize matrices
	 */
	@Override
	public void initialize(double[] initial, int numberOfParameters, double minimumFunctionValue) {
		super.initialize(initial, numberOfParameters,minimumFunctionValue);
		y.reshape(numberOfParameters,1);
		tmpN1.reshape(numberOfParameters,1);
		tmpN2.reshape(numberOfParameters,1);

		xPrevious.reshape(numberOfParameters,1);
		x.reshape(numberOfParameters,1);

		// Set the hessian to identity. There are other potentially better methods
		hessian.reshape(numberOfParameters,numberOfParameters);
		math.setIdentity(hessian);

		if( computeInverse ) {
			hessianInverse.reshape(numberOfParameters,numberOfParameters);
			math.setIdentity(hessianInverse);
		}

		// set the previous gradient to zero
		gradientPrevious.reshape(numberOfParameters,1);
		gradientPrevious.zero();

		firstIteration = true;
	}

	@Override
	protected double cost(DMatrixRMaj x) {
		return functionCost.process(x.data);
	}

	@Override
	protected void functionGradientHessian(DMatrixRMaj x, boolean sameStateAsCost, DMatrixRMaj gradient, DMatrixRMaj hessian) {
		functionGradient.process(x.data, gradient.data);

		if( !firstIteration ) {
			//			if( isScaling() ) {
//				// undo the scaling which was previous applied to the hessian
//				// The gradient was just computed so it's not scaled yet
//				math.scaleColumns(scaling.data, hessian);
//				math.scaleRows(scaling.data, hessian);
//			}

			// compute the change in Gradient
			CommonOps_DDRM.subtract(gradient, gradientPrevious, y);
			CommonOps_DDRM.subtract(x, xPrevious, s);

			// Only update when the Wolfe condition is true of the Hessian gets corrected very quickly
			if( wolfeCondition(s,y,gradientPrevious)) {
				// Apply DFP equation and update H
				// Note: there is some duplication in math between Wolfe, update(), and inverseUpdate()
				EquationsBFGS.update(hessian, s, y, tmpN1, tmpN2);
				if( computeInverse ) {
					EquationsBFGS.inverseUpdate(hessianInverse,s,y,tmpN1,tmpN2);
				}

				gradientPrevious.set(gradient);
				xPrevious.set(x);
				f_prev = fx;
			}
		} else {
			firstIteration = false;
			gradientPrevious.set(gradient);
			xPrevious.set(x);
			f_prev = fx;
		}
	}

	protected boolean wolfeCondition( DMatrixRMaj s , DMatrixRMaj y , DMatrixRMaj g_k) {
//		return true;
		double left = CommonOps_DDRM.dot(y,s);
		double g_s = CommonOps_DDRM.dot(g_k,s);
		double right = (c2-1)*g_s;
		if( left >= right ) {
			return (fx-f_prev) <= c1*g_s;
		}
		return false;
	}

	public void setComputeInverse(boolean computeInverse) {
		this.computeInverse = computeInverse;
	}

	public DMatrixRMaj getHessianInverse() {
		return hessianInverse;
	}

	@Override
	public double[] getParameters() {
		return x.data;
	}

	@Override
	public double getFunctionValue() {
		return fx;
	}

	@Override
	public boolean isUpdated() {
		return mode == Mode.FULL_STEP;
	}

	@Override
	public boolean isConverged() {
		return mode == Mode.CONVERGED;
	}

	@Override
	public String getWarning() {
		return null;
	}
}
