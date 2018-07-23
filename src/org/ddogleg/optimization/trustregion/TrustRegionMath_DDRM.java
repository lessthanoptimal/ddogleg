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
	public DMatrixRMaj createMatrix() {
		return new DMatrixRMaj(1,1);
	}

	@Override
	public double innerProduct(DMatrixRMaj v, DMatrixRMaj M) {
		tmpM0.reshape(v.numRows,v.numCols);
		CommonOps_DDRM.multTransA(v,M,tmpM0);
		return CommonOps_DDRM.dot(tmpM0,v);
	}
}
