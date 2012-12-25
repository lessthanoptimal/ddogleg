package org.ddogleg.nn.alg;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestKdTreeMemory {

	@Test
	public void requestNode() {
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
	public void requestNode_leaf() {
		// create a node with values that need to be changed
		KdTree.Node n = new KdTree.Node();
		n.point = new double[2];
		n.split = 123;
		n.data = 3;

		KdTreeMemory alg = new KdTreeMemory();
		alg.unusedNodes.add(n);

		KdTree.Node m = alg.requestNode(new double[]{1,2},4);

		assertTrue(m==n);
		assertTrue(m.point[0]==1);
		assertTrue(m.data.equals(4));
		assertTrue(m.split==-1);
	}

	@Test
	public void requestTree() {
		KdTreeMemory alg = new KdTreeMemory();

		// Empty unused list
		KdTree n = alg.requestTree();
		assertTrue(n.root==null);

		// put it into the unused list and see if it is returned
		alg.unusedTrees.add(n);
		KdTree m = alg.requestTree();

		assertTrue(n==m);
	}

	@Test
	public void recycle() {
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
	public void recycleGraph() {
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


}
