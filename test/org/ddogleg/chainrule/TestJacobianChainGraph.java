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

import gnu.trove.set.hash.TIntHashSet;
import org.ddogleg.DDoglegJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/// The graph these tests build has the same shape as the one lens aberration needs: a chain that feeds a
/// second chain from the middle, with the second chain feeding back into the first further along.
///
/// ```
///   P:  opA --> opB --------> opC --> output
///                 \            ^
///                  \--> opD --/
/// ```
///
/// opB's output goes two places, so the derivatives arriving from opC and from opD have to be summed.
/// That is the part most likely to be wrong, and a numerical Jacobian is the only thing that catches it.
class TestJacobianChainGraph extends DDoglegJUnit {
	MathCRMatrix<double[]> math = new MathCRMatrixD();

	CROperatorPolyA32 opA = new CROperatorPolyA32(); // [3,3] -> 2
	CROperatorPolyC23 opB = new CROperatorPolyC23(); // [2,1] -> 3
	CROperatorPolyB33 opC = new CROperatorPolyB33(); // [3,3] -> 3
	CROperatorPolyB33 opD = new CROperatorPolyB33(); // [3,3] -> 3

	/// opA input 1 at 0, opB input 1 at 3, opD input 0 at 4
	static final int NUM_PARAM = 7;
	static final int OUTPUT = 3;
	static final int INPUT = 3;

	JacobianChainGraph<double[]> create() {
		var alg = new JacobianChainGraph<>(math);
		alg.chains.add(opA).in(1, 0);
		alg.chains.add(opB).in(1, 3);
		alg.chains.add(opC);                    // input 1 comes from the other chain
		alg.chains.add(opD).inputIdx(1).in(0, 4);
		alg.chains.chain(0, 1, 2);
		alg.chains.chain(3);
		return alg;
	}

	int buildGraph( JacobianChainGraph<double[]> alg ) {
		return alg.graph()
				.depMid(0, 1, 1, 0, 1)   // opB's output feeds opD
				.depMid(1, 0, 0, 2, 1)   // opD's output feeds opC's second input
				.fixate();
	}

	/// Blocks have to run in an order where everything they read has already been computed
	@Test void schedule() {
		var alg = create();
		int graph = buildGraph(alg);

		var found = new StringBuilder();
		for (var s : alg.graphs.get(graph).steps)
			found.append(s.chain).append('.').append(s.block).append(' ');

		assertEquals("0.0 0.1 1.0 0.2 ", found.toString());
	}

	/// Compare the whole graph's Jacobian against numerical differentiation
	@Test void numerical() {
		var alg = create();
		int graph = buildGraph(alg);

		double[] params = random(NUM_PARAM);
		var input = new CRArray<>(random(INPUT), 0);

		var found = new DMatrixRMaj(OUTPUT, NUM_PARAM);
		alg.chains.setPublishJac(( col0, jac ) -> {
			for (int row = 0; row < jac.rows; row++) {
				for (int col = 0; col < jac.cols; col++) {
					found.add(row, col0 + col, jac.d(row, col));
				}
			}
		});

		var output = new CRArray<>(new double[OUTPUT], 0);
		forward(alg, graph, params, input, output);
		alg.backwards(graph);

		DMatrixRMaj expected = numericalJacobian(alg, graph, params, input);

		for (int row = 0; row < OUTPUT; row++) {
			for (int col = 0; col < NUM_PARAM; col++) {
				assertEquals(expected.get(row, col), found.get(row, col), 1e-4,
						"row=" + row + " col=" + col);
			}
		}
	}

	/// The graph's output has to be the same as running the blocks by hand in the same order
	@Test void forward_matchesByHand() {
		var alg = create();
		int graph = buildGraph(alg);
		double[] params = random(NUM_PARAM);
		var input = new CRArray<>(random(INPUT), 0);

		var output = new CRArray<>(new double[OUTPUT], 0);
		forward(alg, graph, params, input, output);

		// opA -> opB -> opD, then opC with opB's output and opD's output
		var wrap = new CRArray<>(params, 0);
		opA.setInput(0, input);
		wrap.offset = 0; opA.setInput(1, wrap);
		var outA = new CRArray<>(new double[2], 0);
		opA.forward(outA);

		opB.setInput(0, outA);
		wrap.offset = 3; opB.setInput(1, wrap);
		var outB = new CRArray<>(new double[3], 0);
		opB.forward(outB);

		wrap.offset = 4; opD.setInput(0, wrap);
		opD.setInput(1, outB);
		var outD = new CRArray<>(new double[3], 0);
		opD.forward(outD);

		opC.setInput(0, outB);
		opC.setInput(1, outD);
		var expected = new CRArray<>(new double[3], 0);
		opC.forward(expected);

		for (int i = 0; i < OUTPUT; i++)
			assertEquals(expected.d(i), output.d(i), 1e-12);
	}

