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
import org.ddogleg.chainrule.MathCRMatrixD;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// Compiles a [Sym] expression onto a [JacobianChainGraph]: every operator node becomes its own
/// single-block chain, expression edges become cross-chain links, and terminals become the feeds
/// of the returned [SymFunction]. Unbound operator slots are an error at build time, listed by
/// operator and index, rather than a failure mid-solve.
///
/// This round compiles the forward pass only: `param` terminals are fed like the others and no
/// Jacobian columns are declared yet.
public class SymGraphBuilder {
	final JacobianChainGraph<double[]> graphs = new JacobianChainGraph<>(new MathCRMatrixD());

	/// Compiles the expression rooted at `root` and returns the runnable function
	public SymFunction build( Sym root ) {
		// depth-first post-order over the DAG, deduplicated by node identity so a shared
		// subexpression (the undistorted pixel) becomes one chain with several consumers
		var order = new ArrayList<Sym>();
		collect(root, order);

		var chains = graphs.getChains();
		var blockOf = new LinkedHashMap<Sym, Integer>();
		var chainOf = new LinkedHashMap<Sym, Integer>();
		for (Sym node : order) {
			if (node.kind != Sym.Kind.APPLY)
				continue;
			int block = chains.add(Objects.requireNonNull(node.op)).lastBlockId();
			blockOf.put(node, block);
			chainOf.put(node, chains.chain(block));
		}

		var f = new SymFunction(graphs, Objects.requireNonNull(chainOf.get(root)));

		// extensions install their own hooks and captures before the graph is fixated
		for (Sym node : order) {
			if (node.extension != null)
				node.extension.install(chains, Objects.requireNonNull(blockOf.get(node)), f);
		}

		// expression edges become links; terminals become feeds. Slot 0 is a chain's running
		// input, which the framework insists arrives through the graph, so a terminal there is
		// delivered per run while other slots are plain side inputs
		var graph = graphs.graph();
		boolean anyLink = false;
		for (Sym node : order) {
			if (node.kind != Sym.Kind.APPLY)
				continue;
			CROperatorBackwardsJac<double[]> op = Objects.requireNonNull(node.op);
			int nodeChain = Objects.requireNonNull(chainOf.get(node));
			int nodeBlock = Objects.requireNonNull(blockOf.get(node));
			for (int slot = 0; slot < op.getInputCount(); slot++) {
				// an extension may supply inputs of its own, which the expression never binds
				if (node.extension != null && node.extension.ownsInput(slot))
					continue;
				Sym arg = node.bound.get(slot);
				if (arg == null) {
					// an unbound slot is only legal when something outside the expression owns it,
					// and the caller has to have said so
					throw new IllegalArgumentException("Unbound input " + slot + " of " + node.name);
				}
				if (arg.kind == Sym.Kind.APPLY) {
					graph.depMid(Objects.requireNonNull(chainOf.get(arg)), 0, nodeChain, 0, slot);
					anyLink = true;
				} else if (slot == 0) {
					// a chain's running input arrives through the graph rather than as a side
					// input, but a parameter there still owns a column
					CRArray<double[]> value = f.terminal(arg);
					f.runningFeeds.add(new SymFunction.RunningFeed(nodeChain, value));
					if (arg.kind == Sym.Kind.PARAM)
						f.paramFeeds.add(new SymFunction.SideFeed(op, nodeBlock, 0, value));
				} else {
					var feed = new SymFunction.SideFeed(op, nodeBlock, slot, f.terminal(arg));
					f.sideFeeds.add(feed);
					if (arg.kind == Sym.Kind.PARAM)
						f.paramFeeds.add(feed);
				}
			}
		}
		// a single-operator expression has no links, and then nothing has registered its chain
		if (!anyLink)
			graph.chain(Objects.requireNonNull(chainOf.get(root)));
		f.graphRoot = graph.fixate();
		return f;
	}

	private void collect( Sym node, List<Sym> order ) {
		if (order.contains(node))
			return;
		for (Sym arg : node.bound.values()) {
			collect(arg, order);
		}
		order.add(node);
	}
}
