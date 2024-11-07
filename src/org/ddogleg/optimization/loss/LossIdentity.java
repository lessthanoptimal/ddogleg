/*
 * Copyright (c) 2012-2024, Peter Abeles. All Rights Reserved.
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
 * This loss function simply passes through the computed "residual". Primary for debugging purposes.
 */
public class LossIdentity extends LossFunctionBase {
	public static class Function extends LossIdentity implements LossFunction {
		@Override public double process( double[] input ) {
			double max = UtilOptimize.maxAbs(input, 0, numberOfFunctions);
			if (max == 0.0)
				return 0.0;

			double sum = 0.0;
			for (int i = 0; i < numberOfFunctions; i++) {
				sum += input[i]/max;
			}
			return sum*max;
		}
	}

	public static class Gradient extends LossIdentity implements LossFunctionGradient {
		@Override public void process( double[] input, double[] output ) {
			for (int i = 0; i < numberOfFunctions; i++) {
				output[i] = 1.0;
			}
		}
	}
}
