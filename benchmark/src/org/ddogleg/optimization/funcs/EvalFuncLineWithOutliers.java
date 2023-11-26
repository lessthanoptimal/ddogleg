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

package org.ddogleg.optimization.funcs;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ejml.data.DMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple line in 2D. Points are added to it with no noise. We then a bunch of very noisy points.
 */
public class EvalFuncLineWithOutliers<S extends DMatrix> implements
		EvalFuncLeastSquaresSchur<S>, EvalFuncLeastSquares<S> {
	List<double[]> points = new ArrayList<>();
	Random rand = new Random(2344);

	public EvalFuncLineWithOutliers() {
		double[] line = getOptimal();
		addPointsOnLine(200, rand, 0.1, points, line[0], line[1]);

		// Noisy points
		addPointsOnLine(50, rand, 30, points, line[0], line[1]);
	}

	@Override public FunctionNtoM getFunction() {
		return new FunctionNtoM() {
			@Override public void process( double[] input, double[] output ) {
				// tangent equation
				double tanX = input[0], tanY = input[1];

				// convert into parametric equation
				double lineX = tanX;
				double lineY = tanY;
				double slopeX = -tanY;
				double slopeY = tanX;

				// compute the residual error for each point in the data set
				for (int i = 0; i < points.size(); i++) {
					double[] p = points.get(i);

					double t = slopeX*(p[0] - lineX) + slopeY*(p[1] - lineY);
					t /= slopeX*slopeX + slopeY*slopeY;

					double closestX = lineX + t*slopeX;
					double closestY = lineY + t*slopeY;

					output[i*2] = p[0] - closestX;
					output[i*2 + 1] = p[1] - closestY;
				}
			}

			@Override public int getNumOfInputsN() {
				return 2;
			}

			@Override public int getNumOfOutputsM() {
				return points.size()*2;
			}
		};
	}

	@Override public double[] getInitial() {
		return new double[]{2, -3};
	}

	@Override public double[] getOptimal() {
		return new double[]{-0.5, 1.4};
	}

	@Override public FunctionNtoMxN<S> getJacobian() {
		return null;
	}

	@Override public SchurJacobian<S> getJacobianSchur() {
		return null;
	}

	@Override public boolean requireRobustLoss() {
		return true;
	}

	/** Add noisy points that are near the line */
	private static void addPointsOnLine( int count, Random rand, double noiseSigma,
										 List<double[]> points, double lineX, double lineY ) {
		for (int i = 0; i < count; i++) {
			double t = (rand.nextDouble() - 0.5)*20;

			double noiseX = rand.nextGaussian()*noiseSigma;
			double noiseY = rand.nextGaussian()*noiseSigma;

			points.add(new double[]{lineX + t*lineY + noiseX, lineY - t*lineX + noiseY});
		}
	}
}
