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

package org.ddogleg.chainrule;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import lombok.Getter;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.VerbosePrint;
import org.ddogleg.util.VerboseUtils;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.*;

/// Computes Jacobians for a graph of [JacobianSerialChain] chains. Each chain is built there as usual,
/// then a dependency is declared for each value passed from a block in one chain to an input of a block
/// in another. Blocks are the nodes of the graph, so a chain is a path through it.
///
/// The graph is acyclic with a single output. A value may be read in more than one place, including
/// twice inside one chain, so it is not a tree: the derivatives arriving from every consumer of a value
/// are summed before being propagated further, which a graph without that fan out never has to do.
///
/// Two chains can depend on each other, so there is not always an order in which whole chains can run.
/// Blocks are interleaved instead: a chain advances until it reaches a block whose inputs are not ready,
/// then another chain runs, and so on. That order is worked out once by [GraphConstructor#fixate] and
/// replayed afterwards, so the optimization loop pays nothing for it, and a graph that cannot be ordered
/// is rejected where it is defined rather than where it is used. The backwards pass walks the same order
/// in reverse.
///
/// Usage:
/// 1. Build the chains on [#getChains] as normal, including [JacobianSerialChain#setPublishJac]
/// 2. Call [#graph] and declare a dependency for every value crossing between chains
/// 3. Call [GraphConstructor#fixate] to check the graph and work out the order
/// 4. Call [#forward] then [#backwards], neither of which allocates
///
/// A chain whose first block no dependency feeds is given its input by the caller, by name, through
/// [Forward#input]. There is always at least one and there can be several, so which chain a value belongs
/// to is stated rather than worked out.
///
/// A graph can hold a single chain, declared with [GraphConstructor#chain] since no dependency names it.
/// It computes what the chain does on its own, so a caller with a mix of the two does not have to move
/// between the two APIs depending on whether a particular calculation needed a graph.
///
/// Rejected when the graph is defined, since each one is a silent wrong answer otherwise: a cycle, more
/// than one unread output, no output at all, an operator used by two blocks, a dependency arriving
/// at a chain input anywhere but the start of a chain, two dependencies arriving at one input, and a
/// chain already belonging to another graph.
public class JacobianChainGraph<T> implements VerbosePrint {
	/// Shared by every graph that has not been built, so none of them allocate one
	static final int[] EMPTY_TABLE = new int[0];

	@Getter JacobianSerialChain<T> chains;

	/// Created in the constructor, not here: the Graph factory reads `chains`, which a field
	/// initializer would see as null since DogArray instantiates eagerly
	final DogArray<Graph> graphs;

	/// Which graph owns each chain. A chain belongs to at most one, since the capture a graph installs
	/// in [GraphConstructor#fixate] lands on the shared block and a second graph's would replace it,
	/// leaving the first to lose that term out of its sum
	final TIntIntMap chainToGraph = new TIntIntHashMap(
			gnu.trove.impl.Constants.DEFAULT_CAPACITY, gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR, -1, -1);

	/// Set by [Forward#run]. Written by the last block of the root chain
	@Nullable CRArray<T> graphOutput;

	/// What the chain was using before the graph borrowed it
	@SuppressWarnings("NullAway.Init") CRArray<T> loanedInputs;
	@SuppressWarnings("NullAway.Init") CRMatrix<T> loanedDout;

	/// Storage for forward pass. Recycled to avoid memory creation.
	Forward forward = new Forward();

	@Nullable PrintStream verbose;

	public JacobianChainGraph( MathCRMatrix<T> math ) {
		chains = new JacobianSerialChain<>(math);
		graphs = new DogArray<>(Graph::new, Graph::reset);
	}

	/// Used to construct a new graph
	public GraphConstructor graph() {
		return new GraphConstructor();
	}

