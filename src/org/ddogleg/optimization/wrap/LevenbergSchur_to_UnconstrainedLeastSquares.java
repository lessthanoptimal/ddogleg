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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.impl.LevenbergMarquardtSchur_DSCC;
import org.ddogleg.optimization.impl.NumericalJacobianForward_DSCC;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

/**
 * Wrapper around {@link org.ddogleg.optimization.impl.LevenbergMarquardtSchur_DSCC} for {@link UnconstrainedLeastSquares}
 *
 * @author Peter Abeles
 */
public class LevenbergSchur_to_UnconstrainedLeastSquares
		implements UnconstrainedLeastSquares<DMatrixSparseCSC> {

	private LevenbergMarquardtSchur_DSCC alg;
	private int split;

	public LevenbergSchur_to_UnconstrainedLeastSquares(LevenbergMarquardtSchur_DSCC alg, int split ) {
		this.alg = alg;
		this.split = split;
	}

	@Override
	public void setFunction(FunctionNtoM function, FunctionNtoMxN<DMatrixSparseCSC> _jacobian) {

		if( _jacobian == null )
			_jacobian = new NumericalJacobianForward_DSCC(function);

		FunctionNtoMxN<DMatrixSparseCSC> j = _jacobian;

		SchurJacobian<DMatrixSparseCSC> jacobian = new SchurJacobian<DMatrixSparseCSC>() {
			DMatrixSparseCSC J = new DMatrixSparseCSC(1,1);
			@Override
			public int getNumOfInputsN() {
				return j.getNumOfInputsN();
			}

			@Override
			public int getNumOfOutputsM() {
				return j.getNumOfOutputsM();
			}

			@Override
			public void process(double[] input, DMatrixSparseCSC left, DMatrixSparseCSC right) {
				J.reshape(getNumOfOutputsM(),getNumOfInputsN());
				j.process(input,J);

				left.reshape(J.numRows,split);
				right.reshape(J.numRows,J.numCols-split);

				CommonOps_DSCC.extract(J, 0, J.numRows, 0, split, left, 0, 0);
				CommonOps_DSCC.extract(J, 0, J.numRows, split, J.numCols, right, 0, 0);

			}
		};

		alg.setFunction(function,jacobian);
	}

	@Override
	public void initialize(double[] initial, double ftol , double gtol) {
		alg.setConvergence(ftol,gtol);
		alg.initialize(initial);
	}

	@Override
	public double[] getParameters() {
		return alg.getParameters();
	}

	@Override
	public boolean iterate() throws OptimizationException {
		return alg.iterate();
	}

	@Override
	public boolean isConverged() {
		return alg.isConverged();
	}

	@Override
	public String getWarning() {
		return alg.getMessage();
	}

	@Override
	public void setVerbose(boolean verbose) {

	}

	@Override
	public double getFunctionValue() {
		return alg.getFnorm();
	}

	@Override
	public boolean isUpdated() {
		return alg.isUpdatedParameters();
	}
}
