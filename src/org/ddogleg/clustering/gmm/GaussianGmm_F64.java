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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.io.Serializable;

/**
 * A Gaussian in a Gaussian Mixture Model.  Contains a mean, covariance, and weight.  Additional functions
 * are provided to help compute the Gaussian's parameters.
 *
 * @author Peter Abeles
 */
public class GaussianGmm_F64 implements Serializable {
	// These specify the parameters of the Gaussian in the mixture
	public DenseMatrix64F mean;
	public DenseMatrix64F covariance;
	public double weight;

	/**
	 * Declares internal data strucures
	 * @param DOF Number of degrees-of-freedom in the sampled points.
	 */
	public GaussianGmm_F64( int DOF ) {
		mean = new DenseMatrix64F(DOF,1);
		covariance = new DenseMatrix64F(DOF,DOF);
	}

	public GaussianGmm_F64() {
	}

	/**
	 * Sets the mean, covariance, and weight to zero
	 */
	public void zero() {
		CommonOps.fill(mean,0);
		CommonOps.fill(covariance,0);
		weight = 0;
	}

	/**
	 * Helper function for computing Gaussian parameters.  Adds the point to mean and weight.
	 */
	public void addMean( double[] point , double responsibility ) {
		for (int i = 0; i < mean.numRows; i++) {
			mean.data[i] += responsibility*point[i];
		}
		weight += responsibility;
	}

	/**
	 * Helper function for computing Gaussian parameters.  Adds the difference between point and mean to covariance,
	 * adjusted by the weight.
	 */
	public void addCovariance( double[] difference , double responsibility ) {
		int N = mean.numRows;
		for (int i = 0; i < N; i++) {
			for (int j = i; j < N; j++) {
				covariance.data[i*N+j] += responsibility*difference[i]*difference[j];
			}
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < i; j++) {
				covariance.data[i*N+j] = covariance.data[j*N+i];
			}
		}
	}

	/**
	 * Sets the mean to be the same as the provided point\
	 */
	public void setMean( double[] point ) {
		System.arraycopy(point,0,mean.data,0,mean.numRows);
	}

	public GaussianGmm_F64 copy() {
		GaussianGmm_F64 out = new GaussianGmm_F64(mean.getNumElements());

		out.mean.set(mean);
		out.covariance.set(covariance);
		out.weight = weight;

		return out;

	}

	public DenseMatrix64F getMean() {
		return mean;
	}

	public void setMean(DenseMatrix64F mean) {
		this.mean = mean;
	}

	public DenseMatrix64F getCovariance() {
		return covariance;
	}

	public void setCovariance(DenseMatrix64F covariance) {
		this.covariance = covariance;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
