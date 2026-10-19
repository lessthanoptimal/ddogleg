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

import org.ddogleg.DDoglegJUnit;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CROperatorForward;
import org.ddogleg.chainrule.CRTestHelpers;
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac.JacobianCase;
import org.ddogleg.chainrule.CommonCROperatorBackwardsJac;
import org.ddogleg.chainrule.CommonCROperatorForward;
import org.ddogleg.chainrule.grid.ConfigGridCoefND;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.ddogleg.chainrule.grid.ConfigGridCoefND.NUM_KNOTS;
import static org.ddogleg.chainrule.ops.CROperatorGridBilinearWeights.IN_PIXEL;
import static org.junit.jupiter.api.Assertions.*;

public class TestCROperatorGridBilinearWeights extends DDoglegJUnit {
	@TestFactory Stream<DynamicTest> forward() {
		return new CommonCROperatorForward(this::createVariants, rand) {
			@Override public CRArray<double[]> randomInput( int which, CROperatorForward<double[]> alg ) {
				return TestCROperatorGridBilinearWeights.this.randomInput(which, alg);
			}
		}.tests();
	}

	static final int W = 400, H = 300;

	List<CROperatorForward<double[]>> createVariants() {
		return List.of(create(3, 3, 0), create(2, 2, 0), create(5, 4, 1), create(6, 6, 2));
	}

	static CROperatorGridBilinearWeights create( int cols, int rows, int padding ) {
		var alg = new CROperatorGridBilinearWeights();
		alg.configure(W, H, new ConfigGridCoefND().cols(cols).rows(rows).coefs(1).numOut(1).padding(padding));
		return alg;
	}

	/// Any pixel is legal, so the shared checks can use the whole image
	CRArray<double[]> randomInput( int which, CROperatorForward<double[]> alg ) {
		if (which == IN_PIXEL)
			return new CRArray<>(new double[]{W*rand.nextDouble(), H*rand.nextDouble()}, 0);
		return CRTestHelpers.randomCRArray(alg.getInputSize(which), rand);
	}

	static void setPixel( CROperatorGridBilinearWeights alg, double x, double y ) {
		alg.setInput(IN_PIXEL, new CRArray<>(new double[]{x, y}, 0));
	}

	@Test void shape() {
		var alg = create(4, 3, 0);
		assertEquals(1, alg.getInputCount());
		assertEquals(2, alg.getInputSize(IN_PIXEL));
		assertEquals(NUM_KNOTS, alg.getOutputSize());
	}

	/// Bilinear weights are a partition of unity inside a cell, whatever the pixel
	@Test void weightsSumToOne() {
		var alg = create(5, 4, 0);
		var found = new CRArray<>(new double[NUM_KNOTS], 0);

		for (double[] p : new double[][]{{0, 0}, {W, H}, {123.4, 45.6}, {W/2.0, H/2.0}, {399, 1}}) {
			setPixel(alg, p[0], p[1]);
			alg.forward(found);

			double total = 0;
			for (int i = 0; i < NUM_KNOTS; i++) {
				assertTrue(found.d(i) >= -1e-12, "weight " + i + " is negative inside the image");
				total += found.d(i);
			}
			assertEquals(1.0, total, 1e-12, "at (" + p[0] + " , " + p[1] + ")");
		}
	}

	/// A pixel on a knot puts all of the weight on it. Every knot except the last starts a cell of its
	/// own, so the weight lands on that cell's low corner
	@Test void onAKnotAllTheWeightIsThere() {
		final int cols = 3, rows = 3;
		var alg = create(cols, rows, 0);
		var found = new CRArray<>(new double[NUM_KNOTS], 0);

		double cellW = W/(double)(cols - 1), cellH = H/(double)(rows - 1);

		for (int knotCol = 0; knotCol < cols - 1; knotCol++) {
			for (int knotRow = 0; knotRow < rows - 1; knotRow++) {
				setPixel(alg, knotCol*cellW, knotRow*cellH);
				assertEquals(knotCol, alg.col());
				assertEquals(knotRow, alg.row());
				alg.forward(found);
				assertArrayEquals(new double[]{1, 0, 0, 0}, found.a, 1e-12,
						"knot (" + knotCol + " , " + knotRow + ")");
			}
		}

		// the last knot along an axis starts no cell, so it is reached as the far corner of the one before
		setPixel(alg, W, H);
		assertEquals(cols - 2, alg.col());
		assertEquals(rows - 2, alg.row());
		alg.forward(found);
		assertArrayEquals(new double[]{0, 0, 0, 1}, found.a, 1e-12, "far corner");
	}

