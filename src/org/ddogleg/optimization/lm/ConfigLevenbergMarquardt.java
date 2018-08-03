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

/**
 * @author Peter Abeles
 */
public class ConfigLevenbergMarquardt {

	public double lambdaInitial;

	/**
	 * tolerance for termination. magnitude of gradient. absolute
	 */
	public double gtol=1e-8;

	/**
	 * tolerance for termination, change in function value.  relative
	 */
	public double ftol=1e-12;

	public ConfigLevenbergMarquardt copy() {
		ConfigLevenbergMarquardt c = new ConfigLevenbergMarquardt();

		return c;
	}
}