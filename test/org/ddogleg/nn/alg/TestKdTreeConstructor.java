package org.ddogleg.nn.alg;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestKdTreeConstructor {

	@Test
	public void stuff() {
		fail("Redesign tests since it is a skeleton now");
	}

	@Test
	public void empty() {
		List<double[]> points = new ArrayList<double[]>();

		KdTreeConstructor<?> alg = new KdTreeConstructor(2);
		KdTree tree = alg.construct(points,null);

		assertTrue(tree.N == 2);
		assertTrue(tree.root == null);
	}

	@Test
	public void single() {
		List<double[]> points = createPoints(2,1,2);

		KdTreeConstructor<?> alg = new KdTreeConstructor(2);
		KdTree tree = alg.construct(points,null);

		assertTrue(tree.N == 2);
		assertTrue(tree.root != null);
		assertTrue(tree.root.isLeaf());
	}

	@Test
	public void two() {
		List<double[]> points = createPoints(2, 1,2, 1.1,4 );

		KdTreeConstructor<?> alg = new KdTreeConstructor(2);
		KdTree tree = alg.construct(points,null);

		assertTrue(tree.N == 2);
		assertTrue(tree.root != null);

		// second point should be here since the median = 1
		assertFalse(tree.root.isLeaf());
		assertEquals(1, tree.root.split);
		assertTrue(tree.root.point[0] == 1.1);
		// nothing below it, so it should be the parent
		assertTrue(tree.root.left.isLeaf());
		assertTrue(tree.root.left.point[0] == 1);
		// right is a leaf and the first point
		assertTrue(tree.root.right.isLeaf());
		assertTrue(tree.root.right.point[0] == 1.1);
	}

	@Test
	public void three() {
		List<double[]> points = createPoints(2, 1,2, 1.1,4 , 0.9,-2);

		KdTreeConstructor<?> alg = new KdTreeConstructor(2);
		KdTree tree = alg.construct(points,null);

		assertTrue(tree.N == 2);
		assertTrue(tree.root != null);

		// sorted order should be (0.9,-2) (1,2) (1.1,4)

		assertFalse(tree.root.isLeaf());
		assertTrue(tree.root.point[0] == 1);
		// left
		assertTrue(tree.root.left.isLeaf());
		assertTrue(tree.root.left.point[0] == 0.9);
		// right
		assertTrue(tree.root.right.isLeaf());
		assertTrue(tree.root.right.point[0] == 1.1);
	}

	/**
	 * Make two of the points identical and see if things blow up
	 */
	@Test
	public void identical_points() {
		List<double[]> points = createPoints(2, 1,2, 1.1,4 , 1,2);

		KdTreeConstructor<?> alg = new KdTreeConstructor(2);
		KdTree tree = alg.construct(points,null);

		assertTrue(tree.N == 2);
		assertTrue(tree.root != null);

		// sorted order should be (1,2) (1,2) (1.1,4)

		assertFalse(tree.root.isLeaf());
		assertTrue(tree.root.point[0] == 1);
		// left
		assertTrue(tree.root.left.isLeaf());
		assertTrue(tree.root.left.point[0] == 1);
		// right
		assertTrue(tree.root.right.isLeaf());
		assertTrue(tree.root.right.point[0] == 1.1);
	}

	/**
	 * Make sure a reference to point data is being saved
	 */
	@Test
	public void savedPointReference() {
		List<double[]> points = createPoints(2, 1,2, 1.1,4 , 0.9,-2);
		List<Integer> data = new ArrayList<Integer>();
		data.add(2);
		data.add(3);
		data.add(4);

		KdTreeConstructor<Integer> alg = new KdTreeConstructor<Integer>(2);
		KdTree tree = alg.construct(points,data);

		// sorted order should be (0.9,-2) (1,2) (1.1,4)
		assertFalse(tree.root.isLeaf());
		assertTrue(tree.root.data == data.get(0));
		assertTrue(tree.root.left.data == data.get(2));
		assertTrue(tree.root.right.data == data.get(1));
	}

	/**
	 * Make sure points which belong on the left are correctly assigned to the left
	 */
	@Test
	public void checkLeftRightLists() {
		List<double[]> points = createPoints(1, 1,15,3,13,5,6,7,8,9,10,11,12,4,14,2);

		KdTreeConstructor<Integer> alg = new KdTreeConstructor<Integer>(1);
		KdTree tree = alg.construct(points,null);

		assertFalse(tree.root.isLeaf());
		assertTrue(tree.root.point[0] == 8);

		List<double[]> left = flatten(tree.root.left);
		List<double[]> right = flatten(tree.root.right);

		assertEquals(7,left.size());
		assertEquals(7,right.size());

		for( int i = 0; i < 7; i++ ) {
			assertTrue(left.get(i)[0] < 8);
			assertTrue(right.get(i)[0] > 8);
		}
	}

	/**
	 * Makes sure it selects the largest variance to split on
	 */
	@Test
	public void selectLargestVariance() {
		List<double[]> points = createPoints(3, 1,2,10,  1.1,4,10 , 0.9,-2,10 );

		KdTreeConstructor<?> alg = new KdTreeConstructor(3);
		KdTree tree = alg.construct(points,null);

		assertEquals(1, tree.root.split);
	}

	public List<double[]> flatten( KdTree.Node node ) {
		List<double[]>  ret = new ArrayList<double[]>();

		List<KdTree.Node> open = new ArrayList<KdTree.Node>();
		open.add(node);

		while( open.size() > 0 ) {
			KdTree.Node n = open.remove(0);

			if( n.left != null ) open.add(n.left);
			if( n.right != null ) open.add(n.right);

			ret.add(n.point);
		}

		return ret;
	}

	public static List<double[]> createPoints( int dimen , double ...v ) {

		List<double[]> ret = new ArrayList<double[]>();

		for( int i = 0; i < v.length; i += dimen ) {
			double p[] = new double[dimen];
			for( int j = 0; j < dimen; j++ ) {
				p[j] = v[i+j];
			}
			ret.add(p);
		}

		return ret;
	}

}
