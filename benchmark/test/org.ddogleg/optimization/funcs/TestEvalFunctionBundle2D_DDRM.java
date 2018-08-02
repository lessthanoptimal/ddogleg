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

package org.ddogleg.optimization.funcs;

import org.ddogleg.optimization.DerivativeChecker;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.wrap.SchurJacobian_to_NtoMxN;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestEvalFunctionBundle2D_DDRM {
	@Test
	public void compareToNumerical() {
		EvalFunctionBundle2D_DDRM eval = new EvalFunctionBundle2D_DDRM(234,20,10,2,3);

		FunctionNtoMxN<DMatrixRMaj> jac = new SchurJacobian_to_NtoMxN.DDRM(eval.getJacobianSchur());
		FunctionNtoM func = eval.getFunction();
		double[] params = eval.initial;

//		DerivativeChecker.jacobianPrintR(func, jac, params, 1e-3);
		assertTrue(DerivativeChecker.jacobianR(eval.getFunction(), jac, params, 1e-3));
	}
}