	/// Used to run the graph in forward direction. Returns a helper that lets you specify block\[0\] inputs
	/// for chains that require them
	///
	/// @param graphID Which graph is going to be run.
	public Forward forward( int graphID ) {
		return forward.initialize(graphs.get(graphID));
	}

	/// Size of the graph's output, which the last block of the root chain produces
	public int getOutputSize( int graphID ) {
		return graphs.get(graphID).outputSize;
	}

	void forwardSteps( Graph graph ) {
		for (int i = 0; i < graph.steps.size(); i++) {
			Step s = graph.steps.get(i);
			select(graph, s.chain);

			// dependencies arriving at anything other than the chain input are handed over directly
			for (int j = 0; j < s.sideInputs.size(); j++) {
				Link link = s.sideInputs.get(j);
				link.dstOp.setInput(link.dstInput, link.source.value);
			}

			// The values crossing between chains are what a graph adds over a chain, and nothing else
			// prints them. The chain reports what each block produced
			if (verbose != null) {
				verbose.printf("forward chain=%d block=%d\n", s.chain, s.block);
				// only the start of a chain takes its running value from outside. Later blocks get it
				// from the block before, which already reported it
				if (s.block == 0) {
					int size = chains.op(s.chain, s.block).getInputSize(chains.chainInputIdx(s.chain, s.block));
					verbose.printf("_ in       %s\n", Objects.requireNonNull(s.input()).toStringD("%.3e", size));
				}
				for (int j = 0; j < s.sideInputs.size(); j++) {
					Link link = s.sideInputs.get(j);
					verbose.printf("_ in[%2d]   %s\n", link.dstInput,
							link.source.value.toStringD("%.3e", link.dstOp.getInputSize(link.dstInput)));
				}
			}

			chains.forwardBlock(s.chain, s.block, s.input(), s.output());
		}
	}

	/// Computes the Jacobian of every parameter in the graph. [#forward] must have run first
	public void backwards( int graphID ) {
		Graph graph = graphs.get(graphID);

		borrow();
		try {
			backwardsSteps(graph);
		} finally {
			giveBack();
		}
	}

	void backwardsSteps( Graph graph ) {
		for (int i = 0; i < graph.sources.size(); i++) {
			chains.math.zero(graph.sources.get(i).jac);
		}

		for (int i = graph.steps.size() - 1; i >= 0; i--) {
			Step s = graph.steps.get(i);
			select(graph, s.chain);

			seedDout(s);

			if (verbose != null)
				verbose.printf("backwards chain=%d block=%d\n", s.chain, s.block);

			chains.backwardBlock(s.chain, s.block);

			// A chain's first block leaves the derivative of its chain input in dout. Where that input
			// came from decides who receives it
			if (s.block == 0) {
				if (s.chainInput != null)
					chains.math.addTo(s.chainInput.source.jac, chains.dout);
				else
					chains.publishChainInput(s.chain);
			}
		}
	}

	/// Sets the derivative this block's output is measured against. Every consumer has already run, so
	/// anything reading it from another chain has finished contributing
	void seedDout( Step s ) {
		if (s.root) {
			// this is the graph's output, so the derivative relative to it is the identity
			int size = chains.op(s.chain, s.block).getOutputSize();
			chains.dout.reshape(size, size);
			chains.math.zero(chains.dout);
			chains.math.diag(1, chains.dout);
		} else if (s.source != null) {
			// a block at the end of its chain has no other source, otherwise this adds to what the
			// rest of the chain already contributed
			if (s.lastInChain)
				chains.dout.setTo(s.source.jac);
			else
				chains.math.addTo(chains.dout, s.source.jac);
		}
	}

	/// The graph runs the chain out of its own storage, one set per chain. Whatever the chain was using
	/// is put back afterwards, otherwise it would be left pointing at a buffer belonging to the graph
	/// and sized for some other chain
	void borrow() {
		loanedInputs = chains.savedInputs;
		loanedDout = chains.dout;
	}

	void giveBack() {
		chains.savedInputs = loanedInputs;
		chains.dout = loanedDout;
	}

