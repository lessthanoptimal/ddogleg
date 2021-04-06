/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.fitting.modelset;

import org.ddogleg.fitting.modelset.distance.DistanceFromMeanModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A series of tests are provided that test to see the provided model set algorithm has the expected
 * behavior.  This includes removing outliers and estimating the model parameters.  The test
 * cases tend to be fairly easy so that they will work on all model set algorithms.
 *
 * @author Peter Abeles
 */
public abstract class GenericModelMatcherPostTests {

	protected Random rand = new Random(0x353456);

	// how many of the points is it expected to match
	protected double minMatchFrac = 1.0;
	// how close does the parameter set need to be
	protected double parameterTol = 1e-8;
	// should it check the inlier set for accuracy
	protected boolean checkInlierSet = true;

	// mean of the true inlier set
	// this value is the best estimate that can possibly be done
	private double inlierMean;

	// If the model matcher is deterministic and doesn't use any random numbers
	protected boolean deterministic = false;

	protected void configure( double minMatchFrac, double parameterTol, boolean checkInlierSet ) {
		this.minMatchFrac = minMatchFrac;
		this.parameterTol = parameterTol;
		this.checkInlierSet = checkInlierSet;
	}

	/**
	 * Sees if it can correctly select a model set and determine the best fit parameters for
	 * a simple test case.
	 */
	@Test
	public void performSimpleModelFit() {
		double mean = 2.5;
		double tol = 0.2;

		// generate the points with a smaller tolerance to account for fitting error
		// later on.
		ModelMatcher<double[], Double> alg = createModel(4, tol*0.95);

		List<Double> samples = createSampleSet(100, mean, tol, 0.1);

		assertTrue(alg.process(samples));

		List<Double> matchSet = alg.getMatchSet();

		if (checkInlierSet)
			assertTrue(matchSet.size()/90.0 >= minMatchFrac);
		assertEquals(inlierMean, alg.getModelParameters()[0], parameterTol);
	}

	/**
	 * Multiple data sets are processed in an attempt to see if it is properly reinitializing
	 * itself and returns the correct solutions.
	 */
	@Test
	public void runMultipleTimes() {
		double mean = 2.5;
		double tol = 0.2;

		// generate the points with a smaller tolerance to account for fitting error
		// later on.
		ModelMatcher<double[], Double> alg = createModel(4, tol);

		for (int i = 0; i < 10; i++) {
			// try different sample sizes in each trial.  a bug was found once where
			// a small value of N than previous caused a problem
			int N = 200 - i*10;
			List<Double> samples = createSampleSet(N, mean, tol*0.90, 0.1);

			assertTrue(alg.process(samples));

			List<Double> matchSet = alg.getMatchSet();

			double foundMean = alg.getModelParameters()[0];

			if (checkInlierSet)
				assertTrue(matchSet.size()/(N*0.9) >= minMatchFrac);
			assertEquals(inlierMean, foundMean, parameterTol);
		}
	}

	/**
	 * Creates a set of sample points that are part of the model and some outliers
	 *
	 * @param numPoints Number of sample points it will generate
	 * @param mean Mean of the distribution
	 * @param modelDist How close to the model do the points need to be.
	 * @param fracOutlier Fraction of the points which will be outliers.
	 * @return Set of sample points
	 */
	protected List<Double> createSampleSet( int numPoints, double mean, double modelDist, double fracOutlier ) {
		List<Double> ret = new ArrayList<>();

		double numOutlier = (int)(numPoints*fracOutlier);

		inlierMean = 0;

		for (int i = 0; i < numPoints - numOutlier; i++) {
			double d = mean + (rand.nextDouble() - 0.5)*2.0*modelDist;

			inlierMean += d;

			ret.add(d);
		}

		inlierMean /= ret.size();

		while (ret.size() < numPoints) {
			double d = (rand.nextDouble() - 0.5)*200*modelDist;

			// add a point if its sufficiently far away from the model
			if (Math.abs(d) > modelDist*10) {
				ret.add(d);
			}
		}

		// randomize the order
		Collections.shuffle(ret, rand);

		return ret;
	}

	/**
	 * Make sure the function getInputIndex() returns the original index
	 */
	@Test
	public void checkMatchSetToInputIndex() {
		double mean = 2.5;
		double tol = 0.2;

		// generate the points with a smaller tolerance to account for fitting error
		// later on.
		ModelMatcher<double[], Double> alg = createModel(4, tol*0.95);

		List<Double> samples = createSampleSet(100, mean, tol, 0.1);

		assertTrue(alg.process(samples));

		List<Double> matchSet = alg.getMatchSet();

		// sanity check to make sure there is a large enough match set
		assertTrue(matchSet.size() > 20);

		int orderNotTheSame = 0;
		for (int i = 0; i < matchSet.size(); i++) {
			int expected = samples.indexOf(matchSet.get(i));
			int found = alg.getInputIndex(i);

			if (found != i)
				orderNotTheSame++;

			assertEquals(expected, found);
		}

		// sanity check to make sure the order has been changed
		assertTrue(orderNotTheSame != matchSet.size());
	}

	/**
	 * Make sure that if reset is called it produces identical results
	 */
	@Test
	public void reset() {
		double mean = 2.5;
		double tol = 0.2;

		// generate the points with a smaller tolerance to account for fitting error
		// later on.
		ModelMatcher<double[], Double> alg = createModel(1, 0.6);

		// Create a uniform sample with multiple just as good solutions so it's unlikely that two random
		// draws will get the exact same solution
		List<Double> samples = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			samples.add(mean - (0.5 - i/500.0)*2.0);
		}

		assertTrue(alg.process(samples));
		List<Double> matchesA = new ArrayList<>(alg.getMatchSet());

		assertTrue(alg.process(samples));
		List<Double> matchesB = new ArrayList<>(alg.getMatchSet());

		// See if this produces different results
		boolean matched = matchesA.size() == matchesB.size();
		if (matched) {
			for (int i = 0; i < matchesA.size(); i++) {
				if (!matchesA.get(i).equals(matchesB.get(i))) {
					matched = false;
					break;
				}
			}
		}
		assertEquals(deterministic, matched);

		if (deterministic)
			return;

		// It should now produce identical results to the first run
		alg.reset();
		assertTrue(alg.process(samples));
		List<Double> matchesC = new ArrayList<>(alg.getMatchSet());
		assertEquals(matchesA.size(), matchesC.size());
		for (int i = 0; i < matchesA.size(); i++) {
			assertEquals(matchesA.get(i), matchesC.get(i));
		}
	}

	protected ModelMatcherPost<double[], Double> createModel( int minPoints, double fitThreshold ) {
		DoubleArrayManager manager = new DoubleArrayManager(1);

		ModelMatcherPost<double[], Double> alg = createModelMatcher(manager, minPoints, fitThreshold);
		alg.setModel(MeanModelFitter::new, DistanceFromMeanModel::new);
		return alg;
	}

	public abstract ModelMatcherPost<double[], Double> createModelMatcher(
			ModelManager<double[]> manager,
			int minPoints, double fitThreshold );
}
