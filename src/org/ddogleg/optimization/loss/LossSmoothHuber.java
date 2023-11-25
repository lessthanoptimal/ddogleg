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

/**
 * Smooth approximation to the huber loss [1]. This is similar to the L1 Loss in Ceres.
 *
 * <pre>L(a) = t<sup>2</sup>(sqrt(1+(a/t)<sup>2</sup>-1)</pre>, where 'a' is the residual, and 't' is the passed in
 * tuning. For small values it will approximate a<sup>2</sup>/2, but for large values it will be a line with slope
 * 't'
 *
 * <ol>
 *     <li><a href="https://en.wikipedia.org/wiki/Huber_loss">Huber Loss - Wikipedia 2023</a></li>
 * </ol>
 *
 * @author Peter Abeles
 */
public class LossSmoothHuber {
	/** Threshold parameter that determines when errors become linear */
	double threshold;
	// to speed up the computation slightly we check for |a| <= threshold using a**2 <= thresholdSq instead.
	// These are mathematically the equivalent
	double thresholdSq;

	protected LossSmoothHuber( double threshold ) {
		this.threshold = threshold;
		this.thresholdSq = threshold*threshold;
	}

	/**
	 * Implementation of the Huber loss function
	 */
	public static class Function extends LossFunction {
		public LossSmoothHuber huber;

		public Function( double threshold ) {
			this.huber = new LossSmoothHuber(threshold);
		}

		@Override public double process( double[] input ) {
			final double threshold = huber.threshold;
			final double thresholdSq = huber.thresholdSq;

			double sum = 0.0;
			for (int i = 0; i < numberOfFunctions; i++) {
				double r = input[i];
				double rr = r*r;
				if (rr <= thresholdSq) {
					sum += 0.5*rr;
				} else {
					sum += threshold*(Math.abs(r) - 0.5*threshold);
				}
			}
			return sum;
		}
	}

	/**
	 * Implementation of the Huber Loss gradient
	 */
	public static class Gradient extends LossFunctionGradient {
		public LossSmoothHuber huber;

		public Gradient( double threshold ) {
			this.huber = new LossSmoothHuber(threshold);
		}

		@Override public void process( double[] input, double[] output ) {
			final double threshold = huber.threshold;
			final double thresholdSq = huber.thresholdSq;

			for (int funcIdx = 0; funcIdx < numberOfFunctions; funcIdx++) {
				double r = input[funcIdx];
				double rr = r*r;
				output[funcIdx] = (rr <= thresholdSq) ? r : threshold*Math.signum(r);
			}
		}
	}
}
