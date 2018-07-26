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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;

/**
 * Implementation of {@link TrustRegionBase_F64.ParameterUpdate} for {@link DMatrixRMaj}
 *
 * @author Peter Abeles
 */
public class TrustRegionMath_DDRM implements TrustRegionBase_F64.MatrixMath<DMatrixRMaj> {

	protected DMatrixRMaj tmpM0 = new DMatrixRMaj(1,1);

	@Override
	public void setIdentity(DMatrixRMaj matrix) {
		CommonOps_DDRM.setIdentity(matrix);
	}

	@Override
	public void innerMatrixProduct(DMatrixRMaj A, DMatrixRMaj output) {
		CommonOps_DDRM.multTransA(A,A,output);
	}

	@Override
	public void extractDiag(DMatrixRMaj A, double[] diag) {
		for (int i = 0; i < A.numCols; i++) {
			diag[i] = A.data[i*A.numCols+i];
		}
	}

	@Override
	public void divideRows(double[] scaling, DMatrixRMaj A) {
		int index = 0;
		for (int row = 0; row < A.numRows; row++) {
			double v = scaling[row];
			for (int col = 0; col < A.numCols; col++) {
				A.data[index++] /= v;
			}
		}
	}

	@Override
	public void divideColumns(double[] scaling, DMatrixRMaj A) {
		int index = 0;
		for (int row = 0; row < A.numRows; row++) {
			for (int col = 0; col < A.numCols; col++) {
				A.data[index++] /= scaling[col];
			}
		}
	}

	@Override
	public void scaleRows(double[] scaling, DMatrixRMaj A) {
		int index = 0;
		for (int row = 0; row < A.numRows; row++) {
			double v = scaling[row];
			for (int col = 0; col < A.numCols; col++) {
				A.data[index++] *= v;
			}
		}
	}

	@Override
	public void scaleColumns(double[] scaling, DMatrixRMaj A) {
		int index = 0;
		for (int row = 0; row < A.numRows; row++) {
			for (int col = 0; col < A.numCols; col++) {
				A.data[index++] *= scaling[col];
			}
		}
	}

	@Override
	public DMatrixRMaj createMatrix() {
		return new DMatrixRMaj(1,1);
	}

	@Override
	public double innerProduct(DMatrixRMaj v, DMatrixRMaj M) {
		return VectorVectorMult_DDRM.innerProdA(v, M, v);
	}
}
