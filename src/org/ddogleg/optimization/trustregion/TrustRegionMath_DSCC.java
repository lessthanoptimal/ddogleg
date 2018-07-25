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

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;

/**
 * Implementation of {@link TrustRegionBase_F64.ParameterUpdate} for {@link DMatrixSparseCSC}
 *
 * @author Peter Abeles
 */
public class TrustRegionMath_DSCC implements TrustRegionBase_F64.MatrixMath<DMatrixSparseCSC> {

	protected DMatrixRMaj tmpM0 = new DMatrixRMaj(1,1);

	IGrowArray gw = new IGrowArray();
	DGrowArray gx = new DGrowArray();

	@Override
	public void setIdentity(DMatrixSparseCSC matrix) {
		CommonOps_DSCC.setIdentity(matrix);
	}

	@Override
	public void innerMatrixProduct(DMatrixSparseCSC A, DMatrixSparseCSC output) {
		CommonOps_DSCC.multTransA(A,A,output,gw,gx);
	}

	@Override
	public void extractDiag(DMatrixSparseCSC A, double[] diag) {

	}

	@Override
	public void divideRows(double[] scaling, DMatrixSparseCSC A) {

	}

	@Override
	public void divideColumns(double[] scaling, DMatrixSparseCSC A) {

	}

	@Override
	public void scaleRows(double[] scaling, DMatrixSparseCSC A) {

	}

	@Override
	public void scaleColumns(double[] scaling, DMatrixSparseCSC A) {

	}

	@Override
	public DMatrixSparseCSC createMatrix() {
		return new DMatrixSparseCSC(1,1);
	}

	@Override
	public double innerProduct(DMatrixRMaj v, DMatrixSparseCSC M) {
		tmpM0.reshape(v.numRows,v.numCols);
		CommonOps_DSCC.mult(M,v,tmpM0);
		return CommonOps_DDRM.dot(v,tmpM0);
	}
}
