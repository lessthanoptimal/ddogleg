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

package org.ddogleg.optimization.lm;

import org.ddogleg.optimization.ConfigGaussNewton;

/**
 * Configuration for {@link LevenbergMarquardt_F64}
 *
 * @author Peter Abeles
 */
public class ConfigLevenbergMarquardt extends ConfigGaussNewton {

	/**
	 * Initial value for the dampening parameter.
	 */
	public double dampeningInitial = 1e-4;

	/**
	 * Used to switch between Levenberg's and Marquardt's formula. 1.0=levenberg 0.0=marquardt
	 */
	public double mixture = 1e-4;

	/**
	 * Clamps the diagonal values of J'*J when constructing the LM formula.
	 */
	public double diagonalMin = 1e-6, diagonalMax = 1e32;

	@Deprecated
	public ConfigLevenbergMarquardt copy() {
		var c = new ConfigLevenbergMarquardt();

		c.dampeningInitial = dampeningInitial;
		c.mixture = mixture;
		c.diagonalMin = diagonalMin;
		c.diagonalMax = diagonalMax;
		c.setTo(this);

		return c;
	}

	public ConfigLevenbergMarquardt setTo( ConfigLevenbergMarquardt src ) {
		super.setTo(src);
		this.dampeningInitial = src.dampeningInitial;
		this.mixture = src.mixture;
		this.diagonalMin = src.diagonalMin;
		this.diagonalMax = src.diagonalMax;
		return this;
	}

	@Override public void reset() {
		super.reset();
		this.dampeningInitial = 1e-4;
		this.mixture = 1e-4;
		this.diagonalMin = 1e-6;
		this.diagonalMax = 1e32;
	}
}
