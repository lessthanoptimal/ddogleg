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

import java.util.Random;

/// Random inputs for testing chain rule operators. Values are uniform from -1 to 1
public class CRTestHelpers {
	/// Sets every input of the operator to random values, normalized so they are valid
	public static void fillRandom( CROperatorForward<double[]> op, Random rand ) {
		for (int idxInput = 0; idxInput < op.getInputCount(); idxInput++) {
			var param = randomCRArray(op.getInputSize(idxInput), rand);

			// ensure the input is valid
			op.normalizeAfterStep(idxInput, param);

			op.setInput(idxInput, param);
		}
	}

	/// Fills the first `length` elements with random values
	public static void fillRandom( CRArray<double[]> array, int length, Random rand ) {
		for (int i = 0; i < length; i++) {
			array.d(i, 2.0*rand.nextDouble() - 1.0);
		}
	}

	/// Creates an array of the specified length filled with random values
	public static CRArray<double[]> randomCRArray( int length, Random rand ) {
		var array = new CRArray<>(new double[length], 0);
		fillRandom(array, length, rand);
		return array;
	}
}
