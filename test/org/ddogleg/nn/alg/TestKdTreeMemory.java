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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestKdTreeMemory {

	@Test
	void requestNode() {
		KdTreeMemory alg = new KdTreeMemory();

		// Empty unused list
		KdTree.Node n = alg.requestNode();
		assertTrue(n.point == null);
		assertTrue(n.left == null);
		assertTrue(n.right == null);

		// put the node into the unused list
		alg.unusedNodes.add(n);

		KdTree.Node m = alg.requestNode();
		assertTrue(n==m);
	}

	@Test
	void requestNode_leaf() {
		// create a node with values that need to be changed
		KdTree.Node n = new KdTree.Node();
		n.point = new double[2];
		n.split = 123;
		n.index = 3;

		KdTreeMemory alg = new KdTreeMemory();
		alg.unusedNodes.add(n);

		KdTree.Node m = alg.requestNode(new double[]{1,2},4);

		assertTrue(m==n);
		assertTrue(((double[])m.point)[0]==1);
		assertEquals(m.index,4);
		assertTrue(m.split==-1);
	}

	@Test
	void requestTree() {
		KdTreeMemory alg = new KdTreeMemory();

		// Empty unused list
		KdTree n = alg.requestTree(2);
		assertTrue(n.root==null);

		// put it into the unused list and see if it is returned
		alg.unusedTrees.add(n);
		KdTree m = alg.requestTree(2);

		assertTrue(n==m);
	}

	@Test
	void recycle() {
		KdTreeMemory alg = new KdTreeMemory();

		KdTree.Node n = new KdTree.Node();
		n.point = new double[2];
		n.left = n; n.right = n;

		alg.recycle(n);
		assertTrue(n.point == null);
		assertTrue(n.left == null);
		assertTrue(n.right == null);
		assertEquals(1,alg.unusedNodes.size());
	}

	@Test
	@SuppressWarnings("NullAway")
	void recycleGraph() {
		KdTreeMemory alg = new KdTreeMemory();

		KdTree tree = new KdTree();
		tree.root = new KdTree.Node();
		tree.root.left = new KdTree.Node();
		tree.root.right = new KdTree.Node();
		tree.root.left.left = new KdTree.Node();
		tree.root.left.right = new KdTree.Node();


		alg.recycleGraph(tree);

		assertEquals(0,alg.open.size());
		assertEquals(1,alg.unusedTrees.size());
		assertEquals(5,alg.unusedNodes.size());
	}

	@Test
	void recycleGraph_nullRoot() {
		KdTreeMemory alg = new KdTreeMemory();

		KdTree tree = new KdTree();
		tree.root = null;

		alg.recycleGraph(tree);

		assertEquals(0,alg.open.size());
		assertEquals(1,alg.unusedTrees.size());
		assertEquals(0,alg.unusedNodes.size());
	}


}
