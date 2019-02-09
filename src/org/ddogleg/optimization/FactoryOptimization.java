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

import org.ddogleg.optimization.lm.ConfigLevenbergMarquardt;
import org.ddogleg.optimization.lm.UnconLeastSqLevenbergMarquardtSchur_F64;
import org.ddogleg.optimization.lm.UnconLeastSqLevenbergMarquardt_F64;
import org.ddogleg.optimization.math.*;
import org.ddogleg.optimization.quasinewton.ConfigQuasiNewton;
import org.ddogleg.optimization.quasinewton.LineSearchFletcher86;
import org.ddogleg.optimization.quasinewton.LineSearchMore94;
import org.ddogleg.optimization.quasinewton.QuasiNewtonBFGS;
import org.ddogleg.optimization.trustregion.*;
import org.ddogleg.optimization.wrap.QuasiNewtonBFGS_to_UnconstrainedMinimization;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;

import javax.annotation.Nullable;

/**
 * Creates optimization algorithms using easy to use interfaces.  These implementations/interfaces
 * are designed to be easy to use and effective for most tasks.  If more control is needed then
 * create an implementation directly.
 *
 * @author Peter Abeles
 */
public class FactoryOptimization {

	/**
	 * Creates a sparse Schur Complement trust region optimization using dogleg steps.
	 *
	 * @see UnconLeastSqTrustRegionSchur_F64
	 *
	 * @param config Trust region configuration
	 * @return The new optimization routine
	 */
	public static UnconstrainedLeastSquaresSchur<DMatrixRMaj> doglegSchur(boolean robust, @Nullable ConfigTrustRegion config ) {
		if( config == null )
			config = new ConfigTrustRegion();

		HessianSchurComplement_DDRM hessian;

		if( robust ) {
			LinearSolverDense<DMatrixRMaj> solverA = LinearSolverFactory_DDRM.pseudoInverse(true);
			LinearSolverDense<DMatrixRMaj> solverD = LinearSolverFactory_DDRM.pseudoInverse(true);
			hessian = new HessianSchurComplement_DDRM(solverA,solverD);
		} else {
			// defaults to cholesky
			hessian = new HessianSchurComplement_DDRM();
		}
		TrustRegionUpdateDogleg_F64<DMatrixRMaj> update = new TrustRegionUpdateDogleg_F64<>();
		UnconLeastSqTrustRegionSchur_F64<DMatrixRMaj> alg = new UnconLeastSqTrustRegionSchur_F64<>(update,hessian);
		alg.configure(config);
		return alg;
	}

	/**
	 * Returns an implementation of {@link QuasiNewtonBFGS} with {@link LineSearchMore94} for the internal line search.
	 * This is a specific implementation of {@link UnconstrainedMinimization} and allows full access to all
	 * tuning parameters.
	 *
	 * @return UnconstrainedMinimization
	 */
	public static QuasiNewtonBFGS_to_UnconstrainedMinimization quasiNewtonBfgs(@Nullable ConfigQuasiNewton config ) {
		if( config == null )
			config = new ConfigQuasiNewton();

		LineSearch lineSearch;
		switch( config.lineSearch ) {
			case FLETCHER86:
				LineSearchFletcher86 fletcher86 = new LineSearchFletcher86();
				fletcher86.setConvergence(config.line_ftol,config.line_gtol);
				lineSearch = fletcher86;
				break;

			case MORE94:
				LineSearchMore94 more94 = new LineSearchMore94();
				more94.setConvergence(config.line_ftol,config.line_gtol,0.1);
				lineSearch = more94;
				break;

			default:
				throw new RuntimeException("Unknown line search. "+config.lineSearch);
		}

		QuasiNewtonBFGS qn = new QuasiNewtonBFGS(lineSearch);
		return new QuasiNewtonBFGS_to_UnconstrainedMinimization(qn);
	}

