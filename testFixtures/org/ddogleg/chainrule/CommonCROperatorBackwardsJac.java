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

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// Checks the Jacobian of a [CROperatorBackwardsJac] against numerical differentiation. Return [#tests()]
/// from a `@TestFactory` method.
@SuppressWarnings({"unchecked", "rawtypes"})
public class CommonCROperatorBackwardsJac<T> {
	public double tolerance = 1e-6;
	public double deltaT = 1e-6;

	public boolean verbose = false;
	public boolean printPartials = false;

	protected final Supplier<List<JacobianCase<T>>> variants;
	protected final Random rand;
	protected final MathCRMatrix<T> math = (MathCRMatrix<T>)new MathCRMatrixD();

	/// @param variants Creates different variants. For example, different parameters might be marked as constant
	/// or different encodings used. Each comes with a set of valid inputs.
	/// @param rand Source of random values
	public CommonCROperatorBackwardsJac( Supplier<List<JacobianCase<T>>> variants, Random rand ) {
		this.variants = variants;
		this.rand = rand;
	}

	/// Every check as its own named test
	public Stream<DynamicTest> tests() {
		return Stream.of(
				dynamicTest("backwardsMultipleCalls", this::backwardsMultipleCalls),
				dynamicTest("checkBackwards", this::checkBackwards));
	}

	/// Verifies that it gets the same answer when called multiple times. this will catch bugs where variables
	/// are not properly cleared
	public void backwardsMultipleCalls() {
		for (JacobianCase<T> variant : variants.get()) {
			CROperatorBackwardsJac<T> alg = variant.alg;
			List<CRArray<T>> inputs = inputs(variant);

			for (int i = 0; i < alg.getInputCount(); i++) {
				alg.setInput(i, inputs.get(i));
			}
			int outputSize = alg.getOutputSize();
			CRMatrix<T> dout = math.random(rand, -1, 1, outputSize, outputSize);

			var foundJacA = new ArrayList<CRMatrix<T>>();
			var foundJacB = new ArrayList<CRMatrix<T>>();
			for (int i = 0; i < alg.getInputCount(); i++) {
				int inputSize = alg.getInputSize(i);
				foundJacA.add(math.random(rand, -1, 1, outputSize, inputSize));
				foundJacB.add(math.random(rand, -1, 1, outputSize, inputSize));
			}

			alg.jacobian(dout, foundJacA);
			alg.jacobian(dout, foundJacB);

			assertEquals(foundJacA.size(), foundJacB.size());
			for (int i = 0; i < foundJacA.size(); i++) {
				assertTrue(foundJacA.get(i).isEquals(foundJacB.get(i), 1e-8));
			}
		}
	}

	/// Verify backwards propagation using user specified test cases and numerical differentiation
	public void checkBackwards() {
		JacobianNumerical<T> numeric = (JacobianNumerical)new JacobianNumerical_F64();
		numeric.configure(deltaT);
		((JacobianNumerical_F64)numeric).printPartials = printPartials;

		List<JacobianCase<T>> list = variants.get();
		for (int idxVariant = 0; idxVariant < list.size(); idxVariant++) {
			if (verbose)
				System.out.println("Test Variant: " + idxVariant);

			CROperatorBackwardsJac<T> alg = list.get(idxVariant).alg;
			List<CRArray<T>> inputs = inputs(list.get(idxVariant));

			assertEquals(alg.getInputCount(), inputs.size(), "Incorrect number of inputs provided");

			for (int i = 0; i < alg.getInputCount(); i++) {
				// apply an offset to input array to make sure it's reading from the array correctly
				alg.setInput(i, copyWithOffset(1 + i, inputs.get(i)));
			}
			numeric.setFunction(inputs, alg);

			int outputSize = alg.getOutputSize();

			// storage for Jacobians
			List<CRMatrix<T>> expectedJac = new ArrayList<>();
			List<CRMatrix<T>> foundJac = new ArrayList<>();
			for (int i = 0; i < alg.getInputCount(); i++) {
				int inputSize = alg.getInputSize(i);
				expectedJac.add(math.random(rand, -1, 1, outputSize, inputSize));
				foundJac.add(math.random(rand, -1, 1, outputSize, inputSize));
			}

			CRArray<T> output = FactoryCRArray.random(rand, outputSize, alg.getArrayType());
			CRMatrix<T> dout = math.random(rand, -1, 1, outputSize, outputSize);

			numeric.differentiate(dout, expectedJac);

			// invoke the forwards pass first. Some algorithms require it be called first to compute gradient
			for (int i = 0; i < alg.getInputCount(); i++) {
				alg.setInput(i, inputs.get(i));
			}
			alg.forward(output);

			// compute the gradient using the function being tested
			alg.jacobian(dout, foundJac);

			for (int i = 0; i < foundJac.size(); i++) {
				if (verbose) printDiff("input[" + i + "]", expectedJac.get(i), foundJac.get(i));

				assertTrue(expectedJac.get(i).isEquals(foundJac.get(i), tolerance));
			}
		}
	}

