package org.ddogleg.nn.alg;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Standard tests for {@link KdTreeSearchBbf}.
 *
 * @author Peter Abeles
 */
public abstract class StandardKdTreeSearchTests {

	/**
	 * Creates a KdTreeSearch which will produce optimal results
	 */
	public abstract KdTreeSearch createAlg();

	/**
	 * Try several searches and see if they all produce good results
	 */
	@Test
	public void findClosest_basic() {
		KdTreeSearch alg = createAlg();

		KdTree tree = createTreeA();
		alg.setTree(tree);
		alg.setMaxDistance(Double.MAX_VALUE);

		// the first decision will be incorrect and it will need to back track
		KdTree.Node found = alg.findClosest(new double[]{11,8});
		assertTrue(found == tree.root.right.right);

		// the root will be the best match
		found = alg.findClosest(new double[]{1.001,1.99999});
		assertTrue(found == tree.root);

		// a point on the left branch will be a perfect fit
		found = alg.findClosest(new double[]{2,0.8});
		assertTrue(found == tree.root.left.right);

		// a point way outside the tree's bounds
		found = alg.findClosest(new double[]{-10000,0.5});
		assertTrue(found == tree.root.left.left);
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

	/**
	 * The tree is empty and it should always fail
	 */
	@Test
	public void findClosest_empty() {
		KdTreeSearch alg = createAlg();
		alg.setTree( new KdTree(2) );

		KdTree.Node found = alg.findClosest(new double[]{11,8});
		assertTrue(found == null);
	}

	/**
	 * The tree is a leaf and should always return the same result
	 */
	@Test
	public void findClosest_leaf() {
		KdTree tree = new KdTree(2);
		tree.root = new KdTree.Node(new double[]{1,2},null);

		KdTreeSearch alg =createAlg();
		alg.setTree( tree );

		KdTree.Node found = alg.findClosest(new double[]{11,8});
		assertTrue(found == tree.root);
		found = alg.findClosest(new double[]{2,5});
		assertTrue(found == tree.root);
	}

	/**
	 * See if max distance is being respected
	 */
	@Test
	public void findClosest_maxDistance() {
		KdTree tree = new KdTree(2);
		tree.root = new KdTree.Node(new double[]{1,2},null);

		KdTreeSearch alg = createAlg();
		alg.setTree( tree );
		alg.setMaxDistance(2);

		KdTree.Node found = alg.findClosest(new double[]{11,8});
		assertTrue(found == null);
		found = alg.findClosest(new double[]{1,1.5});
		assertTrue(found == tree.root);
	}
}
