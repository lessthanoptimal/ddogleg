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

import org.ejml.UtilEjml;
import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/// Standard checks for any [CROperatorForward]. Return [#tests()] from a `@TestFactory` method. Override
/// [#randomInput] if an operator needs inputs which are not uniform from -1 to 1.
public class CommonCROperatorForward {
	protected final Supplier<List<CROperatorForward<double[]>>> variants;
	protected final Random rand;

	/// @param variants Creates new instances of the operators being tested, configured differently
	/// @param rand Source of random inputs
	public CommonCROperatorForward( Supplier<List<CROperatorForward<double[]>>> variants, Random rand ) {
		this.variants = variants;
		this.rand = rand;
	}

	/// Every check as its own named test
	public Stream<DynamicTest> tests() {
		return Stream.of(
				dynamicTest("getArrayType", this::getArrayType),
				dynamicTest("forward_BlowUp", this::forward_BlowUp),
				dynamicTest("settings_BlowUp", this::settings_BlowUp),
				dynamicTest("swapSettings", this::swapSettings),
				dynamicTest("normalizeAfterStep", this::normalizeAfterStep),
				dynamicTest("doNotSaveInputRef", this::doNotSaveInputRef),
				dynamicTest("forward_doesNotAccumulate", this::forward_doesNotAccumulate),
				dynamicTest("honorsTheOffsets", this::honorsTheOffsets),
				dynamicTest("unexpectedInputThrows", this::unexpectedInputThrows));
	}

	public void getArrayType() {
		for (CROperatorForward<double[]> alg : variants.get()) {
			assertSame(double[].class, alg.getArrayType());
		}
	}

	/// Give it random inputs and see if it throws an exception
	public void forward_BlowUp() {
		for (CROperatorForward<double[]> alg : variants.get()) {
			for (int i = 0; i < alg.getInputCount(); i++) {
				// Randomly generate parameters and ensure that they are valid
				CRArray<double[]> params = randomInput(i, alg);
				alg.normalizeAfterStep(i, params);

				// Pass in the valid inputs
				alg.setInput(i, params);
			}

			alg.forward(new CRArray<>(new double[alg.getOutputSize()], 0));
		}
	}

	/// Very simple test that sees if calling get and set Settings causes it to blow up
	public void settings_BlowUp() {
		for (CROperatorForward<double[]> alg : variants.get()) {
			Map<String, String> found = alg.getSettings();
			alg.setSettings(found);
		}
	}

	/// If at least two variants are provided, swap the settings and see if they stick
	public void swapSettings() {
		List<CROperatorForward<double[]>> list = variants.get();
		if (list.size() < 2)
			return;

		Map<String, String> settings1 = list.get(0).getSettings();
		Map<String, String> settings2 = list.get(1).getSettings();

		if (settings1.isEmpty() && settings2.isEmpty())
			return;

		// If the settings are already identical then why are there two variants?
		assertNotEquals(settings1, settings2);

		// Make the first variant the same as the second variant
		list.get(0).setSettings(settings2);
		Map<String, String> found = list.get(0).getSettings();

		assertEquals(settings2, found);
	}

	/// Checks to see if this function produces a state which is valid. We simulate a iterative step when
	/// optimizing that might not produce a valid step, the check the output for NaN and similar.
	public void normalizeAfterStep() {
		for (CROperatorForward<double[]> alg : variants.get()) {
			var output = new CRArray<>(new double[alg.getOutputSize()], 0);

			for (int trial = 0; trial < 10; trial++) {
				List<CRArray<double[]>> inputs = randomInputs(alg);

				for (int inputIdx = 0; inputIdx < inputs.size(); inputIdx++) {
					CRArray<double[]> param = inputs.get(inputIdx);
					int size = alg.getInputSize(inputIdx);

					// let's add 10% noise to simulate a step
					for (int paramIdx = 0; paramIdx < size; paramIdx++) {
						double v = param.d(paramIdx);
						param.d(paramIdx, v + Math.abs(v)*(rand.nextDouble() - 0.5)*0.2);
					}

					// normalize it
					alg.normalizeAfterStep(inputIdx, param);

					// Apply
					alg.setInput(inputIdx, param);
				}

				alg.forward(output);

				// See if it blew up
				for (int i = 0; i < alg.getOutputSize(); i++) {
					assertFalse(UtilEjml.isUncountable(output.d(i)));
				}
			}
		}
	}

