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

package org.ddogleg.nn.alg;

import org.ddogleg.nn.alg.distance.KdTreeEuclideanSq_F64;
import org.ddogleg.struct.DogArray_I32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.ddogleg.nn.alg.TestKdTreeConstructor.createPoints;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestAxisSplitterMedian {

	List<double[]> left = new ArrayList<>();
	List<double[]> right = new ArrayList<>();
	DogArray_I32 leftData = new DogArray_I32();
	DogArray_I32 rightData = new DogArray_I32();

	KdTreeDistance<double[]> distance = new KdTreeEuclideanSq_F64(2);

	@BeforeEach
	public void init() {
		left.clear();
		right.clear();
		leftData.reset();
		rightData.reset();
	}

	@Test
	public void splitData_one() {
		List<double[]> points = createPoints(2, 1,2);

		AxisSplitterMedian<double[]> alg = new AxisSplitterMedian<>(distance,new DummyRule(0));
		alg.splitData(points,null,left,null,right,null);

		// the median point is not included and will become the node's point
		assertEquals(0,left.size());
		assertEquals(0,right.size());
	}

	@Test
	public void splitData_two() {
		List<double[]> points = createPoints(2, 1,2 , 3,5);

		AxisSplitterMedian<double[]> alg = new AxisSplitterMedian<>(distance,new DummyRule(0));
		alg.splitData(points,null,left,null,right,null);

		// The second point is selected to be the median
		assertEquals(1,left.size());
		assertEquals(0, right.size());

		assertEquals(1,left.get(0)[0],1e-8);
	}

	@Test
	public void splitData_three() {
		List<double[]> points = createPoints(2, 1,2 , 3,5 , -3,4);

		AxisSplitterMedian<double[]> alg = new AxisSplitterMedian<>(distance,new DummyRule(0));
		alg.splitData(points,null,left,null,right,null);

		// The first point is selected to be the median
		assertEquals(1,left.size());
		assertEquals(1,right.size());

		assertEquals(-3,left.get(0)[0],1e-8);
		assertEquals(3,right.get(0)[0],1e-8);
	}

	/**
	 * Make sure the split point is returned
	 */
	@Test
	public void splitData_split_point() {
		List<double[]> points = createPoints(2, 1,2 , 3,5 , -3,4);

		AxisSplitterMedian<double[]> alg = new AxisSplitterMedian<>(distance,new DummyRule(1));
		alg.splitData(points,null,left,null,right,null);

		assertEquals(1,alg.getSplitAxis());
		assertEquals(-3,alg.getSplitPoint()[0],1e-8);
//		assertEquals(2, alg.getSplitIndex());
	}

	@Test
	public void splitData_withData() {
		List<double[]> points = createPoints(2, 1,2 , 3,5 , -3,4);
		DogArray_I32 data = new DogArray_I32();
		for( int i = 0; i < points.size(); i++ )
			data.add(i);

		AxisSplitterMedian<double[]> alg = new AxisSplitterMedian<>(distance,new DummyRule(1));
		alg.splitData(points,data,left,leftData,right,rightData);

		assertEquals(1,left.size());
		assertEquals(1,right.size());
		assertEquals(1,leftData.size());
		assertEquals(1,rightData.size());

		assertEquals(1,alg.getSplitAxis(),1e-8);
		assertEquals(-3,alg.getSplitPoint()[0],1e-8);
		assertEquals(alg.getSplitIndex(), data.get(2));
		assertEquals(leftData.get(0), data.get(0));
		assertEquals(rightData.get(0), data.get(1));
	}

	/**
	 * Make two of the points identical and see if things blow up
	 */
	@Test
	public void identical_points() {
		List<double[]> points = createPoints(2, 1,2, 1.1,4 , 1,2);

		AxisSplitterMedian<double[]> alg = new AxisSplitterMedian<>(distance,new DummyRule(0));
		alg.splitData(points,null,left,null,right,null);

		// sorted order should be (1,2) (1,2) (1.1,4)

		assertEquals(1, left.size());
		assertEquals(1, right.size());

		assertEquals(1,alg.getSplitPoint()[0],1e-8);
		assertEquals(1,left.get(0)[0],1e-8);
		assertEquals(1.1,right.get(0)[0],1e-8);
	}

	@Test
	public void checkRuleSetCalled() {
		DummyRule rule = new DummyRule(2);
		new AxisSplitterMedian<>(distance,rule);
		assertTrue(rule.calledSetDimension);
	}

	private static class DummyRule implements AxisSplitRule {

		int which;
		boolean calledSetDimension = false;

		private DummyRule(int which) {
			this.which = which;
		}

		@Override
		public void setDimension(int N) {
			calledSetDimension = true;
		}

		@Override
		public int select(double[] variance) {
			return which;
		}
	}

}