	/// A value can also be read twice inside one chain, which is the same summation with only one
	/// chain involved. Here opB feeds both of opC's inputs
	///
	/// ```
	///   opA --> opB --> opC --> output
	///             \____^
	/// ```
	@Test void skipConnection() {
		var alg = new JacobianChainGraph<>(math);
		alg.chains.add(opA).in(1, 0);
		alg.chains.add(opB).in(1, 3);
		alg.chains.add(opC);
		alg.chains.chain(0, 1, 2);
		int graph = alg.graph().depMid(0, 1, 0, 2, 1).fixate();

		double[] params = random(4);
		var input = new CRArray<>(random(INPUT), 0);

		var found = new DMatrixRMaj(OUTPUT, params.length);
		alg.chains.setPublishJac(( col0, jac ) -> {
			for (int row = 0; row < jac.rows; row++) {
				for (int col = 0; col < jac.cols; col++) {
					found.add(row, col0 + col, jac.d(row, col));
				}
			}
		});

		var output = new CRArray<>(new double[OUTPUT], 0);
		alg.chains.insertInputs(0, params);
		alg.forward(graph).input(0, input).run(output);
		alg.backwards(graph);

		var expected = new DMatrixRMaj(OUTPUT, params.length);
		var plus = new CRArray<>(new double[OUTPUT], 0);
		var minus = new CRArray<>(new double[OUTPUT], 0);
		for (int i = 0; i < params.length; i++) {
			double original = params[i];
			params[i] = original - 1e-6;
			alg.chains.insertInputs(0, params);
			alg.forward(graph).input(0, input).run(minus);
			params[i] = original + 1e-6;
			alg.chains.insertInputs(0, params);
			alg.forward(graph).input(0, input).run(plus);
			params[i] = original;
			for (int row = 0; row < OUTPUT; row++)
				expected.set(row, i, (plus.d(row) - minus.d(row))/2e-6);
		}

		for (int row = 0; row < OUTPUT; row++)
			for (int col = 0; col < params.length; col++)
				assertEquals(expected.get(row, col), found.get(row, col), 1e-4, "row=" + row + " col=" + col);
	}

	/// A graph of one chain has to compute exactly what that chain does on its own, forward and
	/// backwards, since the whole point is that a caller can stop choosing between the two
	@Test void singleChainMatchesTheChainAlone() {
		double[] params = random(NUM_PARAM);
		var input = new CRArray<>(random(INPUT), 0);

		// through the graph
		var alg = create();
		int graph = alg.graph().chain(0).fixate();
		var viaGraph = new DMatrixRMaj(OUTPUT, NUM_PARAM);
		alg.chains.setPublishJac(( col0, jac ) -> collect(viaGraph, col0, jac));

		var outGraph = new CRArray<>(new double[OUTPUT], 0);
		alg.chains.insertInputs(0, params);
		alg.forward(graph).input(0, input).run(outGraph);
		alg.backwards(graph);

		// the same chain, run directly, on operators that have never seen a graph
		var bare = create();
		var viaChain = new DMatrixRMaj(OUTPUT, NUM_PARAM);
		bare.chains.setPublishJac(( col0, jac ) -> collect(viaChain, col0, jac));

		var outChain = new CRArray<>(new double[OUTPUT], 0);
		bare.chains.insertInputs(0, params);
		bare.chains.forward(0, input, outChain);
		bare.chains.backwards(0);

		for (int i = 0; i < OUTPUT; i++)
			assertEquals(outChain.d(i), outGraph.d(i), UtilEjml.TEST_F64);
		assertTrue(MatrixFeatures_DDRM.isIdentical(viaChain, viaGraph, UtilEjml.TEST_F64));

		// and the graph knows how much it produces without anyone reaching through to the chain
		assertEquals(bare.chains.getOutputSize(0), alg.getOutputSize(graph));
	}

