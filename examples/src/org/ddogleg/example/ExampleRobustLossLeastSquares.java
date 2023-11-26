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

import org.ddogleg.optimization.FactoryOptimization;
import org.ddogleg.optimization.UnconstrainedLeastSquares;
import org.ddogleg.optimization.UtilOptimize;
import org.ddogleg.optimization.loss.*;
import org.ejml.data.DMatrixRMaj;

import java.util.ArrayList;
import java.util.Random;

/**
 * Multiple robust Loss functions are available. These attempt to reduce the influence of outliers in the answer by
 * decreasing their influence in the state.
 */
public class ExampleRobustLossLeastSquares {
	public static void main( String[] args ) {
		// define a line in 2D space as the tangent from the origin
		double lineX = -2.1;
		double lineY = 1.3;

		// randomly generate points along the line with a little bit of noise
		var rand = new Random(234);
		var points = new ArrayList<Point2D>();
		double noise = 0.5;
		addPointsOnLine(300, rand, noise, points, lineX, lineY);

		// Add a outliers now. 25% of the points are noise! This will mess up a least squares solver.
		addPointsOnLine(100, rand, 30, points, lineX, lineY);

		// Define the function being optimized and create the optimizer
		var func = new FunctionLineDistanceEuclidean(points);
		UnconstrainedLeastSquares<DMatrixRMaj> optimizer = FactoryOptimization.levenbergMarquardt(null, true);

		// if no jacobian is specified it will be computed numerically
		optimizer.setFunction(func, null);

		// provide it an extremely crude initial estimate of the line equation
		optimizer.initialize(new double[]{-0.5, 0.5}, 1e-12, 1e-12);

		// Compute the error with no robust solver being used
		UtilOptimize.process(optimizer, 500);
		printModelError("Squared Error", lineX, lineY, optimizer.getParameters());

		// Now compare it to several robust loss functions. Be sure to tune them for your application!
		// NOTE: You must call setLoss() before initialize or else the initial cost will be computed incorrectly

		double thresholdHuber = 0.5;
		optimizer.setLoss(new LossHuber.Function(thresholdHuber), new LossHuber.Gradient(thresholdHuber));
		optimizer.initialize(new double[]{-0.5, 0.5}, 1e-12, 1e-12);
		UtilOptimize.process(optimizer, 500);
		printModelError("Huber", lineX, lineY, optimizer.getParameters());

		double thresholdSmoothHuber = 0.25;
		optimizer.setLoss(new LossSmoothHuber.Function(thresholdSmoothHuber), new LossSmoothHuber.Gradient(thresholdSmoothHuber));
		optimizer.initialize(new double[]{-0.5, 0.5}, 1e-12, 1e-12);
		UtilOptimize.process(optimizer, 500);
		printModelError("Smooth-Huber", lineX, lineY, optimizer.getParameters());

		double alphaCauchy = 1.0;
		optimizer.setLoss(new LossCauchy.Function(alphaCauchy), new LossCauchy.Gradient(alphaCauchy));
		optimizer.initialize(new double[]{-0.5, 0.5}, 1e-12, 1e-12);
		UtilOptimize.process(optimizer, 500);
		printModelError("Cauchy", lineX, lineY, optimizer.getParameters());

		double thresholdTukey = 3.0;
		optimizer.setLoss(new LossTukey.Function(thresholdTukey), new LossTukey.Gradient(thresholdTukey));
		optimizer.initialize(new double[]{-0.5, 0.5}, 1e-12, 1e-12);
		UtilOptimize.process(optimizer, 500);
		printModelError("Tukey", lineX, lineY, optimizer.getParameters());

		// Iteratively Reweighted Least-Squares (IRLS) is a special case. It allows you to recompute the weights
		// every iteration. This is very flexible and can approximate the functions above.
		// What we are going to do is use known information about the noise model to clip anything that is far away

		var lossIRLS = new LossIRLS();
		lossIRLS.computeOp = ( residuals, weights ) -> {
			for (int i = 0; i < weights.length; i++) {
				// try to remove outliers completely by assuming everything over 3 sigma is an outlier
				weights[i] = Math.abs(residuals[i]) > noise*3 ? 0.0 : 1.0;
			}
		};
		optimizer.setLoss(lossIRLS, lossIRLS);
		optimizer.initialize(new double[]{-0.5, 0.5}, 1e-12, 1e-12);
		UtilOptimize.process(optimizer, 500);
		printModelError("IRLS", lineX, lineY, optimizer.getParameters());

		System.out.println();
		System.out.println("What happens if you remove the outliers. How do the scores change?");

		// It's hard to predict which loss function is the best to your application. You will need to try them all
		// and adjust the tuning parameter. In this case there was a clear line that seperated inlier from outlier
		// which is why functions with a hard cut off performed best
	}

	/** Computes a distance measure for how far the found results are from the truth. Prints the results */
	private static void printModelError( String description, double lineX, double lineY, double[] found ) {
		double dx = lineX - found[0];
		double dy = lineY - found[1];

		System.out.printf("%15s: error %e\n", description, (dx*dx + dy*dy));
	}

	/** Add noisy points that are near the line */
	private static void addPointsOnLine( int count, Random rand, double noiseSigma,
										 ArrayList<Point2D> points, double lineX, double lineY ) {
		for (int i = 0; i < count; i++) {
			double t = (rand.nextDouble() - 0.5)*20;

			double noiseX = rand.nextGaussian()*noiseSigma;
			double noiseY = rand.nextGaussian()*noiseSigma;

			points.add(new Point2D(lineX + t*lineY + noiseX, lineY - t*lineX + noiseY));
		}
	}
}
