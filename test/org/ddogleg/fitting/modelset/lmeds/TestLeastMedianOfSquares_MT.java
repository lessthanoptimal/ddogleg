/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.fitting.modelset.lmeds;

import org.ddogleg.fitting.modelset.DoubleArrayManager;
import org.ddogleg.fitting.modelset.GenericModelMatcherPostTests;
import org.ddogleg.fitting.modelset.MeanModelFitter;
import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.fitting.modelset.ModelMatcherPost;
import org.ddogleg.fitting.modelset.distance.DistanceFromMeanModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestLeastMedianOfSquares_MT {
	@Nested class Fraction extends Common {
		Fraction() {super(0.9);}
	}

	@Nested class Sigma extends Common {
		Sigma() {super(0);}
	}

	/** Runs the generic tests with the specified inlier fraction. 0 = sigma */
	abstract class Common extends GenericModelMatcherPostTests {
		double inlierFraction;
		int numTrials = 50;

		Common( double inlierFraction ) {
			this.inlierFraction = inlierFraction;
			configure(0.9, 0.1, inlierFraction == 0);
		}

		@Override
		public ModelMatcherPost<double[], Double> createModelMatcher( ModelManager<double[]> manager,
																	  int minPoints,
																	  double fitThreshold ) {
			var alg = new LeastMedianOfSquares_MT<>(4234, numTrials, fitThreshold, manager, Double.class);
			alg.setInlierFraction(inlierFraction);
			return alg;
		}

		protected ModelMatcherPost<double[], Double> createModelSingle( double fitThreshold ) {
			var alg = new LeastMedianOfSquares<>(4234, numTrials, fitThreshold, new DoubleArrayManager(1), Double.class);
			alg.setInlierFraction(inlierFraction);
			alg.setModel(MeanModelFitter::new, DistanceFromMeanModel::new);
			return alg;
		}

		@Test void compareToSingleThread() {
			double mean = 2.5;
			double tol = 0.2;

			numTrials = 200;

			ModelMatcherPost<double[], Double> multi = createModel(4, tol);
			ModelMatcherPost<double[], Double> single = createModelSingle(tol);

			for (int trial = 0; trial < 10; trial++) {
				// try different sample sizes in each trial.  a bug was found once where
				// a small value of N than previous caused a problem
				int N = 500;
				List<Double> samples = createSampleSet(N, mean, tol*0.90, 0.1);

				assertTrue(multi.process(samples));
				assertTrue(single.process(samples));

				assertEquals(single.getFitQuality(), multi.getFitQuality());

				List<Double> expected = single.getMatchSet();
				List<Double> found = multi.getMatchSet();
				assertEquals(expected.size(), found.size());
				for (int i = 0; i < expected.size(); i++) {
					assertEquals(expected.get(i), found.get(i));
					assertEquals(single.getInputIndex(i), multi.getInputIndex(i));
				}

				assertArrayEquals(single.getModelParameters(), multi.getModelParameters(), 1e-16);
			}
		}
	}
}
