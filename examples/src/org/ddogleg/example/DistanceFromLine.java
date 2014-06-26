/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.example;

import org.ddogleg.fitting.modelset.DistanceFromModel;

import java.util.List;

/**
 * Computes the distance a point is from a line in 2D. Line is parameterized by the tangent point
 * from the origin.
 *
 * @author Peter Abeles
 */
public class DistanceFromLine implements DistanceFromModel<Line2D,Point2D> {

	// parametric line equation
	double x0, y0;
	double slopeX,slopeY;

	@Override
	public void setModel(Line2D param) {
		x0 = param.x;
		y0 = param.y;

		slopeX = -y0;
		slopeY = x0;
	}

	@Override
	public double computeDistance(Point2D p) {

		// find the closest point on the line to the point
		double t = slopeX * ( p.x - x0) + slopeY * ( p.y - y0);
		t /= slopeX * slopeX + slopeY * slopeY;

		double closestX = x0 + t*slopeX;
		double closestY = y0 + t*slopeY;

		// compute the Euclidean distance
		double dx = p.x - closestX;
		double dy = p.y - closestY;

		return Math.sqrt(dx*dx + dy*dy);
	}

	/**
	 * There are some situations where processing everything as a list can speed things up a lot.
	 * This is not one of them.
	 */
	@Override
	public void computeDistance(List<Point2D> obs, double[] distance) {
		for( int i = 0; i < obs.size(); i++ ) {
			distance[i] = computeDistance(obs.get(i));
		}
	}
}
