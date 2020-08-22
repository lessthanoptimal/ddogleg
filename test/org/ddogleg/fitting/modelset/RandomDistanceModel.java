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

import java.util.List;
import java.util.Random;


/**
 * Returns a random distance
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway"})
public class RandomDistanceModel implements DistanceFromModel<double[],Double> {
	Random rand = new Random(234);

	double errorMagnitude;
	double errorMinimum;

	public RandomDistanceModel(double errorMagnitude, double errorMinimum) {
		this.errorMagnitude = errorMagnitude;
		this.errorMinimum = errorMinimum;
	}

	@Override
	public void setModel(double[] param) {
	}

	@Override
	public double computeDistance(Double pt) {
		return rand.nextDouble()*errorMagnitude+errorMinimum;
	}

	@Override
	public void computeDistance(List<Double> points, double[] distance) {
		for (int i = 0; i < points.size(); i++) {
			distance[i] = computeDistance(null);
		}
	}

	@Override
	public Class<Double> getPointType() {
		return Double.class;
	}

	@Override
	public Class<double[]> getModelType() {
		return double[].class;
	}
}
