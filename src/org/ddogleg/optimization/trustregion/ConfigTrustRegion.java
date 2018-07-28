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

	/**
	 * Random noise which is added to the state when certain conditions are meet.
	 * Set to a non-null value to turn on. If turned on then this will at worse double the number of
	 * score computations calls. For some problems it drastically reduce the number of iterations and
	 * enable the finding of a solution.
	 */
	public Noise noise = null;

	/**
	 * Describes when and how noise is added to the state estimate.
	 */
	public static class Noise {
		public long seed = 0xDEADBEEF;

		/**
		 * <p>Noise is added when "reduction < threshold"</p>
		 * <p>The score reduction is computed as follows:<br>
		 * reduction = (f_k-f_kp)/||p||<br>
		 * where f_k is the previous function value, f_kp is the current, and p is
		 * the change in state.
		 * </p>
		 * The default value 1e-10 is a conservative number. For some
		 * trust region variants 1e-4 seems to work better.
		 */
		public double thresholdReduction = 1e-5;

		/**
		 * Ammount of noise added. x[i] = x[i] + x[i]*normal(sigma)
		 */
		public double noiseSigma = 0.01;

		public Noise copy() {
			Noise n = new Noise();
			n.seed = seed;
			n.thresholdReduction = thresholdReduction;
			n.noiseSigma = noiseSigma;
			return n;
		}
	}

	public ConfigTrustRegion copy() {
		ConfigTrustRegion out = new ConfigTrustRegion();
		out.regionMaximum = regionMaximum;
		out.gtol = gtol;
		out.ftol = ftol;
		out.candidateAcceptThreshold = candidateAcceptThreshold;
		out.regionInitial = regionInitial;
		out.scalingMinimum = scalingMinimum;
		out.scalingMaximum = scalingMaximum;

		if( noise != null ) {
			out.noise = noise.copy();
		}

		return out;
	}

}
