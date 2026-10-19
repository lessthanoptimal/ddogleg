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

import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// Checks the gradient of a [CROperatorBackwardsGrad] against numerical differentiation. Return [#tests()]
/// from a `@TestFactory` method.
@SuppressWarnings({"unchecked", "rawtypes"})
public class CommonCROperatorBackwardsGrad<T> {
	public boolean verbose = false;

	protected final Supplier<List<GradientCase<T>>> variants;
	protected final Random rand;
	protected final MathCRMatrix<T> math = (MathCRMatrix<T>)new MathCRMatrixD();

	/// @param variants Creates different variants. For example, different parameters might be marked as constant
	/// or different encodings used. Each comes with a set of valid inputs.
	/// @param rand Source of random values
	public CommonCROperatorBackwardsGrad( Supplier<List<GradientCase<T>>> variants, Random rand ) {
		this.variants = variants;
		this.rand = rand;
	}

	/// Every check as its own named test
	public Stream<DynamicTest> tests() {
		return Stream.of(dynamicTest("checkBackwards", this::checkBackwards));
	}

	public void checkBackwards() {
		GradientNumerical<T> numeric = (GradientNumerical)new GradientNumerical_F64();

		List<GradientCase<T>> list = variants.get();
		for (int idxVariant = 0; idxVariant < list.size(); idxVariant++) {
			if (verbose)
				System.out.println("Test Variant: " + idxVariant);

			CROperatorBackwardsGrad<T> alg = list.get(idxVariant).alg;
			List<CRArray<T>> inputs = list.get(idxVariant).inputs;

			assertEquals(alg.getInputCount(), inputs.size(), "Incorrect number of inputs provided");
			numeric.setFunction(inputs, alg);

			int outputSize = alg.getOutputSize();

			// storage for input Gradients
			List<CRArray<T>> expectedGrad = new ArrayList<>();
			List<CRArray<T>> foundGrad = new ArrayList<>();
			for (int i = 0; i < alg.getInputCount(); i++) {
				int addSize = alg.getInputSize(i);
				expectedGrad.add(math.random(rand, -1, 1, addSize));
				foundGrad.add(math.random(rand, -1, 1, addSize));
			}

			CRArray<T> outputTensor = FactoryCRArray.random(rand, outputSize, alg.getArrayType());
			CRArray<T> dout = FactoryCRArray.random(rand, outputSize, alg.getArrayType());

			for (int i = 0; i < outputSize; i++) {
				dout.d(i, 1.0);
			}

			numeric.differentiate(dout, expectedGrad);

			// invoke the forwards pass first. Some algorithms require it be called first to compute gradient
			for (int i = 0; i < alg.getInputCount(); i++) {
				alg.setInput(i, inputs.get(i));
			}
			alg.forward(outputTensor);

			// compute the gradient using the function being tested
			alg.gradient(dout, foundGrad);

			for (int i = 0; i < alg.getInputCount(); i++) {
				CRArray<T> expectedA = expectedGrad.get(i);
				CRArray<T> foundA = foundGrad.get(i);
				int lengthA = alg.getInputSize(i);

				if (verbose) printDiff("Input[" + i + "]", expectedA, foundA, lengthA);
				assertTrue(expectedA.isEquals(foundA, lengthA, 1e-4));
			}
		}
	}

	void printDiff( String what, CRArray<T> expected, CRArray<T> found, int length ) {
		System.out.println(what + ": expected vs found: size: " + length);
		for (int j = 0; j < length; j++) {
			System.out.printf("param %6.2e   %6.2e\n", expected.d(j), found.d(j));
		}
	}

	/// An operator and the inputs it's checked at
	public static class GradientCase<B> {
		public CROperatorBackwardsGrad<B> alg;
		public List<CRArray<B>> inputs;

		public GradientCase( CROperatorBackwardsGrad<B> alg, List<CRArray<B>> inputs ) {
			this.alg = alg;
			this.inputs = new ArrayList<>(inputs);
		}

		public GradientCase( CROperatorBackwardsGrad<B> alg ) {
			this(alg, new ArrayList<>());
		}

		public GradientCase<B> in( double... values ) {
			inputs.add(new CRArray<>((B)values, 0));
			return this;
		}
	}
}
