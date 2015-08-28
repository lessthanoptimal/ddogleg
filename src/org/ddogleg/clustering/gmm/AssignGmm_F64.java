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

import java.util.ArrayList;
import java.util.List;

/**
 * Given a mixture model it will compute the hard and soft assignment of a point to Gaussians in the cluster.
 *
 * @author Peter Abeles
 */
public class AssignGmm_F64 implements AssignCluster<double[]> {

	protected List<GaussianGmm_F64> mixture;
	volatile GaussianLikelihoodManager glm;

	/**
	 * Use reference to provided mixtures
	 */
	public AssignGmm_F64(List<GaussianGmm_F64> mixture) {
		this.mixture = mixture;
		int N = mixture.get(0).mean.getNumElements();
		glm = new GaussianLikelihoodManager(N,mixture);
		glm.precomputeAll();
	}

	/**
	 * Copy constructor
	 */
	public AssignGmm_F64( AssignGmm_F64 original ) {
		mixture = new ArrayList<GaussianGmm_F64>();

		for (int i = 0; i < original.mixture.size(); i++) {
			GaussianGmm_F64 o = original.mixture.get(i);
			mixture.add(o.copy());
		}

		int N = mixture.get(0).mean.getNumElements();
		glm = new GaussianLikelihoodManager(N,mixture);
		glm.precomputeAll();
	}

	@Override
	public int assign(double[] point) {
		int indexBest = -1;
		double scoreBest = 0;

		for (int i = 0; i < mixture.size(); i++) {
			double score = glm.getLikelihood(i).likelihood(point);
			if( score > scoreBest ) {
				scoreBest = score;
				indexBest = i;
			}
		}

		return indexBest;
	}

	@Override
	public void assign(double[] point, double[] fit) {
		double total = 0;
		for (int i = 0; i < mixture.size(); i++) {
			total += fit[i] = glm.getLikelihood(i).likelihood(point);
		}

		for (int i = 0; i < mixture.size(); i++) {
			fit[i] /= total;
		}
	}

	@Override
	public int getNumberOfClusters() {
		return mixture.size();
	}

	@Override
	public AssignCluster<double[]> copy() {
		return new AssignGmm_F64(this);
	}

	public List<GaussianGmm_F64> getMixture() {
		return mixture;
	}

	public void setMixture(List<GaussianGmm_F64> mixture) {
		this.mixture = mixture;
	}
}
