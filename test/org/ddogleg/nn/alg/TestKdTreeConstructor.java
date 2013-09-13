/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestKdTreeConstructor {

	/**
	 * Makes sure a branch is created correctly given the results from the splitter
	 */
	@Test
	public void computeBranch() {
		DummySplitter splitter = createSplitter(1,1,false);
		splitter.splitAxis = 1;
		splitter.splitPoint = new double[]{2,3};

		KdTreeConstructor<?> alg = new KdTreeConstructor(new KdTreeMemory(),2,splitter);

		KdTree.Node n = alg.computeBranch(new ArrayList<double[]>(),null);

		assertTrue(n.point == splitter.splitPoint);
		assertTrue(n.split == splitter.splitAxis);
		assertTrue(n.data == null);
		assertTrue(n.left.isLeaf());
		assertTrue(n.right.isLeaf());
		assertTrue(n.left.point == splitter.left.get(0));
		assertTrue(n.left.data == null);
		assertTrue(n.right.point == splitter.right.get(0));
		assertTrue(n.right.data == null);
	}

	/**
	 * Same test as above but with associated data
	 */
	@Test
	public void computeBranch_widthData() {
		DummySplitter splitter = createSplitter(1,1,true);
		splitter.splitAxis = 1;
		splitter.splitPoint = new double[]{2,3};
		splitter.splitData = 2;

		KdTreeConstructor<?> alg = new KdTreeConstructor(new KdTreeMemory(),2,splitter);

		KdTree.Node n = alg.computeBranch(new ArrayList<double[]>(),new ArrayList());

		assertTrue(n.point == splitter.splitPoint);
		assertTrue(n.split == splitter.splitAxis);
		assertTrue(n.data == splitter.splitData);
		assertTrue(n.left.isLeaf());
		assertTrue(n.right.isLeaf());
		assertTrue(n.left.point == splitter.left.get(0));
		assertTrue(n.left.data == splitter.leftData.get(0));
		assertTrue(n.right.point == splitter.right.get(0));
		assertTrue(n.right.data == splitter.rightData.get(0));
	}

	@Test
	public void computeChild() {
		KdTreeConstructor alg = new KdTreeConstructor(2);

		List points = new ArrayList();
		List data = new ArrayList();

		// empty lists should be null
		KdTree.Node n = new KdTree.Node();
		n.point = new double[2];
		n.data = 1;
		KdTree.Node found = alg.computeChild(points,data);
		assertTrue(found == null);

		// add a point
		points.add( new double[2] );
		data.add(2);
		found = alg.computeChild(points,data);
		assertTrue(found.isLeaf());
		assertTrue(found.point == points.get(0));
		assertTrue(found.data == data.get(0));

		// for all the other cases it will create a branch.  testing that will require a bit more work...
	}

	/**
	 * Basic tests to see if it can handle different sized input lists.
	 */
	@Test
	public void construct() {
		KdTreeConstructor<?> alg = new KdTreeConstructor(new KdTreeMemory(),2,createSplitter(1,1,false));

		// test an empty list
		List<double[]> points = new ArrayList<double[]>();
		KdTree tree = alg.construct(points,null);
		assertTrue(tree.N == 2);
		assertTrue(tree.root == null);

		// add a point
		points.add( new double[]{1,2});
		tree = alg.construct(points,null);
		assertTrue(tree.N == 2);
		assertTrue(tree.root.point == points.get(0));
		assertTrue(tree.root.isLeaf());

		// add another point.  These input points are ignored by the dummy splitter
		points.add( new double[]{1,2,4,5});
		tree = alg.construct(points,null);
		assertTrue(tree.N == 2);
		assertTrue(tree.root.left.isLeaf());
		assertTrue(tree.root.right.isLeaf());
	}



	public static List<double[]> createPoints( int dimen , double ...v ) {

		List<double[]> ret = new ArrayList<double[]>();

		for( int i = 0; i < v.length; i += dimen ) {
			double p[] = new double[dimen];
			for( int j = 0; j < dimen; j++ ) {
				p[j] = v[i+j];
			}
			ret.add(p);
		}

		return ret;
	}

	private DummySplitter createSplitter( int numLeft , int numRight , boolean withData) {
		List<double[]> left = new ArrayList<double[]>();
		List<double[]> right = new ArrayList<double[]>();
		List<Integer> leftData = null;
		List<Integer> rightData = null;

		for( int i = 0; i < numLeft; i++ ) {
			left.add( new double[2] );
		}
		for( int i = 0; i < numRight; i++ ) {
			right.add( new double[2] );
		}

		if( withData ) {
			leftData = new ArrayList<Integer>();
			rightData = new ArrayList<Integer>();

			for( int i = 0; i < numLeft; i++ ) {
				leftData.add( i );
			}
			for( int i = 0; i < numRight; i++ ) {
				rightData.add( i );
			}
		}


		return new DummySplitter(null,null,1,left,leftData,right,rightData);
	}

	public static class DummySplitter implements AxisSplitter<Integer> {

		boolean calledSetDimension = false;
		Integer splitData;
		double[] splitPoint;
		int splitAxis;
		List<double[]> left;
		List<Integer> leftData;
		List<double[]> right;
		List<Integer> rightData;

		public DummySplitter(Integer splitData, double[] splitPoint, int splitAxis,
							 List<double[]> left, List<Integer> leftData,
							 List<double[]> right, List<Integer> rightData)
		{
			this.splitData = splitData;
			this.splitPoint = splitPoint;
			this.splitAxis = splitAxis;
			this.left = left;
			this.leftData = leftData;
			this.right = right;
			this.rightData = rightData;
		}

		@Override
		public void setDimension(int N) {
			calledSetDimension = true;
		}

		@Override
		public void splitData(List<double[]> points, List<Integer> data,
							  List<double[]> left, List<Integer> leftData,
							  List<double[]> right, List<Integer> rightData)
		{
			left.addAll(this.left);
			right.addAll(this.right);
			if( leftData != null )
				leftData.addAll(this.leftData);
			if( rightData != null )
				rightData.addAll(this.rightData);
		}

		@Override
		public double[] getSplitPoint() {
			return splitPoint;
		}

		@Override
		public Integer getSplitData() {
			return splitData;
		}

		@Override
		public int getSplitAxis() {
			return splitAxis;
		}
	}

}
