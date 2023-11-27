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

package org.ddogleg.example;

import org.ddogleg.fitting.modelset.ModelGenerator;

import java.util.List;

/**
 * Estimate the parameters of a line using two points
 */
public class LineGenerator implements ModelGenerator<Line2D, Point2D> {

	// a point at the origin (0,0)
	Point2D origin = new Point2D();

	@Override
	public boolean generate( List<Point2D> dataSet, Line2D output ) {
		Point2D p1 = dataSet.get(0);
		Point2D p2 = dataSet.get(1);

		// First find the slope of the line
		double slopeX = p2.x - p1.x;
		double slopeY = p2.y - p1.y;

		// Now that we have the slope, all we need is a line on the point (we pick p1) to find
		// the closest point on the line to the origin. This closest point is the parametrization.
		double t = slopeX*(origin.x - p1.x) + slopeY*(origin.y - p1.y);
		t /= slopeX*slopeX + slopeY*slopeY;

		output.x = p1.x + t*slopeX;
		output.y = p1.y + t*slopeY;

		return true;
	}

	@Override
	public int getMinimumPoints() {
		return 2;
	}
}
