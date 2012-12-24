package org.ddogleg.nn.alg;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestExhaustiveNeighbor {

	@Test
	public void zero() {
		List<double[]> list = new ArrayList<double[]>();

		ExhaustiveNeighbor alg = new ExhaustiveNeighbor(2);
		alg.setPoints(list);

		assertTrue(alg.findClosest(new double[]{1,2},10) == -1);
	}

	@Test
	public void one() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2);

		ExhaustiveNeighbor alg = new ExhaustiveNeighbor(2);
		alg.setPoints(list);

		assertTrue(alg.findClosest(new double[]{1,2.1},10) == 0);
		assertFalse(alg.findClosest(new double[]{1, 200}, 10) == 0);
	}

	@Test
	public void two() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4);

		ExhaustiveNeighbor alg = new ExhaustiveNeighbor(2);
		alg.setPoints(list);

		assertTrue(alg.findClosest(new double[]{1, 2.1}, 10) == 0);
	}

	@Test
	public void three() {
		List<double[]> list = TestKdTreeConstructor.createPoints(2,  1,2,  3,4,  6,7);

		ExhaustiveNeighbor alg = new ExhaustiveNeighbor(2);
		alg.setPoints(list);

		assertTrue(alg.findClosest(new double[]{3.1, 3.9}, 10) == 1);
	}

}