	/// Points the chain at the storage belonging to the chain about to run. Both carry their position,
	/// so swapping them is what lets two chains be part way through at the same time
	void select( Graph graph, int chainID ) {
		chains.savedInputs = graph.savedInputs.get(chainID);
		chains.dout = graph.douts.get(chainID);
	}

	@Override public void setVerbose( @Nullable PrintStream out, @Nullable Set<String> configuration ) {
		this.verbose = VerboseUtils.addPrefix(this, out);
	}

	/// Used to define an in-graph using dependencies between chains
	public class GraphConstructor {
		final List<Link> links = new ArrayList<>();
		final TIntHashSet declared = new TIntHashSet();

		/// Declares a chain to be part of the graph. Only needed for one no dependency names, which
		/// means a graph of a single chain: the same thing the chain does on its own, reached through
		/// the graph so that a caller does not have to switch between the two
		public GraphConstructor chain( int chainID ) {
			declared.add(chainID);
			return this;
		}

		/// Specifies a dependency where output is taken from any block inside the source chain and placed
		/// into an input in any block in the output chain
		public GraphConstructor depMid( int chainSrc, int blockSrc, int chainDst, int blockDst, int blockDstInput ) {
			links.add(new Link(chainSrc, blockSrc, chainDst, blockDst, blockDstInput));
			return this;
		}

		/// Specifies a simple dependency where the output from the source chain feeds into the first block
		/// in the destination chain. Shorthand for calling [#depMid].
		public GraphConstructor depOut( int chainSrc, int chainDst, int dstInput ) {
			return depMid(chainSrc, 0, chainDst, 0, dstInput);
		}

		/// Creates the graph and verifies its structure
		///
		/// @return The graph ID for the new graph
		public int fixate() {
			Graph graph = graphs.grow();
			graph.build(links, declared);

			// claimed only once the graph is known to be good, so that one rejected for a cycle does
			// not hold onto chains the corrected version needs
			int graphID = graphs.size - 1;
			for (int chain : graph.members) {
				chainToGraph.put(chain, graphID);
			}
			return graphID;
		}
	}

	/// A block whose output another chain reads. Shared by every link that reads it, which is what makes
	/// the derivatives from each of them add up in one place
	class Source {
		final int chain, block;
		/// The output, copied here by the forward pass
		final CRArray<T> value;
		/// Sum of the derivatives arriving from every consumer
		final CRMatrix<T> jac;

		Source( int chain, int block, int size, int rootSize ) {
			this.chain = chain;
			this.block = block;
			this.value = chains.math.createArray(size);
			this.jac = chains.math.createMatrix(rootSize, size);
		}
	}

	/// One value passed from a block in one chain to an input of a block in another
	class Link {
		final int chainSrc, blockSrc, chainDst, blockDst, dstInput;
		@SuppressWarnings("NullAway.Init") Source source;
		/// Looked up once so the forward pass does not have to
		@SuppressWarnings("NullAway.Init") CROperatorBackwardsJac<T> dstOp;

		Link( int chainSrc, int blockSrc, int chainDst, int blockDst, int dstInput ) {
			this.chainSrc = chainSrc;
			this.blockSrc = blockSrc;
			this.chainDst = chainDst;
			this.blockDst = blockDst;
			this.dstInput = dstInput;
		}

		/// String with the dst address
		public String dstString() {
			return "chain=" + chainDst + " block=" + blockDst + " input=" + dstInput;
		}
	}

	/// One block of one chain, in the order it runs
	class Step {
		final int chain, block;
		/// True when this block produces the graph's output, where the backwards pass starts
		boolean root;
		/// True when no later block in this chain reads its output
		boolean lastInChain;
		/// Set when another chain reads this block's output
		@Nullable Source source;
		/// Set when this block's chain input comes from another chain
		@Nullable Link chainInput;
		/// Dependencies arriving at an input other than the chain input
		final List<Link> sideInputs = new ArrayList<>();
		/// Set by [Forward#input] when this block starts a chain no dependency feeds
		@Nullable CRArray<T> externalInput;

