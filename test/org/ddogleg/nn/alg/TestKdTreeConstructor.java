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
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway"})
public class TestKdTreeConstructor {

	KdTreeDistance<double[]> distance = new KdTreeEuclideanSq_F64(2);

	/**
	 * Makes sure a branch is created correctly given the results from the splitter
	 */
	@Test
	public void computeBranch_dontTrack() {
		DummySplitter splitter = createSplitter(1,1,false);
		splitter.splitAxis = 1;
		splitter.splitPoint = new double[]{2,3};

		KdTreeConstructor<double[]> alg = new KdTreeConstructor<>(new KdTreeMemory(),splitter);

		KdTree.Node n = alg.computeBranch(new ArrayList<>(),null);

		assertSame(n.point, splitter.splitPoint);
		assertEquals(n.split, splitter.splitAxis);
		assertEquals(-2, n.index); // default value in splitter
		assertTrue(n.left.isLeaf());
		assertTrue(n.right.isLeaf());
		assertSame(n.left.point, splitter.left.get(0));
		assertEquals(-1, n.left.index);
		assertSame(n.right.point, splitter.right.get(0));
		assertEquals(-1, n.right.index);
	}

	/**
	 * Same test as above but with associated data
	 */
	@Test
	public void computeBranch_trackIndexes() {
		DummySplitter splitter = createSplitter(1,1,true);
		splitter.splitAxis = 1;
		splitter.splitPoint = new double[]{2,3};
		splitter.splitIndex = 2;

		KdTreeConstructor<double[]> alg = new KdTreeConstructor<>(new KdTreeMemory(),splitter);

		KdTree.Node n = alg.computeBranch(new ArrayList<>(),new DogArray_I32());

		assertSame(n.point, splitter.splitPoint);
		assertEquals(n.split, splitter.splitAxis);
		assertEquals(n.index, splitter.splitIndex);
		assertTrue(n.left.isLeaf());
		assertTrue(n.right.isLeaf());
		assertSame(n.left.point, splitter.left.get(0));
		assertEquals(n.left.index, splitter.leftIndex.get(0));
		assertSame(n.right.point, splitter.right.get(0));
		assertEquals(n.right.index, splitter.rightIndex.get(0));
	}

	@Test
	public void computeChild() {
		KdTreeConstructor<double[]> alg = new KdTreeConstructor<>(distance);

		List<double[]> points = new ArrayList<>();
		DogArray_I32 data = new DogArray_I32();

		// empty lists should be null
		KdTree.Node n = new KdTree.Node();
		n.point = new double[2];
		n.index = 1;
		KdTree.Node found = alg.computeChild(points,data);
		assertNull(found);

		// add a point
		points.add( new double[2] );
		data.add(2);
		found = alg.computeChild(points,data);
		assertTrue(found.isLeaf());
		assertSame(found.point, points.get(0));
		assertEquals(found.index, data.get(0));

		// for all the other cases it will create a branch.  testing that will require a bit more work...
	}

	/**
	 * Basic tests to see if it can handle different sized input lists.
	 */
	@Test
	public void construct() {
		KdTreeConstructor<double[]> alg = new KdTreeConstructor<>(new KdTreeMemory(),createSplitter(1,1,false));

		// test an empty list
		List<double[]> points = new ArrayList<>();
		KdTree tree = alg.construct(points,false);
		assertEquals(2, tree.N);
		assertNull(tree.root);

		// add a point
		points.add( new double[]{1,2});
		tree = alg.construct(points,false);
		assertEquals(2, tree.N);
		assertSame(tree.root.point, points.get(0));
		assertTrue(tree.root.isLeaf());

		// add another point.  These input points are ignored by the dummy splitter
		points.add( new double[]{1,2,4,5});
		tree = alg.construct(points,false);
		assertEquals(2, tree.N);
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
		List<double[]> left = new ArrayList<>();
		List<double[]> right = new ArrayList<>();
		DogArray_I32 leftData = null;
		DogArray_I32 rightData = null;

		for( int i = 0; i < numLeft; i++ ) {
			left.add( new double[2] );
		}
		for( int i = 0; i < numRight; i++ ) {
			right.add( new double[2] );
		}

		if( withData ) {
			leftData = new DogArray_I32();
			rightData = new DogArray_I32();

			for( int i = 0; i < numLeft; i++ ) {
				leftData.add( i );
			}
			for( int i = 0; i < numRight; i++ ) {
				rightData.add( i );
			}
		}

		return new DummySplitter(-2,null,1,left,leftData,right,rightData);
	}

	public static class DummySplitter implements AxisSplitter<double[]> {

		int splitIndex;
		double[] splitPoint;
		int splitAxis;
		List<double[]> left;
		DogArray_I32 leftIndex;
		List<double[]> right;
		DogArray_I32 rightIndex;

		public DummySplitter( int splitIndex, double[] splitPoint,
							  int splitAxis, List<double[]> left,
							  DogArray_I32 leftIndex, List<double[]> right,
							  DogArray_I32 rightIndex)
		{
			this.splitIndex = splitIndex;
			this.splitPoint = splitPoint;
			this.splitAxis = splitAxis;
			this.left = left;
			this.leftIndex = leftIndex;
			this.right = right;
			this.rightIndex = rightIndex;
		}

		@Override
		public void splitData(List<double[]> points, @Nullable DogArray_I32 data,
							  List<double[]> left, @Nullable DogArray_I32 leftData,
							  List<double[]> right, @Nullable DogArray_I32 rightData)
		{
			left.addAll(this.left);
			right.addAll(this.right);
			if( leftData != null )
				leftData.addAll(this.leftIndex);
			if( rightData != null )
				rightData.addAll(this.rightIndex);
		}

		@Override
		public double[] getSplitPoint() {
			return splitPoint;
		}

		@Override
		public int getSplitIndex() {
			return splitIndex;
		}

		@Override
		public int getSplitAxis() {
			return splitAxis;
		}

		@Override
		public int getPointLength() {
			return 2;
		}
	}

}
