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
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.Setter;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.VerbosePrint;
import org.ddogleg.util.VerboseUtils;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Set;

import static gnu.trove.impl.Constants.DEFAULT_CAPACITY;
import static gnu.trove.impl.Constants.DEFAULT_LOAD_FACTOR;

/// Sequential chain of operations for forward and backwards calculations used to compute Jacobians using the
/// chain rule. The first input for all operators is where the output of the previous operator is passed in.
/// All other inputs for an operator must be externally handled. When adding a new operator you then
/// specify where the input came from so that it can be passed into the larger system Jacobian
///
/// Usage:
/// 1. Call [#setPublishJac] to specify how Jacobians are handled
/// 2. Call [#add] for each operator in the chain in order from first to last
/// 3. Call [#in] after a call to [#add] and specify where the input came from
/// 4. Call [#chain] for each chain of ops you wish to process
/// 5. Call [#backwardsF] performs the backward pass to get the Jacobian, which invokes [#forward] internally
///
/// Constraints:
///
/// - An operator instance may appear at most once in a chain.
/// - [#savedInputs] is overwritten on each call to forward.
/// - [#backwards] only produces valid results if the previous call to [#forward] was done on the same chain. Use
///   [#backwardsF] instead to avoid this bookkeeping issue.
///
/// Explanation:
/// - Operators keep their inputs as state, so reusing one would have the second copy overwrite the first,
///   and the backwards pass would then differentiate the wrong inputs. [#chain] rejects it
public class JacobianSerialChain<T> implements VerbosePrint {
	/// When a Jacobian has been computed and needs to be added this function is called. Must be configured.
	/// Called for every input mapped to a parameter by [#in] or [#setInputMap], identified only by that
	/// parameter's index. One handler serves every chain
	@SuppressWarnings("NullAway.Init") @Getter @Setter AddToJacobian<T> publishJac;

	/// Storage for all operations in the chain
	final DogArray<Block<T>> blocks = new DogArray<>(Block::new, Block::reset);

	/// List of chains. A chain is defined by a list of blocks.
	final DogArray<Chain> chains = new DogArray<>(Chain::new, Chain::reset);

	/// Storage for inputs found in the forward pass, and the cursor into it. Not final so that a
	/// caller running several chains at once can swap in a separate array for each
	CRArray<T> savedInputs;

	/// Jacobian from the next operation in the chain. Not final so that a caller running several
	/// chains at once can swap in a separate one for each, see [#savedInputs]
	@Getter CRMatrix<T> dout;
	@Getter final MathCRMatrix<T> math;

	/// Storage for all jacobians
	final DogArray<CRMatrix<T>> inputJacs;

	@Nullable PrintStream verbose;

	public JacobianSerialChain( MathCRMatrix<T> math ) {
		this.math = math;
		this.dout = math.createMatrix(0, 0);
		this.inputJacs = new DogArray<>(() -> math.createMatrix(0, 0), CRMatrix::reset);
		this.savedInputs = math.createArray(0);
	}

	/// Adds a new operation to the chain and internally creates a block
	public JacobianSerialChain<T> add( CROperatorBackwardsJac<T> op ) {
		blocks.grow().op = op;
		return this;
	}

	/// Returns the ID for the most recently added block
	public int lastBlockId() {
		return blocks.size - 1;
	}

	/// Used to specify which input should the output from the previous block be passed into. By default, input\[0\]
	/// is used. This allows you to specify input\[i\]. Modifies the most recently added block.
	public JacobianSerialChain<T> inputIdx( int idx ) {
		blocks.getTail().inputChainIdx = idx;
		return this;
	}

	/// Specifies if an input is being optimized and where the optimized parameter is stored in the system's parameter
	/// vector
	///
	/// @param input Which input in the array corresponds to this parameter
	/// @param parameter Index in the entire system's parameter vector
	public JacobianSerialChain<T> in( int input, int parameter ) {
		setInputMap(blocks.size - 1, input, parameter);
		return this;
	}

