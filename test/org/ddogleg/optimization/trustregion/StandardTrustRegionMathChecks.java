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
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class StandardTrustRegionMathChecks<T extends DMatrix> {
	public abstract T convertA(DMatrixRMaj A );

	public abstract DMatrixRMaj convertB(T A );

	public abstract T create(int numRows , int numCols );

	Random rand = new Random(234);
	TrustRegionBase_F64.MatrixMath<T> alg;


	public StandardTrustRegionMathChecks( TrustRegionBase_F64.MatrixMath<T> alg ) {
		this.alg = alg;
	}

	@Test
	public void setIdentity() {
		DMatrixRMaj A = new DMatrixRMaj(3,3);

		T _A = convertA(A);
		alg.setIdentity(_A);
		A = convertB(_A);
		assertTrue(MatrixFeatures_DDRM.isIdentity(A, UtilEjml.TEST_F64));
	}

	@Test
	public void innerMatrixProduct() {
		DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,2,-1,1,rand);
		DMatrixRMaj expected = new DMatrixRMaj(2,2);

		CommonOps_DDRM.multTransA(A,A,expected);

		T _found = create(2,2);

		T _A = convertA(A);
		alg.innerMatrixProduct(_A,_found);

		DMatrixRMaj found = convertB(_found);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found, UtilEjml.TEST_F64));
	}

	@Test
	public void innerProduct() {
		DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,1,-1,1,rand);
		DMatrixRMaj B = RandomMatrices_DDRM.rectangle(4,4,-1,1,rand);

		DMatrixRMaj tmp = new DMatrixRMaj(1,4);
		CommonOps_DDRM.multTransA(A,B,tmp);
		double expected = CommonOps_DDRM.dot(A,tmp);

		double found = alg.innerProductVectorMatrix(A, convertA(B));

		assertEquals(expected, found, UtilEjml.TEST_F64);
	}

	@Test
	public void extractDiag() {
		DMatrixRMaj A = RandomMatrices_DDRM.rectangle(4,4,-1,1,rand);
		double diag[] = new double[4];
		alg.extractDiag(convertA(A),diag);

		for (int i = 0; i < 4; i++) {
			assertEquals(A.get(i,i),diag[i], UtilEjml.TEST_F64);
		}
	}

	@Test
	public void divideRows() {
		DMatrixRMaj original = RandomMatrices_DDRM.rectangle(4,4,-1,1,rand);
		double array[] = new double[]{0.5,0.75,2,0.9};

		T _A = convertA(original);
		alg.divideRows(array,_A);
		DMatrixRMaj A = convertB(_A);

		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				assertEquals(original.get(row,col)/array[row],A.get(row,col), UtilEjml.TEST_F64);
			}
		}
	}

	@Test
	public void divideColumns() {
		DMatrixRMaj original = RandomMatrices_DDRM.rectangle(4,4,-1,1,rand);
		double array[] = new double[]{0.5,0.75,2,0.9};

		T _A = convertA(original);
		alg.divideColumns(array,_A);
		DMatrixRMaj A = convertB(_A);

		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				assertEquals(original.get(row,col)/array[col],A.get(row,col), UtilEjml.TEST_F64);
			}
		}
	}

	@Test
	public void scaleRows() {
		DMatrixRMaj original = RandomMatrices_DDRM.rectangle(4,4,-1,1,rand);
		double array[] = new double[]{0.5,0.75,2,0.9};

		T _A = convertA(original);
		alg.scaleRows(array,_A);
		DMatrixRMaj A = convertB(_A);

		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				assertEquals(original.get(row,col)*array[row],A.get(row,col), UtilEjml.TEST_F64);
			}
		}
	}

	@Test
	public void scaleColumns() {
		DMatrixRMaj original = RandomMatrices_DDRM.rectangle(4,4,-1,1,rand);
		double array[] = new double[]{0.5,0.75,2,0.9};

		T _A = convertA(original);
		alg.scaleColumns(array,_A);
		DMatrixRMaj A = convertB(_A);

		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				assertEquals(original.get(row,col)*array[col],A.get(row,col), UtilEjml.TEST_F64);
			}
		}
	}

	@Test
	public void multTransA() {
		DMatrixRMaj A = RandomMatrices_DDRM.rectangle(6,2,-1,1,rand);
		DMatrixRMaj B = RandomMatrices_DDRM.rectangle(6,4,-1,1,rand);
		DMatrixRMaj expected = new DMatrixRMaj(1,1);
		DMatrixRMaj found = new DMatrixRMaj(1,1);

		CommonOps_DDRM.multTransA(A,B,expected);

		alg.multTransA(convertA(A),B,found);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected,found,UtilEjml.TEST_F64));
	}
}
