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

package org.ddogleg.optimization.loss;

import org.ddogleg.optimization.UtilOptimize;

/**
 * Iteratively Reweighted Least-Squares (IRLS) allows the weights to be recomputed every iteration. At the start
 * of an internation the weights are computed and saved. This is to ensure the cost function doesn't change as the
 * solver is trying to optimise and it needs to back step.
 */
public class LossIRLS implements LossFunction, LossFunctionGradient {
	/**
	 * Function that's called when the weights need to be updated. Must be specified.
	 */
	public ComputeWeights computeOp = ( r, w ) -> {
		throw new RuntimeException("You must specify the computeOp function");
	};

	/** Weight assigned to each function */
	protected double[] weights = new double[0];

	@Override public boolean fixate( double[] residuals ) {
		computeOp.process(residuals, weights);
		return true;
	}

	/**
	 * Computes the lost function
	 */
	@Override public double process( double[] input ) {
		// Avoid numerical overflow by ensuring values are around one
		double max = UtilOptimize.maxAbs(input, 0, weights.length);
		if (max == 0.0)
			return 0.0;

		double sum = 0.0;
		for (int i = 0; i < weights.length; i++) {
			double r = weights[i]*(input[i]/max);
			sum += r*r;
		}

		return 0.5*max*sum*max;
	}

	/**
	 * Computes the gradient
	 */
	@Override public void process( double[] input, double[] output ) {
		for (int i = 0; i < weights.length; i++) {
			double w = weights[i];
			output[i] = w*w*input[i];
		}
	}

	@Override public int getNumberOfFunctions() {
		return weights.length;
	}

	@Override public void setNumberOfFunctions( int value ) {
		weights = new double[value];
	}

	@FunctionalInterface public interface ComputeWeights {
		/**
		 * @param residuals (Input) residuals
		 * @param weights (Output) weights
		 */
		void process( double[] residuals, double[] weights );
	}
}
