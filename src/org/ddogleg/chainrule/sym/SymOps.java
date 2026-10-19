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

import org.ddogleg.chainrule.grid.ConfigGridCoefND;
import org.ddogleg.chainrule.ops.CRErrorVectorSW;
import org.ddogleg.chainrule.ops.CROperatorClampLower;
import org.ddogleg.chainrule.ops.CROperatorConcat;
import org.ddogleg.chainrule.ops.CROperatorDivide;
import org.ddogleg.chainrule.ops.CROperatorDivideVS;
import org.ddogleg.chainrule.ops.CROperatorDivideVSDamped;
import org.ddogleg.chainrule.ops.CROperatorMult;
import org.ddogleg.chainrule.ops.CROperatorMultVS;
import org.ddogleg.chainrule.ops.CROperatorPlus;
import org.ddogleg.chainrule.ops.CROperatorRemapParams;

/// Static sugar over [Sym#of] for the operators that exist today. Nothing here is privileged:
/// every method is one `of(...).in(...)` expression a user could write for their own operator.
public class SymOps {
	/// `a + b`, element-wise over `dof` values
	public static Sym plus( int dof, Sym a, Sym b ) {
		return Sym.of(new CROperatorPlus(dof)).in(CROperatorPlus.IN_A, a).in(CROperatorPlus.IN_B, b);
	}

	/// Rearranges elements of a vector: `mappings[i]` says which output each input lands in, -1 drops it
	public static Sym remap( int outputSize, Sym src, int... mappings ) {
		return Sym.of(new CROperatorRemapParams(outputSize, mappings)).in0(src);
	}

	/// A bilinear knot grid conditional on a value, read as `grid(...).at(u).of(v)`
	public static SymGrid grid( String name, ConfigGridCoefND config, int width, int height ) {
		return new SymGrid(name, config, width, height);
	}

	/// Holds a value at or above a floor, for feeding something that cannot accept what a solver
	/// might step to
	public static Sym clampLower( Sym value, int dof, double minimum ) {
		return Sym.of(new CROperatorClampLower(dof, minimum)).in0(value);
	}

	/// `weight*(observed - estimated)`: the residual row(s) of one measurement. The observation is
	/// a terminal, so it is constant by construction
	public static Sym residual( double weight, Sym estimated, Sym observed ) {
		var op = new CRErrorVectorSW(estimated.size > 0 ? estimated.size : observed.size, weight);
		return Sym.of(op).in0(estimated).in1(observed);
	}

	/// `vectorA / vectorB`: element-wise division between the two vectors
	public static Sym divide( Sym vectorA, Sym vectorB ) {
		var op = new CROperatorDivide(vectorA.size > 0 ? vectorA.size : vectorB.size);
		return Sym.of(op).in0(vectorA).in1(vectorB);
	}

	/// `vector ./ scalar`: divide each element in vector by scalar
	public static Sym divideVS( Sym vector, Sym scalar ) {
		var op = new CROperatorDivideVS(vector.size);
		return Sym.of(op).in0(vector).in1(scalar);
	}

	/// `vector ./ (beta + scalar)`: divide each element by a damped scalar. For a scalar at or
	/// above zero, `beta > 0` keeps the denominator away from zero
	public static Sym divideDamped( Sym vector, Sym scalar, double beta ) {
		var op = new CROperatorDivideVSDamped(vector.size, beta);
		return Sym.of(op).in0(vector).in1(scalar);
	}

	/// `vectorA * vectorB`: element-wise multiplication between the two vectors
	public static Sym mult( Sym vectorA, Sym vectorB ) {
		var op = new CROperatorMult(vectorA.size > 0 ? vectorA.size : vectorB.size);
		return Sym.of(op).in0(vectorA).in1(vectorB);
	}

	/// `vector .* scalar`: multiply each element in vector by scalar
	public static Sym multVS( Sym vector, Sym scalar ) {
		var op = new CROperatorMultVS(vector.size);
		return Sym.of(op).in0(vector).in1(scalar);
	}

	/// Joins residual groups into one row block, in argument order
	public static Sym concat( Sym... parts ) {
		var sizes = new int[parts.length];
		for (int i = 0; i < parts.length; i++)
			sizes[i] = parts[i].size;
		Sym node = Sym.of(new CROperatorConcat(sizes));
		for (int i = 0; i < parts.length; i++)
			node.in(i, parts[i]);
		return node;
	}
}
