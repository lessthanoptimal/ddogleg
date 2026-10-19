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
import lombok.experimental.Accessors;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.ddogleg.chainrule.MathCRMatrixD;
import org.ddogleg.chainrule.grid.ConfigGridCoefND;

import java.util.List;
import java.util.Map;

import static org.ddogleg.chainrule.grid.ConfigGridCoefND.NUM_KNOTS;

/// Turns a pixel into the 4 bilinear weights of the cell it lands in, ready to be blended by
/// [CROperatorBilinearPolyND]. This is the half that knows how an image maps onto a grid.
///
/// The cell is worked out from the pixel. Whatever needs to fetch the matching knots reads it back through
/// [#col] and [#row], which is what the pre-forward hook does. A pixel outside the grid is not an error: the
/// cell clamps to the nearest real one, the fractions leave `[0,1]`, and the weights extrapolate.
///
/// The knots are ordered (col,row), (col+1,row), (col,row+1), (col+1,row+1)
///
/// @see ConfigGridCoefND
@Accessors(chain = true, fluent = true)
public class CROperatorGridBilinearWeights implements CROperatorBackwardsJac<double[]> {
	public static final int IN_PIXEL = 0;

	/// Size of the image the grid is spread across
	@Getter int width, height;

	/// Describes the grid, including how much of it lies outside the image
	@Getter final ConfigGridCoefND grid = new ConfigGridCoefND();

	/// Cell the weights are measured against, worked out from the pixel
	@Getter private int col, row;

	/// Pixel being sampled
	@Getter double pixelX, pixelY;

	/// Where the pixel sits inside the cell. Outside `[0,1]` when the pixel is outside the cell
	private double fracCol, fracRow;

	private final CRMatrix<double[]> workJac = CRMatrix.createD(NUM_KNOTS, 2);
	private final MathCRMatrixD math = new MathCRMatrixD();

	public CROperatorGridBilinearWeights configure( int width, int height, ConfigGridCoefND grid ) {
		grid.checkValid();

		this.width = width;
		this.height = height;
		this.grid.setTo(grid);

		return this;
	}

	private void locate() {
		double fcol = grid.pixelToGridX(pixelX, width);
		double frow = grid.pixelToGridY(pixelY, height);

		col = ConfigGridCoefND.cellIndex(fcol, grid.cols);
		row = ConfigGridCoefND.cellIndex(frow, grid.rows);

		fracCol = fcol - col;
		fracRow = frow - row;
	}

	@Override public void forward( CRArray<double[]> output ) {
		output.d(0, (1.0 - fracCol)*(1.0 - fracRow));
		output.d(1, fracCol*(1.0 - fracRow));
		output.d(2, (1.0 - fracCol)*fracRow);
		output.d(3, fracCol*fracRow);
	}

	/// The cell is fixed, so a weight only moves through the fractions, and those are linear in the pixel
	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		CRMatrix<double[]> output = jacobianInputs.get(IN_PIXEL);
		if (output.isInvalid())
			return;

		double dColdX = (grid.cols - 1 - 2*grid.padding)/(double)width;
		double dRowdY = (grid.rows - 1 - 2*grid.padding)/(double)height;

		workJac.reshape(NUM_KNOTS, 2);
		workJac.d(0, 0, -(1.0 - fracRow)*dColdX);
		workJac.d(0, 1, -(1.0 - fracCol)*dRowdY);
		workJac.d(1, 0, (1.0 - fracRow)*dColdX);
		workJac.d(1, 1, -fracCol*dRowdY);
		workJac.d(2, 0, -fracRow*dColdX);
		workJac.d(2, 1, (1.0 - fracCol)*dRowdY);
		workJac.d(3, 0, fracRow*dColdX);
		workJac.d(3, 1, fracCol*dRowdY);

		math.mult(dout, workJac, output);
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		if (which != IN_PIXEL)
			throw new IndexOutOfBoundsException("Unexpected input " + which);

		pixelX = input.d(0);
		pixelY = input.d(1);
		locate();
	}

	@Override public int getInputSize( int which ) {
		if (which != IN_PIXEL)
			throw new IndexOutOfBoundsException("Unexpected input " + which);
		return 2;
	}

	@Override public Map<String, String> getSettings() {
		return Map.of("width", "" + width, "height", "" + height,
				"cols", "" + grid.cols, "rows", "" + grid.rows, "padding", "" + grid.padding);
	}

	@Override public void setSettings( Map<String, String> settings ) {
		int[] v = {width, height, grid.cols, grid.rows, grid.padding};
		settings.forEach(( key, value ) -> {
			switch (key) {
				case "width" -> v[0] = Integer.parseInt(value);
				case "height" -> v[1] = Integer.parseInt(value);
				case "cols" -> v[2] = Integer.parseInt(value);
				case "rows" -> v[3] = Integer.parseInt(value);
				case "padding" -> v[4] = Integer.parseInt(value);
				default -> throw new IllegalArgumentException("Unknown key: " + key);
			}
		});
		// coefs and numOut belong to the blending operator, but the config checks them, so keep them legal
		configure(v[0], v[1], new ConfigGridCoefND()
				.cols(v[2]).rows(v[3]).coefs(grid.coefs > 0 ? grid.coefs : 1)
				.numOut(grid.numOut > 0 ? grid.numOut : 1).padding(v[4]));
	}

	@Override public int getInputCount() {return 1;}

	@Override public int getOutputSize() {return NUM_KNOTS;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
