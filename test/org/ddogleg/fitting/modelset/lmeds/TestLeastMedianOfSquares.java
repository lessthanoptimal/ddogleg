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

package org.ddogleg.fitting.modelset.lmeds;

import org.ddogleg.fitting.modelset.*;
import org.ddogleg.fitting.modelset.distance.DistanceFromMeanModel;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TestLeastMedianOfSquares {
	Random rand = new Random(0xBEEF);

	@Nested class Fraction extends Common {
		Fraction() {super(0.9);}

		/** If the user set the max error to Double.MAX_VALUE then it should still work as expected */
		@Test void handleMaxMaxError() {
			ModelMatcherPost<double[], Double> matcher = createModel(1, Double.MAX_VALUE);

			// The model fitter will always fail, so process should fail too
			matcher.setModel(() -> new MeanModelFitter() {
				@Override
				public boolean fitModel( List<Double> dataSet, @Nullable double[] initParam, double[] foundParam ) {
					return false;
				}
			}, DistanceFromMeanModel::new);

			List<Double> samples = createSampleSet(100, 1, 1, 0.1);

			assertFalse(matcher.process(samples));
		}
	}

	@Nested class Sigma extends Common {
		Sigma() {super(0);}
	}

	/** Runs the generic tests with the specified inlier fraction. 0 = sigma */
	abstract class Common extends GenericModelMatcherPostTests {
		double inlierFraction;

		Common( double inlierFraction ) {
			this.inlierFraction = inlierFraction;
			configure(0.9, 0.1, inlierFraction == 0);
		}

		@Override
		public ModelMatcherPost<double[], Double> createModelMatcher( ModelManager<double[]> manager,
																	  int minPoints,
																	  double fitThreshold ) {
			var alg = new LeastMedianOfSquares<>(4234, 50, fitThreshold, manager, Double.class);
			alg.setInlierFraction(inlierFraction);
			return alg;
		}
	}

	/** The inlier set should be the N*fraction points with the smallest error, rounded up */
	@Test void fraction_inliers() {
		List<Double> points = createGaussian(10, 2.5, 0.1);

		checkSmallestErrors(points, 0.3, 1, 3);
		checkSmallestErrors(points, 0.75, 1, 8);
		// Can't be less than the sample size
		checkSmallestErrors(points, 0.3, 4, 4);
		// Every point. This used to read past the end of the array
		checkSmallestErrors(points, 1.0, 1, 10);
	}

	void checkSmallestErrors( List<Double> points, double fraction, int sampleSize, int expected ) {
		LeastMedianOfSquares<double[], Double> alg = createAlg();
		alg.setInlierFraction(fraction);
		alg.setSampleSize(sampleSize);
		assertTrue(alg.process(points));

		// brute force find the points with the smallest error
		double mean = alg.getModelParameters()[0];
		Integer[] order = new Integer[points.size()];
		for (int i = 0; i < order.length; i++) {
			order[i] = i;
		}
		Arrays.sort(order, Comparator.comparingDouble(i -> Math.abs(points.get(i) - mean)));

		List<Double> matchSet = alg.getMatchSet();
		assertEquals(expected, matchSet.size());
		var found = new HashSet<Integer>();
		for (int i = 0; i < matchSet.size(); i++) {
			found.add(alg.getInputIndex(i));
		}
		assertEquals(new HashSet<>(Arrays.asList(order).subList(0, expected)), found);
		assertEquals(Math.abs(points.get(order[expected - 1]) - mean), alg.getInlierThreshold(), 1e-12);
	}

	@Test void setInlierFraction_tooLarge() {
		LeastMedianOfSquares<double[], Double> alg = createAlg();
		alg.setInlierFraction(1.0);
		assertThrows(IllegalArgumentException.class, () -> alg.setInlierFraction(1.1));
	}

	/** With only Gaussian noise the threshold should be inlierRouseeuwSigma*sigma */
	@Test void sigma_threshold() {
		double sigma = 0.1;
		List<Double> points = createGaussian(2000, 2.5, sigma);

		LeastMedianOfSquares<double[], Double> alg = createAlg();
		assertTrue(alg.process(points));

		assertEquals(alg.getInlierRouseeuwSigma()*sigma, alg.getInlierThreshold(), 0.05*alg.getInlierThreshold());
	}

	/** Compare to a brute force computation of the threshold using the best model. Outliers should be removed. */
	@Test void sigma_bruteForce() {
		int numInliers = 450;
		List<Double> points = createGaussian(numInliers, 2.5, 0.1);
		for (int i = 0; i < 50; i++) {
			points.add(2.5 + (rand.nextBoolean() ? 1 : -1)*(2.0 + rand.nextDouble()*10.0));
		}
		Collections.shuffle(points, rand);
		int N = points.size();

		LeastMedianOfSquares<double[], Double> alg = createAlg();
		assertTrue(alg.process(points));

		double mean = alg.getModelParameters()[0];
		double[] errors = new double[N];
		for (int i = 0; i < N; i++) {
			errors[i] = Math.abs(points.get(i) - mean);
		}
		double[] sorted = errors.clone();
		Arrays.sort(sorted);
		double median = sorted[LeastMedianOfSquares.selectThresholdIndex(N, 0.5)];
		double expected = alg.getInlierRouseeuwSigma()*1.4826*(1.0 + 5.0/(N - 1))*median;
		assertEquals(expected, alg.getInlierThreshold(), 1e-12);

		List<Double> matchSet = alg.getMatchSet();
		int count = 0;
		for (int i = 0; i < N; i++) {
			if (errors[i] <= expected) {
				assertSame(points.get(i), matchSet.get(count));
				assertEquals(i, alg.getInputIndex(count));
				count++;
			}
		}
		assertEquals(count, matchSet.size());

		// All the outliers should be removed and most of the inliers kept
		for (Double p : matchSet) {
			assertTrue(Math.abs(p - 2.5) < 2.0);
		}
		assertTrue(matchSet.size() > numInliers*0.95);
	}

	/** Not enough points to estimate the noise. Every point should be an inlier */
	@Test void sigma_tooFewPoints() {
		LeastMedianOfSquares<double[], Double> alg = createAlg();
		alg.setSampleSize(3);
		List<Double> points = createGaussian(3, 2.5, 0.1);
		assertTrue(alg.process(points));
		assertEquals(3, alg.getMatchSet().size());

		double mean = alg.getModelParameters()[0];
		double largest = 0;
		for (double p : points) {
			largest = Math.max(largest, Math.abs(p - mean));
		}
		assertEquals(largest, alg.getInlierThreshold(), 1e-12);
	}

	@Test void selectThresholdIndex() {
		// typical cases
		assertEquals(5, LeastMedianOfSquares.selectThresholdIndex(10, 0.5));
		assertEquals(6, LeastMedianOfSquares.selectThresholdIndex(11, 0.5));
		// clamped to the last valid index
		assertEquals(0, LeastMedianOfSquares.selectThresholdIndex(1, 0.5));
		assertEquals(9, LeastMedianOfSquares.selectThresholdIndex(10, 1.0));
	}

	LeastMedianOfSquares<double[], Double> createAlg() {
		var alg = new LeastMedianOfSquares<>(234, 200, new DoubleArrayManager(1), Double.class);
		alg.setModel(MeanModelFitter::new, DistanceFromMeanModel::new);
		return alg;
	}

	List<Double> createGaussian( int count, double mean, double sigma ) {
		var points = new ArrayList<Double>();
		for (int i = 0; i < count; i++) {
			points.add(mean + rand.nextGaussian()*sigma);
		}
		return points;
	}
}
