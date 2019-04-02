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
import org.ddogleg.nn.alg.KdTreeSearchN;
import org.ddogleg.nn.alg.distance.KdTreeEuclideanSq_F64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestKdTreeSearchNBbf extends StandardKdTreeSearchNTests {
	@Override
	public KdTreeSearchN<double[]> createAlg() {
		// specify so many max nodes that it will be optimal
		return new KdTreeSearchNBbf<>(new KdTreeEuclideanSq_F64(2),10000);
	}

	@Override
	public void setTree(KdTreeSearchN<double[]> alg, KdTree tree) {
		((KdTreeSearchNBbf)alg).setTree(tree);
	}

	/**
	 * Provide an insufficient number of steps to produce an optimal solution and see if it produces the expected
	 * result
	 */
	@Test
	public void checkMaxNodes() {
		KdTree tree = StandardKdTreeSearch1Tests.createTreeA();

		KdTreeSearch1Bbf<double[]> alg = new KdTreeSearch1Bbf<>(new KdTreeEuclideanSq_F64(2),0);
		alg.setTree(tree);

		KdTree.Node found = alg.findNeighbor(new double[]{12, 2});

		// The first search from the root node is not counted.  In that search it will traverse down to a leaf
		assertTrue(found==tree.root.left.right);
	}

	/**
	 * Provide multiple trees for input and see if it finds the best one
	 */
	@Test
	public void multiTreeSearch() {
		KdTree forest[] = new KdTree[2];
		forest[0] = StandardKdTreeSearch1Tests.createTreeA();
		forest[1] = new KdTree(2);
		forest[1].root = new KdTree.Node(new double[]{12,2});

		KdTreeSearch1Bbf<double[]> alg = new KdTreeSearch1Bbf<>(new KdTreeEuclideanSq_F64(2),200);
		alg.setTrees(forest);

		KdTree.Node found = alg.findNeighbor(new double[]{12, 3});

		// make sure it searched some nodes besides the root ones
		assertTrue(alg.numNodesSearched>0);
		// the best node should be the root node in the second forest
		assertSame(found, forest[1].root);
	}

}
