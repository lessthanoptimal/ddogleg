/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.math;

import org.ejml.data.DMatrixRMaj;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;

/**
 * @author Peter Abeles
 */
public class TestHessianMath_DSCC extends StandardHessianMathChecks {
	public TestHessianMath_DSCC() {
		super(new HessianMath_DSCC(LinearSolverFactory_DSCC.cholesky(FillReducing.NONE)));
	}

	@Override
	protected void setHessian(HessianMath alg, DMatrixRMaj H) {
		DConvertMatrixStruct.convert(H, ((HessianMath_DSCC)alg).hessian);
	}
}