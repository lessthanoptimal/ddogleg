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

package org.ddogleg.optimization.derivative;

import org.ddogleg.optimization.functions.FunctionStoS;
import org.ejml.UtilEjml;

/**
 * Finite difference numerical derivative calculation using the forward+backwards equation.
 * Difference equation, f'(x) = (f(x+h)-f(x-h))/(2*h).  Scaling is taken in account by h based
 * upon the magnitude of the elements in variable x.
 *
 * <p>
 * NOTE: If multiple input parameters are modified by the function when a single one is changed numerical
 * derivatives aren't reliable.
 * </p>
 *
 * @author Peter Abeles
 */
public class NumericalDerivativeFB implements FunctionStoS
{
	// function being differentiated
	private FunctionStoS function;

	// scaling of the difference parameter
	private double differenceScale;

	public NumericalDerivativeFB(FunctionStoS function, double differenceScale) {
		this.function = function;
		this.differenceScale = differenceScale;
	}

	public NumericalDerivativeFB(FunctionStoS function) {
		this(function,Math.sqrt(UtilEjml.EPS));
	}

	@Override
	public double process(double x) {

		double temp;
		double h = x != 0 ? differenceScale*Math.abs(x) : differenceScale;

		// backwards
		temp = x-h;
		double h0 = x-temp;
		double backwards = function.process(temp);

		// forward
		temp = x+h;
		double h1 = temp-x;
		double forwards = function.process(temp);

		return (forwards - backwards)/(h0+h1);
	}
}
