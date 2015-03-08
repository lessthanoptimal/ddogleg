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

package org.ddogleg.clustering.gmm;

import org.ejml.alg.dense.mult.VectorVectorMult;
import org.ejml.data.DenseMatrix64F;
import org.ejml.interfaces.decomposition.CholeskyDecomposition;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps;

/**
 * A Gaussian in a Gaussian Mixture Model.  Contains a mean, covariance, and weight.
 *
 * @author Peter Abeles
 */
public class GaussianGmm_F64 {
	// These specify the parameters of the Gaussian in the mixture
	public DenseMatrix64F mean;
	public DenseMatrix64F covariance;
	public double weight;

	// used to precompute parts of the likelihood function
	public DenseMatrix64F invCov;
	public double chisq; // chi-sq (x-mu)'*inv(Sigma)*(x-mu)
	public double leftSide; // precomputed left side of likelihood

	public GaussianGmm_F64( int DOF ) {
		mean = new DenseMatrix64F(DOF,1);
		covariance = new DenseMatrix64F(DOF,DOF);
		invCov = new DenseMatrix64F(DOF,DOF);
	}

	public void zero() {
		CommonOps.fill(mean,0);
		CommonOps.fill(covariance,0);
		weight = 0;
	}

	public void addMean( double[] point , double responsibility ) {
		for (int i = 0; i < mean.numRows; i++) {
			mean.data[i] += responsibility*point[i];
		}
		weight += responsibility;
	}

	public void addCovariance( double[] difference , double responsibility ) {
		int N = mean.numRows;
		for (int i = 0; i < N; i++) {
			for (int j = i; j < N; j++) {
				covariance.data[i*N+j] = covariance.data[j*N+i] = responsibility*difference[i]*difference[j];
			}
		}
	}

	public void setMean( double[] point ) {
		System.arraycopy(point,0,mean.data,0,mean.numRows);
	}

	/**
	 * Precomputes everything in the likelihood calculation which can be to make it run faster.
	 *
	 * @param solver Solver used to invert the matrix.  Must not modify the passed in covariance
	 */
	public boolean preprocessLikelihood( LinearSolver<DenseMatrix64F> solver ) {
		if( !solver.setA(covariance) )
			return false;
		solver.invert(invCov);

		CholeskyDecomposition<DenseMatrix64F> decomposition = solver.getDecomposition();
		double det = decomposition.computeDeterminant().real;

		// (2*PI)^(D/2) has been omitted since it's the same for all the Gaussians and will get normalized out
		leftSide = 1.0/Math.sqrt(det);

		return true;
	}

	public double getChiSq() {
		return chisq;
	}

	/**
	 * Computes p(x|mu,Sigma) where x is the point.
	 * @param point The point being examined
	 * @param workSpace row vector with a length of DOF
	 * @return likelihood of the point
	 */
	public double likelihood( double[] point , DenseMatrix64F workSpace) {
		int N = mean.numRows;
		// x - mu
		for (int i = 0; i < N; i++) {
			workSpace.data[i] = point[i]-mean.data[i];
		}
		chisq = VectorVectorMult.innerProdA(workSpace,invCov,workSpace);

		return leftSide*Math.exp(-0.5*chisq);
	}
}
