package org.ddogleg.nn.alg;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestKdTree {

	@Test
	public void distanceSq() {
		KdTree alg = new KdTree(2);
		KdTree.Node n = new KdTree.Node(new double[]{1,2},null);
		double p[] = new double[]{2,5};

		double expected = 1*1 + 3*3;
		assertEquals(expected,KdTree.distanceSq(n,p,2),1e-8);
	}

	@Test
	public void isLeaf() {
		KdTree.Node n = new KdTree.Node();

		n.split = -1;
		assertTrue(n.isLeaf());
		n.split = 1;
		assertFalse(n.isLeaf());
	}

}
