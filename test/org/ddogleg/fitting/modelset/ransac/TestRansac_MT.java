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

package org.ddogleg.fitting.modelset.ransac;

import org.ddogleg.fitting.modelset.*;
import org.ddogleg.fitting.modelset.distance.DistanceFromMeanModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestRansac_MT extends GenericModelMatcherPostTests {

	int numTrials = 50;

	public TestRansac_MT() {
		configure(0.9, 0.05, true);
	}

	@Override
	public ModelMatcherPost<double[], Double> createModelMatcher( ModelManager<double[]> manager,
																  int minPoints,
																  double fitThreshold ) {
		var ret = new Ransac_MT<>(344, 50, fitThreshold, manager, Double.class);
		ret.setSampleSize(minPoints);
		return ret;
	}

	protected ModelMatcherPost<double[], Double> createModelSingle( int minPoints, double fitThreshold ) {
		var manager = new DoubleArrayManager(1);
		var ret = new Ransac<>(344, 50, fitThreshold, manager, Double.class);
		ret.setSampleSize(minPoints);
		ret.setModel(MeanModelFitter::new, DistanceFromMeanModel::new);
		return ret;
	}

	@Test void compareToSingleThread() {
		double mean = 2.5;
		double tol = 0.2;

		numTrials = 200;

		ModelMatcherPost<double[], Double> multi = createModel(4, tol);
		ModelMatcherPost<double[], Double> single = createModelSingle(4, tol);

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
