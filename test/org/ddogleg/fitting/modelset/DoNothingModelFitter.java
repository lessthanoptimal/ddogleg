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

package org.ddogleg.fitting.modelset;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Claims to fit a model but really doesn't
 *
 * @author Peter Abeles
 */
public class DoNothingModelFitter implements ModelFitter<double[],Double> ,
		ModelGenerator<double[],Double>{

	int minPoints;

	public DoNothingModelFitter(int minPoints) {
		this.minPoints = minPoints;
	}

	@Override
	public boolean generate(List<Double> dataSet, double[] param ) {
		return fitModel(dataSet,null,param);
	}

	@Override
	public boolean fitModel(List<Double> dataSet, @Nullable double[] initParam, double[] foundParam) {
		assertEquals(minPoints,dataSet.size());
		return true;
	}

	@Override
	public double getFitScore() {
		return 0;
	}

	@Override
	public int getMinimumPoints() {
		return minPoints;
	}
}
