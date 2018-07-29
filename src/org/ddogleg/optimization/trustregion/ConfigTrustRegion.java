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

package org.ddogleg.optimization.trustregion;

/**
 * Configuration parameters for {@link TrustRegionBase_F64 Trust Region}
 *
 * @author Peter Abeles
 */
public class ConfigTrustRegion {
	/**
	 * Initial size of the trust region. If a negative value is set attempt is made to automatically determine
	 * a good initial size. If a positive number then that's the initial region size. 100 is often a reasonable initial
	 * value for the region size.
	 */
	public double regionInitial = -1;

	/**
	 * Trust Region's maximum size
	 */
	public double regionMaximum = Double.MAX_VALUE;

	/**
	 * tolerance for termination. magnitude of gradient. absolute
	 */
	public double gtol=1e-8;

	/**
	 * tolerance for termination, change in function value.  relative
	 */
	public double ftol=1e-12;

	/**
	 * if the prediction ratio his higher than this threshold it is accepted
	 */
	public double candidateAcceptThreshold = 0.05;

	/**
	 * <p>
	 *     Optional dynamic scaling of variables is possible at each iteration. The scale factor is set using the
	 *     Hessian's diagonal elements. The square root of the diagonal elements absolute value.
	 * </p>
	 *
	 * <p>
	 *     These variables are used to clamp scaling individual scaling values. To turn on this automatic scaling
	 *     simply set the minimum value such that it is less than the maximum value.
	 *     The minimum value seems to be of particular importance and don't forget try larger values, such as one.
	 * </p>
	 *
	 * <p>Recommended initial tuning values are min=1e-5 and max=1e5</p>
	 */
	public double scalingMinimum =1, scalingMaximum =-1;

	public ConfigTrustRegion copy() {
		ConfigTrustRegion out = new ConfigTrustRegion();
		out.regionMaximum = regionMaximum;
		out.gtol = gtol;
		out.ftol = ftol;
		out.candidateAcceptThreshold = candidateAcceptThreshold;
		out.regionInitial = regionInitial;
		out.scalingMinimum = scalingMinimum;
		out.scalingMaximum = scalingMaximum;

		return out;
	}

}
