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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.sparse.csc.CommonOps_DSCC;

/**
 * Provides a way to convert a regular Jacobian matrix into a SchurJacobian
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public abstract class SchurJacobian_to_NtoMxN<T extends DMatrix> implements FunctionNtoMxN<T> {

	SchurJacobian<T> function;

	T left,right;

	protected SchurJacobian_to_NtoMxN(SchurJacobian<T> function) {
		this.function = function;
	}

	@Override
	public int getNumOfInputsN() {
		return function.getNumOfInputsN();
	}

	@Override
	public int getNumOfOutputsM() {
		return function.getNumOfOutputsM();
	}

	public static class DDRM extends SchurJacobian_to_NtoMxN<DMatrixRMaj> {

		public DDRM(SchurJacobian<DMatrixRMaj> function) {
			super(function);
			left = new DMatrixRMaj(1,1);
			right = new DMatrixRMaj(1,1);
		}

		@Override
		public void process(double[] input, DMatrixRMaj output) {
			function.process(input,left,right);

			CommonOps_DDRM.concatColumns(left,right,output);
		}

		@Override
		public DMatrixRMaj declareMatrixMxN() {
			return new DMatrixRMaj(getNumOfOutputsM(),getNumOfInputsN());
		}
	}

	public static class DSCC extends SchurJacobian_to_NtoMxN<DMatrixSparseCSC> {

		public DSCC(SchurJacobian<DMatrixSparseCSC> function) {
			super(function);
			left = new DMatrixSparseCSC(1,1);
			right = new DMatrixSparseCSC(1,1);
		}

		@Override
		public void process(double[] input, DMatrixSparseCSC output) {
			function.process(input,left,right);

			CommonOps_DSCC.concatColumns(left,right,output);
		}

		@Override
		public DMatrixSparseCSC declareMatrixMxN() {
			return new DMatrixSparseCSC(getNumOfOutputsM(),getNumOfInputsN());
		}
	}
}