	/// The four weights are the outer product of the column and row fractions, in that order. A swap
	/// would still sum to one and still be a valid blend, so only an asymmetric case can see it
	@Test void cornerOrderIsColumnThenRow() {
		var alg = create(3, 3, 0);
		var found = new CRArray<>(new double[NUM_KNOTS], 0);

		// a quarter of the way along the cell in x, three quarters in y
		setPixel(alg, 0.25*W/2.0, 0.75*H/2.0);
		alg.forward(found);

		double fc = 0.25, fr = 0.75;
		assertArrayEquals(new double[]{(1 - fc)*(1 - fr), fc*(1 - fr), (1 - fc)*fr, fc*fr},
				found.a, 1e-12);
	}

	/// The middle of a cell splits evenly four ways
	@Test void cellCentreIsAnEvenSplit() {
		var alg = create(3, 3, 0);
		var found = new CRArray<>(new double[NUM_KNOTS], 0);

		setPixel(alg, W/4.0, H/4.0);
		alg.forward(found);
		assertArrayEquals(new double[]{0.25, 0.25, 0.25, 0.25}, found.a, 1e-12);
	}

	/// A pixel off the edge of the grid clamps to the nearest cell and extrapolates from it. This is how
	/// a point outside the image is handled and must not be an error
	@Test void pixelOutsideTheGridExtrapolates() {
		var alg = create(3, 3, 0);
		var found = new CRArray<>(new double[NUM_KNOTS], 0);

		// one whole image to the left, so the fractional coordinate is -2 against cell 0
		setPixel(alg, -W, 0);
		assertEquals(0, alg.col());
		alg.forward(found);
		assertArrayEquals(new double[]{3, -2, 0, 0}, found.a, 1e-12);

		// still a partition of unity, which is what keeps the blend an extension rather than a jump
		double total = 0;
		for (int i = 0; i < NUM_KNOTS; i++)
			total += found.d(i);
		assertEquals(1.0, total, 1e-12);
	}

	/// A negative pixel is an ordinary sample now, not something to reject
	@Test void negativePixelIsAccepted() {
		var alg = create(4, 4, 1);
		var found = new CRArray<>(new double[NUM_KNOTS], 0);

		setPixel(alg, -10, -5);
		alg.forward(found);

		double total = 0;
		for (int i = 0; i < NUM_KNOTS; i++)
			total += found.d(i);
		assertEquals(1.0, total, 1e-12);
	}

	/// Padding moves the image inside the grid and grows the cells to match. With padding 1 on a 4x4
	/// grid the image spans knots 1 to 2, so a corner pixel sits exactly on an interior knot
	@Test void paddingMovesTheImageInwards() {
		var alg = create(4, 4, 1);
		var found = new CRArray<>(new double[NUM_KNOTS], 0);

		// x=0 lands on knot 1, which is the low corner of cell 1
		setPixel(alg, 0, 0);
		assertEquals(1, alg.col());
		assertEquals(1, alg.row());
		alg.forward(found);
		assertArrayEquals(new double[]{1, 0, 0, 0}, found.a, 1e-12);

		// and the far corner lands on knot 2. A pixel on a knot starts the cell above it, so it takes
		// the next cell along with no fraction rather than the far corner of this one
		setPixel(alg, W, H);
		assertEquals(2, alg.col());
		assertEquals(2, alg.row());
		alg.forward(found);
		assertArrayEquals(new double[]{1, 0, 0, 0}, found.a, 1e-12);
	}

	/// Padding buys coverage outside the image: a pixel one interior cell to the left of the image is
	/// still inside the grid, where without padding it would have to extrapolate
	@Test void paddingCoversOutsideTheImage() {
		var alg = create(4, 4, 1);
		var found = new CRArray<>(new double[NUM_KNOTS], 0);

		setPixel(alg, -W, -H);
		assertEquals(0, alg.col());
		assertEquals(0, alg.row());
		alg.forward(found);
		assertArrayEquals(new double[]{1, 0, 0, 0}, found.a, 1e-12);
	}

	/// The cell is clamped to one that exists, so the last knot along an axis never starts a cell and
	/// a pixel far outside the grid extrapolates from the nearest cell instead of indexing off the end
	@Test void cellIsAlwaysAValidOne() {
		var alg = create(4, 3, 0);
		for (double[] p : new double[][]{{0, 0}, {W, H}, {-1000, -1000}, {10*W, 10*H}}) {
			setPixel(alg, p[0], p[1]);
			assertTrue(alg.col() >= 0 && alg.col() <= 4 - 2, "col at " + p[0]);
			assertTrue(alg.row() >= 0 && alg.row() <= 3 - 2, "row at " + p[1]);
		}
	}

	/// Runs a pixel through the pair: weights from the geometry here, knots picked from the same cell,
	/// then blended. This is the seam between the two operators, and it is only correct if the corner
	/// order in [CROperatorGridBilinearWeights#forward] matches the offset order in
	/// [ConfigGridCoefND#knotOffsets]
	static double blend( CROperatorGridBilinearWeights weights, CROperatorBilinearPolyND poly,
						 ConfigGridCoefND config, double[] grid, double x, double y ) {
		setPixel(weights, x, y);

		var found = new CRArray<>(new double[NUM_KNOTS], 0);
		weights.forward(found);

		var knotOffsets = new int[NUM_KNOTS];
		config.knotOffsets(weights.col(), weights.row(), 0, knotOffsets);
		for (int knot = 0; knot < NUM_KNOTS; knot++) {
			poly.setInput(CROperatorBilinearPolyND.IN_KNOT_0 + knot, new CRArray<>(grid, knotOffsets[knot]));
		}
		poly.setInput(CROperatorBilinearPolyND.IN_WEIGHTS, found);

		var out = new CRArray<>(new double[1], 0);
		poly.forward(out);
		return out.d(0);
	}

