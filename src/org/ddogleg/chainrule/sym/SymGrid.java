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

package org.ddogleg.chainrule.sym;

import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.JacobianSerialChain;
import org.ddogleg.chainrule.grid.ConfigGridCoefND;
import org.ddogleg.chainrule.ops.CROperatorBilinearPolyND;
import org.ddogleg.chainrule.ops.CROperatorGridBilinearWeights;

/// A bilinear knot grid whose per-knot polynomials are conditional on a value: the composite
/// behind the notation `G(u; v)`, written `G.at(u).of(v)`. One instance owns one parameter block,
/// however many places it is read.
///
/// A read expands to two operators — weights at the pixel, then a polynomial blending the four
/// surrounding knots — plus the hook that hands those knots' parameter slices to the polynomial
/// once the pixel is known, and the Jacobian captures publishing them as columns. The hook and the
/// captures arrive through [SymExtension], so the graph builder needs no notion of a grid.
public class SymGrid {
	public final ConfigGridCoefND config;
	public final int width, height;

	/// The knot coefficients, a [Sym#param] terminal sized to the grid
	public final Sym knots;

	public SymGrid( String name, ConfigGridCoefND config, int width, int height ) {
		this.config = config;
		this.width = width;
		this.height = height;
		this.knots = Sym.param(name, config.dof());
	}

	/// The field position the grid is read at, an expression producing a pixel
	public Read at( Sym pixel ) {
		return new Read(pixel);
	}

	/// A read in progress: [#at] fixed the pixel, [Read#of] fixes the conditioning value
	public class Read {
		final Sym pixel;

		Read( Sym pixel ) {
			this.pixel = pixel;
		}

		/// The value the per-knot polynomials are evaluated at, completing `G(u; v)`
		public Sym of( Sym value ) {
			var weightsOp = new CROperatorGridBilinearWeights().configure(width, height, config);
			var polyOp = new CROperatorBilinearPolyND().configure(config);

			Sym weights = Sym.of(weightsOp).in(CROperatorGridBilinearWeights.IN_PIXEL, pixel);
			Sym read = Sym.of(polyOp)
					.in(CROperatorBilinearPolyND.IN_WEIGHTS, weights)
					.in(CROperatorBilinearPolyND.IN_VALUE, value);
			read.extension = new KnotHook(weightsOp, polyOp);
			return read;
		}
	}

	/// Chooses the cell's knots once the pixel has been projected, which is why this cannot be part
	/// of the expression: which parameters the operator reads depends on the value flowing through it
	class KnotHook implements SymExtension {
		final CROperatorGridBilinearWeights weightsOp;
		final CROperatorBilinearPolyND polyOp;

		/// Recycled between evaluations, which the operator's input contract allows
		final CRArray<double[]> slice = new CRArray<>();
		final int[] coors = new int[ConfigGridCoefND.NUM_KNOTS];

		KnotHook( CROperatorGridBilinearWeights weightsOp, CROperatorBilinearPolyND polyOp ) {
			this.weightsOp = weightsOp;
			this.polyOp = polyOp;
		}

		@Override public void install( JacobianSerialChain<double[]> chains, int block, SymFunction function ) {
			CRArray<double[]> knotBlock = function.terminal(knots);
			chains.setPreForward(block, ( op ) -> {
				config.knotOffsets(weightsOp.col(), weightsOp.row(), knotBlock.offset, coors);
				for (int knot = 0; knot < ConfigGridCoefND.NUM_KNOTS; knot++) {
					polyOp.setInput(CROperatorBilinearPolyND.IN_KNOT_0 + knot,
							slice.wrap(knotBlock.a, coors[knot]));
				}
			});
			// which knots an observation touches changes with its pixel, so the columns come from
			// the operator as each entry is captured rather than being declared once
			for (int knot = 0; knot < ConfigGridCoefND.NUM_KNOTS; knot++) {
				final int index = knot;
				function.addMovingCapture(knots, block, CROperatorBilinearPolyND.IN_KNOT_0 + knot,
						() -> polyOp.knotOffset(index));
			}

		}

		@Override public boolean ownsInput( int slot ) {
			return slot >= CROperatorBilinearPolyND.IN_KNOT_0
					&& slot < CROperatorBilinearPolyND.IN_KNOT_0 + ConfigGridCoefND.NUM_KNOTS;
		}
	}
}
