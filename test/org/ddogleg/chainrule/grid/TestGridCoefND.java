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

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class TestGridCoefND extends DDoglegJUnit {
	static GridCoefND create( int cols, int rows, int coefs, int numOut ) {
		var grid = new GridCoefND(new ConfigGridCoefND().cols(cols).rows(rows).coefs(coefs).numOut(numOut));
		for (int i = 0; i < grid.parameters.length; i++)
			grid.parameters[i] = 0.5*Math.sin(i*1.7);
		return grid;
	}

	@Test void reset() {
		checkReset(GridCoefND.class);
	}

	/// The coefficients are sized from the shape, so the two can never disagree
	@Test void configure_sizesTheParameters() {
		var grid = new GridCoefND(new ConfigGridCoefND().cols(5).rows(4).coefs(2).numOut(3));
		assertEquals(5*4*2*3, grid.parameters.length);
		assertEquals(grid.config.dof(), grid.parameters.length);

		// and re-configuring resizes rather than leaving the old ones behind
		grid.configure(new ConfigGridCoefND().cols(2).rows(2).coefs(1).numOut(1));
		assertEquals(4, grid.parameters.length);
	}

	/// setTo has to copy the coefficients, not share them, or two models would move together
	@Test void setTo_copiesTheParameters() {
		var src = create(3, 3, 2, 2);
		var dst = new GridCoefND().setTo(src);
		assertTrue(src.isIdentical(dst));

		src.parameters[0] += 1;
		assertFalse(src.isIdentical(dst), "the two are sharing an array");
	}

	/// isIdentical is used to check that a round trip changed nothing, so it has to notice everything
	@Test void isIdentical() {
		assertTrue(create(3, 3, 2, 2).isIdentical(create(3, 3, 2, 2)));

		assertFalse(create(3, 3, 2, 2).isIdentical(create(4, 3, 2, 2)));
		assertFalse(create(3, 3, 2, 2).isIdentical(create(3, 4, 2, 2)));
		assertFalse(create(3, 3, 2, 2).isIdentical(create(3, 3, 1, 2)));
		assertFalse(create(3, 3, 2, 2).isIdentical(create(3, 3, 2, 1)));

		var a = create(3, 3, 2, 2);
		var b = create(3, 3, 2, 2);
		b.config.padding(1);
		assertFalse(a.isIdentical(b), "padding was not compared");

		b = create(3, 3, 2, 2);
		b.parameters[b.parameters.length - 1] += 1e-12;
		assertFalse(a.isIdentical(b), "the last coefficient was not compared");
	}

	/// Everything has to come back bit for bit, since these are the numbers a fit produced

}
