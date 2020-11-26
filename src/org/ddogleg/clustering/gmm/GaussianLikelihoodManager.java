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

package org.ddogleg.clustering.gmm;

import org.ddogleg.struct.DogArray;
import org.ejml.LinearSolverSafe;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;
import org.ejml.interfaces.decomposition.CholeskyDecomposition_F64;
import org.ejml.interfaces.linsol.LinearSolverDense;

import java.util.List;

/**
 * Computes the likelihood of a Gaussian distribution.  Parts of the equation are precomputed to seed up the process.
 *
 * @author Peter Abeles
 */
public class GaussianLikelihoodManager {

	// Set of Gaussians which describe the mixture
	List<GaussianGmm_F64> mixtures;
	// Storage for precomputed likelihood functions
	DogArray<Likelihood> precomputes;

	// used to compute likelihood
	LinearSolverDense<DMatrixRMaj> solver;

	// Used internally when computing difference between point and mean
	DMatrixRMaj diff;

	public GaussianLikelihoodManager( final int pointDimension , List<GaussianGmm_F64> mixtures ) {
		this.mixtures = mixtures;

		// this will produce a cholesky decomposition
		solver = LinearSolverFactory_DDRM.symmPosDef(pointDimension);
		solver = new LinearSolverSafe<>(solver);

		precomputes = new DogArray<>(()->new Likelihood(pointDimension));

		diff = new DMatrixRMaj(pointDimension,1);
	}

	/**
	 * Precomputes likelihood for all the mixtures
	 */
	public void precomputeAll() {
		precomputes.resize(mixtures.size());
		for (int i = 0; i < precomputes.size; i++) {
			precomputes.get(i).setGaussian(mixtures.get(i));
		}
	}

	/**
	 * Returns a precomputed likelihood function
	 */
	public Likelihood getLikelihood( int which ) {
		return precomputes.get(which);
	}

	/**
	 * Likelihood for a specific Gaussian
	 */
	@SuppressWarnings("NullAway.Init")
	public class Likelihood {

		public GaussianGmm_F64 gaussian;

		// used to precompute parts of the likelihood function
		public DMatrixRMaj invCov;
		public double leftSide; // precomputed left side of likelihood

		public double chisq; // chi-sq (x-mu)'*inv(Sigma)*(x-mu)

		public boolean valid = false; // is there a valid distribution?  e.g. more than 1 point matched to it

		public Likelihood(int N) {
			invCov = new DMatrixRMaj(N,N);
		}

		/**
		 * Precomputes the parts of the likelihood functions which can be.  Matrix inversion and determinant are the
		 * main parts.
		 */
		public void setGaussian(GaussianGmm_F64 gaussian) {
			this.gaussian = gaussian;

			if (!solver.setA(gaussian.covariance)) {
				valid = false;
				return;
			} else {
				valid = true;
			}
			solver.invert(invCov);

			CholeskyDecomposition_F64<DMatrixRMaj> decomposition = solver.getDecomposition();
			double det = decomposition.computeDeterminant().real;

			// (2*PI)^(D/2) has been omitted since it's the same for all the Gaussians and will get normalized out
			leftSide = 1.0 / Math.sqrt(det);
		}

		/**
		 * Computes p(x|mu,Sigma) where x is the point.  THe chi-square value is also computed.
		 *
		 * @param point     The point being examined
		 * @return likelihood of the point
		 */
		public double likelihood(double[] point) {
			if( !valid )
				return 0;

			int N = gaussian.mean.numRows;
			// x - mu
			for (int i = 0; i < N; i++) {
				diff.data[i] = point[i] - gaussian.mean.data[i];
			}
			chisq = VectorVectorMult_DDRM.innerProdA(diff, invCov, diff);

			return leftSide * Math.exp(-0.5 * chisq);
		}

		public double getChisq() {
			return chisq;
		}
	}
}
