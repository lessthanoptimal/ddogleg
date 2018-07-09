/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.nn.alg.searches;

import org.ddogleg.nn.alg.KdTree;
import org.ddogleg.nn.alg.KdTreeSearch1;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Standard tests for implemntations of {@link KdTreeSearch1}.
 *
 * @author Peter Abeles
 */
public abstract class StandardKdTreeSearch1Tests {

	// DOF of a point
	public int N = 2;

	/**
	 * Creates a KdTreeSearch which will produce optimal results
	 */
	public abstract KdTreeSearch1<double[]> createAlg();

	/**
	 * Try several searches and see if they all produce good results
	 */
	@Test
	public void findClosest_basic() {
		KdTreeSearch1<double[]> alg = createAlg();

		KdTree tree = createTreeA();
		alg.setTree(tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		// the first decision will be incorrect and it will need to back track
		KdTree.Node found = alg.findNeighbor(new double[]{11, 8});
		assertSame(found, tree.root.right.right);

		// the root will be the best match
		found = alg.findNeighbor(new double[]{1.001, 1.99999});
		assertSame(found, tree.root);

		// a point on the left branch will be a perfect fit
		found = alg.findNeighbor(new double[]{2, 0.8});
		assertSame(found, tree.root.left.right);

		// a point way outside the tree's bounds
		found = alg.findNeighbor(new double[]{-10000, 0.5});
		assertSame(found, tree.root.left.left);
	}

	public static KdTree createTreeA() {

		KdTree tree = new KdTree(2);

		tree.root = new KdTree.Node(new double[]{1,2});
		tree.root.split = 1;
		tree.root.left = new KdTree.Node(new double[]{-0.21,1});
		tree.root.left.split = 0;
		tree.root.left.left = new KdTree.Node(new double[]{-2,0.5});
		tree.root.left.left.split = -1;
		tree.root.left.right = new KdTree.Node(new double[]{2,0.8});
		tree.root.left.right.split = -1;
		tree.root.right = new KdTree.Node(new double[]{10,5});
		tree.root.right.split = 0;
		tree.root.right.left = new KdTree.Node(new double[]{10,5}); // duplicate
		tree.root.right.left.split = -1;
		tree.root.right.right = new KdTree.Node(new double[]{12,10});
		tree.root.right.right.split = -1;

		return tree;
	}

	/**
	 * See if it can handle a null leaf
	 */
	@Test
	public void findClosest_nullLeaf() {
		KdTreeSearch1<double[]> alg = createAlg();

		KdTree tree = createTreeWithNull();
		alg.setTree(tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		// the first decision will be incorrect and it will need to back track
		KdTree.Node found = alg.findNeighbor(new double[]{2, 3});
		assertSame(found, tree.root);

	}

	public static KdTree createTreeWithNull() {

		KdTree tree = new KdTree(2);

		tree.root = new KdTree.Node(new double[]{1,2});
		tree.root.split = 1;
		tree.root.left = new KdTree.Node(new double[]{-0.2,1});
		tree.root.left.split = -1;
		tree.root.right = null;

		return tree;
	}

	/**
	 * The tree is empty and it should always fail
	 */
	@Test
	public void findClosest_empty() {
		KdTreeSearch1<double[]> alg = createAlg();
		alg.setTree( new KdTree(2) );

		KdTree.Node found = alg.findNeighbor(new double[]{11, 8});
		assertNull(found);
	}

	/**
	 * The tree is a leaf and should always return the same result
	 */
	@Test
	public void findClosest_leaf() {
		KdTree tree = new KdTree(2);
		tree.root = new KdTree.Node(new double[]{1,2});

		KdTreeSearch1<double[]> alg =createAlg();
		alg.setTree( tree );

		KdTree.Node found = alg.findNeighbor(new double[]{11, 8});
		assertSame(found, tree.root);
		found = alg.findNeighbor(new double[]{2, 5});
		assertSame(found, tree.root);
	}

	/**
	 * See if max distance is being respected
	 */
	@Test
	public void findClosest_maxDistance() {
		KdTree tree = new KdTree(2);
		tree.root = new KdTree.Node(new double[]{1,2});

		KdTreeSearch1<double[]> alg = createAlg();
		alg.setTree( tree );
		alg.setMaxDistance(2);

		KdTree.Node found = alg.findNeighbor(new double[]{11, 8});
		assertNull(found);
		found = alg.findNeighbor(new double[]{1, 1.5});
		assertSame(found, tree.root);
	}

	/**
	 * Make sure the distance it returns is correct
	 */
	@Test
	public void checkDistance() {
		KdTreeSearch1<double[]> alg = createAlg();

		KdTree tree = createTreeA();
		alg.setTree(tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		double[] pt = new double[]{11.5,8.2};
		KdTree.Node found = alg.findNeighbor(pt);

		double d0 = ((double[])found.point)[0]-pt[0];
		double d1 = ((double[])found.point)[1]-pt[1];

		assertEquals(d0*d0 + d1*d1,alg.getDistance(),1e-8);
	}
}
