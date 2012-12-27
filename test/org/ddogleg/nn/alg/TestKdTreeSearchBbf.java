package org.ddogleg.nn.alg;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

		KdTree.Node found = alg.findClosest(new double[]{12,2});

		// In one iteration it will search down to a leaf.  The point was selected such that the wrong path would
		// be followed
		assertTrue(found==tree.root.left.right);
	}

	@Test
	public void multiTreeSearch() {
		fail("implement");
	}

}
