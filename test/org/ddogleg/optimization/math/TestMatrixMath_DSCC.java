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

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.ops.DConvertMatrixStruct;

/**
 * @author Peter Abeles
 */
public class TestMatrixMath_DSCC extends StandardMatrixMathChecks<DMatrixSparseCSC> {
	public TestMatrixMath_DSCC() {
		super(new MatrixMath_DSCC());
	}

	@Override
	public DMatrixSparseCSC convertA(DMatrixRMaj A) {
		DMatrixSparseCSC out = new DMatrixSparseCSC(A.numRows,A.numCols,1);
		DConvertMatrixStruct.convert(A,out);
		return out;
	}

	@Override
	public DMatrixRMaj convertB(DMatrixSparseCSC A) {
		DMatrixRMaj out = new DMatrixRMaj(A.numRows,A.numCols);
		DConvertMatrixStruct.convert(A,out);
		return out;
	}

	@Override
	public DMatrixSparseCSC create(int numRows, int numCols) {
		return new DMatrixSparseCSC(numRows,numCols,1);
	}
}