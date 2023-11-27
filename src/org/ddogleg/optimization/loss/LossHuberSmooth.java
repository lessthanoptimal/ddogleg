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
 * 't'. The point of inflection where the functions begins to behave more linear is for values of 'a' > 't'.
 *
 * <ol>
 *     <li><a href="https://en.wikipedia.org/wiki/Huber_loss">Huber Loss - Wikipedia 2023</a></li>
 * </ol>
 *
 * @author Peter Abeles
 */
public abstract class LossHuberSmooth extends LossFunctionBase {
	/** Threshold parameter that determines when errors become linear */
	final double threshold;

	protected LossHuberSmooth( double threshold ) {
		this.threshold = threshold;
	}

	/**
	 * Implementation of the smooth Huber loss function
	 */
	public static class Function extends LossHuberSmooth implements LossFunction {
		public Function( double threshold ) {
			super(threshold);
		}

		@Override public double process( double[] input ) {
			final double thresholdSq = threshold*threshold;

			double sum = 0.0;
			for (int i = 0; i < numberOfFunctions; i++) {
				double r = input[i];
				double tmp = r/threshold;
				sum += thresholdSq*(Math.sqrt(1 + tmp*tmp) - 1);
			}
			return sum;
		}
	}

	/**
	 * Implementation of the smooth Huber loss gradient
	 */
	public static class Gradient extends LossHuberSmooth implements LossFunctionGradient {
		public Gradient( double threshold ) {
			super(threshold);
		}

		@Override public void process( double[] input, double[] output ) {
			for (int funcIdx = 0; funcIdx < numberOfFunctions; funcIdx++) {
				double r = input[funcIdx];
				double tmp = r/threshold;
				output[funcIdx] = r/Math.sqrt(1.0 + tmp*tmp);
			}
		}
	}
}
