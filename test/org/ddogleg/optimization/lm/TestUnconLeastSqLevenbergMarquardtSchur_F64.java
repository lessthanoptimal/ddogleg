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

package org.ddogleg.optimization.lm;

import org.ddogleg.optimization.UnconstrainedLeastSquaresSchur;
import org.ddogleg.optimization.impl.CommonChecksUnconstrainedLeastSquaresSchur_DSCC;
import org.ddogleg.optimization.math.HessianSchurComplement_DSCC;
import org.ddogleg.optimization.math.MatrixMath_DSCC;
import org.ejml.data.DMatrixSparseCSC;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
public class TestUnconLeastSqLevenbergMarquardtSchur_F64 {
	@Test
	public void computeGradientHessian() {
		fail("Implement");
	}

	@Test
	public void computeResiduals() {
		fail("Implement");
	}

	@Nested
	class LeastSquaresDSCC extends CommonChecksUnconstrainedLeastSquaresSchur_DSCC {
		@Override
		protected UnconstrainedLeastSquaresSchur<DMatrixSparseCSC> createSearch(double minimumValue) {
			ConfigLevenbergMarquardt config = new ConfigLevenbergMarquardt();

			HessianSchurComplement_DSCC hessian = new HessianSchurComplement_DSCC();
			UnconLeastSqLevenbergMarquardtSchur_F64<DMatrixSparseCSC> lm =
					new UnconLeastSqLevenbergMarquardtSchur_F64<>(new MatrixMath_DSCC(),hessian);
			lm.configure(config);
//			lm.setVerbose(true);
			return lm;
		}
	}
}