/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.chainrule.ops;

import lombok.experimental.Accessors;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.grid.ConfigGridCoefND;

/// Implementation of [CROperatorBilinearND] where the knots are general polynomials.
@Accessors(chain = true, fluent = true)
public class CROperatorBilinearPolyND extends CROperatorBilinearND {

	public CROperatorBilinearPolyND configure( ConfigGridCoefND grid ) {
		grid.checkValid();
		this.configureGrid(grid);
		return this;
	}

	/// Values the specified polynomial encoded in the knot using `value`.
	@Override protected double evaluate( int knot, int polyOffset ) {
		CRArray<double[]> src = knots[knot];
		double result = src.d(polyOffset);
		double pow = value;
		for (int i = 1; i < numCoefs; i++) {
			result += src.d(polyOffset + i)*pow;
			pow *= value;
		}
		return result;
	}

	/// A coefficient's derivative is its power of `value`
	@Override protected void functionDerivatives( double[] derivatives ) {
		double pow = 1.0;
		for (int k = 0; k < numCoefs; k++) {
			derivatives[k] = pow;
			pow *= value;
		}
	}

	/// Derivative of [#evaluate] with respect to `value`
	@Override protected double evaluateDerivative( int knot, int polyOffset ) {
		CRArray<double[]> src = knots[knot];
		double result = 0;
		double pow = 1.0;
		for (int i = 1; i < numCoefs; i++) {
			result += i*src.d(polyOffset + i)*pow;
			pow *= value;
		}
		return result;
	}
}
