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

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionMath_DDRM {

	Random rand = new Random(234);
	TrustRegionMath_DDRM alg = new TrustRegionMath_DDRM();

	@Test
	public void setIdentity() {
		DMatrixRMaj A = new DMatrixRMaj(3,3);
		alg.setIdentity(A);
		assertTrue(MatrixFeatures_DDRM.isIdentity(A,UtilEjml.TEST_F64));
	}

	@Test
	public void innerMatrixProduct() {
		DMatrixRMaj A = new DMatrixRMaj(4,2);
		DMatrixRMaj expected = new DMatrixRMaj(2,2);
		RandomMatrices_DDRM.fillUniform(A,-1,1,rand);

		CommonOps_DDRM.multTransA(A,A,expected);

		DMatrixRMaj found = new DMatrixRMaj(2,2);
		alg.innerMatrixProduct(A,found);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found, UtilEjml.TEST_F64));
	}

	@Test
	public void innerProduct() {
		DMatrixRMaj A = new DMatrixRMaj(4,1);
		DMatrixRMaj B = new DMatrixRMaj(4,4);

		RandomMatrices_DDRM.fillUniform(A,-1,1,rand);
		RandomMatrices_DDRM.fillUniform(B,-1,1,rand);

		DMatrixRMaj tmp = new DMatrixRMaj(1,4);
		CommonOps_DDRM.multTransA(A,B,tmp);
		double expected = CommonOps_DDRM.dot(A,tmp);


		double found = alg.innerProduct(A,B);

		assertEquals(expected, found, UtilEjml.TEST_F64);
	}
}