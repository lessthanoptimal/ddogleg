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

package org.ddogleg.optimization.impl;

import org.ddogleg.optimization.functions.CoupledJacobian;
import org.ejml.data.DMatrix;

/**
 * {@link LevenbergBase} with a {@link CoupledJacobian}
 *
 * @author Peter Abeles
 */
public abstract class LevenbergFuncBase<S extends DMatrix> extends LevenbergBase {

	// Least-squares Function being optimized
	protected CoupledJacobian<S> function;


	public LevenbergFuncBase(double initialDampParam) {
		super(initialDampParam);
	}

	@Override
	protected void setFunctionParameters(double[] param) {
		function.setInput(param);
	}

	@Override
	protected void computeResiduals(double[] output) {
		function.computeFunctions(output);
	}

	public abstract void setFunction( CoupledJacobian<S> function );
}