		Step( int chain, int block ) {
			this.chain = chain;
			this.block = block;
		}

		/// Only the first block of a chain reads this, so a later block having nothing to return is
		/// not a mistake: it takes its input from where the block before left it
		@Nullable CRArray<T> input() {
			if (chainInput != null)
				return chainInput.source.value;
			if (block == 0 && externalInput == null)
				throw new IllegalArgumentException("No input given to chain " + chain);
			return externalInput;
		}

		@Nullable CRArray<T> output() {
			if (root)
				return graphOutput;
			return source == null ? null : source.value;
		}
	}

	class Graph {
		final List<Step> steps = new ArrayList<>();
		final List<Source> sources = new ArrayList<>();
		final List<Link> links = new ArrayList<>();
		/// The steps that start a chain no dependency feeds, so the caller has to supply their input.
		/// Kept apart from [#steps] so that clearing and setting them costs nothing per block
		final List<Step> inputSteps = new ArrayList<>();

		/// Chain ID to its index in [#inputSteps], -1 where a dependency feeds the chain. Holds indices
		/// rather than the steps themselves because [Step] belongs to a generic class and cannot be
		/// put in an array
		int[] chainToInput = EMPTY_TABLE;

		/// Indexed by chain ID, so only the chains this graph uses are filled in
		final DogArray<CRArray<T>> savedInputs = new DogArray<>(() -> chains.math.createArray(0));
		final DogArray<CRMatrix<T>> douts = new DogArray<>(() -> chains.math.createMatrix(0, 0));

		/// Every chain in the graph, kept so that [#reset] can release them
		int[] members = EMPTY_TABLE;

		/// Size of the value the root chain's last block produces, which is the graph's output
		int outputSize;

		void build( List<Link> definition, TIntHashSet declared ) {
			links.addAll(definition);
			members = chainMembers(declared);

			checkChainOwnership();
			checkNoRecycledOperators(members);
			checkChainInputConflict();
			checkDuplicateInputs();

			int root = findRoot(members);
			outputSize = chains.op(root, chains.chainSize(root) - 1).getOutputSize();

			allocate(members, outputSize);
			schedule(members);
			describeSteps(root);
		}

		/// Every chain a dependency names or the caller declared, in ascending order
		int[] chainMembers( TIntHashSet declared ) {
			var set = new TIntHashSet(declared);
			for (int i = 0; i < links.size(); i++) {
				Link link = links.get(i);
				set.add(link.chainSrc);
				set.add(link.chainDst);
			}
			int[] found = set.toArray();
			Arrays.sort(found);
			return found;
		}

		/// A chain in two graphs would have the second graph's captures replace the first's, and the
		/// first would then be missing a term from its sum without any sign of it
		void checkChainOwnership() {
			for (int chain : members) {
				int owner = chainToGraph.get(chain);
				if (owner != -1)
					throw new IllegalArgumentException(
							"Chain " + chain + " already belongs to graph " + owner);
			}
		}

		/// Two chains in a graph are part way through at the same time, so an operator shared between
		/// them would have the second use overwrite the first
		void checkNoRecycledOperators( int[] members ) {
			var seen = new HashSet<CROperatorBackwardsJac<T>>();
			for (int chain : members) {
				for (int block = 0; block < chains.chainSize(chain); block++) {
					CROperatorBackwardsJac<T> op = chains.op(chain, block);
					if (seen.contains(op))
						throw new IllegalArgumentException(
								"Same operator used twice in the graph, at chain " + chain + " block " + block);
					seen.add(op);
				}
			}
		}

