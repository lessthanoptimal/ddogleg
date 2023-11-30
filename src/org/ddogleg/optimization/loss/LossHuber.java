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
 * Huber Loss is a robust loss function that is less sensitive to outliers than the squared error loss. For values
 * less than a threshold it returns the squared error, for values greater than it returns an error that grows linearly
 * instead of quadratic.
 *
 * @author Peter Abeles
 */
public abstract class LossHuber extends LossFunctionBase {
	/** Threshold parameter that determines when errors become linear */
	final double threshold;

	protected LossHuber( double threshold ) {
		this.threshold = threshold;
	}

	/**
	 * Implementation of the Huber loss function
	 */
	public static class Function extends LossHuber implements LossFunction {
		public Function( double threshold ) {
			super(threshold);
		}

		@Override public double process( double[] input ) {
			// Avoid numerical overflow by ensuring values are around one
			double max = UtilOptimize.maxAbs(input, 0, numberOfFunctions);
			if (max == 0.0)
				return 0.0;

			double scaleThreshold = threshold/max;
			double scaleThresholdSq = scaleThreshold*scaleThreshold;

			double sum = 0.0;
			for (int i = 0; i < numberOfFunctions; i++) {
				double r = input[i]/max;
				double rr = r*r;
				if (rr <= scaleThresholdSq) {
					sum += 0.5*rr;
				} else {
					sum += scaleThreshold*(Math.abs(r) - 0.5*scaleThreshold);
				}
			}
			return max*sum*max;
		}
	}

	/**
	 * Implementation of the Huber Loss gradient
	 */
	public static class Gradient extends LossHuber implements LossFunctionGradient {
		public Gradient( double threshold ) {
			super(threshold);
		}

		@Override public void process( double[] input, double[] output ) {
			final double thresholdSq = threshold*threshold;

			for (int funcIdx = 0; funcIdx < numberOfFunctions; funcIdx++) {
				double r = input[funcIdx];
				double rr = r*r;
				output[funcIdx] = (rr <= thresholdSq) ? r : threshold*Math.signum(r);
			}
		}
	}
}
