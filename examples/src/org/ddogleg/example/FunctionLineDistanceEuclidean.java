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

import org.ddogleg.optimization.functions.FunctionNtoM;

import java.util.List;

/**
 * Computes the distance a point is from a line in 2D.
 * The line is defined using the tangent from origin equation.
 *
 * @author Peter Abeles
 */
public class FunctionLineDistanceEuclidean implements FunctionNtoM {

	// Data which the line is being fit too
	List<Point2D> data;

	public FunctionLineDistanceEuclidean(List<Point2D> data) {
		this.data = data;
	}

	/**
	 * Number of parameters used to define the line.
	 */
	@Override
	public int getNumOfInputsN() {
		return 2;
	}

	/**
	 * Number of output error functions.  Two for each point.
	 */
	@Override
	public int getNumOfOutputsM() {
		return data.size()*2;
	}

	@Override
	public void process(double[] input, double[] output) {

		// tangent equation
		double tanX = input[0], tanY = input[1];

		// convert into parametric equation
		double lineX = tanX; double lineY = tanY;
		double slopeX = -tanY; double slopeY = tanX;

		// compute the residual error for each point in the data set
		for( int i = 0; i < data.size(); i++ ) {
			Point2D p = data.get(i);

			double t = slopeX * ( p.x - lineX ) + slopeY * ( p.y - lineY );
			t /= slopeX * slopeX + slopeY * slopeY;

			double closestX = lineX + t*slopeX;
			double closestY = lineY + t*slopeY;

			output[i*2]   = p.x-closestX;
			output[i*2+1] = p.y-closestY;
		}
	}
}
