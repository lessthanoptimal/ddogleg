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
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.equation.Equation;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MatrixFeatures_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionMath_DSCC {
	Random rand = new Random(234);
	TrustRegionMath_DSCC alg = new TrustRegionMath_DSCC();

	@Test
	public void setIdentity() {
		DMatrixSparseCSC A = new DMatrixSparseCSC(3,3);
		alg.setIdentity(A);
		assertTrue(MatrixFeatures_DSCC.isIdentity(A,UtilEjml.TEST_F64));
	}

	@Test
	public void innerMatrixProduct() {
		DMatrixSparseCSC A = RandomMatrices_DSCC.rectangle(4,2,6,-1,1,rand);
		DMatrixSparseCSC expected = new DMatrixSparseCSC(2,2);

		CommonOps_DSCC.multTransA(A,A,expected,null,null);

		DMatrixSparseCSC found = new DMatrixSparseCSC(2,2);
		alg.innerMatrixProduct(A,found);

		assertTrue(MatrixFeatures_DSCC.isIdenticalSort(expected,found, UtilEjml.TEST_F64));
	}

	@Test
	public void innerProduct() {
		DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,1,-1,1,rand);
		DMatrixSparseCSC B = RandomMatrices_DSCC.rectangle(4,4,9,-1,1,rand);


		Equation eq = new Equation();
		eq.alias(A,"A",B,"B");
		eq.process("n=A'*B*A");

		double expected = eq.lookupDDRM("n").get(0,0);

		double found = alg.innerProduct(A,B);

		assertEquals(expected, found, UtilEjml.TEST_F64);
	}
}