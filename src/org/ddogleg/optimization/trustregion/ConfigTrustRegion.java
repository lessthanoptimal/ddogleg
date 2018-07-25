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
	 * initial size of the trust region
	 */
	public double regionInitial = 1;

	/**
	 * Trust Region's maximum size
	 */
	public double regionMaximum = Double.MAX_VALUE;
	/**
	 * Trust Region's minimum size
	 */
	public double regionMinimum = 0;

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
	 * Minimum and maximum scaling possible. If max < min then scaling is turned off. Off by default.
	 */
	public double scalingMinimum =1, scalingMaximum =-1;

	public ConfigTrustRegion copy() {
		ConfigTrustRegion out = new ConfigTrustRegion();
		out.regionMaximum = regionMaximum;
		out.regionMinimum = regionMinimum;
		out.gtol = gtol;
		out.ftol = ftol;
		out.candidateAcceptThreshold = candidateAcceptThreshold;
		out.regionInitial = regionInitial;
		out.scalingMinimum = scalingMinimum;
		out.scalingMaximum = scalingMaximum;

		return out;
	}

}
