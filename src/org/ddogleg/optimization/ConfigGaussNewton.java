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

package org.ddogleg.optimization;

/**
 * Configuration for {@link GaussNewtonBase_F64}.
 *
 * @author Peter Abeles
 */
public class ConfigGaussNewton {
	/**
	 * tolerance for termination. magnitude of gradient. absolute
	 */
	public double gtol=1e-8;

	/**
	 * tolerance for termination, change in function value.  relative
	 */
	public double ftol=1e-12;

	/**
	 * Optional scaling of Jacobian to make the Hessian matrix better suited for decomposition by improving
	 * the matrice's condition
	 *
	 * scaling = sqrt(diag(B))  where B is the Hessian matrix.
	 *
	 * For Least-Squares B = J'*J
	 */
	public boolean hessianScaling =false;

	public void set( ConfigGaussNewton config ) {
		gtol = config.gtol;
		ftol = config.ftol;
		hessianScaling = config.hessianScaling;
	}
}
