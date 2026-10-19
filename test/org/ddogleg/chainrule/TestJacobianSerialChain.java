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

import org.ddogleg.DDoglegJUnit;
import org.ddogleg.chainrule.JacobianSerialChain.Block;
import org.ddogleg.chainrule.ops.CROperatorPlus;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestJacobianSerialChain extends DDoglegJUnit {
	MathCRMatrix<double[]> math = new MathCRMatrixD();

	CROperatorPolyA32 op1 = new CROperatorPolyA32(); // [3,3] -> 2
	CROperatorPolyC23 op2 = new CROperatorPolyC23(); // [2,1] -> 3
	CROperatorPolyB33 op3 = new CROperatorPolyB33(); // [3,3] -> 3
	CROperatorPolyB33 op4 = new CROperatorPolyB33(); // [3,3] -> 3
	double T = 1e-6;

	/// Compare jacobian to a numerical jacobian in a general case
	@Test void numerical() {
		// Construct the chain so that stuff will be out of order
		var alg = new JacobianSerialChain<>(math);
		alg.add(op2).in(1, 6);
		alg.add(op1).in(0, 3).in(1, 0);
		alg.add(op3).in(1, 7);
		alg.chain(1, 0, 2);

		numerical(alg, 0);
	}

	/// The first block has an input in its first argument that's not a parameter. It might be a constant
	/// or whatever. It needs to be handled correctly
	@Test void firstBlockFirstInputNotParameter() {
		var alg = new JacobianSerialChain<>(math);

		alg.add(op1).in(1, 0); // no parameter for first input
		alg.add(op2).in(1, 3);
		alg.add(op3).in(1, 4);
		alg.chain(0, 1, 2);

		numerical(alg, 0);
	}

	/// First block has no parameters so its Jacobian should be skipped
	@Test void firstBlockFirstNotParameter() {
		var alg = new JacobianSerialChain<>(math);

		alg.add(op1);
		alg.add(op2).in(1, 0);
		alg.add(op3).in(1, 1);
		alg.chain(0, 1, 2);

		numerical(alg, 0);
	}

	/// Multiple calls to different chains with different structures. See if everything still works.
	@Test void multipleChainsAndCalls() {
		var alg = new JacobianSerialChain<>(math);

		alg.add(op1).in(1, 0);
		alg.add(op2).in(1, 3);
		alg.add(op3).in(1, 4);

		alg.add(op2).in(0, 1).in(1, 0);
		alg.add(op1).in(1, 3);
		alg.chain(0, 1, 2);
		alg.chain(3, 4);

		numerical(alg, 0);
		numerical(alg, 1);
		numerical(alg, 0);
	}

	/// Test scenario where there is a single operator. This is an edge case
	@Test void numerical_single() {
		var alg = new JacobianSerialChain<>(math);
		alg.add(op1).in(0, 3).in(1, 0);
		alg.chain(0);

		numerical(alg, 0);
	}

	/// Test situation where one of the inputs to a block is invalid and Jacobian shouldn't be computed
	@Test void invalidInput() {
		var alg = new JacobianSerialChain<>(math);
		alg.add(op1).in(0, 3); // input 1 is invalid
		alg.chain(0);
		numerical(alg, 0);
	}

	/// Makes a custom capture is called
	@Test void customCapture() {
		var alg = new JacobianSerialChain<>(math) {
			int callCounter = 0;
		};
		alg.setPublishJac(( a, b ) -> {});
		alg.add(op2).in(1, 6);
		alg.add(op1).in(0, 3);
		alg.add(op3).in(1, 7);
		alg.chain(1, 0, 2);
		alg.setCapture(1, 1, ( jac ) -> alg.callCounter++);

		// Run it. Input values don't matter
		alg.backwardsF(0, new CRArray<>(new double[20], 0));

		// It should only becalled once
		assertEquals(1, alg.callCounter);
	}

	/// Use inputIdx to tell it to use an alternative input besides 0 as the input from the chain
	@Test void alternativeInputIndex() {
		var alg = new JacobianSerialChain<>(math);
		alg.add(op1).inputIdx(1).in(0, 0);
		alg.add(op2).in(0, 3);
		alg.add(op3).inputIdx(1).in(0, 5);
		alg.add(op4).inputIdx(1).in(0, 8);
		alg.chain(0, 1, 2, 3);

		numerical(alg, 0);
	}

	/// Computes the jacobian and compares to a numerical jacobian. Examine data structures
	/// construct parameters and pass in inputs
	private void numerical( JacobianSerialChain<double[]> alg, int chainID ) {
		JacobianSerialChain<double[]>.Chain chain = alg.chains.get(chainID);
		// Determine how big the parameter vector needs to be by looking at where
		// inputs are source from
		int countParam = 0;
		for (int idxBlock = 0; idxBlock < chain.size(); idxBlock++) {
			Block<double[]> b = chain.block(idxBlock);
			for (int idxInput = 0; idxInput < b.op.getInputCount(); idxInput++) {
				int param = b.inputToParameter.get(idxInput);
				if (param >= 0) {
					countParam = Math.max(param + b.op.getInputSize(idxInput), countParam);
					continue;
				}

				// For inputs that are not from a parameter, let's give it a random input. This way
				// coefficients won't be all zeros which can be pathological or not interesting
				double[] input = randomInput(b.op.getInputSize(idxInput));
				b.op.setInput(idxInput, new CRArray<>(input, 0));
			}
		}

		var params = randomInput(countParam);

		var found = new DMatrixRMaj(alg.getOutputSize(chainID), countParam);
		alg.setPublishJac(( col0, jac ) -> {
			for (int row = 0; row < jac.rows; row++) {
				for (int col = 0; col < jac.cols; col++) {
					found.add(row, col0 + col, jac.d(row, col));
				}
			}
		});

		// Pass operator inputs
		alg.insertInputs(chainID, params);
		CRArray<double[]> input;

		// If the first block is a parameter, pass that in. Otherwise, assign it a random value.
		Block<double[]> firstBlock = chain.block(0);
		if (firstBlock.inputToParameter.get(0) >= 0) {
			input = new CRArray<>(params, firstBlock.inputToParameter.get(0));
		} else {
			input = new CRArray<>(randomInput(firstBlock.op.getInputSize(0)), 0);
		}
		alg.forward(chainID, input, new CRArray<>(new double[1000], 0));

//        alg.backwardsF(chainID, input);
//
//        DMatrixRMaj expected = numerical(chainID, params, input, alg);
//
////        SIVisionUtils.printZ(expected);
////        SIVisionUtils.printZ(found);
//
//        String errors = SIVisionUtils.stringErrors(expected, found, T);
//        assertFalse(errors.contains("E"), errors);
	}

	private double[] randomInput( int countParam ) {
		var params = new double[countParam];
		for (int i = 0; i < params.length; i++) {
			params[i] = 2.0*rand.nextDouble() - 1.0;
		}
		return params;
	}

	DMatrixRMaj numerical( int chainID, double[] params, CRArray<double[]> input,
						   JacobianSerialChain<double[]> op ) {
		int outputN = op.getOutputSize(chainID);
		var outputP = new CRArray<>(new double[outputN], 0);
		var outputM = new CRArray<>(new double[outputN], 0);

		var jacobian = new DMatrixRMaj(outputN, params.length);

		for (int i = 0; i < params.length; i++) {
			double v = params[i];
			params[i] = v + T;
			op.insertInputs(chainID, params);
			op.forward(chainID, input, outputP);
			params[i] = v - T;
			op.insertInputs(chainID, params);
			op.forward(chainID, input, outputM);
			params[i] = v;

			for (int j = 0; j < outputN; j++) {
				double d = (outputP.d(j) - outputM.d(j))/(2.0*T);
				jacobian.set(j, i, d);
			}
		}
		return jacobian;
	}

	/// A capture on the first block's chain input is how a caller asks for the Jacobian of what it
	/// fed in, which is what a value shared with another chain needs
	@Test void capture_chainInputOfFirstBlock() {
		var chain = new JacobianSerialChain<>(new MathCRMatrixD());
		var op0 = new CROperatorPlus(2);
		var op1 = new CROperatorPlus(2);

		int b0 = chain.add(op0).lastBlockId();
		int b1 = chain.add(op1).lastBlockId();
		chain.chain(b0, b1);
		chain.setPublishJac(( col, jac ) -> {});

		var fired = new boolean[1];
		var found = new double[4];
		chain.setCapture(b0, CROperatorPlus.IN_A, ( jac ) -> {
			fired[0] = true;
			System.arraycopy(jac.data, 0, found, 0, 4);
		});

		op0.setInput(CROperatorPlus.IN_B, new CRArray<>(new double[]{1, 2}, 0));
		op1.setInput(CROperatorPlus.IN_B, new CRArray<>(new double[]{3, 4}, 0));

		chain.backwardsF(0, new CRArray<>(new double[]{5, 6}, 0));

		assertTrue(fired[0], "capture on the chain input never fired");
		// d(out)/d(in) through two adds is the identity
		assertArrayEquals(new double[]{1, 0, 0, 1}, found, 1e-12);
	}
}