		/// Arriving at a chain input only makes sense at the start of a chain, since anywhere else the
		/// previous block already supplies it
		void checkChainInputConflict() {
			for (Link link : links) {
				if (link.dstInput != chains.chainInputIdx(link.chainDst, link.blockDst))
					continue;
				if (link.blockDst != 0)
					throw new IllegalArgumentException(
							"User specified input to a block conflicts with the chain previous block input. " + link.dstString());
			}
		}

		/// Both passes resolve two dependencies on one input the same way, by letting the later
		/// overwrite the earlier, so the graph quietly becomes a different but self-consistent one than
		/// the user described and no later check can tell. The dropped dependency is not even inert:
		/// [#ready] still waits on it, so it can order the blocks or refuse to
		void checkDuplicateInputs() {
			for (int i = 0; i < links.size(); i++) {
				Link a = links.get(i);
				for (int j = i + 1; j < links.size(); j++) {
					Link b = links.get(j);
					if (a.chainDst == b.chainDst && a.blockDst == b.blockDst && a.dstInput == b.dstInput)
						throw new IllegalArgumentException(
								"Two dependencies arrive at the same input. " + a.dstString());
				}
			}
		}

		/// The one chain whose final output nothing reads. That is the graph's output
		int findRoot( int[] members ) {
			int root = -1;
			for (int chain : members) {
				if (isConsumed(chain))
					continue;
				if (root != -1)
					throw new IllegalArgumentException(
							"More than one chain's output is unused: " + root + " and " + chain);
				root = chain;
			}
			if (root == -1)
				throw new IllegalArgumentException("No chain produces the graph's output");
			return root;
		}

		/// True if something reads the chain's last block. Reading an earlier block leaves the output free
		boolean isConsumed( int chain ) {
			for (Link link : links) {
				if (link.chainSrc == chain && link.blockSrc == chains.chainSize(chain) - 1)
					return true;
			}
			return false;
		}

		/// Storage for everything the two passes need, so that neither of them allocates
		void allocate( int[] members, int rootSize ) {
			savedInputs.resize(highest(members) + 1);
			douts.resize(highest(members) + 1);

			for (int chain : members) {
				savedInputs.get(chain).reserve(chains.savedLength(chain));
			}

			// one place per block that is read, which is what makes the derivatives from several
			// consumers add up together
			for (Link link : links) {
				link.source = source(link.chainSrc, link.blockSrc, rootSize);
				link.dstOp = chains.op(link.chainDst, link.blockDst);
			}
		}

		/// Arrays are indexed by chain ID, so they are sized to the largest one the graph names
		int highest( int[] members ) {
			int highest = 0;
			for (int chain : members) {
				highest = Math.max(highest, chain);
			}
			return highest;
		}

		Source source( int chain, int block, int rootSize ) {
			for (Source s : sources) {
				if (s.chain == chain && s.block == block)
					return s;
			}
			var created = new Source(chain, block, chains.op(chain, block).getOutputSize(), rootSize);
			sources.add(created);
			return created;
		}

		/// Advances every chain as far as its dependencies allow, then goes around again. A sweep that
		/// gets nowhere means the graph has a cycle
		void schedule( int[] members ) {
			var done = new boolean[highest(members) + 1][];
			int remaining = 0;
			for (int chain : members) {
				done[chain] = new boolean[chains.chainSize(chain)];
				remaining += done[chain].length;
			}

			var next = new int[done.length];
			while (remaining > 0) {
				boolean progress = false;
				for (int chain : members) {
					while (next[chain] < chains.chainSize(chain) && ready(done, chain, next[chain])) {
						steps.add(new Step(chain, next[chain]));
						done[chain][next[chain]] = true;
						next[chain]++;
						remaining--;
						progress = true;
					}
				}
				if (!progress)
					throw new IllegalArgumentException("Dependencies cannot be ordered, the graph has a cycle");
			}
		}

		/// True when everything this block reads from another chain has already run
		boolean ready( boolean[][] done, int chain, int block ) {
			for (Link link : links) {
				if (link.chainDst != chain || link.blockDst != block)
					continue;
				if (!done[link.chainSrc][link.blockSrc])
					return false;
			}
			return true;
		}

