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

import org.ddogleg.fitting.modelset.*;

/**
 * @author Peter Abeles
 */
public class TestStatisticalDistanceModelMatcher extends GenericModelMatcherTests {

	public TestStatisticalDistanceModelMatcher() {
		configure(0.9, 0.1, false);
		this.deterministic = true;
	}

	@Override
	public ModelMatcher<double[],Double> createModelMatcher(ModelManager<double[]> manager,
															DistanceFromModel<double[],Double> distance,
															ModelGenerator<double[],Double> generator,
															ModelFitter<double[],Double> fitter,
															int minPoints, double fitThreshold) {
		return new StatisticalDistanceModelMatcher<>(5, 0, 0, 10000, minPoints,
				StatisticalDistance.PERCENTILE,
				0.95, manager, generator, distance, new ArrayCodec());
	}

	private static class ArrayCodec implements ModelCodec<double[]>
	{

		@Override
		public void decode(double[] input, double[] outputModel) {
			System.arraycopy(outputModel,0, input,0, input.length);
		}

		@Override
		public void  encode(double[] model, double[] param) {
			System.arraycopy(model,0,param,0,model.length);
		}

		@Override
		public int getParamLength() {
			return 1;
		}
	}
}
