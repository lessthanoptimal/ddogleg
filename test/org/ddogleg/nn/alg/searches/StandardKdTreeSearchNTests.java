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

package org.ddogleg.nn.alg.searches;

import org.ddogleg.nn.alg.KdTree;
import org.ddogleg.nn.alg.KdTreeResult;
import org.ddogleg.nn.alg.KdTreeSearchN;
import org.ddogleg.sorting.QuickSelect;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.DogArray_F64;
import org.ddogleg.struct.DogArray_I32;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Standard tests for implementations of {@link org.ddogleg.nn.alg.KdTreeSearchN}.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway"})
public abstract class StandardKdTreeSearchNTests {

	public int N = 2;
	DogArray<KdTreeResult> found = new DogArray<>(KdTreeResult::new);

	Random rand = new Random(234);

	/**
	 * Creates a KdTreeSearch which will produce optimal results
	 */
	public abstract KdTreeSearchN<double[]> createAlg();

	public abstract void setTree( KdTreeSearchN<double[]> alg , KdTree tree );
	
	/**
	 * Try several searches and see if they all produce good results.  Just fine the nearest-neighbor
	 */
	@Test
	public void findClosest_basic_1() {
		KdTreeSearchN<double[]> alg = createAlg();

		KdTree tree = StandardKdTreeSearch1Tests.createTreeA();
		setTree(alg,tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		// the first decision will be incorrect and it will need to back track
		found.reset();
		alg.findNeighbor(new double[]{11, 8}, 1, found);
		assertEquals(1,found.size);
		assertSame(found.data[0].node, tree.root.right.right);

		// the root will be the best
		found.reset();
		alg.findNeighbor(new double[]{1.001, 1.99999}, 1, found);
		assertSame(found.data[0].node, tree.root);

		// a point on the left branch will be a perfect fit
		found.reset();
		alg.findNeighbor(new double[]{2, 0.8}, 1, found);
		assertSame(found.data[0].node, tree.root.left.right);

		// a point way outside the tree's bounds
		found.reset();
		alg.findNeighbor(new double[]{-10000, 0.5}, 1, found);
		assertSame(found.data[0].node, tree.root.left.left);
	}

	/**
	 * See if it can handle a null leaf
	 */
	@Test
	public void findClosest_nullLeaf() {
		KdTreeSearchN<double[]> alg = createAlg();

		KdTree tree = StandardKdTreeSearch1Tests.createTreeWithNull();
		setTree(alg,tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		// the first decision will be incorrect and it will need to back track
		found.reset();
		alg.findNeighbor(new double[]{2, 3}, 1, found);
		assertSame(found.get(0).node, tree.root);

	}

	/**
	 * Randomly generate several searches and check the result
	 */
	@Test
	public void randomTests() {
		KdTreeSearchN<double[]> alg = createAlg();

		KdTree tree = StandardKdTreeSearch1Tests.createTreeA();
		setTree(alg,tree);

		List<double[]> data = new ArrayList<>();
		flattenTree(tree.root,data);

		for( int i = 0; i < 100; i++ ) {
			int searchN = rand.nextInt(data.size()+5)+1;

			double[] target = data.get( rand.nextInt(data.size()));

			double maxDistance = rand.nextDouble()*10;

			List<double[]> expected = findNeighbors(data,target,maxDistance,searchN);

			found.reset();
			alg.setMaxDistance(maxDistance);
			alg.findNeighbor(target, searchN, found);
			assertEquals(expected.size(),found.size);

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
		KdTreeSearchN<double[]> alg = createAlg();
		setTree(alg, new KdTree(2) );

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
		tree.root = new KdTree.Node(new double[]{1,2});

		KdTreeSearchN<double[]> alg =createAlg();
		setTree(alg, tree );

		found.reset();
		alg.findNeighbor(new double[]{11, 8}, 2, found);
		assertEquals(1,found.size);
		assertSame(found.data[0].node, tree.root);
		found.reset();
		alg.findNeighbor(new double[]{2, 5}, 2, found);
		assertEquals(1,found.size);
		assertSame(found.data[0].node, tree.root);
	}

	/**
	 * See if max distance is being respected
	 */
	@Test
	public void findClosest_maxDistance() {
		KdTree tree = new KdTree(2);
		tree.root = new KdTree.Node(new double[]{1,2});

		KdTreeSearchN<double[]> alg = createAlg();
		setTree(alg, tree );
		alg.setMaxDistance(2);

		found.reset();
		alg.findNeighbor(new double[]{11, 8}, 1, found);
		assertEquals(0, found.size);
		found.reset();
		alg.findNeighbor(new double[]{1, 1.5}, 1, found);
		assertEquals(1, found.size);
		assertSame(found.data[0].node, tree.root);
	}

	/**
	 * Make sure the distance it returns is correct
	 */
	@Test
	public void checkDistance() {
		KdTreeSearchN<double[]> alg = createAlg();

		KdTree tree = StandardKdTreeSearch1Tests.createTreeA();
		setTree(alg,tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		double[] pt = new double[]{11.5,8.2};
		found.reset();
		alg.findNeighbor(pt, 1, found);

		assertEquals(1,found.size);
		double d0 = ((double[])found.get(0).node.point)[0]-pt[0];
		double d1 = ((double[])found.get(0).node.point)[1]-pt[1];

		assertEquals(d0*d0 + d1*d1,found.get(0).distance,1e-8);
	}

	/**
	 * See of it can handle duplicate values correctly
	 */
	@Test
	public void checkDuplicates() {
		KdTreeSearchN<double[]> alg = createAlg();

		KdTree tree = createTreeDuplicates();
		setTree(alg,tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		double[] pt = new double[]{1,2};
		found.reset();
		alg.findNeighbor(pt, 3, found);
		assertEquals(3,found.size);

		// make sure each instance is unique
		for( int i = 0; i < 3; i++ ) {
			double[] a = (double[])found.get(i).node.point;
			for( int j = i+1; j < 3; j++ ) {
				assertNotSame(found.get(j).node.point, a);
			}
		}
	}

	private void checkContains( double[] d ) {
		for( int i = 0; i < found.size; i++ ) {
			boolean identical = true;
			double f[] = (double[])found.data[i].node.point;
			for( int j = 0; j < d.length; j++ ) {
				if( d[j] != f[j] ) {
					identical = false;
					break;
				}
			}
			if( identical )
				return;
		}

		fail("can't find");
	}

	private static void flattenTree(KdTree.Node n , List<double[]> data ) {
		data.add((double[])n.point);
		if( !n.isLeaf() ) {
			flattenTree(n.left,data);
			flattenTree(n.right,data);
		}
	}

	private static List<double[]> findNeighbors( List<double[]> data , double[]target , double maxDistance , int maxN ) {
		List<double[]> ret = new ArrayList<>();

		List<double[]> found = new ArrayList<>();
		DogArray_F64 distances = new DogArray_F64();
		DogArray_I32 indexes = new DogArray_I32();


		for( int i = 0; i < data.size(); i++ ) {
			double[] d = data.get(i);

			double dx = d[0] - target[0];
			double dy = d[1] - target[1];

			double dist = dx*dx + dy*dy;
			if( dist <= maxDistance ) {
				distances.add(dist);
				found.add(d);
			}
		}

		indexes.resize(distances.size);

		maxN = Math.min(maxN,distances.size);

		QuickSelect.selectIndex(distances.data,maxN,distances.size,indexes.data);

		for( int i = 0; i < maxN; i++ ) {
			ret.add( found.get( indexes.data[i]));
		}

		return ret;
	}

	public static KdTree createTreeDuplicates() {

		KdTree tree = new KdTree(2);

		tree.root = new KdTree.Node(new double[]{1,2});
		tree.root.split = 1;
		tree.root.left = new KdTree.Node(new double[]{1,2});
		tree.root.left.split = 0;
		tree.root.left.left = new KdTree.Node(new double[]{1,2});
		tree.root.left.left.split = -1;
		tree.root.left.right = null;
		tree.root.right = new KdTree.Node(new double[]{1,2});
		tree.root.right.split = -1;

		return tree;
	}

}
