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

package org.ddogleg.optimization.math;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class StandardHessianLeastSquaresChecks extends StandardHessianMathChecks {
	public StandardHessianLeastSquaresChecks(HessianMath alg) {
		super(alg);
	}

	protected abstract DMatrix convert( DMatrixRMaj M );

	@Test
	public void updateHessian() {
		DMatrixRMaj J = RandomMatrices_DDRM.rectangle(30,6,rand);
		DMatrixRMaj H = new DMatrixRMaj(1,1);

		CommonOps_DDRM.multTransA(J,J,H);

		((HessianLeastSquares)alg).updateHessian(convert(J));

		DMatrixRMaj expected = new DMatrixRMaj(6,1);
		DMatrixRMaj found = expected.createLike();

		alg.extractDiag(found);
		CommonOps_DDRM.extractDiag(H,expected);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
	}
}
