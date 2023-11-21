/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.fitting.modelset.GenericModelMatcherPostTests;
import org.ddogleg.fitting.modelset.MeanModelFitter;
import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.fitting.modelset.ModelMatcherPost;
import org.ddogleg.fitting.modelset.distance.DistanceFromMeanModel;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestLeastMedianOfSquares extends GenericModelMatcherPostTests {

	public TestLeastMedianOfSquares() {
		configure(0.9, 0.1, false);
	}

	@Override
	public ModelMatcherPost<double[], Double> createModelMatcher( ModelManager<double[]> manager,
																  int minPoints,
																  double fitThreshold ) {
		return new LeastMedianOfSquares<>(4234, 50, fitThreshold, 0.9, manager, Double.class);
	}

	/** If the user set the max error to Double.MAX_VALUE then it should still work as expected */
	@Test void handleMaxMaxError() {
		ModelMatcherPost<double[], Double> matcher = createModel(1, Double.MAX_VALUE);

		// The model fitter will always fail, so process should fail too
		matcher.setModel(() -> new MeanModelFitter() {
			@Override
			public boolean fitModel( List<Double> dataSet, @Nullable double[] initParam, double[] foundParam ) {
				return false;
			}
		}, DistanceFromMeanModel::new);

		List<Double> samples = createSampleSet(100, 1, 1, 0.1);

		assertFalse(matcher.process(samples));
	}
}