	/// Crossing a knot swaps the cell and jumps the weights, but the knot each weight refers to changes
	/// with it, so the value does not move. That is what stops the warp having a seam, and it is a
	/// property of the two operators together rather than of either one
	@Test void blendIsContinuousAcrossAKnot() {
		final int cols = 4, rows = 3;
		var config = new ConfigGridCoefND().cols(cols).rows(rows).coefs(1).numOut(1);
		var weights = new CROperatorGridBilinearWeights().configure(W, H, config);
		var poly = new CROperatorBilinearPolyND().configure(config);
		poly.setInput(CROperatorBilinearPolyND.IN_VALUE, new CRArray<>(new double[]{0}, 0));

		var grid = new double[config.dof()];
		for (int i = 0; i < grid.length; i++)
			grid[i] = Math.sin(i*2.3);          // every knot different, so a seam cannot cancel out

		final double step = 1e-7;
		double cellW = W/(double)(cols - 1), cellH = H/(double)(rows - 1);

		for (int knotCol = 1; knotCol < cols - 1; knotCol++) {
			for (double y : new double[]{7.0, 123.0, 250.0}) {
				double x = knotCol*cellW;
				assertEquals(blend(weights, poly, config, grid, x - step, y),
						blend(weights, poly, config, grid, x + step, y), 1e-6,
						"seam at column knot " + knotCol + ", y=" + y);
			}
		}

		for (int knotRow = 1; knotRow < rows - 1; knotRow++) {
			for (double x : new double[]{15.0, 210.0, 380.0}) {
				double y = knotRow*cellH;
				assertEquals(blend(weights, poly, config, grid, x, y - step),
						blend(weights, poly, config, grid, x, y + step), 1e-6,
						"seam at row knot " + knotRow + ", x=" + x);
			}
		}
	}

	/// The weights on their own are not continuous at a knot: the cell changes and the vector jumps from
	/// one corner to another. Only the blend above survives that. This is why the jacobian cases have to
	/// stay strictly inside a cell, since a central difference there straddles the jump
	@Test void weightsJumpAtAKnot() {
		final int cols = 4, rows = 3;
		var alg = create(cols, rows, 0);
		double cellW = W/(double)(cols - 1);
		final double step = 1e-7;

		var before = new CRArray<>(new double[NUM_KNOTS], 0);
		var after = new CRArray<>(new double[NUM_KNOTS], 0);

		setPixel(alg, cellW - step, 30);
		alg.forward(before);
		int cellBefore = alg.col();

		setPixel(alg, cellW + step, 30);
		alg.forward(after);

		assertEquals(cellBefore + 1, alg.col(), "the cell should have changed");
		// the same physical knot, reached as corner 1 on one side and corner 0 on the other
		assertTrue(Math.abs(before.d(0) - after.d(0)) > 0.5, "weight 0 should jump");
		assertTrue(Math.abs(before.d(1) - after.d(1)) > 0.5, "weight 1 should jump");
	}

	@TestFactory Stream<DynamicTest> jacobian() {
		return new CommonCROperatorBackwardsJac<double[]>(this::jacobianVariants, rand).tests();
	}

	List<JacobianCase<double[]>> jacobianVariants() {
		var out = new ArrayList<JacobianCase<double[]>>();
		out.add(testCase(3, 3, 0, 123.4, 45.6));
		out.add(testCase(2, 2, 0, 350.0, 20.0));
		out.add(testCase(5, 4, 1, 210.0, 160.0));
		out.add(testCase(6, 6, 2, 90.0, 130.0));
		// outside the image, where the weights extrapolate. The derivative is the same either way,
		// since the cell is fixed and the fractions stay linear in the pixel
		out.add(testCase(4, 4, 0, -50.0, 33.0));
		return out;
	}

	private JacobianCase<double[]> testCase( int cols, int rows, int padding, double x, double y ) {
		// every case sits strictly inside a cell, since the weights are only piecewise smooth and a
		// central difference across a boundary would average two different one sided derivatives
		var alg = create(cols, rows, padding);
		setPixel(alg, x, y);
		assertNotEquals(0.0, alg.grid().pixelToGridX(x, W) - alg.col(), "x sits on a knot");
		assertNotEquals(0.0, alg.grid().pixelToGridY(y, H) - alg.row(), "y sits on a knot");
		return new JacobianCase<double[]>(alg).in(x, y);
	}
}
