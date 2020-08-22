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

package org.ddogleg.optimization.math;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestHessianBFGS_DDRM extends StandardHessianMathChecks {


	public TestHessianBFGS_DDRM() {
		super(new HessianBFGS_DDRM(true));
	}

	@Override
	protected void setHessian(HessianMath alg, DMatrixRMaj H) {
		HessianBFGS_DDRM a = (HessianBFGS_DDRM)alg;

		a.hessian.set(H);
		CommonOps_DDRM.invert(H,a.hessianInverse);
	}

	@Test
	public void initialize() {
		alg.init(8);

		HessianBFGS_DDRM a = (HessianBFGS_DDRM)alg;
		assertTrue(MatrixFeatures_DDRM.isIdentity(a.hessian,UtilEjml.TEST_F64));
		assertTrue(MatrixFeatures_DDRM.isIdentity(a.hessianInverse,UtilEjml.TEST_F64));
	}

	/**
	 * Not supported yet
	 */
	@Test
	@Override
	public void divideRowsCols() {
		assertThrows(RuntimeException.class, super::divideRowsCols);
	}
}