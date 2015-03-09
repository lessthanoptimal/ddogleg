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

import org.ddogleg.clustering.AssignCluster;
import org.ddogleg.clustering.ComputeClusters;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_F64;
import org.ejml.ops.CommonOps;

import java.util.List;

/**
 * Standard expectation maximization based approach to fitting mixture-of-Gaussian models to a set of data.
 * A locally optimal maximum likelihood estimate is found.  The full covariance is found.  Some other
 * variants will estimate just diagonal elements or a single covariance, but that isn't yet supported.
 *
 * @author Peter Abeles
 */
// TODO Unconstrained covariance
// TODO just diagonal covariance
	// TODO added shared and tied covariance?
public class ExpectationMaximizationGmm_F64 implements ComputeClusters<double[]>  {

	 // Used to select initial parameters
	InitializeGmm_F64 selectInitial;

	// storage for mixture models
	FastQueue<GaussianGmm_F64> mixture;

	// info for each points
	FastQueue<PointInfo> info = new FastQueue<PointInfo>(PointInfo.class,true);

	// Maximum number of iterations\
	int maxIterations;

	// If the fractional change in score is less or equal to this value then it has converged.
	// ||prev-curr||/prev
	double threshScoreChange;

	// Used to compute the likelihood for each Gaussian
	GaussianLikelihoodManager likelihoodManager;

	// internal work space for computing the difference between the mean and point
	double dx[] = new double[1];

	// compute chi-square error
	double errorChiSquare;

	/**
	 * Configures EM parameters
	 *
	 * @param maxIterations Maximum number of iterations
	 * @param threshScoreChange If the relative change in score is less or equal than this amount it has converged
	 * @param selectInitial Used to select initial seeds for the clusters
	 */
	public ExpectationMaximizationGmm_F64(int maxIterations,
										  double threshScoreChange,
										  InitializeGmm_F64 selectInitial) {
		this.maxIterations = maxIterations;
		this.threshScoreChange = threshScoreChange;
		this.selectInitial = selectInitial;
	}

	@Override
	public void init(final int pointDimension, long randomSeed) {
		mixture = new FastQueue<GaussianGmm_F64>(GaussianGmm_F64.class,true ) {
			@Override
			protected GaussianGmm_F64 createInstance() {
				return new GaussianGmm_F64(pointDimension);
			}
		};
		selectInitial.init(pointDimension,randomSeed);

		if( dx.length < pointDimension )
			dx = new double[pointDimension];
		likelihoodManager = new GaussianLikelihoodManager(pointDimension,mixture.toList());
	}

	@Override
	public void process(List<double[]> points, int numCluster) {
		// setup data structures
		mixture.resize(numCluster);
		for (int i = 0; i < points.size(); i++) {
			PointInfo p = info.grow();
			p.point = points.get(i);
			p.weights.resize(numCluster);
		}

		// Select initial distributions
		selectInitial.selectSeeds(points,mixture.toList());
		likelihoodManager.precomputeAll();

		// perform EM iteration
		double errorBefore = Double.MAX_VALUE;
		for (int iteration = 0; iteration < maxIterations; iteration++) {
			// compute the expectation for each point and compute the chi-square error
			errorChiSquare = expectation();

			// check for convergence
			if( 1.0 - errorChiSquare/errorBefore <= threshScoreChange ) {
				break;
			}
			errorBefore = errorChiSquare;

			maximization();
			if( !likelihoodManager.precomputeAll() ) {
				// if this fails something seriously went wrong
				throw new RuntimeException("EM GMM - precompute likelihood failed!");
			}
		}

		// clean up
		for (int i = 0; i < info.size; i++) {
			info.data[i].point = null; // de-reference so the memory could be freed
		}
		info.reset();
	}

	/**
	 * For each point compute the "responsibility" for each Gaussian
	 *
	 * @return The sum of chi-square.  Can be used to estimate the total error.
	 */
	protected double expectation() {
		double sumChiSq = 0;

		for (int i = 0; i < info.size(); i++) {
			PointInfo p = info.get(i);

			double total = 0;
			for (int j = 0; j < mixture.size; j++) {
				GaussianLikelihoodManager.Likelihood g = likelihoodManager.getLikelihood(j);
				total += p.weights.data[j] = g.likelihood(p.point);
				sumChiSq += g.getChisq();
			}

			// make sure it sums up to 1
			for (int j = 0; j < mixture.size; j++) {
				p.weights.data[j] /= total;
			}
		}

		return sumChiSq;
	}

	/**
	 * Using points responsibility information to recompute the Gaussians and their weights, maximizing
	 * the likelihood of the mixture.
	 */
	protected void maximization() {
		// discard previous parameters by zeroing
		for (int i = 0; i < mixture.size; i++) {
			mixture.get(i).zero();
		}

		// compute the new mean
		for (int i = 0; i < info.size; i++) {
			PointInfo p = info.get(i);

			for (int j = 0; j < mixture.size; j++) {
				mixture.get(j).addMean(p.point,p.weights.get(j));
			}
		}
		for (int i = 0; i < mixture.size; i++) {
			GaussianGmm_F64 g = mixture.get(i);
			CommonOps.divide(g.mean,g.weight);
		}

		// compute new covariance
		for (int i = 0; i < info.size; i++) {
			PointInfo pp = info.get(i);
			double[] p = pp.point;


			for (int j = 0; j < mixture.size; j++) {
				GaussianGmm_F64 g = mixture.get(j);

				for (int k = 0; k < p.length; k++) {
					dx[k] = p[k]-g.mean.data[k];
				}

				mixture.get(j).addCovariance(dx, pp.weights.get(j));
			}
		}
		double totalMixtureWeight = 0;
		for (int i = 0; i < mixture.size; i++) {
			GaussianGmm_F64 g = mixture.get(i);
			CommonOps.divide(g.covariance,g.weight);
			totalMixtureWeight += g.weight;
		}

		// update the weight
		for (int i = 0; i < mixture.size; i++) {
			mixture.get(i).weight /= totalMixtureWeight;
		}
	}

	@Override
	public AssignCluster<double[]> getAssignment() {
		return new AssignGmm_F64(mixture.toList());
	}

	@Override
	public double getDistanceMeasure() {
		return errorChiSquare;
	}

	public static class PointInfo
	{
		public double[] point; // reference to the original input point
		public GrowQueue_F64 weights = new GrowQueue_F64();
	}
}
