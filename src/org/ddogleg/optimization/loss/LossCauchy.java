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
 * <p>Loss function inspired by the Cauchy distribution, a.k.a Lorentzian loss function.</p>
 *
 * <pre>L(x) = a<sup>2</sup>(log(1+(x/a)<sup>2</sup>)</pre>, where 'x' is the residual and 'a' is a scale
 * parameter. Smaller values of the tuning parameter 'a' make it behave more similar to the L2 loss while
 * larger values make it more robust to outliers.
 *
 * <ol>
 *     <li>Black, Michael J., and Paul Anandan. "The robust estimation of multiple motions: Parametric and
 *     piecewise-smooth flow fields." Computer vision and image understanding 63.1 (1996): 75-104.</li>
 * </ol>
 *
 * @author Peter Abeles
 */
public class LossCauchy {
	protected double alpha;

	/**
	 * @param alpha scale parameter that changes sensitivity to outliers
	 */
	protected LossCauchy( double alpha ) {
		this.alpha = alpha;
	}

	/**
	 * Implementation of the smooth Cauchy loss function
	 */
	public static class Function extends LossFunction {
		public LossCauchy params;

		public Function( double threshold ) {
			this.params = new LossCauchy(threshold);
		}

		@Override public double process( double[] input ) {
			final double alpha = params.alpha;

			double sum = 0.0;
			for (int i = 0; i < numberOfFunctions; i++) {
				double r = input[i];
				double tmp = r/alpha;
				sum += alpha*alpha*Math.log(1 + tmp*tmp);
			}
			return sum;
		}
	}

	/**
	 * Implementation of the smooth Cauchy loss gradient
	 */
	public static class Gradient extends LossFunctionGradient {
		public LossCauchy params;

		public Gradient( double threshold ) {
			this.params = new LossCauchy(threshold);
		}

		@Override public void process( double[] input, double[] output ) {
			final double threshold = params.alpha;

			for (int funcIdx = 0; funcIdx < numberOfFunctions; funcIdx++) {
				double r = input[funcIdx];
				double tmp = r/threshold;
				output[funcIdx] = 2.0*r/(1.0 + tmp*tmp);
			}
		}
	}
}