	void collect( DMatrixRMaj dst, int col0, CRMatrix<double[]> jac ) {
		for (int row = 0; row < jac.rows; row++)
			for (int col = 0; col < jac.cols; col++)
				dst.add(row, col0 + col, jac.d(row, col));
	}

	/// A chain the links already name does not have to be declared, and declaring it anyway is not
	/// an error. The graph is the same either way
	@Test void declaringALinkedChainChangesNothing() {
		var alg = create();
		var plain = alg.graphs.get(buildGraph(alg));

		var alg2 = create();
		var declared = alg2.graphs.get(alg2.graph().chain(0).chain(1)
				.depMid(0, 1, 1, 0, 1)
				.depMid(1, 0, 0, 2, 1).fixate());

		assertArrayEquals(plain.members, declared.members);
		assertEquals(plain.steps.size(), declared.steps.size());
		for (int i = 0; i < plain.steps.size(); i++) {
			assertEquals(plain.steps.get(i).chain, declared.steps.get(i).chain);
			assertEquals(plain.steps.get(i).block, declared.steps.get(i).block);
		}
	}

	/// A chain in two graphs would have the second graph's captures replace the first's
	@Test void chainCannotBeInTwoGraphs() {
		var alg = create();
		alg.graph().chain(0).fixate();

		assertThrows(IllegalArgumentException.class, () -> alg.graph().chain(0).fixate());
		// reached through a link rather than a declaration, same answer
		assertThrows(IllegalArgumentException.class, () -> alg.graph()
				.depMid(0, 1, 1, 0, 1)
				.depMid(1, 0, 0, 2, 1).fixate());

		// a chain no graph has taken is still free
		alg.graph().chain(1).fixate();
	}

	/// A graph that was rejected must not hold onto the chains the corrected one needs
	@Test void rejectedGraphClaimsNothing() {
		var alg = create();
		assertThrows(IllegalArgumentException.class, () -> alg.graph()
				.depMid(0, 0, 1, 0, 1)
				.depMid(1, 0, 0, 0, 1)   // a cycle
				.fixate());

		// chains 0 and 1 were named by the graph that failed, and both are still available
		buildGraph(alg);
	}

	/// Two chains that each wait on the other can never start
	@Test void cycleIsRejected() {
		var alg = create();
		assertThrows(IllegalArgumentException.class, () -> alg.graph()
				.depMid(0, 0, 1, 0, 1)
				.depMid(1, 0, 0, 0, 1)   // back into the block that feeds it
				.fixate());
	}

	/// Two chains can each take their own input from the caller, told apart by name. Here nothing
	/// feeds opD, so it needs one as well as opA
	@Test void twoInputsAreNamed() {
		var alg = create();
		int graph = alg.graph()
				.depMid(1, 0, 0, 2, 1)   // opD feeds opC, but nothing feeds opD
				.fixate();
		alg.chains.setPublishJac(( col0, jac ) -> {});

		double[] params = random(NUM_PARAM);
		alg.chains.insertInputs(0, params);
		alg.chains.insertInputs(1, params);

		// opD takes its chain input at index 1, which is 3 long, unlike opA's 3-long input at 0
		var inA = new CRArray<>(random(INPUT), 0);
		var inD = new CRArray<>(random(3), 0);
		var output = new CRArray<>(new double[OUTPUT], 0);
		alg.forward(graph).input(0, inA).input(1, inD).run(output);

		// opD ran on the input it was given, not on opA's
		var expectedD = new CRArray<>(new double[3], 0);
		var wrap = new CRArray<>(params, 4);
		opD.setInput(0, wrap);
		opD.setInput(1, inD);
		opD.forward(expectedD);

		var expected = new CRArray<>(new double[OUTPUT], 0);
		opC.setInput(1, expectedD);
		opC.forward(expected);
		for (int i = 0; i < OUTPUT; i++)
			assertEquals(expected.d(i), output.d(i), 1e-12);
	}

