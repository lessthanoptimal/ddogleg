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


/**
 * Configuration for built in {@link org.ddogleg.optimization.loss.LossFunction Loss Functions}.
 */
public class ConfigLoss {
	/**
	 * Which loss function is to be created
	 */
	public Type type = Type.HUBER;

	/**
	 * If a parameter is needed by the implementation this value is used. Starting at 1 or 2 and increasing is
	 * often reasonable. By default, this is unassigned and needs to be assigned for most distrbutions.
	 */
	public double parameter = Double.NaN;

	public ConfigLoss( Type type, double parameter ) {
		this.type = type;
		this.parameter = parameter;
	}

	public ConfigLoss( Type type ) {
		this.type = type;
	}

	public ConfigLoss() {}

	public ConfigLoss setTo( ConfigLoss src ) {
		this.type = src.type;
		this.parameter = src.parameter;
		return this;
	}

	public void reset() {
		type = Type.HUBER;
		parameter = Double.NaN;
	}

	/**
	 * Returns true if it's configured to use a robust loss function. Anything but SQUARED is robust
	 */
	public boolean isRobust() {
		return type != Type.SQUARED;
	}

	public enum Type {
		/** @see org.ddogleg.optimization.loss.LossCauchy */
		CAUCHY,
		/** @see org.ddogleg.optimization.loss.LossHuber */
		HUBER,
		/** @see org.ddogleg.optimization.loss.LossHuberSmooth */
		HUBER_SMOOTH,
		/** @see org.ddogleg.optimization.loss.LossSquared */
		SQUARED,
		/** @see org.ddogleg.optimization.loss.LossTukey */
		TUKEY,
		/** @see org.ddogleg.optimization.loss.LossIRLS */
		IRLS,
	}
}
