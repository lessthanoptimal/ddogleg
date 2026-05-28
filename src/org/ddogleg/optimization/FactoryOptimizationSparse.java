/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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
import org.ddogleg.optimization.math.HessianLeastSquares_DSCC;
import org.ddogleg.optimization.math.HessianSchurComplement_DSCC;
import org.ddogleg.optimization.math.MatrixMath_DSCC;
import org.ddogleg.optimization.trustregion.*;
import org.ejml.LinearSolverType;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for sparse optimization algorithms.  These implementations/interfaces
 * are designed to be easy to use and effective for most tasks.  If more control is needed then
 * create an implementation directly.
 *
 * @author Peter Abeles
 */
public class FactoryOptimizationSparse {

	/**
	 * Generic factory for any unconstrained least squares solver using Schur decomposition
	 */
	public static UnconstrainedLeastSquaresSchur<DMatrixSparseCSC>
	leastSquaresSchur( ConfigNonLinearLeastSquares config ) {
		return switch (config.type) {
			case TRUST_REGION -> doglegSchur(config.trust);
			case LEVENBERG_MARQUARDT -> levenbergMarquardtSchur(config.lm);
		};
	}

	/**
	 * Generic factory for any unconstrained least squares solver
	 */
	public static UnconstrainedLeastSquares<DMatrixSparseCSC>
	leastSquares( ConfigNonLinearLeastSquares config ) {
		return switch (config.type) {
			case TRUST_REGION -> dogleg(config.trust);
			case LEVENBERG_MARQUARDT -> levenbergMarquardt(config.lm);
		};
	}

	/**
	 * Creates a sparse Schur Complement trust region optimization using dogleg steps.
	 *
	 * @param config Trust region configuration
	 * @return The new optimization routine
	 * @see UnconLeastSqTrustRegionSchur_F64
	 */
	public static UnconLeastSqTrustRegionSchur_F64<DMatrixSparseCSC> doglegSchur( @Nullable ConfigTrustRegion config ) {
		if (config == null)
			config = new ConfigTrustRegion();

		HessianSchurComplement_DSCC hessian = hessianSchurComplement(config.solverType);
		var update = new TrustRegionUpdateDogleg_F64<DMatrixSparseCSC>();
		var alg = new UnconLeastSqTrustRegionSchur_F64<>(update, hessian);
		alg.configure(config);
		return alg;
	}

	/**
	 * Creates a sparse trust region optimization using dogleg steps.
	 *
	 * @param config Trust region configuration
	 * @return The new optimization routine
	 * @see UnconLeastSqTrustRegion_F64
	 */
	public static UnconLeastSqTrustRegion_F64<DMatrixSparseCSC> dogleg( @Nullable ConfigTrustRegion config ) {
		if (config == null)
			config = new ConfigTrustRegion();

		HessianLeastSquares_DSCC hessian = hessianLeastSquares(config.solverType);

		var math = new MatrixMath_DSCC();
		var update = new TrustRegionUpdateDogleg_F64<DMatrixSparseCSC>();
		var alg = new UnconLeastSqTrustRegion_F64<>(update, hessian, math);
		alg.configure(config);
		return alg;
	}

	/**
	 * Creates a sparse trust region optimization using cauchy steps.
	 *
	 * @param config Trust region configuration
	 * @return The new optimization routine
	 * @see UnconLeastSqTrustRegion_F64
	 */
	public static UnconLeastSqTrustRegion_F64<DMatrixSparseCSC> cauchy( @Nullable ConfigTrustRegion config ) {
		if (config == null)
			config = new ConfigTrustRegion();

		var hessian = new HessianLeastSquares_DSCC();
		var math = new MatrixMath_DSCC();
		var update = new TrustRegionUpdateCauchy_F64<DMatrixSparseCSC>();
		var alg = new UnconLeastSqTrustRegion_F64<>(update, hessian, math);
		alg.configure(config);
		return alg;
	}

	public static UnconLeastSqLevenbergMarquardt_F64<DMatrixSparseCSC> levenbergMarquardt(
			@Nullable ConfigLevenbergMarquardt config ) {
		if (config == null)
			config = new ConfigLevenbergMarquardt();

		HessianLeastSquares_DSCC hessian = hessianLeastSquares(config.solverType);

		var lm = new UnconLeastSqLevenbergMarquardt_F64<>(new MatrixMath_DSCC(), hessian);
		lm.configure(config);
		return lm;
	}

	public static UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> levenbergMarquardtSchur(
			@Nullable ConfigLevenbergMarquardt config ) {
		if (config == null)
			config = new ConfigLevenbergMarquardt();

		HessianSchurComplement_DSCC hessian = hessianSchurComplement(config.solverType);

		var lm = new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DSCC(), hessian);
		lm.configure(config);
		return lm;
	}

	private static @NotNull HessianLeastSquares_DSCC hessianLeastSquares( LinearSolverType config ) {
		return switch (config) {
			case DEFAULT -> new HessianLeastSquares_DSCC();
			default -> {
				LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = solver(config);
				yield new HessianLeastSquares_DSCC(solver);
			}
		};
	}

	private static HessianSchurComplement_DSCC hessianSchurComplement( LinearSolverType config ) {
		return switch (config) {
			case DEFAULT -> new HessianSchurComplement_DSCC();
			default -> {
				LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solverA = solver(config);
				LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solverD = solver(config);
				yield new HessianSchurComplement_DSCC(solverA, solverD);
			}
		};
	}

	public static LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver( LinearSolverType type ) {
		return switch (type) {
			case CHOLESKY, DEFAULT -> LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
			case QR -> LinearSolverFactory_DSCC.qr(FillReducing.NONE);
			default -> throw new RuntimeException("Type not supported " + type);
		};
	}
}
