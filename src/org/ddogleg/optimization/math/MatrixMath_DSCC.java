/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

/**
 * @author Peter Abeles
 */
public class MatrixMath_DSCC implements MatrixMath<DMatrixSparseCSC> {
	DGrowArray workArray = new DGrowArray();

	@Override
	public void divideColumns(DMatrixRMaj divisor, DMatrixSparseCSC A) {
		CommonOps_DSCC.divideColumns(A,divisor.data,0);
	}

	@Override
	public void multTransA(DMatrixSparseCSC A, DMatrixRMaj B, DMatrixRMaj output) {
		CommonOps_DSCC.multTransA(A,B,output,workArray);
	}

	@Override
	public DMatrixSparseCSC createMatrix() {
		return new DMatrixSparseCSC(1,1);
	}
}
