package org.ddogleg.nn.alg;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestKdTreeSearchBbf extends StandardKdTreeSearchTests {
	@Override
	public KdTreeSearch createAlg() {
		// specify so many max nodes that it will be optimal
		return new KdTreeSearchBbf(10000);
	}

	/**
	 * Provide an insufficient number of steps to produce an optimal solution and see if it produces the expected
	 * result
	 */
	@Test
	public void checkMaxNodes() {
		KdTree tree = createTreeA();

		KdTreeSearchBbf alg = new KdTreeSearchBbf(1);
		alg.setTree(tree);

		KdTree.Node found = alg.findClosest(tree.root.left.left.point);

		// The root node is the only node that is processed in the main loop, but the left child is considered
		// and distance computed
		assertTrue(found==tree.root.left);
	}
}