	/// The variant's inputs, with a random valid value for each one that was left unspecified
	protected List<CRArray<T>> inputs( JacobianCase<T> variant ) {
		var out = new ArrayList<CRArray<T>>();
		for (int inputID = 0; inputID < variant.inputs.size(); inputID++) {
			@Nullable CRArray<T> input = variant.inputs.get(inputID);
			if (input == null) {
				input = (CRArray<T>)CRTestHelpers.randomCRArray(variant.alg.getInputSize(inputID), rand);
				variant.alg.normalizeAfterStep(inputID, input);
				variant.inputs.set(inputID, input);
			}
			out.add(input);
		}
		return out;
	}

	void printDiff( String what, CRMatrix<T> expected, CRMatrix<T> found ) {
		System.out.println(what + ": expected vs found: size: " + found.rows + "x" + found.cols);
		for (int row = 0; row < expected.rows; row++) {
			for (int col = 0; col < expected.cols; col++) {
				double e = expected.d(row, col);
				double f = found.d(row, col);
				String star = Math.abs(e - f) > tolerance ? " *" : "";
				System.out.printf("[%2d, %2d] = %9.2e %9.2e%s\n", row, col, e, f, star);
			}
		}
	}

	/// Creates a copy of the array, but with an offset
	private CRArray<T> copyWithOffset( int adjust, CRArray<T> src ) {
		if (!(src.a instanceof double[]))
			throw new RuntimeException("Add support for non-double[]");
		double[] srcArray = (double[])src.a;
		double[] dstArray = new double[srcArray.length + adjust];
		System.arraycopy(srcArray, 0, dstArray, adjust, srcArray.length);

		// Fill in random values in the region it shouldn't read
		for (int i = 0; i < adjust; i++) {
			dstArray[i] = rand.nextGaussian();
		}
		return (CRArray)new CRArray<>(dstArray, adjust + src.offset);
	}

	/// An operator and the inputs it's checked at
	public static class JacobianCase<B> {
		public CROperatorBackwardsJac<B> alg;
		/// Inputs in order. A null input is replaced by a random valid value when the check runs
		public List<@Nullable CRArray<B>> inputs;

		public JacobianCase( CROperatorBackwardsJac<B> alg, List<CRArray<B>> inputs ) {
			this.alg = alg;
			this.inputs = new ArrayList<>(inputs);
		}

		public JacobianCase( CROperatorBackwardsJac<B> alg ) {
			this(alg, new ArrayList<>());
		}

		/// Assigns the next input to this vector. If empty then a random input will be used
		public JacobianCase<B> in( double... values ) {
			inputs.add(values.length == 0 ? null : new CRArray<>((B)values, 0));
			return this;
		}
	}
}
