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

package org.ddogleg.optimization.trustregion;

import org.ddogleg.optimization.ConfigGaussNewton;

/**
 * Configuration parameters for {@link TrustRegionBase_F64 Trust Region}
 *
 * @author Peter Abeles
 */
public class ConfigTrustRegion extends ConfigGaussNewton {
	/**
	 * Initial size of the trust region. Automatic and manual methods are available. There is no universally
	 * best way to select the region size and the default method is the more conservative automatic Cauchy.
	 *
	 * <ul>
	 *     <li>
	 *         If a positive number then that's the initial region size. 0.11 to 1000 is often a reasonable initial
	 *         value for the region size. Starting at 1 is recommended.
	 *     </li>
	 *     <li>
	 *         If set to -1 then it will perform a step with a trust region of MAX_VALUE and then set the trust
	 *         region to that result. This works very well for may problems but some times it will jump too far and
	 *         get stuck. If that happens auto-initialization with -2 will probably work.
	 *     </li>
	 *     <li>
	 *        If set to -2 then it will compute the length of a Cauchy step and use that as the initial value. This
	 *        tends to be a conservative method.
	 *     </li>
	 * </ul>
	 */
	public double regionInitial = -2;

	/**
	 * Trust Region's maximum size
	 */
	public double regionMaximum = Double.MAX_VALUE;

	@Deprecated
	public ConfigTrustRegion copy() {
		ConfigTrustRegion out = new ConfigTrustRegion();
		out.regionInitial = regionInitial;
		out.regionMaximum = regionMaximum;
		out.setTo(this);

		return out;
	}

	public ConfigTrustRegion setTo( ConfigTrustRegion src ) {
		super.setTo(src);
		this.regionInitial = src.regionInitial;
		this.regionMaximum = src.regionMaximum;
		return this;
	}

	@Override public void reset() {
		super.reset();
		this.regionInitial = -2;
		this.regionMaximum = Double.MAX_VALUE;
	}
}
