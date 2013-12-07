/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.functions.CoupledJacobian;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;

/**
 * Wrapper around {@link org.ddogleg.optimization.functions.FunctionNtoM} and {@link FunctionNtoMxN} for {@link org.ddogleg.optimization.functions.CoupledJacobian}.
 * 
 * @author Peter Abeles
 */
public class Individual_to_CoupledJacobian implements CoupledJacobian {
	
	FunctionNtoM func;
	FunctionNtoMxN jacobian;

	double[] x;
	
	public Individual_to_CoupledJacobian(FunctionNtoM func, FunctionNtoMxN jacobian) {
		if( func.getNumOfOutputsM() != jacobian.getNumOfOutputsM() )
			throw new IllegalArgumentException("M not equal");

		if( func.getNumOfInputsN() != jacobian.getNumOfInputsN() )
			throw new IllegalArgumentException("N not equal");

		this.func = func;
		this.jacobian = jacobian;
	}

	@Override
	public int getN() {
		return func.getNumOfInputsN();
	}

	@Override
	public int getM() {
		return func.getNumOfOutputsM();
	}

	@Override
	public void setInput(double[] x) {
		this.x = x;
	}

	@Override
	public void computeFunctions(double[] output) {
		func.process(x,output);
	}

	@Override
	public void computeJacobian(double[] jacobian) {
		this.jacobian.process(x,jacobian);
	}
}
