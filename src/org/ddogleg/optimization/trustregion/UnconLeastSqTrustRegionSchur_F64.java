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
import org.ddogleg.optimization.UnconstrainedLeastSquaresSchur;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.impl.SchurComplementMath;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.SpecializedOps_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;

import javax.annotation.Nonnull;

/**
 * Implementations of {@link UnconstrainedLeastSquaresSchur}. Uses {@link SchurComplementMath}
 * to compute the Schur complement and perform all math related to the Hessian. The Hessian
 * is stored in a custom block format and the Hessian referenced in the parent class is ignored.
 * All functions which reference the original Hessian are overriden.
 *
 * @author Peter Abeles
 */
public class UnconLeastSqTrustRegionSchur_F64
		extends TrustRegionBase_F64<DMatrixSparseCSC>
		implements UnconstrainedLeastSquaresSchur<DMatrixSparseCSC>
{
	protected DMatrixRMaj residuals = new DMatrixRMaj(1,1);

	protected FunctionNtoM functionResiduals;
	protected SchurJacobian<DMatrixSparseCSC> functionJacobian;

	// Left and right side of the jacobian matrix
	protected DMatrixSparseCSC jacLeft = new DMatrixSparseCSC(1,1,1);
	protected DMatrixSparseCSC jacRight = new DMatrixSparseCSC(1,1,1);

	// Contains math for Schur complement
	protected SchurComplementMath schur;

	public UnconLeastSqTrustRegionSchur_F64(){
		this.parameterUpdate = new SchurDogleg();
		this.math = null; // the math is represented completely differently here.
		this.schur = new SchurComplementMath();

		// Mark the hessian as null to ensure the code will blow up if a function is missed
		// that attempts to access the hessian. Well it was already null, but this makes it clear
		// that it was null intentionally.
		hessian = null;
	}

	@Override
	public void setFunction(FunctionNtoM function, @Nonnull SchurJacobian<DMatrixSparseCSC> jacobian) {
		this.functionResiduals = function;
		this.functionJacobian = jacobian;
		schur.initialize();
	}

	/**
	 * <p>Checks for convergence using f-test:</p>
	 *
	 * f-test : ftol &le; || r(x+p) || infinity
	 *
	 * @param fx Ignored. Residual used instead
	 * @param fx_prev Ignored. Residual used instead
	 *
	 * @return true if converged or false if it hasn't converged
	 */
	@Override
	protected boolean checkConvergenceFTest(double fx, double fx_prev ) {
		// something really bad has happened if this gets triggered before it thinks it converged
		if( UtilEjml.isUncountable(regionRadius) || regionRadius <= 0 )
			throw new OptimizationException("Failing to converge. Region size hit a wall. r="+regionRadius);

		for (int i = 0; i < residuals.numRows; i++) {
			if( Math.abs(residuals.data[i]) > config.ftol )
				return false;
		}
		return true;
	}

	@Override
	public void initialize(double[] initial, double ftol, double gtol) {
		this.initialize(initial,functionResiduals.getNumOfInputsN(),0);
		config.ftol = ftol;
		config.gtol = gtol;
	}

	@Override
	public void initialize(double[] initial, int numberOfParameters, double minimumFunctionValue) {
		int M = functionResiduals.getNumOfOutputsM();
		residuals.reshape(M,1);

		super.initialize(initial, numberOfParameters, minimumFunctionValue);
	}

	@Override
	protected double cost(DMatrixRMaj x) {
		functionResiduals.process(x.data,residuals.data);
		return 0.5*SpecializedOps_DDRM.elementSumSq(residuals);
	}

	@Override
	protected void functionGradientHessian(DMatrixRMaj x, boolean sameStateAsCost,
										   DMatrixRMaj gradient, DMatrixSparseCSC hessian) {
		if( !sameStateAsCost )
			functionResiduals.process(x.data,residuals.data);
		functionJacobian.process(x.data,jacLeft,jacRight);
		schur.computeHessian(jacLeft,jacRight);
		schur.computeGradient(jacLeft,jacRight,residuals,gradient);
	}

	@Override
	protected void computeScaling() {
		schur.extractHessianDiagonal(scaling);
		computeScaling(scaling, config.scalingMinimum, config.scalingMaximum);
	}

	@Override
	protected void applyScaling() {
		CommonOps_DDRM.elementDiv(gradient,scaling);
		schur.elementDivHessian(scaling);
	}

	@Override
	public double computePredictedReduction(DMatrixRMaj p) {
		double p_dot_g = VectorVectorMult_DDRM.innerProd(p,gradient);
		double p_JJ_p = schur.innerProductHessian(p);

		return -p_dot_g - 0.5*p_JJ_p;
	}

	private class SchurDogleg extends TrustRegionUpdateDogleg_F64<DMatrixSparseCSC> {
		@Override
		protected double innerProductHessian(DMatrixRMaj v) {
			return schur.innerProductHessian(v);
		}

		@Override
		protected boolean solveGaussNewtonPoint(DMatrixRMaj pointGN) {
			return schur.computeStep(gradient,pointGN);
		}
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

	public DMatrixRMaj getResiduals() {
		return residuals;
	}

}