	/// Makes sure it does not save the actual input reference by immediately modifying the reference and seeing
	/// if it gets the same result
	public void doNotSaveInputRef() {
		for (CROperatorForward<double[]> alg : variants.get()) {
			var expected = new CRArray<>(new double[alg.getOutputSize()], 0);
			var found = new CRArray<>(new double[alg.getOutputSize()], 0);

			// Compute output with the original references
			List<CRArray<double[]>> inputs = randomInputs(alg);
			for (int inputIdx = 0; inputIdx < inputs.size(); inputIdx++) {
				alg.setInput(inputIdx, inputs.get(inputIdx));
			}
			alg.forward(expected);

			// Modify the references so that they will produce garbage if saved internally
			for (int inputIdx = 0; inputIdx < inputs.size(); inputIdx++) {
				inputs.get(inputIdx).offset = 10;
				inputs.get(inputIdx).a = new double[5];
			}

			// This should still work because the original arrays passed in have not been modified
			alg.forward(found);
			assertTrue(expected.isEquals(found, alg.getOutputSize(), 0.0f));
		}
	}

	/// The output is written, not accumulated into. A chain hands over a recycled buffer, so an
	/// implementation that added to whatever it already held would drift on the second call
	public void forward_doesNotAccumulate() {
		for (CROperatorForward<double[]> alg : variants.get()) {
			List<CRArray<double[]>> inputs = randomInputs(alg);
			for (int inputIdx = 0; inputIdx < inputs.size(); inputIdx++) {
				alg.setInput(inputIdx, inputs.get(inputIdx));
			}

			var expected = new CRArray<>(new double[alg.getOutputSize()], 0);
			alg.forward(expected);

			// hand it a buffer with something in it already, as a recycled one would have
			var found = new CRArray<>(new double[alg.getOutputSize()], 0);
			CRTestHelpers.fillRandom(found, alg.getOutputSize(), rand);
			alg.forward(found);

			assertTrue(expected.isEquals(found, alg.getOutputSize(), 0.0f),
					alg.getClass().getSimpleName() + " accumulated into the output");
		}
	}

	/// Inputs and the output are read and written through their offsets, so a caller can point an
	/// operator at a slice of a larger array without copying it out first
	public void honorsTheOffsets() {
		final int shift = 3;

		for (CROperatorForward<double[]> alg : variants.get()) {
			List<CRArray<double[]>> inputs = randomInputs(alg);
			for (int inputIdx = 0; inputIdx < inputs.size(); inputIdx++) {
				alg.setInput(inputIdx, inputs.get(inputIdx));
			}
			var expected = new CRArray<>(new double[alg.getOutputSize()], 0);
			alg.forward(expected);

			// the same values, sitting inside larger arrays with something else in front of them
			for (int inputIdx = 0; inputIdx < inputs.size(); inputIdx++) {
				CRArray<double[]> src = inputs.get(inputIdx);
				int size = alg.getInputSize(inputIdx);
				var moved = new CRArray<>(new double[size + 2*shift], shift);
				for (int i = 0; i < moved.a.length; i++) {
					moved.a[i] = rand.nextGaussian();
				}
				for (int i = 0; i < size; i++) {
					moved.d(i, src.d(i));
				}
				alg.setInput(inputIdx, moved);
			}

			var found = new CRArray<>(new double[alg.getOutputSize() + 2*shift], shift);
			for (int i = 0; i < found.a.length; i++) {
				found.a[i] = rand.nextGaussian();
			}
			alg.forward(found);

			for (int i = 0; i < alg.getOutputSize(); i++) {
				assertEquals(expected.d(i), found.d(i), UtilEjml.TEST_F64,
						alg.getClass().getSimpleName() + " ignored an offset, element " + i);
			}
		}
	}

	/// An input index the operator does not have is a wiring mistake rather than something to
	/// quietly ignore
	public void unexpectedInputThrows() {
		for (CROperatorForward<double[]> alg : variants.get()) {
			int count = alg.getInputCount();
			var any = new CRArray<>(new double[10], 0);

			assertThrows(RuntimeException.class, () -> alg.setInput(count, any),
					alg.getClass().getSimpleName() + " accepted input " + count);
			assertThrows(RuntimeException.class, () -> alg.setInput(-1, any),
					alg.getClass().getSimpleName() + " accepted input -1");
			assertThrows(RuntimeException.class, () -> alg.getInputSize(count),
					alg.getClass().getSimpleName() + " sized input " + count);
		}
	}

	/// Random value for the specified input. Override when an operator needs inputs in a specific range
	public CRArray<double[]> randomInput( int which, CROperatorForward<double[]> alg ) {
		return CRTestHelpers.randomCRArray(alg.getInputSize(which), rand);
	}

	public List<CRArray<double[]>> randomInputs( CROperatorForward<double[]> alg ) {
		var list = new ArrayList<CRArray<double[]>>();
		for (int i = 0; i < alg.getInputCount(); i++) {
			list.add(randomInput(i, alg));
		}
		return list;
	}
}
