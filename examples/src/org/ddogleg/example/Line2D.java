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

/**
 * Line in 2D space parameterized by the closest point to the origin. Almost every line in 2D can be uniquely defined
 * by the point on the line which lies closest to the original. The slope of the line will then be the vector
 * which is perpendicular to the slope of the line defined by the original to the closest point on the line being
 * parameterised. The one exception to this rule is the set of lines which pass through the origin.
 *
 * We use this less common line parameterization because it only requires 2 variables and uniquely defines the line.
 * Point-Slope requires 4 variables and each line has an infinite number of parametrizations. Using a model with
 * the minimum number of variables isn't important for RANSAC, but is very important for non-linear optimization.
 *
 * <pre>x = a + t*b
 * y = b - t*a</pre>
 * where (a,b) is the closest point on the line to the origin.
 *
 * @author Peter Abeles
 */
public class Line2D {
	/**
	 * Coordinate of the closest point on the line to the origin.
	 */
	double x, y;

	@Override
	public String toString() {
		return "Line2D( x=" + x + " y=" + y + " )";
	}
}
