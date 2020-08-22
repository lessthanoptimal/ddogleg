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
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class StandardHessianMathChecks {

	Random rand = new Random(234);
	HessianMath alg;

	protected StandardHessianMathChecks(HessianMath alg ) {
		this.alg = alg;
	}

	protected abstract void setHessian( HessianMath alg, DMatrixRMaj H );

	@Test
	public void innerVectorHessian() {
		DMatrixRMaj M = RandomMatrices_DDRM.symmetricPosDef(10,rand);
		DMatrixRMaj v = RandomMatrices_DDRM.rectangle(10,1,rand);

		double expected = VectorVectorMult_DDRM.innerProdA(v,M,v);

		setHessian(alg,M);
		double found = alg.innerVectorHessian(v);

		assertEquals(expected,found, UtilEjml.TEST_F64);
	}

	@Test
	public void extractDiagonals() {
		DMatrixRMaj M = RandomMatrices_DDRM.rectangle(6,6,rand);

		setHessian(alg,M);

		DMatrixRMaj v = RandomMatrices_DDRM.rectangle(6,1,rand);
		alg.extractDiagonals(v);

		for (int i = 0; i < M.numRows; i++) {
			assertEquals(M.get(i,i),v.get(i,0), UtilEjml.TEST_F64);
		}
	}

	@Test
	public void setDiagonals() {
		DMatrixRMaj M = RandomMatrices_DDRM.rectangle(6,6,rand);

		setHessian(alg,M);

		DMatrixRMaj v = RandomMatrices_DDRM.rectangle(6,1,rand);
		alg.setDiagonals(v);

		DMatrixRMaj found = RandomMatrices_DDRM.rectangle(6,1,rand);
		alg.extractDiagonals(found);


		for (int i = 0; i < M.numRows; i++) {
			assertEquals(found.get(i),v.get(i), UtilEjml.TEST_F64);
		}
	}

	@Test
	public void divideRowsCols() {
		DMatrixRMaj M = RandomMatrices_DDRM.symmetricPosDef(10,rand);
		DMatrixRMaj scale = RandomMatrices_DDRM.rectangle(10,1,0,1,rand);

		DMatrixRMaj expected = M.copy();
		CommonOps_DDRM.divideRows(scale.data,expected);
		CommonOps_DDRM.divideCols(expected,scale.data);

		setHessian(alg,M);

		alg.divideRowsCols(scale);

		// Not a great unit test since it doesn't check the off diagonal elements
		DMatrixRMaj found = RandomMatrices_DDRM.rectangle(10,1,rand);
		alg.extractDiagonals(found);

		for (int i = 0; i < M.numRows; i++) {
			assertEquals(expected.get(i,i),found.get(i), UtilEjml.TEST_F64);
		}
	}

	@Test
	public void solve() {
		DMatrixRMaj M = RandomMatrices_DDRM.symmetricPosDef(10,rand);
		DMatrixRMaj v = RandomMatrices_DDRM.rectangle(10,1,rand);

		DMatrixRMaj origv = v.copy();

		DMatrixRMaj expected = v.createLike();
		CommonOps_DDRM.solve(M,v,expected);

		DMatrixRMaj found = v.createLike();

		alg.init(M.numCols);
		setHessian(alg,M);
		assertTrue(alg.initializeSolver());
		assertTrue(alg.solve(v,found));

		// make sure it didn't modify the input
		assertTrue(MatrixFeatures_DDRM.isIdentical(origv,origv,UtilEjml.TEST_F64));

		// check the solution
		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));

		// run it again, if nothing was modified it should produce the same solution
		assertTrue(alg.initializeSolver());
		assertTrue(alg.solve(v,found));
		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
	}

}