	/**
	 * Creates a dense trust region least-squares optimization using dogleg steps. Solver works on the B=J<sup>T</sup>J matrix.
	 *
	 * @see UnconLeastSqTrustRegion_F64
	 *
	 * @param config Trust region configuration
	 * @return The new optimization routine
	 */
	public static UnconstrainedLeastSquares<DMatrixRMaj> dogleg( @Nullable ConfigTrustRegion config, boolean robust ) {
		if( config == null )
			config = new ConfigTrustRegion();

		LinearSolverDense<DMatrixRMaj> solver;
		if( robust )
			solver = LinearSolverFactory_DDRM.leastSquaresQrPivot(true,false);
		else
			solver = LinearSolverFactory_DDRM.chol(100);

		HessianLeastSquares_DDRM hessian = new HessianLeastSquares_DDRM(solver);
		MatrixMath_DDRM math = new MatrixMath_DDRM();
		TrustRegionUpdateDogleg_F64<DMatrixRMaj> update = new TrustRegionUpdateDogleg_F64<>();
		UnconLeastSqTrustRegion_F64<DMatrixRMaj> alg = new UnconLeastSqTrustRegion_F64<>(update,hessian,math);
		alg.configure(config);
		return alg;
	}

	/**
	 * Creates a dense trust region least-squares optimization using cauchy steps.
	 *
	 * @see UnconLeastSqTrustRegion_F64
	 *
	 * @param config Trust region configuration
	 * @return The new optimization routine
	 */
	public static UnconstrainedLeastSquares<DMatrixRMaj> cauchy( @Nullable ConfigTrustRegion config ) {
		if( config == null )
			config = new ConfigTrustRegion();

		HessianLeastSquares_DDRM hessian = new HessianLeastSquares_DDRM();
		MatrixMath_DDRM math = new MatrixMath_DDRM();
		TrustRegionUpdateCauchy_F64<DMatrixRMaj> update = new TrustRegionUpdateCauchy_F64<>();
		UnconLeastSqTrustRegion_F64<DMatrixRMaj> alg = new UnconLeastSqTrustRegion_F64<>(update,hessian,math);
		alg.configure(config);
		return alg;
	}

	/**
	 * Dense trust-region unconstrained minimization using Dogleg steps and BFGS to estimate the Hessian.
	 * @param config Trust region configuration
	 * @return The new optimization routine
	 */
	public static UnconstrainedMinimization doglegBFGS( @Nullable ConfigTrustRegion config ) {
		if( config == null )
			config = new ConfigTrustRegion();

		HessianBFGS hessian = new HessianBFGS_DDRM(true);
		TrustRegionUpdateDogleg_F64<DMatrixRMaj> update = new TrustRegionUpdateDogleg_F64<>();
		UnconMinTrustRegionBFGS_F64 alg = new UnconMinTrustRegionBFGS_F64(update,hessian);
		alg.configure(config);
		return alg;
	}

	public static UnconstrainedLeastSquares<DMatrixRMaj> levenbergMarquardt(
			@Nullable ConfigLevenbergMarquardt config, boolean robust  )
	{
		if( config == null )
			config = new ConfigLevenbergMarquardt();

		LinearSolverDense<DMatrixRMaj> solver;
		if( robust )
			solver = LinearSolverFactory_DDRM.leastSquaresQrPivot(true,false);
		else
			solver = LinearSolverFactory_DDRM.chol(100);

		HessianLeastSquares_DDRM hessian = new HessianLeastSquares_DDRM(solver);
		UnconLeastSqLevenbergMarquardt_F64<DMatrixRMaj> lm = new UnconLeastSqLevenbergMarquardt_F64<>(new MatrixMath_DDRM(),hessian);
		lm.configure(config);
		return lm;
	}

	/**
	 * LM with Schur Complement
	 *
	 * @param robust If true then a slow by robust solver is used. true = use SVD
	 * @param config configuration for LM
	 * @return the solver
	 */
	public static UnconstrainedLeastSquaresSchur<DMatrixRMaj> levenbergMarquardtSchur(
			boolean robust,
			@Nullable ConfigLevenbergMarquardt config  )
	{
		if( config == null )
			config = new ConfigLevenbergMarquardt();

		HessianSchurComplement_DDRM hessian;

		if( robust ) {
			LinearSolverDense<DMatrixRMaj> solverA = LinearSolverFactory_DDRM.pseudoInverse(true);
			LinearSolverDense<DMatrixRMaj> solverD = LinearSolverFactory_DDRM.pseudoInverse(true);
			hessian = new HessianSchurComplement_DDRM(solverA,solverD);
		} else {
			// defaults to cholesky
			hessian = new HessianSchurComplement_DDRM();
		}

		UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixRMaj> lm =
				new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DDRM(),hessian);
		lm.configure(config);
		return lm;
	}
}
