/*
 * Copyright (c) 2012-2017, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.rand;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.interfaces.linsol.LinearSolverDense;

import java.util.Random;

import static org.ejml.dense.row.CommonOps_DDRM.multAdd;

/**
 * Draw a number from a multivariate Gaussian distribution.
 */
public class MultivariateGaussianDraw {
	private LinearSolverDense<DMatrixRMaj> solver;
	private DMatrixRMaj mean;
	private DMatrixRMaj A;
	private Random rand;
	private DMatrixRMaj r;
	private DMatrixRMaj Q_inv;

	double likelihoodLeft;

	/**
	 * Creates a random distribution with the specified mean and covariance.  The references
	 * to the variables are not saved, their value are copied.
	 *
	 * @param rand Used to create the random numbers for the draw.
	 * @param mean If not null this will be the mean of the distribution, if null then nothing is assigned.
	 * this is useful if someone is being anal about performance and will soon call assignMean()
	 * @param cov The covariance of the distribution
	 */
	public MultivariateGaussianDraw( Random rand , DMatrixRMaj mean , DMatrixRMaj cov )
	{
		if( mean != null )
			this.mean = new DMatrixRMaj(mean);
		else
			this.mean = new DMatrixRMaj(cov.numCols,1);
		r = new DMatrixRMaj(cov.numRows,1);
		Q_inv = new DMatrixRMaj(cov.numRows,cov.numCols);

		solver = LinearSolverFactory_DDRM.chol(cov.numRows);

		// will invoke decompose in cholesky
		solver.setA(cov);
		CholeskyDecomposition<DMatrixRMaj> chol = solver.getDecomposition();

		A = chol.getT(null);


		solver.invert(Q_inv);

		likelihoodLeft = Math.pow(Math.PI*2,-this.mean.numRows/2.0)*Math.sqrt(CommonOps_DDRM.det(cov));

		this.rand = rand;
	}

	/**
	 * Uses the referenced variable as the internal mean.  This does not perform a copy but
	 * actually points to the specified matrix as the mean.
	 */
	public void assignMean( DMatrixRMaj mean ) {
		this.mean = mean;
	}

	/**
	 * Makes a draw on the distribution and stores the results in parameter 'x'
	 */
	public DMatrixRMaj next( DMatrixRMaj x )
	{
		for( int i = 0; i < r.numRows; i++ ) {
			r.set(i,0,rand.nextGaussian());
		}
		x.set(mean);

		multAdd(A,r,x);

		return x;
	}

	public double computeLikelihoodP() {
		double inner = VectorVectorMult_DDRM.innerProdA(r,Q_inv,r);

		return likelihoodLeft*Math.exp(-0.5*inner);
	}
}
