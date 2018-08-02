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

import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedLeastSquares_DDRM;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedLeastSquares_DSCC;
import org.ejml.LinearSolverSafe;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import org.junit.jupiter.api.Nested;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionUpdateDoglegLS_F64 {
	@Nested
	class LeastSquaresDDRM extends CommonChecksUnconstrainedLeastSquares_DDRM {

		@Override
		protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();

			return declare(config);
		}
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquares_DSCC {

		@Override
		protected UnconstrainedLeastSquares<DMatrixSparseCSC> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();

			LinearSolver<DMatrixSparseCSC,DMatrixRMaj> solver = LinearSolverFactory_DSCC.qr(FillReducing.NONE);
			TrustRegionUpdateDogleg_F64<DMatrixSparseCSC> alg = new TrustRegionUpdateDoglegLS_F64<>(solver);

			UnconLeastSqTrustRegion_F64<DMatrixSparseCSC> tr =
					new UnconLeastSqTrustRegion_F64<>(alg, new TrustRegionMath_DSCC());
			tr.configure(config);
			return tr;
		}
	}

	/**
	 * Test to see if scaling is handled correctly
	 */
	@Nested
	class LeastSquaresDDRM_Scaling extends CommonChecksUnconstrainedLeastSquares_DDRM {

		@Override
		protected UnconstrainedLeastSquares<DMatrixRMaj> createSearch(double minimumValue) {
			ConfigTrustRegion config = new ConfigTrustRegion();
			config.regionInitial = 100;
			config.scalingMinimum = 1e-4;
			config.scalingMaximum = 1e4;

			return declare(config);
		}
	}

	private static UnconstrainedLeastSquares<DMatrixRMaj> declare(ConfigTrustRegion config) {
		LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.qr(4, 2);
		solver = new LinearSolverSafe<>(solver);
		TrustRegionUpdateDogleg_F64<DMatrixRMaj> alg = new TrustRegionUpdateDoglegLS_F64<>(solver);

		UnconLeastSqTrustRegion_F64<DMatrixRMaj> tr =
				new UnconLeastSqTrustRegion_F64<>(alg, new TrustRegionMath_DDRM());
		tr.configure(config);
		return tr;
	}
}