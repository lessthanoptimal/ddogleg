/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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
	public double dampeningInitial =1e-4;

	/**
	 * Used to switch between Levenberg's and Marquardt's forumula. 1.0=levenberg 0.0=marquardt
	 */
	public double mixture=1e-4;

	public ConfigLevenbergMarquardt copy() {
		ConfigLevenbergMarquardt c = new ConfigLevenbergMarquardt();

		c.dampeningInitial = dampeningInitial;
		c.mixture = mixture;
		c.set(this);

		return c;
	}
}
