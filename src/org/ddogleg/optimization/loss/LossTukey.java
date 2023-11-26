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
 * Tukey loss (Tukey's biweight function) has similar behavior to {@link LossHuber} but is less sensitive to outliers
 * because they contribute nothing to the loss.
 *
 * <pre>
 * l(c) = c<sup>2</sup>/6(1 - [1 - (r/c)**2]**3)   if |r| <= c
 *        c<sup>2</sup>/6                          otherwise
 * </pre>
 *
 * @author Peter Abeles
 */
public abstract class LossTukey extends LossFunctionBase {
	/** Threshold parameter that determines when errors become linear */
	final double threshold;
	// to speed up the computation slightly we check for |a| <= threshold using a**2 <= thresholdSq instead.
	// These are mathematically the equivalent
	final double thresholdSq;

	protected LossTukey( double threshold ) {
		this.threshold = threshold;
		this.thresholdSq = threshold*threshold;
	}

	/**
	 * Implementation of the Tukey loss function
	 */
	public static class Function extends LossTukey implements LossFunction {
		public Function( double threshold ) {
			super(threshold);
		}

		@Override public double process( double[] input ) {
			double coef = threshold*threshold/6.0;

			double sum = 0.0;
			for (int i = 0; i < numberOfFunctions; i++) {
				double r = input[i];
				double rr = r*r;
				if (rr <= thresholdSq) {
					double tmp = 1 - (r/threshold)*(r/threshold);
					sum += coef*(1 - tmp*tmp*tmp);
				} else {
					sum += coef;
				}
			}
			return sum;
		}
	}

	/**
	 * Implementation of the Tukey Loss gradient
	 */
	public static class Gradient extends LossTukey implements LossFunctionGradient {
		public Gradient( double threshold ) {
			super(threshold);
		}

		@Override public void process( double[] input, double[] output ) {
			for (int funcIdx = 0; funcIdx < numberOfFunctions; funcIdx++) {
				double r = input[funcIdx];
				double rr = r*r;
				if (rr <= thresholdSq) {
					double tmp = 1 - (r/threshold)*(r/threshold);
					output[funcIdx] = r*tmp*tmp;
				} else {
					output[funcIdx] = 0.0;
				}
			}
		}
	}
}
