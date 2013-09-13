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

import org.ddogleg.sorting.QuickSelectArray;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_F64;
import org.ddogleg.struct.GrowQueue_I32;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Standard tests for implementations of {@link org.ddogleg.nn.alg.KdTreeSearchN}.
 *
 * @author Peter Abeles
 */
public abstract class StandardKdTreeSearchNTests {

	FastQueue<KdTreeResult> found = new FastQueue<KdTreeResult>(KdTreeResult.class,true);

	Random rand = new Random(234);

	/**
	 * Creates a KdTreeSearch which will produce optimal results
	 */
	public abstract KdTreeSearchN createAlg();

	/**
	 * Try several searches and see if they all produce good results.  Just fine the nearest-neighbor
	 */
	@Test
	public void findClosest_basic_1() {
		KdTreeSearchN alg = createAlg();

		KdTree tree = createTreeA();
		alg.setTree(tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		// the first decision will be incorrect and it will need to back track
		found.reset();
		alg.findNeighbor(new double[]{11, 8}, 1, found);
		assertEquals(1,found.size);
		assertTrue(found.data[0].node == tree.root.right.right);

		// the root will be the best
		found.reset();
		alg.findNeighbor(new double[]{1.001, 1.99999}, 1, found);
		assertTrue(found.data[0].node == tree.root);

		// a point on the left branch will be a perfect fit
		found.reset();
		alg.findNeighbor(new double[]{2, 0.8}, 1, found);
		assertTrue(found.data[0].node == tree.root.left.right);

		// a point way outside the tree's bounds
		found.reset();
		alg.findNeighbor(new double[]{-10000, 0.5}, 1, found);
		assertTrue(found.data[0].node == tree.root.left.left);
	}

	/**
	 * See if it can handle a null leaf
	 */
	@Test
	public void findClosest_nullLeaf() {
		KdTreeSearchN alg = createAlg();

		KdTree tree = StandardKdTreeSearch1Tests.createTreeWithNull();
		alg.setTree(tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		// the first decision will be incorrect and it will need to back track
		found.reset();
		alg.findNeighbor(new double[]{2, 3}, 1, found);
		assertTrue(found.get(0).node == tree.root);

	}

	/**
	 * Randomly generate several searches and check the result
	 */
	@Test
	public void randomTests() {
		KdTreeSearchN alg = createAlg();

		KdTree tree = createTreeA();
		alg.setTree(tree);

		List<double[]> data = new ArrayList<double[]>();
		flattenTree(tree.root,data);

		for( int i = 0; i < 100; i++ ) {
			int searchN = rand.nextInt(data.size()+5);

			double[] target = data.get( rand.nextInt(data.size()));

			double maxDistance = rand.nextDouble()*10;

			List<double[]> expected = findNeighbors(data,target,maxDistance,searchN);

			found.reset();
			alg.setMaxDistance(maxDistance);
			alg.findNeighbor(target, searchN, found);
			assertEquals(found.size,expected.size());

			for( int j = 0; j < expected.size(); j++ ) {
				checkContains(expected.get(j));
			}
		}
	}

	/**
	 * The tree is empty and it should always fail
	 */
	@Test
	public void findClosest_empty() {
		KdTreeSearchN alg = createAlg();
		alg.setTree( new KdTree(2) );

		found.reset();
		alg.findNeighbor(new double[]{11, 8}, 2, found);
		assertEquals(0, found.size());
	}

	/**
	 * The tree is a leaf and should always return the same result
	 */
	@Test
	public void findClosest_leaf() {
		KdTree tree = new KdTree(2);
		tree.root = new KdTree.Node(new double[]{1,2},null);

		KdTreeSearchN alg =createAlg();
		alg.setTree( tree );

		found.reset();
		alg.findNeighbor(new double[]{11, 8}, 2, found);
		assertEquals(1,found.size);
		assertTrue(found.data[0].node == tree.root);
		found.reset();
		alg.findNeighbor(new double[]{2, 5}, 2, found);
		assertEquals(1,found.size);
		assertTrue(found.data[0].node == tree.root);
	}

	/**
	 * See if max distance is being respected
	 */
	@Test
	public void findClosest_maxDistance() {
		KdTree tree = new KdTree(2);
		tree.root = new KdTree.Node(new double[]{1,2},null);

		KdTreeSearchN alg = createAlg();
		alg.setTree( tree );
		alg.setMaxDistance(2);

		found.reset();
		alg.findNeighbor(new double[]{11, 8}, 1, found);
		assertEquals(0, found.size);
		found.reset();
		alg.findNeighbor(new double[]{1, 1.5}, 1, found);
		assertEquals(1, found.size);
		assertTrue(found.data[0].node == tree.root);
	}

	/**
	 * Make sure the distance it returns is correct
	 */
	@Test
	public void checkDistance() {
		KdTreeSearchN alg = createAlg();

		KdTree tree = createTreeA();
		alg.setTree(tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		double[] pt = new double[]{11.5,8.2};
		found.reset();
		alg.findNeighbor(pt, 1, found);

		assertEquals(1,found.size);
		double d0 = found.get(0).node.point[0]-pt[0];
		double d1 = found.get(0).node.point[1]-pt[1];

		assertEquals(d0*d0 + d1*d1,found.get(0).distance,1e-8);
	}

	private void checkContains( KdTree.Node node ) {
		for( int i = 0; i < found.size; i++ ) {
			if( found.data[i].node == node )
				return;
		}

		fail("can't find");
	}

	private void checkContains( double[] d ) {
		for( int i = 0; i < found.size; i++ ) {
			if( found.data[i].node.point == d )
				return;
		}

		fail("can't find");
	}

	private static void flattenTree( KdTree.Node n , List<double[]> data ) {
		data.add(n.point);
		if( !n.isLeaf() ) {
			flattenTree(n.left,data);
			flattenTree(n.right,data);
		}
	}

	private static List<double[]> findNeighbors( List<double[]> data , double[]target , double maxDistance , int maxN ) {
		List<double[]> ret = new ArrayList<double[]>();

		List<double[]> found = new ArrayList<double[]>();
		GrowQueue_F64 distances = new GrowQueue_F64();
		GrowQueue_I32 indexes = new GrowQueue_I32();


		for( int i = 0; i < data.size(); i++ ) {
			double[] d = data.get(i);

			double dx = d[0] - target[0];
			double dy = d[1] - target[1];

			double dist = Math.sqrt(dx*dx + dy*dy);
			if( dist <= maxDistance ) {
				distances.add(dist);
				found.add(d);
			}
		}

		indexes.resize(distances.size);

		maxN = Math.min(maxN,distances.size);

		QuickSelectArray.selectIndex(distances.data,maxN,distances.size,indexes.data);

		for( int i = 0; i < maxN; i++ ) {
			ret.add( found.get( indexes.data[i]));
		}

		return ret;
	}

	public static KdTree createTreeA() {

		KdTree tree = new KdTree(2);

		tree.root = new KdTree.Node(new double[]{1,2},null);
		tree.root.split = 1;
		tree.root.left = new KdTree.Node(new double[]{-0.2,1},null);
		tree.root.left.split = 0;
		tree.root.left.left = new KdTree.Node(new double[]{-2,0.5},null);
		tree.root.left.left.split = -1;
		tree.root.left.right = new KdTree.Node(new double[]{2,0.8},null);
		tree.root.left.right.split = -1;
		tree.root.right = new KdTree.Node(new double[]{10,5},null);
		tree.root.right.split = 0;
		tree.root.right.left = new KdTree.Node(tree.root.right.point,null);
		tree.root.right.left.split = -1;
		tree.root.right.right = new KdTree.Node(new double[]{12,10},null);
		tree.root.right.right.split = -1;

		return tree;
	}
}