	/// A chain the caller never supplied is caught when its first block is reached, rather than
	/// silently reusing whatever another chain was given
	@Test void missingInputRejected() {
		var alg = create();
		int graph = alg.graph().depMid(1, 0, 0, 2, 1).fixate();
		alg.chains.setPublishJac(( col0, jac ) -> {});
		alg.chains.insertInputs(0, random(NUM_PARAM));

		var input = new CRArray<>(random(INPUT), 0);
		var output = new CRArray<>(new double[OUTPUT], 0);
		assertThrows(IllegalArgumentException.class,
				() -> alg.forward(graph).input(0, input).run(output));
	}

	/// A stale input from the previous run cannot stand in for one the caller forgot
	@Test void inputsClearedBetweenRuns() {
		var alg = create();
		int graph = alg.graph().depMid(1, 0, 0, 2, 1).fixate();
		alg.chains.setPublishJac(( col0, jac ) -> {});
		alg.chains.insertInputs(0, random(NUM_PARAM));
		alg.chains.insertInputs(1, random(NUM_PARAM));

		var inA = new CRArray<>(random(INPUT), 0);
		var inD = new CRArray<>(random(3), 0);
		var output = new CRArray<>(new double[OUTPUT], 0);
		alg.forward(graph).input(0, inA).input(1, inD).run(output);

		assertThrows(IllegalArgumentException.class,
				() -> alg.forward(graph).input(0, inA).run(output));
	}

	/// Naming a chain a dependency already feeds is the same mistake in mirror image
	@Test void inputToSuppliedChainRejected() {
		var alg = create();
		int graph = buildGraph(alg);

		var input = new CRArray<>(random(INPUT), 0);
		assertThrows(IllegalArgumentException.class, () -> alg.forward(graph).input(1, input));

		// a chain the graph does not contain lands in the same place, rather than off the end of
		// the lookup table
		assertThrows(IllegalArgumentException.class, () -> alg.forward(graph).input(7, input));
		assertThrows(IllegalArgumentException.class, () -> alg.forward(graph).input(-1, input));
	}

	/// Every chain that takes an input is reachable by name, and nothing else is
	@Test void indexInputsByChain() {
		var alg = create();

		// only chain 0 needs one, but the table is still long enough to answer for chain 1
		var g = alg.graphs.get(buildGraph(alg));
		assertEquals(1, g.inputSteps.size());
		assertEquals(0, g.chainToInput[0]);
		for (int chain = 1; chain < g.chainToInput.length; chain++)
			assertEquals(-1, g.chainToInput[chain]);

		// nothing feeds opD, so chain 1 takes one as well and both are indexed
		var alg2 = create();
		var g2 = alg2.graphs.get(alg2.graph().depMid(1, 0, 0, 2, 1).fixate());
		assertEquals(2, g2.inputSteps.size());
		assertEquals(2, g2.chainToInput.length);
		for (int chain = 0; chain < 2; chain++)
			assertEquals(chain, g2.inputSteps.get(g2.chainToInput[chain]).chain);
	}

	/// The chain gets its own storage back, so calling it directly afterwards does not write into a
	/// buffer belonging to the graph and sized for a different chain
	@Test void storageIsReturned() {
		var alg = create();
		int graph = buildGraph(alg);
		alg.chains.setPublishJac(( col0, jac ) -> {});

		CRArray<double[]> before = alg.chains.savedInputs;
		CRMatrix<double[]> beforeDout = alg.chains.dout;

		double[] params = random(NUM_PARAM);
		forward(alg, graph, params, new CRArray<>(random(INPUT), 0), new CRArray<>(new double[OUTPUT], 0));
		alg.backwards(graph);

		assertSame(before, alg.chains.savedInputs);
		assertSame(beforeDout, alg.chains.dout);
	}

	/// The graph needs exactly one output. Here chains 1 and 2 both have one nothing reads
	@Test void twoOutputsRejected() {
		var alg = new JacobianChainGraph<>(math);
		alg.chains.add(opA).in(1, 0);
		alg.chains.add(opB).in(1, 3);
		alg.chains.add(opC).in(1, 4);
		alg.chains.chain(0);
		alg.chains.chain(1);
		alg.chains.chain(2);
		assertThrows(IllegalArgumentException.class, () -> alg.graph()
				.depMid(0, 0, 1, 0, 0)
				.depMid(0, 0, 2, 0, 0)
				.fixate());
	}

