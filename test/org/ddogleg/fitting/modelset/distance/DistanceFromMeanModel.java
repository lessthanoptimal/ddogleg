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

import org.ddogleg.fitting.modelset.DistanceFromModel;

import java.util.List;


/**
 * Computes the absolute distance from the mean a point is.
 *
 * @author Peter Abeles
 */
public class DistanceFromMeanModel implements DistanceFromModel<double[],Double> {
	double mean;

	@Override
	public void setModel(double[] param) {
		mean = param[0];
	}

	@Override
	public double distance(Double pt) {
		return Math.abs(pt - mean);
	}

	@Override
	public void distances(List<Double> points, double[] distance) {
		for (int i = 0; i < points.size(); i++) {
			distance[i] = Math.abs(points.get(i) - mean);
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
