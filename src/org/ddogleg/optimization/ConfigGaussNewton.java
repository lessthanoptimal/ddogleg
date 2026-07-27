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

import org.ejml.LinearSolverType;

/// Configuration for [GaussNewtonBase_F64].
public class ConfigGaussNewton {
	/// tolerance for termination. magnitude of gradient. absolute
	public double gtol = 1e-8;

	/// tolerance for termination, change in function value. relative
	public double ftol = 1e-12;

	/// Optional scaling of Jacobian to make the Hessian matrix better suited for decomposition by improving
	/// the matrice's condition
	/// scaling = sqrt(diag(B))  where B is the Hessian matrix.
	/// For Least-Squares B = J'\*J
	public boolean hessianScaling = false;

	/// Which linear solver should it use internally. Default will be what most people will use, but other choices
	/// could be faster or more stable. If the specific implementation does not use a linear solver this must
	/// be default.
	public LinearSolverType solverType = LinearSolverType.DEFAULT;

	/// Checks to see if the config is valid and throws an excepton if not
	public void checkConfig() {
		checkFtol(ftol);
		if (gtol < 0)
			throw new RuntimeException("gtol can't be negative");
	}

	public static void checkFtol( double ftol ) {
		if (ftol < 0 || ftol > 1)
			throw new RuntimeException("ftol is relative and must be 0 to 1");
	}

	public ConfigGaussNewton setTo( ConfigGaussNewton config ) {
		gtol = config.gtol;
		ftol = config.ftol;
		hessianScaling = config.hessianScaling;
		solverType = config.solverType;
		return this;
	}

	public void reset() {
		gtol = 1e-8;
		ftol = 1e-12;
		hessianScaling = false;
		solverType = LinearSolverType.DEFAULT;
	}
}
