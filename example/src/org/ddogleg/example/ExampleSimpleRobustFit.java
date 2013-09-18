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
import org.ddogleg.fitting.modelset.ModelGenerator;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.fitting.modelset.ransac.Ransac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Simple example demonstrating how to robustly fit a line to a noisy set of points.
 *
 * @author Peter Abeles
 */
public class ExampleSimpleRobustFit {

	public static void main( String args[] ) {
		Random rand = new Random(234);

		//------------------------ Create Observations
		// define a line in 2D space as the tangent from the origin
		double lineX = -2.1;
		double lineY = 1.3;
		List<Point2D> points = generateObservations(rand, lineX, lineY);

		//------------------------ Compute the solution
		// Let it know how to compute the model and fit errors
		ModelGenerator<Line2D,Point2D> generator = new LineGenerator();
		DistanceFromModel<Line2D,Point2D> distance = new DistanceFromLine();

		// RANSAC or LMedS work well here
		ModelMatcher<Line2D,Point2D> alg =
				new Ransac<Line2D,Point2D>(234234,generator,distance,500,0.01);
//		ModelMatcher<Line2D,Point2D> alg =
//				new LeastMedianOfSquares<Line2D, Point2D>(234234,100,0.1,0.5,generator,distance);

		if( !alg.process(points) )
			throw new RuntimeException("Robust fit failed!");

		// let's look at the results
		Line2D found = alg.getModelParameters();

		// notice how all the noisy points were removed and an accurate line was estimated?
		System.out.println("Found line   "+found);
		System.out.println("Actual line   x = "+lineX+" y = "+lineY);
		System.out.println("Match set size = "+alg.getMatchSet().size());
	}

	private static List<Point2D> generateObservations(Random rand, double lineX, double lineY) {
		// randomly generate points along the line
		List<Point2D> points = new ArrayList<Point2D>();
		for( int i = 0; i < 20; i++ ) {
			double t = (rand.nextDouble()-0.5)*10;
			points.add( new Point2D(lineX + t*lineY, lineY - t*lineX) );
		}

		// Add in some random points
		for( int i = 0; i < 5; i++ ) {
			points.add( new Point2D(rand.nextGaussian()*10,rand.nextGaussian()*10));
		}

		// Shuffle the list to remove any structure
		Collections.shuffle(points);
		return points;
	}
}
