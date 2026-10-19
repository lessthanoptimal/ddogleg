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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.grid.ConfigGridSplineND;

import java.util.HashMap;
import java.util.Map;

/// Implementation of [CROperatorBilinearND] where the knots are also splines which use linear interpolation.
///
/// A knot's coefficients are samples on a regular grid spanning `valueMin` to `valueMax`, so `coefs`
/// samples make `coefs - 1` buckets. Values outside that range are clamped to the nearest end rather than
/// extrapolated, which bounds what this returns no matter what it is handed.
@Accessors(chain = true, fluent = true)
public class CROperatorBilinearSplineND extends CROperatorBilinearND {

	/// The min and max expected values of `value`
	@Getter @Setter public double valueMin, valueMax;

	/// Which bucket the lower extent is in
	protected int bucket;

	/// The weight assigned to the upper end of the bucket. 0 to 1, inclusive
	protected double bucketFrac1;

	public CROperatorBilinearSplineND configure( ConfigGridSplineND grid ) {
		if (grid.grid.coefs < 2)
			throw new IllegalArgumentException("At least 2 knots are required for the spline/function");
		grid.checkValid();
		this.configureGrid(grid.grid);
		this.valueMin = grid.min;
		this.valueMax = grid.max;
		return this;
	}

	/// Values the specified polynomial encoded in the knot using `value`.
	@Override protected double evaluate( int knot, int polyOffset ) {
		CRArray<double[]> src = knots[knot];
		double bucketVal0 = src.d(polyOffset + bucket);
		double bucketVal1 = src.d(polyOffset + bucket + 1);
		return bucketVal0*(1.0 - bucketFrac1) + bucketVal1*bucketFrac1;
	}

	/// Piecewise linear, so this is the bucket's slope. Exactly zero where the value clamped, since
	/// moving further out changes nothing
	@Override protected double evaluateDerivative( int knot, int polyOffset ) {
		if (value <= valueMin || value >= valueMax)
			return 0;

		CRArray<double[]> src = knots[knot];
		double rise = src.d(polyOffset + bucket + 1) - src.d(polyOffset + bucket);
		return rise*(numCoefs - 1)/(valueMax - valueMin);
	}

	/// Only the two samples bracketing the value contribute, and they contribute their interpolation
	/// weights. The rest of the knot is untouched by this observation
	@Override protected void functionDerivatives( double[] derivatives ) {
		for (int k = 0; k < numCoefs; k++) {
			derivatives[k] = 0;
		}
		derivatives[bucket] = 1.0 - bucketFrac1;
		derivatives[bucket + 1] = bucketFrac1;
	}

	@Override protected void setValue( double value ) {
		super.setValue(value);

		double range = valueMax - valueMin;

		// coordinate is 0 to 1 in the allowed range
		double coordinate = (value - valueMin)/range;
		// bucket coordinate as a float, clamp to force it onto an allowed bucket.
		// Better than blowing up if out of range
		double bucketFloat = Math.min(1.0, Math.max(0.0, coordinate))*(numCoefs - 1);
		// the top sample starts no bucket of its own, so the last bucket has to own the upper edge
		bucket = Math.min((int)bucketFloat, numCoefs - 2);
		bucketFrac1 = bucketFloat - bucket;
	}

	@Override public Map<String, String> getSettings() {
		var map = new HashMap<>(super.getSettings());
		map.put("value_min", Double.toString(valueMin));
		map.put("value_max", Double.toString(valueMax));
		return map;
	}

	@Override public void setSettings( Map<String, String> settings ) {
		var remaining = new HashMap<>(settings);
		String min = remaining.remove("value_min");
		String max = remaining.remove("value_max");
		// the base rejects keys it does not know, so only its own are left for it
		super.setSettings(remaining);

		if (min != null)
			valueMin = Double.parseDouble(min);
		if (max != null)
			valueMax = Double.parseDouble(max);
	}
}
