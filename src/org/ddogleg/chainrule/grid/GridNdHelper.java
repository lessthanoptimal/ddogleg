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

package org.ddogleg.chainrule.grid;

import lombok.Getter;
import org.ddogleg.chainrule.ops.CROperatorBilinearSplineND;

/// Helper/utility class for different operations on a [GridCoefND]
public class GridNdHelper {
	/// Input image shape
	@Getter public int width, height;

	/// The grid
	@SuppressWarnings("NullAway.Init") @Getter GridCoefND grid;

	/// Grid coordinate point is inside
	@Getter public int col, row;
	/// Fractional coordinate inside the grid cell
	@Getter public double fracCol, fracRow;

	/// Blending weights for bilinear interpolation
	@Getter public double w0, w1, w2, w3;

	/// Location of knot offsets
	@Getter public int[] knotOffsets = new int[4];

	/// Range the spline's samples are spread across, evenly. Only the `*Spline` methods read it
	@Getter public double valueMin, valueMax;

	//------------- Internal
	@SuppressWarnings("NullAway.Init") private ConfigGridCoefND config;

	public void configure( GridCoefND grid, int width, int height ) {
		this.grid = grid;
		this.width = width;
		this.height = height;
		this.config = grid.config;
	}

	/// Range the `*Spline` methods spread a knot's coefficients across. The grid itself says nothing
	/// about it, so whatever configured the fit has to say it again here or the samples are read at
	/// the wrong places
	public GridNdHelper configureSpline( double valueMin, double valueMax ) {
		if (valueMax <= valueMin)
			throw new IllegalArgumentException("valueMax must be greater than valueMin");
		this.valueMin = valueMin;
		this.valueMax = valueMax;
		return this;
	}

	public void coorToGrid( double x, double y ) {
		double fcol = config.pixelToGridX(x, width);
		double frow = config.pixelToGridY(y, height);

		col = ConfigGridCoefND.cellIndex(fcol, config.cols);
		row = ConfigGridCoefND.cellIndex(frow, config.rows);

		fracCol = fcol - col;
		fracRow = frow - row;
	}

	/// Bilinear weights for the cell found by [#coorToGrid], along with where that cell's knots live.
	/// Both depend only on the point, so [#blendPoly] can be called once per output without redoing them
	public void gridToWeights() {
		w0 = (1.0 - fracCol)*(1.0 - fracRow);
		w1 = fracCol*(1.0 - fracRow);
		w2 = (1.0 - fracCol)*fracRow;
		w3 = fracCol*fracRow;

		config.knotOffsets(col, row, 0, knotOffsets);
	}

	/// Evaluates a polynomial which starts at the offset
	public double evaluatePoly( double value, int offset ) {
		double result = grid.parameters[offset];
		double pow = value;
		for (int i = 1; i < config.coefs; i++) {
			result += grid.parameters[offset + i]*pow;
			pow *= value;
		}
		return result;
	}

	/// Derivative of [#evaluatePoly] with respect to the value
	public double evaluatePolyDeriv( double value, int offset ) {
		double result = 0.0;
		double pow = 1.0;
		for (int i = 1; i < config.coefs; i++) {
			result += i*grid.parameters[offset + i]*pow;
			pow *= value;
		}
		return result;
	}

	/// Evaluates a spline which starts at the offset. The coefficients are samples spread evenly from
	/// [#valueMin] to [#valueMax], so `coefs` of them make `coefs - 1` buckets, and a value outside
	/// that range is clamped to the nearest end rather than extrapolated
	///
	/// @see org.ddogleg.chainrule.ops.CROperatorBilinearSplineND
	public double evaluateSpline( double value, int offset ) {
		double coordinate = (value - valueMin)/(valueMax - valueMin);
		double bucketFloat = Math.min(1.0, Math.max(0.0, coordinate))*(config.coefs - 1);
		// the top sample starts no bucket of its own, so the last bucket has to own the upper edge
		int bucket = Math.min((int)bucketFloat, config.coefs - 2);
		double frac1 = bucketFloat - bucket;

		return grid.parameters[offset + bucket]*(1.0 - frac1) +
				grid.parameters[offset + bucket + 1]*frac1;
	}

	/// Blends the four knots for one output. The knots are the same for every output of a point, so
	/// their locations are found once by [#gridToWeights] and only stepped along here
	public double blendPoly( double value, int outputIdx ) {
		int polyOffset = outputIdx*config.coefs;

		double v0 = evaluatePoly(value, knotOffsets[0] + polyOffset);
		double v1 = evaluatePoly(value, knotOffsets[1] + polyOffset);
		double v2 = evaluatePoly(value, knotOffsets[2] + polyOffset);
		double v3 = evaluatePoly(value, knotOffsets[3] + polyOffset);

		return w0*v0 + w1*v1 + w2*v2 + w3*v3;
	}

	/// Derivative of [#blendPoly] with respect to the value
	public double blendPolyDeriv( double value, int outputIdx ) {
		int polyOffset = outputIdx*config.coefs;

		double v0 = evaluatePolyDeriv(value, knotOffsets[0] + polyOffset);
		double v1 = evaluatePolyDeriv(value, knotOffsets[1] + polyOffset);
		double v2 = evaluatePolyDeriv(value, knotOffsets[2] + polyOffset);
		double v3 = evaluatePolyDeriv(value, knotOffsets[3] + polyOffset);

		return w0*v0 + w1*v1 + w2*v2 + w3*v3;
	}

	/// [#blendPoly] with the knots read as splines instead
	public double blendSpline( double value, int outputIdx ) {
		int polyOffset = outputIdx*config.coefs;

		double v0 = evaluateSpline(value, knotOffsets[0] + polyOffset);
		double v1 = evaluateSpline(value, knotOffsets[1] + polyOffset);
		double v2 = evaluateSpline(value, knotOffsets[2] + polyOffset);
		double v3 = evaluateSpline(value, knotOffsets[3] + polyOffset);

		return w0*v0 + w1*v1 + w2*v2 + w3*v3;
	}
}
