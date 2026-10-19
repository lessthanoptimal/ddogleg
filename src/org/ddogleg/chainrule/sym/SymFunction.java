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
import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.ddogleg.chainrule.JacobianChainGraph;
import org.ddogleg.optimization.LeastSquaresSwitcher;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

/// A compiled [Sym] expression. The caller binds each terminal to storage with [#feed] once, then
/// mutates that storage — contents or offset — inside its loop and calls [#run]. There is no
/// per-iteration lookup: the handles are raw [CRArray]s and the loop touches them directly.
public class SymFunction {
	final JacobianChainGraph<double[]> graphs;
	final int rootChain;
	int graphRoot;

	/// One handle per terminal, resolved by node identity
	final Map<Sym, CRArray<double[]>> terminals = new IdentityHashMap<>();

	final List<RunningFeed> runningFeeds = new ArrayList<>();
	final List<SideFeed> sideFeeds = new ArrayList<>();

	/// Side feeds backed by `param` terminals, whose columns [#mapParamColumns] declares
	final List<SideFeed> paramFeeds = new ArrayList<>();

	/// Columns an extension publishes per evaluation, rather than declaring once
	final List<MovingCapture> movingCaptures = new ArrayList<>();

	/// Where Jacobian entries land, and the row the caller says the current observation starts at.
	/// The row is a raw field for the same reason the feeds are raw arrays
	@SuppressWarnings("NullAway.Init") public LeastSquaresSwitcher.JacobianOut jacobianOut;
	public int jacobianRow;

	SymFunction( JacobianChainGraph<double[]> graphs, int rootChain ) {
		this.graphs = graphs;
		this.rootChain = rootChain;
	}

	/// Binds the terminal to the caller's own storage. No copy is ever made: the caller updates
	/// the array's contents, and the offset is the caller's responsibility
	public CRArray<double[]> feed( Sym terminal, double[] data ) {
		CRArray<double[]> handle = terminal(terminal);
		handle.wrap(data, 0);
		return handle;
	}

	/// Binds the terminal to a freshly allocated array of its declared size. Whether the caller
	/// walks the offset or copies values in is the caller's knowledge, not tracked here
	public CRArray<double[]> feed( Sym terminal ) {
		return feed(terminal, new double[terminal.size]);
	}

	/// Declares the Jacobian column of every `param` side feed from its handle's current offset.
	/// Call once after binding, and again whenever a handle's offset moved (a new view's pose).
	/// Columns are the offsets themselves, so params must be slices of the solver's parameter array
	public void mapParamColumns() {
		var chains = graphs.getChains();
		for (int i = 0; i < paramFeeds.size(); i++) {
			SideFeed feed = paramFeeds.get(i);
			// a column given by hand stays where it was put, since this has no way of knowing it
			if (feed.explicitColumn)
				continue;
			chains.setInputMap(feed.block, feed.slot, feed.value.offset);
		}
	}

	/// Declares where a parameter's column is, for a terminal whose storage is not the solver's
	/// parameter array. A column below zero says the value is not being estimated, so nothing is
	/// published for it and it is left undifferentiated
	public void mapParamColumn( Sym terminal, int column ) {
		CRArray<double[]> handle = terminal(terminal);
		var chains = graphs.getChains();
		for (int i = 0; i < paramFeeds.size(); i++) {
			SideFeed feed = paramFeeds.get(i);
			if (feed.value != handle)
				continue;
			feed.explicitColumn = true;
			if (column >= 0)
				chains.setInputMap(feed.block, feed.slot, column);
			else
				// nothing is estimating this, so whatever a previous call mapped is taken back
				chains.setCapture(feed.block, feed.slot, ( jac ) -> {});
		}
		// captures publish on their own schedule, so a terminal that is not being estimated has to
		// silence them too, or a grid fed plain values would write its knot derivatives into
		// whichever parameters happen to share those columns
		for (int i = 0; i < movingCaptures.size(); i++) {
			MovingCapture capture = movingCaptures.get(i);
			if (capture.terminal != terminal)
				continue;
			capture.disabled = column < 0;
			if (capture.disabled)
				chains.setCapture(capture.block, capture.slot, ( jac ) -> {});
		}
	}