		/// Works out what each step has to do, so that neither pass has to search the links
		void describeSteps( int root ) {
			for (Step s : steps) {
				s.root = s.chain == root && s.block == chains.chainSize(root) - 1;
				s.lastInChain = s.block == chains.chainSize(s.chain) - 1;

				for (Link link : links) {
					if (link.chainSrc == s.chain && link.blockSrc == s.block)
						s.source = link.source;

					if (link.chainDst != s.chain || link.blockDst != s.block)
						continue;

					if (link.dstInput == chains.chainInputIdx(s.chain, s.block)) {
						s.chainInput = link;
						chains.forceChainInputJac(s.chain, s.block);
					} else {
						s.sideInputs.add(link);
						// the derivative of a side input is published rather than left in dout, so it
						// is collected through the hook the serial chain already has for parameters
						chains.setCapture(s.chain, s.block, link.dstInput,
								( jac ) -> chains.math.addTo(link.source.jac, jac));
					}
				}

				// whatever no dependency reached has to come from the caller. There is always at
				// least one, since the chain that runs first has nothing upstream of it
				if (s.block == 0 && s.chainInput == null)
					inputSteps.add(s);
			}

			indexInputsByChain();
		}

		/// Sized off the chains that take an input, not off any other array, so nothing here depends on
		/// what ran before it. A chain ID past the end reads as one that takes no input, which is what a
		/// chain outside the graph is
		void indexInputsByChain() {
			int highest = 0;
			for (Step s : inputSteps) {
				highest = Math.max(highest, s.chain);
			}

			chainToInput = new int[highest + 1];
			Arrays.fill(chainToInput, -1);
			for (int i = 0; i < inputSteps.size(); i++) {
				chainToInput[inputSteps.get(i).chain] = i;
			}
		}

		void reset() {
			steps.clear();
			sources.clear();
			links.clear();
			inputSteps.clear();
			chainToInput = EMPTY_TABLE;
			// a recycled graph gives its chains back, so they can be used by whatever replaces it
			for (int chain : members) {
				chainToGraph.remove(chain);
			}
			members = EMPTY_TABLE;
			outputSize = 0;
			savedInputs.reset();
			douts.reset();
		}
	}

	/// Helper class for running the forward pass. Lets you specify and run using a fluent design.
	///
	/// There is one instance, handed out again by every call to [JacobianChainGraph#forward], so use it and
	/// run it in one expression. Holding onto one across a second call leaves both names pointing at the
	/// later graph
	public class Forward {
		@SuppressWarnings("NullAway.Init") Graph graph;

		Forward initialize( Graph graph ) {
			this.graph = graph;
			// only the blocks that take an input from the caller, so this costs nothing per block
			for (int i = 0; i < graph.inputSteps.size(); i++) {
				graph.inputSteps.get(i).externalInput = null;
			}
			return this;
		}

		/// Provides an input for the specified chain. This is only done with the chain has input dependencies
		/// already specified by the graph
		public Forward input( int chainID, CRArray<T> array ) {
			int[] table = graph.chainToInput;
			if (chainID < 0 || chainID >= table.length || table[chainID] < 0)
				throw new IllegalArgumentException(
						"Chain " + chainID + " takes no input from the caller, a dependency already feeds it");

			graph.inputSteps.get(table[chainID]).externalInput = array;
			return this;
		}

		public void run() {run(null);}

		/// Runs every block in the order worked out by [GraphConstructor#fixate]. A chain left without an
		/// input is caught when its first block is reached
		///
		/// @param output Storage for the graph's output. If null the final block is skipped
		public void run( @Nullable CRArray<T> output ) {
			graphOutput = output;
			borrow();
			try {
				forwardSteps(graph);
			} finally {
				giveBack();
			}
		}
	}
}
