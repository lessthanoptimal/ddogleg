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

import org.ddogleg.DDoglegJUnit;
import org.junit.jupiter.api.Test;

import static org.ddogleg.chainrule.grid.ConfigGridCoefND.NUM_KNOTS;
import static org.junit.jupiter.api.Assertions.*;

public class TestConfigGridCoefND extends DDoglegJUnit {
	static final int W = 400, H = 300;

	@Test void reset() {
		checkReset(ConfigGridCoefND.class);
	}

	/// Every knot holds one polynomial per output, and the grid holds one of those per knot
	@Test void sizes() {
		var config = new ConfigGridCoefND().cols(5).rows(4).coefs(3).numOut(2);
		assertEquals(3*2, config.knotStride());
		assertEquals(5*4*3*2, config.dof());

		// padding is part of the totals, so neither of these moves when it changes
		assertEquals(config.dof(),
				new ConfigGridCoefND().cols(5).rows(4).coefs(3).numOut(2).padding(1).dof());
	}

	/// Without padding the image spans the whole grid, so a corner pixel lands on a corner knot
	@Test void pixelToGrid_noPadding() {
		var config = new ConfigGridCoefND().cols(5).rows(3).coefs(1).numOut(1);

		assertEquals(0.0, config.pixelToGridX(0, W), 1e-12);
		assertEquals(4.0, config.pixelToGridX(W, W), 1e-12);
		assertEquals(2.0, config.pixelToGridX(W/2.0, W), 1e-12);

		assertEquals(0.0, config.pixelToGridY(0, H), 1e-12);
		assertEquals(2.0, config.pixelToGridY(H, H), 1e-12);
	}

	/// Padding moves the image inwards and grows the cells to match, so the same totals cover more
	/// ground. That is the point of it: coverage is bought with resolution, not with parameters
	@Test void pixelToGrid_padding() {
		var config = new ConfigGridCoefND().cols(6).rows(6).coefs(1).numOut(1).padding(1);

		// the image now spans knots 1 through 4 rather than 0 through 5
		assertEquals(1.0, config.pixelToGridX(0, W), 1e-12);
		assertEquals(4.0, config.pixelToGridX(W, W), 1e-12);
		assertEquals(1.0, config.pixelToGridY(0, H), 1e-12);
		assertEquals(4.0, config.pixelToGridY(H, H), 1e-12);

		// so one whole cell outside the image is still inside the grid
		assertEquals(0.0, config.pixelToGridX(-W/3.0, W), 1e-12);
		assertEquals(5.0, config.pixelToGridX(W + W/3.0, W), 1e-12);
	}

	/// More padding at the same totals means fewer cells inside the image, each one wider
	@Test void paddingTradesResolutionForCoverage() {
		double previous = 0;
		for (int padding = 0; padding <= 2; padding++) {
			var config = new ConfigGridCoefND().cols(8).rows(8).coefs(1).numOut(1).padding(padding);
			// pixels per unit of grid coordinate, which is the width of one cell
			double cell = W/(config.pixelToGridX(W, W) - config.pixelToGridX(0, W));
			assertTrue(cell > previous, "cells should grow with padding, " + cell + " after " + previous);
			previous = cell;
		}
	}

	/// The last knot along an axis starts no cell of its own, and anything off the end of the grid
	/// extrapolates from the nearest cell rather than indexing past it
	@Test void cellIndex_clampsToARealCell() {
		final int count = 5;

		assertEquals(0, ConfigGridCoefND.cellIndex(0.0, count));
		assertEquals(2, ConfigGridCoefND.cellIndex(2.7, count));
		assertEquals(3, ConfigGridCoefND.cellIndex(3.0, count));

		// the last knot falls back to the cell before it
		assertEquals(count - 2, ConfigGridCoefND.cellIndex(count - 1, count));
		assertEquals(count - 2, ConfigGridCoefND.cellIndex(100.0, count));

		// and below the grid. The lower clamp is what handles this, so floor and truncation happen to
		// agree here. The floor still matters if the clamp ever moves
		assertEquals(0, ConfigGridCoefND.cellIndex(-0.5, count));
		assertEquals(0, ConfigGridCoefND.cellIndex(-100.0, count));
	}

	/// The four locations are the cell's corners in the order the weights arrive, shifted by wherever
	/// this grid's parameters begin. A missing offset would read another model's parameters
	@Test void knotOffsets_orderAndBase() {
		final int base = 17;
		var config = new ConfigGridCoefND().cols(5).rows(4).coefs(2).numOut(3);
		int stride = config.knotStride();

		var found = new int[NUM_KNOTS];
		config.knotOffsets(2, 1, base, found);

		assertEquals(base + (1*5 + 2)*stride, found[0], "(col,row)");
		assertEquals(base + (1*5 + 3)*stride, found[1], "(col+1,row)");
		assertEquals(base + (2*5 + 2)*stride, found[2], "(col,row+1)");
		assertEquals(base + (2*5 + 3)*stride, found[3], "(col+1,row+1)");

		// with no offset they are positions inside the grid alone
		config.knotOffsets(2, 1, 0, found);
		assertEquals((1*5 + 2)*stride, found[0]);
	}

	/// Every knot in the grid is reached by some cell, and nothing lands outside it
	@Test void knotOffsets_coverTheGridExactly() {
		final int cols = 4, rows = 3;
		var config = new ConfigGridCoefND().cols(cols).rows(rows).coefs(1).numOut(1);

		var seen = new boolean[config.dof()];
		var found = new int[NUM_KNOTS];
		for (int col = 0; col < cols - 1; col++) {
			for (int row = 0; row < rows - 1; row++) {
				config.knotOffsets(col, row, 0, found);
				for (int knot = 0; knot < NUM_KNOTS; knot++) {
					assertTrue(found[knot] >= 0 && found[knot] < seen.length,
							"offset " + found[knot] + " is outside the grid");
					seen[found[knot]] = true;
				}
			}
		}
		for (int i = 0; i < seen.length; i++)
			assertTrue(seen[i], "knot " + i + " is never reachable");
	}

	/// A grid needs two knots along each axis to define a cell, and padding eats into that
	@Test void checkValid() {
		valid().checkValid();
		new ConfigGridCoefND().cols(4).rows(4).coefs(1).numOut(1).padding(1).checkValid();

		assertThrows(RuntimeException.class, () -> valid().cols(1).checkValid());
		assertThrows(RuntimeException.class, () -> valid().rows(1).checkValid());
		// 0 coefficients is a disabled grid, so it is allowed; negative is not
		valid().coefs(0).checkValid();
		assertThrows(RuntimeException.class, () -> valid().coefs(-1).checkValid());
		assertThrows(RuntimeException.class, () -> valid().numOut(0).checkValid());
		assertThrows(RuntimeException.class, () -> valid().padding(-1).checkValid());

		// 3 columns with a ring of 1 leaves a single interior knot, which is not a cell
		assertThrows(RuntimeException.class,
				() -> new ConfigGridCoefND().cols(3).rows(4).coefs(1).numOut(1).padding(1).checkValid());
		assertThrows(RuntimeException.class,
				() -> new ConfigGridCoefND().cols(4).rows(3).coefs(1).numOut(1).padding(1).checkValid());

		// a fresh one has not been filled in, so it must not pass
		assertThrows(RuntimeException.class, () -> new ConfigGridCoefND().checkValid());
	}

	static ConfigGridCoefND valid() {
		return new ConfigGridCoefND().cols(4).rows(3).coefs(2).numOut(2);
	}
}
