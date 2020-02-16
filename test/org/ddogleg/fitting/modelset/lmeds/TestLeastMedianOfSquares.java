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

package org.ddogleg.fitting.modelset.lmeds;

import org.ddogleg.fitting.modelset.*;


/**
 * @author Peter Abeles
 */
public class TestLeastMedianOfSquares extends GenericModelMatcherTests {

	public TestLeastMedianOfSquares() {
		configure(0.9, 0.1, false);
	}

	@Override
	public ModelMatcher<double[],Double> createModelMatcher(ModelManager<double[]> manager,
															DistanceFromModel<double[],Double> distance,
															ModelGenerator<double[],Double> generator,
															ModelFitter<double[],Double> fitter,
															int minPoints, double fitThreshold) {
		return new LeastMedianOfSquares<>(4234,50,fitThreshold,0.9,manager,generator,distance);
	}
}
