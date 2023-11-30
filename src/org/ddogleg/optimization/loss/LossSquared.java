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
 * Squared error loss function. This is the typical least squares error function. Gradient isnt defined
 * since it will be set to null and that step skipped.
 *
 * @author Peter Abeles
 */
public class LossSquared extends LossFunctionBase implements LossFunction {
	@Override public double process( double[] input ) {
		// Avoid numerical overflow by ensuring values are around one
		double max = UtilOptimize.maxAbs(input, 0, numberOfFunctions);
		if (max == 0.0)
			return 0.0;

		double sum = 0.0;
		for (int i = 0; i < numberOfFunctions; i++) {
			double r = input[i]/max;
			sum += r*r;
		}

		return 0.5*sum*max*max;
	}
}