	/// Publishes Jacobian entries to `out`. Static columns come from [#mapParamColumns]; a grid
	/// read's knot columns move per observation, so they are captured from the operator, which
	/// knows where its knots were sliced from
	public void jacobianTo( LeastSquaresSwitcher.JacobianOut out ) {
		this.jacobianOut = out;
		var chains = graphs.getChains();
		chains.setPublishJac(( col, jac ) -> jacobianOut.addMatrixToJacobian(jacobianRow, col,
				jac.data, jac.rows, jac.cols));
		for (int i = 0; i < movingCaptures.size(); i++) {
			MovingCapture capture = movingCaptures.get(i);
			if (capture.disabled)
				continue;
			chains.setCapture(capture.block, capture.slot,
					( jac ) -> jacobianOut.addMatrixToJacobian(jacobianRow, capture.column.getAsInt(),
							jac.data, jac.rows, jac.cols));
		}
	}

	/// Runs forward then differentiates, publishing rows at [#jacobianRow]. The caller advances
	/// the row between observations, mirroring how it advances the residual output's offset
	public void runJacobian() {
		applyFeeds(null);
		graphs.backwards(graphRoot);
	}

	/// Runs the forward pass with the current terminal values and writes the residual rows at the
	/// output's offset
	public void run( CRArray<double[]> out ) {
		applyFeeds(out);
	}

	/// Hands every terminal to the operator or chain that reads it, then runs the forward pass.
	/// Indexed loops throughout: this runs once per observation, so nothing here may allocate
	private void applyFeeds( @Nullable CRArray<double[]> out ) {
		for (int i = 0; i < sideFeeds.size(); i++) {
			SideFeed feed = sideFeeds.get(i);
			feed.op.setInput(feed.slot, feed.value);
		}
		var forward = graphs.forward(graphRoot);
		for (int i = 0; i < runningFeeds.size(); i++) {
			RunningFeed feed = runningFeeds.get(i);
			forward.input(feed.chain, feed.value);
		}
		forward.run(out);
	}

	/// Declares an input whose Jacobian column moves as the evaluation walks, published from the
	/// supplier when the entry is captured. For extensions choosing parameters per evaluation.
	/// The terminal is what [#mapParamColumn] disables the capture through when the values it
	/// reads are not parameters of the solve
	public void addMovingCapture( Sym terminal, int block, int slot, IntSupplier column ) {
		movingCaptures.add(new MovingCapture(terminal, block, slot, column));
	}

	/// The handle backing a terminal, creating it on first use. Constants arrive already filled
	public CRArray<double[]> terminal( Sym node ) {
		return terminals.computeIfAbsent(node, ( n ) -> {
			if (n.kind == Sym.Kind.APPLY)
				throw new IllegalArgumentException(n.name + " is computed by the graph, not fed");
			var handle = new CRArray<>(new double[n.size], 0);
			if (n.kind == Sym.Kind.CONST)
				System.arraycopy(n.values, 0, handle.a, 0, n.size);
			return handle;
		});
	}

	/// A terminal delivered to a chain's running input through the graph at every run
	static class RunningFeed {
		final int chain;
		final CRArray<double[]> value;

		RunningFeed( int chain, CRArray<double[]> value ) {
			this.chain = chain;
			this.value = value;
		}
	}

	/// A terminal delivered to an operator side input before every run
	static class SideFeed {
		final CROperatorBackwardsJac<double[]> op;
		final int block;
		final int slot;
		final CRArray<double[]> value;

		/// Set once [#mapParamColumn] has said where this one goes, which [#mapParamColumns] then
		/// leaves alone
		boolean explicitColumn;

		SideFeed( CROperatorBackwardsJac<double[]> op, int block, int slot, CRArray<double[]> value ) {
			this.op = op;
			this.block = block;
			this.slot = slot;
			this.value = value;
		}
	}

	/// An input whose column is only known once the evaluation has reached it. Disabled when its
	/// terminal's values are not parameters of the solve, so nothing is published for them
	static class MovingCapture {
		final Sym terminal;
		final int block;
		final int slot;
		final IntSupplier column;
		boolean disabled;

		MovingCapture( Sym terminal, int block, int slot, IntSupplier column ) {
			this.terminal = terminal;
			this.block = block;
			this.slot = slot;
			this.column = column;
		}
	}

}
