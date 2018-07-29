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


import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedOptimization;
import org.junit.jupiter.api.Nested;

/**
 * @author Peter Abeles
 */
public class TestTrustRegionUpdateDoglegBFGS_F64 {
	@Nested
	class UnconstrainedBFGS extends CommonChecksUnconstrainedOptimization {
		public UnconstrainedBFGS() {
			this.checkFastConvergence = false; // TODO remove?
			this.maxIteration = 100000;
		}

		@Override
		protected UnconstrainedMinimization createSearch() {
			ConfigTrustRegion config = new ConfigTrustRegion();

			TrustRegionUpdateDogleg_F64 alg = new TrustRegionUpdateDoglegBFGS_F64();
			UnconMinTrustRegionBFGS_F64 tr = new UnconMinTrustRegionBFGS_F64(alg);
			tr.configure(config);
			return tr;
		}
	}

	@Nested
	class UnconstrainedBFGS_Scaling extends CommonChecksUnconstrainedOptimization {
		public UnconstrainedBFGS_Scaling() {
			this.checkFastConvergence = false; // TODO remove?
			this.maxIteration = 100000;
		}

		@Override
		protected UnconstrainedMinimization createSearch() {
			ConfigTrustRegion config = new ConfigTrustRegion();
			config.scalingMinimum = 2;
			config.scalingMaximum = 1e6;
			// oddly sensitive to this parameter. This is just a test to see if scaling is handled correctly not a
			// robustness test so I'm fine with that.

			TrustRegionUpdateDogleg_F64 alg = new TrustRegionUpdateDoglegBFGS_F64();
			UnconMinTrustRegionBFGS_F64 tr = new UnconMinTrustRegionBFGS_F64(alg);
			tr.configure(config);
			return tr;
		}
	}
}