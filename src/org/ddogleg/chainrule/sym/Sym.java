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

import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/// One node of a symbolic expression that [SymGraphBuilder] compiles onto a chain graph. An
/// interior node wraps an operator with its inputs bound by index; a terminal names a value that
/// arrives from outside the graph.
///
/// Terminal kinds and their contract:
///
/// * [#input]: per-observation value the caller updates inside its loop. Never differentiated.
/// * [#setting]: known per-dataset value the caller updates between datasets. Never differentiated.
/// * [#param]: a slice of the parameter array. The only kind that will receive a Jacobian column.
/// * [#constant]: fixed values.
///
/// Only what a `param` supplies, or what flows through operators, is ever differentiated: a
/// terminal without a column mapping is structurally constant, no flag involved.
public class Sym {
	public enum Kind {APPLY, INPUT, SETTING, PARAM, CONST}

	public final Kind kind;

	/// Label for build errors and the wiring dump, not used for lookup
	public final String name;

	/// Number of values a terminal supplies. Interior nodes get their size from the operator
	public final int size;

	/// The operator an interior node wraps, one instance per node
	public final @Nullable CROperatorBackwardsJac<double[]> op;

	/// Which expression feeds each input slot, by the operator's own input index
	public final Map<Integer, Sym> bound = new TreeMap<>();

	/// Values of a [#constant] terminal
	public final double @Nullable [] values;

	/// Set when the node needs hooks of its own, e.g. a composite choosing parameters per
	/// evaluation. The builder installs it and otherwise knows nothing about what it does
	public @Nullable SymExtension extension;

	Sym( Kind kind, String name, int size, @Nullable CROperatorBackwardsJac<double[]> op,
		 double @Nullable [] values ) {
		this.kind = kind;
		this.name = name;
		this.size = size;
		this.op = op;
		this.values = values;
	}

	/// Interior node wrapping the operator. Bind its inputs with [#in]; slots left unbound must be
	/// terminals the caller feeds, and the builder rejects anything else
	public static Sym of( CROperatorBackwardsJac<double[]> op ) {
		return new Sym(Kind.APPLY, op.getClass().getSimpleName(), op.getOutputSize(), op, null);
	}

	/// Per-observation value, updated by the caller inside its loop
	public static Sym input( String name, int size ) {
		return new Sym(Kind.INPUT, name, size, null, null);
	}

	/// Known per-dataset scalar, updated by the caller between datasets
	public static Sym setting( String name ) {
		return new Sym(Kind.SETTING, name, 1, null, null);
	}

	/// A slice of the parameter array, the only terminal that is differentiated.
	///
	/// Keep `size` to what one evaluation actually touches and re-bind the feed offset per item
	/// (per view, per frame): the backwards pass carries a dense matrix as wide as the node, so a
	/// param spanning a whole block turns a diagonal dependency into width-squared work
	public static Sym param( String name, int size ) {

		return new Sym(Kind.PARAM, name, size, null, null);
	}

	public static Sym constant( double... values ) {
		return new Sym(Kind.CONST, "const", values.length, null, values.clone());
	}

	/// Binds input slot `index` of this node's operator, using the operator's own input indexes.
	/// Binding a slot twice is an error rather than a silent overwrite
	public Sym in( int index, Sym value ) {
		if (kind != Kind.APPLY)
			throw new IllegalArgumentException("Only operator nodes have inputs to bind: " + name);
		if (index < 0 || index >= Objects.requireNonNull(op).getInputCount())
			throw new IllegalArgumentException(name + " has no input " + index);
		if (bound.containsKey(index))
			throw new IllegalArgumentException(name + " input " + index + " is already bound");
		int expected = op.getInputSize(index);
		if (value.size > 0 && value.size != expected)
			throw new IllegalArgumentException(name + " input " + index + " takes " + expected
					+ " values, got " + value.size + " from " + value.name);
		bound.put(index, value);
		return this;
	}

	public Sym in0( Sym value ) {return in(0, value);}

	public Sym in1( Sym value ) {return in(1, value);}

	public Sym in2( Sym value ) {return in(2, value);}

	public Sym in3( Sym value ) {return in(3, value);}

	public Sym in4( Sym value ) {return in(4, value);}

	public Sym in5( Sym value ) {return in(5, value);}

	public Sym in6( Sym value ) {return in(6, value);}

	public Sym in7( Sym value ) {return in(7, value);}

	public Sym in8( Sym value ) {return in(8, value);}
}
