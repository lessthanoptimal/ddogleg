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

import org.ddogleg.optimization.lm.ConfigLevenbergMarquardt;
import org.ddogleg.optimization.trustregion.ConfigTrustRegion;

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

	/**
	 * Should it use a robust linear solver internally. This can help it solve degenerate problems. Not always
	 * available in every implementation.
	 */
	public boolean robustSolver = false;

	public void reset() {
		type = Type.LEVENBERG_MARQUARDT;
		trust.reset();
		lm.reset();
		robustSolver = false;
	}

	public ConfigNonLinearLeastSquares setTo( ConfigNonLinearLeastSquares src ) {
		this.type = src.type;
		this.trust.setTo(src.trust);
		this.lm.setTo(src.lm);
		this.robustSolver = src.robustSolver;
		return this;
	}

	public enum Type {
		TRUST_REGION,
		LEVENBERG_MARQUARDT,
	}
}