	/// Operators hold their inputs, so one cannot be part way through in two chains at once
	@Test void recycledOperatorRejected() {
		var alg = new JacobianChainGraph<>(math);
		alg.chains.add(opC);
		alg.chains.add(opC);      // same instance
		alg.chains.chain(0);
		alg.chains.chain(1);
		assertThrows(IllegalArgumentException.class, () -> alg.graph().depMid(0, 0, 1, 0, 1).fixate());
	}

	// ------------------------- the pieces of Graph, on their own

	/// A graph with links but not built, so the individual steps of the build can be exercised
	JacobianChainGraph<double[]>.Graph unbuilt( JacobianChainGraph<double[]> alg, int[]... specs ) {
		JacobianChainGraph<double[]>.Graph g = alg.graphs.grow();
		for (int[] spec : specs)
			g.links.add(alg.new Link(spec[0], spec[1], spec[2], spec[3], spec[4]));
		return g;
	}

	/// Every chain a link names, ascending, without repeats and without the ones in between
	@Test void chainMembers() {
		var none = new TIntHashSet();
		var alg = create();
		alg.chains.chain(0, 1, 2);
		alg.chains.chain(3);

		var g = unbuilt(alg, new int[]{0, 1, 1, 0, 1}, new int[]{1, 0, 0, 2, 1}, new int[]{0, 0, 1, 0, 0});
		assertArrayEquals(new int[]{0, 1}, g.chainMembers(none));

		// a gap between the chains that are named
		var alg2 = create();
		var g2 = unbuilt(alg2, new int[]{0, 0, 3, 0, 1});
		assertArrayEquals(new int[]{0, 3}, g2.chainMembers(none));

		// a declared chain joins the ones the links name, and saying one twice changes nothing
		assertArrayEquals(new int[]{0, 2, 3}, g2.chainMembers(new TIntHashSet(new int[]{2, 3})));
	}

	/// Only reading a chain's last block uses up its output. Reading an earlier one leaves it free
	@Test void isConsumed() {
		var alg = create();
		alg.chains.chain(0, 1, 2);
		alg.chains.chain(3);

		var g = unbuilt(alg, new int[]{0, 1, 1, 0, 1});
		assertFalse(g.isConsumed(0));   // block 1 of a 3 block chain

		var g2 = unbuilt(alg, new int[]{0, 2, 1, 0, 1});
		assertTrue(g2.isConsumed(0));   // block 2 is the last one
	}

	/// Everything is read by something, so nothing is left to be the output
	@Test void findRoot_noneLeft() {
		var alg = create();
		alg.chains.chain(0, 1, 2);
		alg.chains.chain(3);

		var g = unbuilt(alg, new int[]{0, 2, 1, 0, 1}, new int[]{1, 0, 0, 0, 0});
		assertThrows(IllegalArgumentException.class, () -> g.findRoot(new int[]{0, 1}));
	}

	/// Two links reading the same block share one place to collect derivatives, which is what makes
	/// them add up rather than overwrite
	@Test void source_isSharedPerBlock() {
		var alg = create();
		alg.chains.chain(0, 1, 2);
		alg.chains.chain(3);
		var g = unbuilt(alg);

		var first = g.source(0, 1, 3);
		var again = g.source(0, 1, 3);
		var other = g.source(0, 0, 3);

		assertSame(first, again);
		assertNotSame(first, other);
		assertEquals(2, g.sources.size());
		// sized by what the block puts out and what the graph puts out
		assertEquals(3, first.jac.rows);
		assertEquals(3, first.jac.cols);
		assertEquals(2, other.jac.cols);
	}

	/// A block is ready once every block another chain feeds it from has run
	@Test void ready() {
		var alg = create();
		alg.chains.chain(0, 1, 2);
		alg.chains.chain(3);
		var g = unbuilt(alg, new int[]{0, 1, 1, 0, 1});

		var done = new boolean[][]{new boolean[3], new boolean[1]};

		assertTrue(g.ready(done, 0, 0));    // nothing feeds it
		assertFalse(g.ready(done, 1, 0));   // waiting on chain 0 block 1
		done[0][1] = true;
		assertTrue(g.ready(done, 1, 0));
	}

