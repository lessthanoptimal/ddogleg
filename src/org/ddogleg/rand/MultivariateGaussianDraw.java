/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

import org.ejml.alg.dense.mult.VectorVectorMult;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps;

import java.util.Random;

import static org.ejml.ops.CommonOps.multAdd;

/**
 * Draw a number from a multivariate Gaussian distribution.
 */
public class MultivariateGaussianDraw {
	private LinearSolver<DenseMatrix64F> solver;
	private DenseMatrix64F mean;
	private DenseMatrix64F A;
	private Random rand;
	private DenseMatrix64F r;
	private DenseMatrix64F Q_inv;

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
	public MultivariateGaussianDraw( Random rand , DenseMatrix64F mean , DenseMatrix64F cov )
	{
		if( mean != null )
			this.mean = new DenseMatrix64F(mean);
		else
			this.mean = new DenseMatrix64F(cov.numCols,1);
		r = new DenseMatrix64F(cov.numRows,1);
		Q_inv = new DenseMatrix64F(cov.numRows,cov.numCols);

		solver = LinearSolverFactory.chol(cov.numRows);

		// will invoke decompose in cholesky
		solver.setA(cov);
		CholeskyDecomposition<DenseMatrix64F> chol = solver.getDecomposition();

		A = chol.getT(null);


		solver.invert(Q_inv);

		likelihoodLeft = Math.pow(Math.PI*2,-this.mean.numRows/2.0)*Math.sqrt(CommonOps.det(cov));

		this.rand = rand;
	}

	/**
	 * Uses the referenced variable as the internal mean.  This does not perform a copy but
	 * actually points to the specified matrix as the mean.
	 */
	public void assignMean( DenseMatrix64F mean ) {
		this.mean = mean;
	}

	/**
	 * Makes a draw on the distribution and stores the results in parameter 'x'
	 */
	public DenseMatrix64F next( DenseMatrix64F x )
	{
		for( int i = 0; i < r.numRows; i++ ) {
			r.set(i,0,rand.nextGaussian());
		}
		x.set(mean);

		multAdd(A,r,x);

		return x;
	}

	public double computeLikelihoodP() {
		double inner = VectorVectorMult.innerProdA(r,Q_inv,r);

		return likelihoodLeft*Math.exp(-0.5*inner);
	}
}
