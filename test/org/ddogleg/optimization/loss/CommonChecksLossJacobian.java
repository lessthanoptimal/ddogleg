/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.loss;

import org.ddogleg.optimization.DerivativeChecker;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class CommonChecksLossJacobian {
	/** Values of residuals are randomly selected from a uniform distribution from -r to r */
	public double residualRadius = 10;

	public int samplePoints = 200;

	protected Random rand = new Random(243);

	double tolerance = 1e-4;

	public abstract LossFunction createFunction();

	public abstract LossFunctionGradient createGradient();

	/**
	 * A larger array than is needed is passed it. The extra elements should be ignored.
	 */
	@Test void ignoreExtra() {
		double[] residuals = randomArray(samplePoints, residualRadius, rand);
		double[] withExtra = new double[residuals.length + 10];
		System.arraycopy(residuals, 0, withExtra, 0, residuals.length);
		for (int i = residuals.length; i < withExtra.length; i++) {
			withExtra[i] = rand.nextGaussian();
		}

		LossFunction function = createFunction();
		LossFunctionGradient gradient = createGradient();
		function.setNumberOfFunctions(residuals.length);
		gradient.setNumberOfFunctions(residuals.length);

		// Ensure this function returns the expected results
		assertEquals(function.process(residuals), function.process(withExtra), 0.0);

		var expected = new double[residuals.length];
		var found = new double[withExtra.length];

		// Gradient should be identical. If output is longer that should also not be touched.
		gradient.process(residuals, expected);
		gradient.process(residuals, found);

		for (int i = 0; i < residuals.length; i++) {
			assertEquals(expected[i], found[i]);
		}
		for (int i = residuals.length; i < withExtra.length; i++) {
			assertEquals(0.0, found[i]);
		}
	}

	/**
	 * Compares the analytical gradient to a numerical gradient
	 */
	@Test void compareToNumerical() {
		LossFunction function = createFunction();
		LossFunctionGradient gradient = createGradient();

		double[] residuals = randomArray(samplePoints, residualRadius, rand);

		function.setNumberOfFunctions(residuals.length);
		gradient.setNumberOfFunctions(residuals.length);

		assertTrue(DerivativeChecker.gradient(function, gradient, residuals, tolerance));
	}

	public static double[] randomArray(int count, double radius, Random rand ) {
		var array = new double[count];
		for (int i = 0; i < array.length; i++) {
			array[i] = 2.0*radius*(rand.nextDouble() - 0.5);
		}
		return array;
	}
}
