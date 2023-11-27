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

package org.ddogleg.optimization;

import org.ddogleg.optimization.loss.*;

/**
 * Factory for creating different {@link LossFunction} and {@link LossFunctionGradient}.
 */
@SuppressWarnings("NullAway")
public class FactoryLossFunctions {
	/**
	 * Create the loss function from a config.
	 */
	public static Funcs general( ConfigLoss config ) {
		return switch (config.type) {
			case CAUCHY -> cauchy(ensureNotNaN(config.parameter));
			case HUBER -> huber(ensureNotNaN(config.parameter));
			case HUBER_SMOOTH -> huberSmooth(ensureNotNaN(config.parameter));
			case TUKEY -> tukey(ensureNotNaN(config.parameter));
			case SQUARED -> squared();
			case IRLS -> throw new IllegalArgumentException("Not supported in this factory. " +
					"You need to create the weight function yourself");
		};
	}

	private static double ensureNotNaN(double value) {
		if (Double.isNaN(value))
			throw new IllegalArgumentException("You must specify the parameter");
		return value;
	}

	/**
	 * @see LossCauchy
	 */
	public static Funcs cauchy( double alpha ) {
		var funcs = new Funcs();
		funcs.function = new LossCauchy.Function(alpha);
		funcs.gradient = new LossCauchy.Gradient(alpha);
		return funcs;
	}

	/**
	 * @see LossHuber
	 */
	public static Funcs huber( double threshold ) {
		var funcs = new Funcs();
		funcs.function = new LossHuber.Function(threshold);
		funcs.gradient = new LossHuber.Gradient(threshold);
		return funcs;
	}

	/**
	 * @see LossHuberSmooth
	 */
	public static Funcs huberSmooth( double threshold ) {
		var funcs = new Funcs();
		funcs.function = new LossHuberSmooth.Function(threshold);
		funcs.gradient = new LossHuberSmooth.Gradient(threshold);
		return funcs;
	}

	/**
	 * @see LossTukey
	 */
	public static Funcs tukey( double threshold ) {
		var funcs = new Funcs();
		funcs.function = new LossTukey.Function(threshold);
		funcs.gradient = new LossTukey.Gradient(threshold);
		return funcs;
	}

	/**
	 * @see LossSquared
	 */
	public static Funcs squared() {
		var funcs = new Funcs();
		funcs.function = new LossSquared();
		funcs.gradient = null;
		return funcs;
	}

	public static class Funcs {
		public LossFunction function;
		public LossFunctionGradient gradient;
	}
}
