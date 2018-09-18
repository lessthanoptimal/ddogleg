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

package org.ddogleg.optimization.quasinewton;

/**
 * Configuration for {@link QuasiNewtonBFGS}
 *
 * @author Peter Abeles
 */
public class ConfigQuasiNewton {

	/**
	 * Specifies which line search algorithm to use
	 */
	public LineSearch lineSearch = LineSearch.MORE94;

	/**
	 * ftol convergence test. 0 {@code <=} ftol {@code <=} 1
	 */
	public double ftol=1e-12;

	/**
	 * gtol convergence test. 0 {@code <=} gtol
	 */
	public double gtol=1e-12;

	/**
	 * gtol convergence for line search. 0 {@code <} lineGTol
	 */
	public double lineGTol=1e-12;

	public enum LineSearch {
		/**
		 * {@link LineSearchFletcher86}
		 */
		FLETCHER86,
		/**
		 * {@link LineSearchMore94}
		 */
		MORE94
	}
}
