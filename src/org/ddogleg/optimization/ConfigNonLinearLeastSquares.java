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

package org.ddogleg.optimization;

import org.ddogleg.optimization.lm.ConfigLevenbergMarquardt;
import org.ddogleg.optimization.trustregion.ConfigTrustRegion;
import org.ejml.LinearSolverType;

/**
 * General configuration for unconstrained non-linear least squares solvers.
 */
public class ConfigNonLinearLeastSquares {
	/**
	 * Which type of solver should it use
	 */
	public Type type = Type.LEVENBERG_MARQUARDT;

	public ConfigTrustRegion trust = new ConfigTrustRegion();
	public ConfigLevenbergMarquardt lm = new ConfigLevenbergMarquardt();

	/// Returns the type of linear solver that has been selected based on non-linear solver type
	public LinearSolverType getLinearSolverType() {
		return switch (type) {
			case LEVENBERG_MARQUARDT -> lm.solverType;
			case TRUST_REGION -> lm.solverType;
		};
	}

	public void reset() {
		type = Type.LEVENBERG_MARQUARDT;
		trust.reset();
		lm.reset();
	}

	public ConfigNonLinearLeastSquares setTo( ConfigNonLinearLeastSquares src ) {
		this.type = src.type;
		this.trust.setTo(src.trust);
		this.lm.setTo(src.lm);
		return this;
	}

	public enum Type {
		TRUST_REGION,
		LEVENBERG_MARQUARDT,
	}
}
