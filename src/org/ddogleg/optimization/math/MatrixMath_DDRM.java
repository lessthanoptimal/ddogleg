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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

/**
 * @author Peter Abeles
 */
public class MatrixMath_DDRM implements MatrixMath<DMatrixRMaj> {
	@Override
	public void divideColumns(DMatrixRMaj divisor, DMatrixRMaj A) {
		CommonOps_DDRM.divideCols(A,divisor.data);
	}

	@Override
	public void multTransA(DMatrixRMaj A, DMatrixRMaj B, DMatrixRMaj output) {
		CommonOps_DDRM.multTransA(A,B,output);
	}

	@Override
	public DMatrixRMaj createMatrix() {
		return new DMatrixRMaj(1,1);
	}
}