	/// Used to set the input mapping for a block after it was first added
	public void setInputMap( int blockIdx, int input, int parameter ) {
		blocks.get(blockIdx).inputToParameter.put(input, parameter);
		blocks.get(blockIdx).setCapture(input, ( jac ) -> publishJac.addJacobian(parameter, jac));
	}

	/// Specifies a custom function for handling a block's Jacobian. This is needed when there are multiple
	/// chains that form a graph. I.e. the output of a block in chain A is an input to a block in chain B.
	///
	/// A capture on the block's chain input is called once at the end of [#backwards]. Every other input's
	/// capture is called as its own block is differentiated
	public void setCapture( int blockIdx, int input, CaptureJacobian<T> capture ) {
		blocks.get(blockIdx).setCapture(input, capture);
	}

	/// Specifies a block's pre-forward hook. See [Block#preForward]
	public void setPreForward( int blockIdx, PreForward<T> preForward ) {
		blocks.get(blockIdx).preForward = preForward;
	}

	/// Creates a new chain that can be invoked later on. You pass in IDs for all the blocks you wish to add. It will
	/// then check the sequence for consistency and precompute anything that's needed. Returns the ID of the new chain
	/// that you will need to call it late ron.
	///
	/// @param ids Sequence of block IDs.
	public int chain( int... ids ) {
		// Sanity check the ids to see that they exist
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] < 0 || ids[i] >= blocks.size())
				throw new IllegalArgumentException("Invalid block ID. " + ids[i]);
		}
		checkNoRecycledOperators(ids);

		Chain chain = chains.grow().init(ids);

		// Save how long an array needs to be to store all the inputs
		int savedInputSize = chain.block(0).getChainInputSize();
		dout.reshape(savedInputSize, savedInputSize);

		for (int i = 1; i < ids.length; i++) {
			int prevOutput = chain.block(i - 1).op.getOutputSize();
			int inputSize = chain.block(i).getChainInputSize();
			if (prevOutput != inputSize) {
				int inputChain = blocks.get(i).inputChainIdx;
				throw new IllegalArgumentException(
						"op[" + i + "] has input[" + inputChain + "].size=" + inputSize + " and not " + prevOutput);
			}
			savedInputSize += inputSize;
		}

		// record where the input for the last block is stored
		chain.savedLength = savedInputSize;

		// Allocate more memory to the array if needed
		savedInputs.reserve(savedInputSize);

		// Return the Chain ID
		return chains.size - 1;
	}

	/// An operator stores its inputs, so the same instance appearing twice would have the second use
	/// overwrite the first and the backwards pass would differentiate stale values
	void checkNoRecycledOperators( int[] ids ) {
		for (int i = 0; i < ids.length; i++) {
			for (int j = i + 1; j < ids.length; j++) {
				if (blocks.get(ids[i]).op == blocks.get(ids[j]).op)
					throw new IllegalArgumentException(
							"Same operator used by block " + ids[i] + " and " + ids[j]);
			}
		}
	}

	/// How many elements a chain needs in [#savedInputs], the total size of every block's chain input
	int savedLength( int chainID ) {
		return chains.get(chainID).savedLength;
	}

	/// Number of blocks in a chain
	int chainSize( int chainID ) {
		return chains.get(chainID).size();
	}

	/// The operator a block runs
	CROperatorBackwardsJac<T> op( int chainID, int blockIdx ) {
		return chains.get(chainID).block(blockIdx).op;
	}

	/// Which input of the block the chain feeds its running value into
	int chainInputIdx( int chainID, int blockIdx ) {
		return chains.get(chainID).block(blockIdx).inputChainIdx;
	}

	/// Registers a handler for one input of a block inside a chain
	void setCapture( int chainID, int blockIdx, int input, CaptureJacobian<T> capture ) {
		chains.get(chainID).block(blockIdx).setCapture(input, capture);
	}

	/// Tells a chain's first block that its chain input came from somewhere that wants the Jacobian,
	/// so it has to be computed even though no capture is registered for it
	void forceChainInputJac( int chainID, int blockIdx ) {
		chains.get(chainID).block(blockIdx).forceChainInputJac = true;
	}

	/// Helper function that will automatically pass in all mapped inputs
	///
	/// @param chainID Which chain are you inserting inputs for
	/// @param params Parameters from the system array
	public void insertInputs( int chainID, T params ) {
		Chain chain = chains.get(chainID);
		var wrapper = new CRArray<T>(params, 0);
		for (int idxBlock = 0; idxBlock < chain.size(); idxBlock++) {
			Block<T> b = chain.block(idxBlock);
			for (int idxInput = 0; idxInput < b.op.getInputCount(); idxInput++) {
				int paramIdx = b.inputToParameter.get(idxInput);
				if (paramIdx < 0)
					continue;
				wrapper.offset = paramIdx;
				b.op.setInput(idxInput, wrapper);
			}
		}
	}

	/// Performs the chain in the forward directly while saving all the intermediate inputs in [#savedInputs] array
	///
	/// @param chainID Which chain it should run
	/// @param input Initial input that's fed into the first block
	/// @param output Storage for output. If null output will not be computed.
	public void forward( int chainID, CRArray<T> input, @Nullable CRArray<T> output ) {
		Chain chain = chains.get(chainID);

		if (input.offset == -1)
			throw new RuntimeException("Invalid chain ID. " + chainID);

		if (verbose != null) {
			verbose.printf("chain[id=%d] len=%d\n", chainID, chain.size());
			verbose.printf("_         %s\n", input.toStringD("%.3e", chain.block(0).getChainInputSize()));
		}

		int last = chain.size() - 1;
		for (int i = 0; i <= last; i++) {
			forwardBlock(chainID, i, input, i == last ? output : null);
		}
	}

	/// Runs a single block of a chain, which is what [#forward] does in a loop. Split out so that a
	/// caller can interleave two chains that depend on each other, see [JacobianChainGraph]
	///
	/// @param input Only read when this is the chain's first block
	/// @param output Where the block's output is copied to, in addition to being saved for the backwards
	/// pass. The last block writes only here, and skips entirely when this is null
	/// @param input The chain's input. Only read by the first block, the others take theirs from the block before
	void forwardBlock( int chainID, int blockIdx, @Nullable CRArray<T> input, @Nullable CRArray<T> output ) {
		Chain chain = chains.get(chainID);
		Block<T> block = chain.block(blockIdx);
		boolean last = blockIdx == chain.size() - 1;

		if (blockIdx == 0) {
			Objects.requireNonNull(input, "The first block of a chain needs an input");
			// the chain's input is copied in so that the backwards pass can find it later
			savedInputs.offset = block.getChainInputSize();
			System.arraycopy(input.a, input.offset, savedInputs.a, 0, savedInputs.offset);
			block.op.setInput(block.inputChainIdx, input);
		} else {
			// the previous block left its output sitting at the cursor
			block.op.setInput(block.inputChainIdx, savedInputs);
			savedInputs.offset += block.getChainInputSize();
		}

		// Nothing downstream wants the last block's output, so there is no reason to compute it
		if (last && output == null)
			return;

		CRArray<T> written = last ? Objects.requireNonNull(output) : savedInputs;
		block.forward(written);

		// A block in the middle can also be the source of a dependency in a larger graph
		if (!last && output != null)
			System.arraycopy(savedInputs.a, savedInputs.offset, output.a, output.offset, block.op.getOutputSize());

		if (verbose != null) {
			verbose.printf("_ out[%2d] %s\n", blockIdx, written.toStringD("%.3e", block.op.getOutputSize()));
		}
	}

	/// Same as [#backwards(int)], except that it runs a forward pass to get the inputs then runs the backwards pass
	/// to compute Jacobians. This makes the pair self-contained when several chains are being driven.
	public void backwardsF( int chainID, CRArray<T> input ) {
		// Compute and save inputs
		forward(chainID, input, null);

		// backwards pass not that inputs are known
		backwards(chainID);
	}

	/// Performs backwards pass and computes Jacobians for each operator in the chain.
	/// You must call [#forward] first to get the inputs for each operation.
	///
	/// @param chainID Which chain it should perform the backwards pass on
	public void backwards( int chainID ) {
		if (publishJac == null) throw new IllegalArgumentException("outputHandler is null");

		Chain chain = chains.get(chainID);

		// Initialize dout. The chain's output is the final output, so this is the identity
		CROperatorBackwardsJac<T> lastOp = chain.block(-1).op;
		dout.reshape(lastOp.getOutputSize(), lastOp.getOutputSize());
		math.zero(dout);
		math.diag(1, dout);

		// Reference the end of the saved inputs
		savedInputs.offset = chain.savedLength;

		for (int i = chain.ids.length - 1; i >= 0; i--) {
			backwardBlock(chainID, i);
		}

		publishChainInput(chainID);
	}

	/// Runs a single block of the backwards pass, which is what [#backwards] does in a loop. Reads the
	/// incoming Jacobian from [#dout] and leaves the outgoing one there, so a caller driving several
	/// chains is responsible for saving and restoring it around a switch
	void backwardBlock( int chainID, int blockIdx ) {
		Block<T> b = chains.get(chainID).block(blockIdx);
		inputJacs.reset();

		boolean allInvalid = true;
		// For all the other inputs, only compute the input Jacobian if configured to do so
		for (int idxInput = 0; idxInput < b.op.getInputCount(); idxInput++) {
			CRMatrix<T> jac = inputJacs.grow();

			// It will always compute a Jacobian for the chain's input. Unless it's the first block,
			// where it is only wanted if something is going to read it
			if (idxInput == b.inputChainIdx && (blockIdx > 0 || b.forceChainInputJac)) {
				allInvalid = false;
				continue;
			}

			CaptureJacobian<T> capture = b.inputToCapture.get(idxInput);
			if (capture == null) {
				jac.markInvalid();
			} else {
				allInvalid = false;
			}
		}

		// Rewind to this block's input, which the forward pass saved
		savedInputs.offset -= b.op.getInputSize(b.inputChainIdx);

		// If no Jacobian will be computed, skip this operation
		if (blockIdx == 0 && allInvalid) {
			return;
		}

		b.op.setInput(b.inputChainIdx, savedInputs);
		b.op.jacobian(dout, inputJacs.toList());

		// Add input Jacobians to the larger Jacobian matrix if configured to do so
		for (int idxInput = 0; idxInput < b.op.getInputCount(); idxInput++) {
			// Skip over input used by the chain
			if (idxInput == b.inputChainIdx)
				continue;
			CRMatrix<T> jac = inputJacs.get(idxInput);
			if (jac.isInvalid()) continue;
			b.inputToCapture.get(idxInput).handle(jac);
		}

		// Update dout for next iteration.
		// NOTE: If first block and jac[0] is invalid, that's OK. dout is ignored
		dout.setTo(inputJacs.get(b.inputChainIdx));
	}

	/// The first input in the chain might be added to the system Jacobian. At this point [#dout] holds
	/// the Jacobian relative to it
	void publishChainInput( int chainID ) {
		Block<T> first = chains.get(chainID).block(0);
		CaptureJacobian<T> capture = first.inputToCapture.get(first.inputChainIdx);

		// first input is not a parameter. dout doesn't need to be saved
		if (capture == null)
			return;

		// Sanity checks
		if (dout.isInvalid())
			throw new IllegalArgumentException("BUG! dout is invalid");
		if (dout.isNaN())
			throw new IllegalArgumentException("BUG! dout contains NaN");
		if (dout.rows == 0 || dout.cols == 0)
			throw new IllegalArgumentException("BUG! dout has no elements");

		capture.handle(dout);
	}

	/// Size of output vector from the chain
	public int getOutputSize( int chainID ) {
		return chains.get(chainID).block(-1).op.getOutputSize();
	}

	/// Size of first input to the specified block in the chain
	public int blockInputSize( int chainID, int sequenceIdx ) {
		return chains.get(chainID).block(sequenceIdx).getChainInputSize();
	}

	@Override public void setVerbose( @Nullable PrintStream out, @Nullable Set<String> configuration ) {
		this.verbose = VerboseUtils.addPrefix(this, out);
	}

	/// Storage for an operator an any associated adata
	protected static class Block<T> {
		/// Which operator is done at this point in the chain
		@SuppressWarnings("NullAway.Init") CROperatorBackwardsJac<T> op;

		/// Called after automatic inputs have been set before it runs. Typically used to make adjustments or
		/// specify other inputs which are dynamic depending on inputs from the chain
		PreForward<T> preForward = ( op ) -> {};

		/// Which input should the output from the previous block in the chain go into
		int inputChainIdx = 0;

		/// The first block of a chain only computes the Jacobian of its chain input when something
		/// wants it. Set when the value came from elsewhere, such as another chain in a graph
		boolean forceChainInputJac = false;

		/// Optional bookkeeping if you use the default capture. Tells you which parameter it maps to
		TIntIntMap inputToParameter = new TIntIntHashMap(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR, -1, -1);

		/// Operator that's called to handle the input Jacobian
		TIntObjectMap<CaptureJacobian<T>> inputToCapture = new TIntObjectHashMap<>();

		/// Calls the forward function and handles bookkeeping
		public void forward(CRArray<T> output) {
			preForward.handle(op);
			op.forward(output);
		}

		/// Returns the input array size that's expected for the input selected to be used by this chain
		public int getChainInputSize() {
			return op.getInputSize(inputChainIdx);
		}

		public void setCapture( int input, CaptureJacobian<T> capture ) {
			if (input < 0 || input >= op.getInputCount())
				throw new IllegalArgumentException("invalid input index: " + input);
			inputToCapture.put(input, capture);
		}

		@SuppressWarnings("NullAway") // releases the operator so a recycled block doesn't keep it alive
		public void reset() {
			op = null;
			// a recycled block would otherwise keep a hook closed over the operator it used to hold
			preForward = ( op ) -> {};
			inputChainIdx = 0;
			forceChainInputJac = false;
			inputToParameter.clear();
			inputToCapture.clear();
		}
	}

	/// Describes a sequence of blocks that will be processed
	protected class Chain {
		/// Block IDs in the chain
		@SuppressWarnings("NullAway.Init") public int[] ids;

		/// Where the input for the last block is stored in [#savedInputs]
		public int savedLength = -1;

		public Chain init( int[] blocks ) {
			this.ids = new int[blocks.length];
			System.arraycopy(blocks, 0, this.ids, 0, blocks.length);
			return this;
		}

		/// Returns the block at the specified index. Supports negative indexes to return from the tail
		public Block<T> block( int idx ) {
			if (idx >= 0) return blocks.get(ids[idx]);
			return blocks.data[ids[ids.length + idx]];
		}

		@SuppressWarnings("NullAway") // a recycled chain must be initialized again before use
		public void reset() {
			ids = null;
			savedLength = -1;
		}

		public int size() {
			return ids.length;
		}
	}

	/// Used to pass a locally computed Jacobian into the system Jacobian
	@FunctionalInterface public interface AddToJacobian<T> {
		/// @param parameter Location of this parameter in the system vector
		/// @param jacobian Locally computed Jacobian by the operator
		void addJacobian( int parameter, CRMatrix<T> jacobian );
	}

	@FunctionalInterface public interface CaptureJacobian<T> {
		void handle( CRMatrix<T> jacobian );
	}

	/// Called once every input is set and before the operator runs. This is only called in the forward pass
	/// and not in the backwards pass.
	@FunctionalInterface public interface PreForward<T> {
		void handle( CROperatorBackwardsJac<T> op );
	}
}
