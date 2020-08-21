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

package org.ddogleg.fitting.modelset.distance;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestFitByMedianStatistics {

	Random rand = new Random(234);

	@Test
	public void metric_and_prune() {
		List<PointIndex<Double>> list = new ArrayList<>(200);

		for (int i = 0; i < 200; i++) {
			list.add(new PointIndex<>((double) i,i));
		}

		// randomize the inputs
		Collections.shuffle(list,rand);
		ArrayDeque<PointIndex<Double>> inliers = new ArrayDeque<>(list);

		FitByMedianStatistics<double[],Double> fit = new FitByMedianStatistics<>(0.90);

		fit.init(new DistanceFromMeanModel(), inliers);

		fit.computeStatistics();

		assertEquals(100, fit.getErrorMetric(), 1e-8);

		fit.prune();

		assertEquals(180, inliers.size());
	}

}
