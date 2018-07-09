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
import org.ddogleg.nn.alg.distance.KdTreeEuclideanSq_F64;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestKdTreeSearchBestBinFirst {

	/**
	 * Provide an insufficient number of steps to produce an optimal solution and see if it produces the expected
	 * result
	 */
	@Test
	public void checkMaxNodes() {
		KdTree tree = StandardKdTreeSearch1Tests.createTreeA();

		BBF alg = new BBF(0,2);
		alg.setTree(tree);

		KdTree.Node found = alg.findClosest(new double[]{12,2});

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

		BBF alg = new BBF(200,3);
		alg.setTrees(forest);

		KdTree.Node found = alg.findClosest(new double[]{12,3});

		// make sure it searched some nodes besides the root ones
		assertTrue(alg.numNodesSearched>0);
		// the best node should be the root node in the second forest
		assertTrue(found==forest[1].root);
	}

	private static class BBF extends KdTreeSearchBestBinFirst<double[]> {

		// the best node so far
		private KdTree.Node bestNode;

		public BBF(int maxNodesSearched , int N ) {
			super(new KdTreeEuclideanSq_F64(N),maxNodesSearched);
		}


		public KdTree.Node findClosest(double[] target) {

			bestNode = null;

			_findClosest(target);

			return bestNode;
		}

		@Override
		protected void checkBestDistance(KdTree.Node node, double[] target) {
			double distanceSq = distance.distance((double[])node.point,target);
			if( distanceSq <= bestDistanceSq ) {
				bestDistanceSq = distanceSq;
				bestNode = node;
			}
		}

		@Override
		protected boolean canImprove(double distanceSq) {
			return distanceSq <= bestDistanceSq;
		}
	}

}
