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
import lombok.Setter;
import lombok.experimental.Accessors;
import org.ddogleg.chainrule.ops.CROperatorBilinearPolyND;
import org.ddogleg.chainrule.ops.CROperatorGridBilinearWeights;
import org.ejml.MapFormattable;
import org.ejml.MapPrintFormat;
import org.ejml.MapPrintReflect;

/// Configuration for a polynomial grid where each cell in the grid has a `numOut` polynomials with `coefs`
/// coefficients each.
///
/// Padding says how many of the outermost rows and columns sit outside the image. The totals do not change,
/// so neither does [#dof] or where anything lives in the parameter array. What changes is the coordinate
/// transform: the interior shrinks to `cols - 2*padding` knots and the cells grow to match, which buys
/// coverage for points outside the image at the cost of resolution inside it.
///
/// @see org.ddogleg.chainrule.ops.CROperatorBilinearPolyND
/// @see org.ddogleg.chainrule.ops.CROperatorGridBilinearWeights
@Accessors(chain = true, fluent = true)
@Getter @Setter
public class ConfigGridCoefND implements MapFormattable  {
	/// How many weights are produced, one per knot of the cell
	public static final int NUM_KNOTS = 4;

	/// Number of columns and rows in the grid, padding included
	public int cols, rows;
	/// Number of coefficients in each output polynomial
	public int coefs;
	/// Number of outputs for cell
	public int numOut;
	/// How many of the outermost rows and columns lie outside the image
	public int padding;

	public ConfigGridCoefND() {reset();}

	/// Makes sure valid values have been entered for all parameters. Two knots per axis is the minimum a
	/// bilinear read needs, which is what every consumer here does
	public void checkValid() {
		checkValid(2);
	}

	/// [#checkValid] with the per-axis knot minimum given. Bilinear needs 2, but a nearest-neighbor field
	/// reads a single knot and so can run on a 1x1 grid
	public void checkValid( int minAxis ) {
		if (!(cols >= minAxis))
			throw new IllegalArgumentException("there must be at least " + minAxis + " columns");
		if (!(rows >= minAxis))
			throw new IllegalArgumentException("there must be at least " + minAxis + " rows");
		if (!(coefs >= 0))
			throw new IllegalArgumentException("coefficients can't be negative; 0 disables the grid");
		if (!(numOut >= 1))
			throw new IllegalArgumentException("Output must be at least 1-DOF");
		if (!(padding >= 0))
			throw new IllegalArgumentException("padding can't be negative");
		if (!(cols - 2*padding >= minAxis))
			throw new IllegalArgumentException("padding leaves fewer than " + minAxis + " columns inside the image");
		if (!(rows - 2*padding >= minAxis))
			throw new IllegalArgumentException("padding leaves fewer than " + minAxis + " rows inside the image");
	}

	/// Resets parameters to an invalid value so you will know if they have been initialled
	public ConfigGridCoefND reset() {
		cols = rows = coefs = numOut = -1;
		padding = 0;
		return this;
	}

	public ConfigGridCoefND setTo( ConfigGridCoefND src ) {
		this.cols = src.cols;
		this.rows = src.rows;
		this.coefs = src.coefs;
		this.numOut = src.numOut;
		this.padding = src.padding;
		return this;
	}

	/// True if every field matches exactly
	public boolean isIdentical( ConfigGridCoefND o ) {
		return cols == o.cols && rows == o.rows && coefs == o.coefs &&
				numOut == o.numOut && padding == o.padding;
	}

	/// Number of parameters needed to describe this polynomial grid
	public int dof() {
		return cols*rows*coefs*numOut;
	}

	/// How many parameters one knot holds
	public int knotStride() {
		return coefs*numOut;
	}

	/// Fractional grid coordinate of a pixel, where a whole number is a knot. `0` and `width` land on the
	/// first and last knot inside the image, which padding moves inwards
	public double pixelToGridX( double pixelX, int width ) {
		return padding + (cols - 1 - 2*padding)*pixelX/width;
	}

	/// @see #pixelToGridX
	public double pixelToGridY( double pixelY, int height ) {
		return padding + (rows - 1 - 2*padding)*pixelY/height;
	}

	/// Cell a fractional grid coordinate falls in. The last knot along an axis starts no cell of its own,
	/// and a coordinate outside the grid is given the nearest cell to extrapolate from
	public static int cellIndex( double gridCoor, int count ) {
		return Math.max(0, Math.min((int)Math.floor(gridCoor), count - 2));
	}

	/// Knot a pixel is nearest to when the axis tiles the image as `count` equal bands rather than a
	/// bilinear lattice of corner knots: band `k` owns `[k*size/count, (k+1)*size/count)`, so the knot
	/// sits at the band's center. Unlike [#cellIndex] every knot owns a band and the result spans
	/// `[0, count-1]`. Padding plays no part; the bands cover the whole image
	public static int nearestIndex( double pixel, int count, int size ) {
		return Math.max(0, Math.min((int)(pixel*count/size), count - 1));
	}

	/// Where each of a cell's four knots starts in the parameter array
	///
	/// @param col Column of the cell's low corner
	/// @param row Row of the cell's low corner
	/// @param offset Where this grid's parameters start in the array
	/// @param knotOffsets Output, the four locations in the order [org.ddogleg.chainrule.ops.CROperatorGridBilinearWeights] expects
	public void knotOffsets( int col, int row, int offset, int[] knotOffsets ) {
		int stride = knotStride();
		knotOffsets[0] = offset + (row*cols + col)*stride;
		knotOffsets[1] = offset + (row*cols + col + 1)*stride;
		knotOffsets[2] = offset + ((row + 1)*cols + col)*stride;
		knotOffsets[3] = offset + ((row + 1)*cols + col + 1)*stride;
	}

	@Override public String formatMap( MapPrintFormat format ) {
		return MapPrintReflect.formatMap(this, format);
	}

	@Override public String toString() { return MapPrintFormat.DEFAULT.toString(this); }
}