	/// Chains have to be picked up and put down repeatedly, so one sweep is not enough
	@Test void schedule_needsSeveralSweeps() {
		var alg = new JacobianChainGraph<>(math);
		alg.chains.add(opA).in(1, 0);
		alg.chains.add(opB).in(1, 3);
		alg.chains.add(opC);
		alg.chains.add(opD).inputIdx(1).in(0, 4);
		alg.chains.chain(0, 1, 2);
		alg.chains.chain(3);

		var g = unbuilt(alg, new int[]{0, 1, 1, 0, 1}, new int[]{1, 0, 0, 2, 1});
		g.schedule(new int[]{0, 1});

		var order = new StringBuilder();
		for (var s : g.steps)
			order.append(s.chain).append('.').append(s.block).append(' ');

		// chain 0 runs, stops for chain 1, then finishes
		assertEquals("0.0 0.1 1.0 0.2 ", order.toString());
	}

	/// A value from another chain can only arrive at the start of a chain, since anywhere else the
	/// previous block already supplies it
	@Test void checkChainInputConflict() {
		var alg = create();
		alg.chains.chain(0, 1, 2);
		alg.chains.chain(3);

		var g = unbuilt(alg, new int[]{1, 0, 0, 1, 0});
		assertThrows(IllegalArgumentException.class, g::checkChainInputConflict);

		// the same link into the first block is fine
		var ok = unbuilt(alg, new int[]{1, 0, 0, 0, 0});
		ok.checkChainInputConflict();
	}

	/// Both passes let a later dependency overwrite an earlier one on the same input, so the graph
	/// would quietly run as one the user did not describe
	@Test void checkDuplicateInputs() {
		var alg = create();

		// opD and opB both feed opC's second input
		var g = unbuilt(alg, new int[]{1, 0, 0, 2, 1}, new int[]{0, 1, 0, 2, 1});
		assertThrows(IllegalArgumentException.class, g::checkDuplicateInputs);

		// repeating one dependency is harmless today, since both name one source, but it is the
		// same mistake and distinguishing it buys the user nothing
		var repeat = unbuilt(alg, new int[]{1, 0, 0, 2, 1}, new int[]{1, 0, 0, 2, 1});
		assertThrows(IllegalArgumentException.class, repeat::checkDuplicateInputs);

		// one source feeding two different destinations is what the graph is for
		var ok = unbuilt(alg, new int[]{0, 1, 1, 0, 1}, new int[]{0, 1, 0, 2, 1});
		ok.checkDuplicateInputs();
	}

	/// The whole thing is rejected where it is defined, not somewhere downstream
	@Test void duplicateInputsRejectedByFixate() {
		var alg = create();
		assertThrows(IllegalArgumentException.class, () -> alg.graph()
				.depMid(0, 1, 1, 0, 1)   // opB feeds opD
				.depMid(1, 0, 0, 2, 1)   // opD feeds opC's second input
				.depMid(0, 1, 0, 2, 1)   // and so does opB, silently replacing opD
				.fixate());
	}

	void forward( JacobianChainGraph<double[]> alg, int graph, double[] params,
				  CRArray<double[]> input, CRArray<double[]> output ) {
		alg.chains.insertInputs(0, params);
		alg.chains.insertInputs(1, params);
		alg.forward(graph).input(0, input).run(output);
	}

	DMatrixRMaj numericalJacobian( JacobianChainGraph<double[]> alg, int graph, double[] params,
								   CRArray<double[]> input ) {
		var jacobian = new DMatrixRMaj(OUTPUT, params.length);
		var plus = new CRArray<>(new double[OUTPUT], 0);
		var minus = new CRArray<>(new double[OUTPUT], 0);
		double delta = 1e-6;

		for (int i = 0; i < params.length; i++) {
			double original = params[i];
			params[i] = original - delta;
			forward(alg, graph, params, input, minus);
			params[i] = original + delta;
			forward(alg, graph, params, input, plus);
			params[i] = original;

			for (int row = 0; row < OUTPUT; row++) {
				jacobian.set(row, i, (plus.d(row) - minus.d(row))/(2*delta));
			}
		}
		return jacobian;
	}

	double[] random( int length ) {
		var out = new double[length];
		for (int i = 0; i < out.length; i++)
			out[i] = 2.0*rand.nextDouble() - 1.0;
		return out;
	}
}
