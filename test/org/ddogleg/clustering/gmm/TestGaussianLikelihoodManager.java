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

package org.ddogleg.clustering.gmm;

import org.ddogleg.struct.FastQueue;
import org.ejml.data.DMatrixRMaj;
import org.ejml.equation.Equation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestGaussianLikelihoodManager {

	@Test
	public void likelihood() {

		int DOF = 3;

		GaussianGmm_F64 a = new GaussianGmm_F64(DOF);
		GaussianGmm_F64 b = new GaussianGmm_F64(DOF);

		a.mean.data = new double[]{5,3,5};
		b.mean.data = new double[]{-5,6,-1.5};

		a.covariance.set(0,0,3);
		a.covariance.set(1,1,6);
		a.covariance.set(2,2,12);

		b.covariance.set(0,0,20);
		b.covariance.set(1,1,30);
		b.covariance.set(2,2,25);


		FastQueue<GaussianGmm_F64> mixtures = new FastQueue<GaussianGmm_F64>(GaussianGmm_F64.class,false);
		mixtures.add(a);
		mixtures.add(b);

		GaussianLikelihoodManager manager = new GaussianLikelihoodManager(DOF,mixtures.toList());

		manager.precomputeAll();

		double p[] = new double[]{4,3,-1};

		double foundA = manager.getLikelihood(0).likelihood(p);
		double chiSqA = manager.getLikelihood(0).getChisq();
		double foundB = manager.getLikelihood(1).likelihood(p);
		double chiSqB = manager.getLikelihood(1).getChisq();

		// make sure it isn't modifying the inputs
		assertEquals(5,a.mean.get(0,0),1e-8);
		assertEquals(3,a.covariance.get(0, 0),1e-8);

		double expectedA = computeLikelihood(a, p);
		double expectedB = computeLikelihood(b, p);

		// check chi-sq first
		assertEquals(computeChiSq(a,p),chiSqA,1e-8);
		assertEquals(computeChiSq(b,p),chiSqB,1e-8);


		// look at the ratio of the two
		// the found will be off by a scale factor from the actual likelihood
		double found = foundA/foundB;
		double expected = expectedA/expectedB;

		assertEquals(found, expected, 1e-8);
	}

	public static double computeLikelihood( GaussianGmm_F64 g , double[] p ) {
		Equation eq = new Equation();

		eq.alias(g.mean,"mu",g.covariance,"S",p.length,"D");
		eq.alias(DMatrixRMaj.wrap(p.length,1,p),"x");

		eq.process("left = 1.0/((2*pi)^(D/2.0)*sqrt(det(S)))");
		eq.process("likelihood = left*exp(-0.5*(x-mu)'*inv(S)*(x-mu))");

		return eq.lookupDouble("likelihood");
	}

	private double computeChiSq( GaussianGmm_F64 g , double[] p ) {
		Equation eq = new Equation();

		eq.alias(g.mean,"mu",g.covariance,"S");
		eq.alias(DMatrixRMaj.wrap(p.length,1,p),"x");

		eq.process("chisq = (x-mu)'*inv(S)*(x-mu)");

		return eq.lookupDouble("chisq");
	}
}
