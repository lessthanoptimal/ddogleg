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

package org.ddogleg.fitting.modelset.ransac;

import org.ddogleg.fitting.modelset.*;
import org.ddogleg.struct.DogArray_I32;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestRansac extends GenericModelMatcherPostTests {

	public TestRansac() {
		configure(0.9, 0.05, true);
	}

	@Override
	public ModelMatcherPost<double[], Double> createModelMatcher( ModelManager<double[]> manager,
																  int minPoints,
																  double fitThreshold ) {
		Ransac<double[], Double> ret = new Ransac<>(344, 50, fitThreshold, manager, Double.class);
		ret.setSampleSize(minPoints);
		return ret;
	}

	/**
	 * See if it correctly randomly selects points when the initial set size is
	 * much smaller than the data set size
	 */
	@SuppressWarnings({"NumberEquality"})
	@Test
	public void randomDraw() {
		randomDraw_sanity(200, 15);
		randomDraw_sanity(200, 150);
	}

	private void randomDraw_sanity( int total, int numSample ) {
		List<Integer> dataSet = new ArrayList<>();

		for (int i = 0; i < total; i++) {
			dataSet.add(i);
		}

		List<Integer> initSet = new ArrayList<>();
		Ransac.randomDraw(dataSet, numSample, initSet, rand);

		assertEquals(numSample, initSet.size());

		// make sure the item is in the original data set and that it is only contained once
		int numTheSame = 0;
		for (int i = 0; i < initSet.size(); i++) {
			Integer o = initSet.get(i);
			// make sure it is in the original dataset
			assertTrue(dataSet.contains(o));

			// make sure the order has been changed
			assertNotSame(dataSet.get(i), o);

			// make sure the order has been changed
			if (o == i)
				numTheSame++;

			// make sure only one copy is in the init set
			for (int j = i + 1; j < initSet.size(); j++) {
				if (o.equals(initSet.get(j))) {
					fail("Multiple copies in initSet");
				}
			}
		}

		// if the order has been randomized then very few should be in the original order
		assertTrue(numTheSame < initSet.size()*0.9);

		// call get init set once more and see if it was cleared
		Ransac.randomDraw(dataSet, numSample, initSet, rand);
		assertEquals(numSample, initSet.size());
	}

	/**
	 * Checks the histogram of selected items to see if it is a uniformly random distribution
	 */
	@Test
	public void randomDraw_Histogram() {
		List<Integer> dataSet = new ArrayList<>();

		for (int i = 0; i < 30; i++) {
			dataSet.add(i);
		}
		int[] histogram = new int[dataSet.size()];

		List<Integer> selected = new ArrayList<>();

		int numTrials = 10000;
		for (int i = 0; i < numTrials; i++) {
			Ransac.randomDraw(dataSet, 3, selected, rand);

			for (int j = 0; j < selected.size(); j++) {
				histogram[selected.get(j)]++;
			}
		}

		double expected = (3.0/30.0)*numTrials;

		for (int i = 0; i < histogram.length; i++) {
			assertTrue(Math.abs(histogram[i] - expected)/expected < 0.1);
		}
	}

	/**
	 * Checks the histogram of selected items to see if it is a uniformly random distribution
	 */
	@Test
	public void randomDraw_integer_Histogram() {
		DogArray_I32 selectedIdx = new DogArray_I32();

		List<Integer> dataSet = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			dataSet.add(i);
		}
		int[] histogram = new int[dataSet.size()];

		List<Integer> selected = new ArrayList<>();

		int numTrials = 10000;
		for (int i = 0; i < numTrials; i++) {
			Ransac.randomDraw(selectedIdx, dataSet.size(), 3, rand);
			Ransac.addSelect(selectedIdx, 3, dataSet, selected);

			for (int idx = 0; idx < selectedIdx.size; idx++) {
				assertEquals(idx, selectedIdx.get(idx));
			}

			for (int j = 0; j < selected.size(); j++) {
				histogram[selected.get(j)]++;
			}
		}

		double expected = (3.0/30.0)*numTrials;

		for (int i = 0; i < histogram.length; i++) {
			assertTrue(Math.abs(histogram[i] - expected)/expected < 0.1);
		}
	}

	/**
	 * See if it will select models with more of the correct points in it
	 */
	@Test
	public void selectMatchSet() {
		double modelVal = 50;

		List<Integer> dataSet = new ArrayList<>();

		for (int i = 0; i < 200; i++) {
			dataSet.add(i);
		}
		Ransac<double[], Integer> ransac = new Ransac<>(234, 20, 1, new DebugModelStuff((int)modelVal), Integer.class);
		ransac.setModel(()-> new DebugModelStuff((int)modelVal), ()-> new DebugModelStuff((int)modelVal));
		ransac.setSampleSize(5);
		// declare the array so it doesn't blow up when accessed
		Objects.requireNonNull(ransac.helper).matchToInput = new int[dataSet.size()];
		double[] param = new double[]{modelVal};

		assertTrue(ransac.helper.selectMatchSet(dataSet, 0, 4, param));

		assertEquals(ransac.helper.candidatePoints.size(), 7);
	}

	/**
	 * Checks to see if it will abort if it can't possibly beat the best model
	 */
	@Test
	public void selectMatchSet_abort() {
		double modelVal = 50;

		List<Integer> dataSet = new ArrayList<>();

		for (int i = 0; i < 200; i++) {
			dataSet.add(i);
		}

		Ransac<double[], Integer> ransac = new Ransac<>(234, 20, 1, new DebugModelStuff((int)modelVal), Integer.class);
		ransac.setModel(()-> new DebugModelStuff((int)modelVal), ()-> new DebugModelStuff((int)modelVal));
		ransac.setSampleSize(5);
		// declare the array so it doesn't blow up when accessed
		Objects.requireNonNull(ransac.helper).matchToInput = new int[dataSet.size()];
		double[] param = new double[]{modelVal};

		assertFalse(ransac.helper.selectMatchSet(dataSet, 150, 4, param));
	}

	@SuppressWarnings({"NullAway"})
	public static class DebugModelStuff
			implements ModelManager<double[]>,
			DistanceFromModel<double[], Integer>,
			ModelGenerator<double[], Integer> {
		int threshold;
		double error;
		double[] param;

		public DebugModelStuff( int threshold ) {
			this.threshold = threshold;
		}

		@Override
		public void setModel( double[] param ) {
			this.param = param;
		}

		@Override
		public double distance( Integer pt ) {
			return Math.abs(pt - param[0]);
		}

		@Override
		public void distances( List<Integer> points, double[] distance ) {
			throw new RuntimeException("Why was this called?");
		}

		@Override
		public Class<Integer> getPointType() {
			return Integer.class;
		}

		@Override
		public Class<double[]> getModelType() {
			return double[].class;
		}

		@Override
		public double[] createModelInstance() {
			return new double[1];
		}

		@Override
		public void copyModel( double[] src, double[] dst ) {
			System.arraycopy(src, 0, dst, 0, 1);
		}

		@Override
		public boolean generate( List<Integer> dataSet, double[] p ) {

			error = 0;

			int offset = (int)p[0];

			for (Integer a : dataSet) {
				if (a + offset >= threshold) {
					error++;
				}
			}

			error += offset;
			p[0] = error;

			return true;
		}

		@Override
		public int getMinimumPoints() {
			return 1;
		}
	}
}
