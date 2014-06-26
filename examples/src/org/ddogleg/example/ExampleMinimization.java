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

import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.UtilOptimize;
import org.ddogleg.optimization.functions.FunctionNtoM;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Very simple example of how to minimize a function.
 *
 * @author Peter Abeles
 */
public class ExampleMinimization {

	public static void main( String args[] ) {

		// define a line in 2D space as the tangent from the origin
		double lineX = -2.1;
		double lineY = 1.3;

		// randomly generate points along the line
		Random rand = new Random(234);
		List<Point2D> points = new ArrayList<Point2D>();
		for( int i = 0; i < 20; i++ ) {
			double t = (rand.nextDouble()-0.5)*10;
			points.add( new Point2D(lineX + t*lineY, lineY - t*lineX) );
		}

		// Define the function being optimized and create the optimizer
		FunctionNtoM func = new FunctionLineDistanceEuclidean(points);
		UnconstrainedLeastSquares optimizer = FactoryOptimization.leastSquaresLM(1e-3, true);

		// if no jacobian is specified it will be computed numerically
		optimizer.setFunction(func,null);

		// provide it an extremely crude initial estimate of the line equation
		optimizer.initialize(new double[]{-0.5,0.5},1e-12,1e-12);

		// iterate 500 times or until it converges.
		// Manually iteration is possible too if more control over is required
		UtilOptimize.process(optimizer,500);

		double found[] = optimizer.getParameters();

		// see how accurately it found the solution
		System.out.println("Final Error = "+optimizer.getFunctionValue());

		// Compare the actual parameters to the found parameters
		System.out.printf("Actual %5.2f  found %5.2f\n",lineX,found[0]);
		System.out.printf("Actual %5.2f  found %5.2f\n",lineY,found[1]);
	}
}
