/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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
import org.ejml.LinearSolverType;

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

	/// Which linear solver should it use internally. Default will be what most people will use, but other choices
	/// could be faster or more stable.
	public LinearSolverType solverType = LinearSolverType.DEFAULT;

	@Deprecated
	public ConfigLevenbergMarquardt copy() {
		return new ConfigLevenbergMarquardt().setTo(this);
	}

	public ConfigLevenbergMarquardt setTo( ConfigLevenbergMarquardt src ) {
		super.setTo(src);
		this.dampeningInitial = src.dampeningInitial;
		this.mixture = src.mixture;
		this.diagonalMin = src.diagonalMin;
		this.diagonalMax = src.diagonalMax;
		this.solverType = src.solverType;
		return this;
	}

	@Override public void reset() {
		super.reset();
		this.dampeningInitial = 1e-4;
		this.mixture = 1e-4;
		this.diagonalMin = 1e-6;
		this.diagonalMax = 1e32;
		this.solverType = LinearSolverType.DEFAULT;
	}
}
