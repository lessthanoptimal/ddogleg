package org.ddogleg.nn;

import org.ddogleg.nn.alg.ExhaustiveNeighbor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public abstract class StandardNearestNeighborTests {

	Random rand = new Random(234);

	NearestNeighbor<Double> alg;

	NnData found = new NnData();

	public void setAlg(NearestNeighbor<Double> alg) {
		this.alg = alg;
	}

	@Test
	public void findNearest_zero() {
		List<double[]> points = new ArrayList<double[]>();

		alg.init(2);
		alg.setPoints(points,null);
		assertFalse(alg.findNearest(new double[]{1, 2}, 10, found));
	}

	@Test
	public void findNearest_one() {
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[]{3,4});

		alg.init(2);
		alg.setPoints(points,null);
		assertTrue(alg.findNearest(new double[]{1, 2}, 10, found));

		assertTrue(points.get(0) == found.point);
	}

	@Test
	public void findNearest_two() {
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});

		alg.init(2);
		alg.setPoints(points,null);
		assertTrue(alg.findNearest(new double[]{6, 7}, 10, found));

		assertTrue(points.get(1) == found.point);
	}

	@Test
	public void findNearest_checkData() {
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});

		List<Double> data = new ArrayList<Double>();
		data.add(3.0);
		data.add(7.0);

		alg.init(2);
		alg.setPoints(points,data);
		assertTrue(alg.findNearest(new double[]{6, 7}, 10, found));

		assertTrue(data.get(1)== found.data);
	}

	@Test
	public void findNearest_compareToNaive() {
		for( int numPoints = 10; numPoints <= 100; numPoints += 10 ) {
//			System.out.println("numPoints = "+numPoints);

			List<double[]> points = new ArrayList<double[]>();
			for( int i = 0; i < numPoints; i++ )
				points.add(new double[]{rand.nextGaussian(),rand.nextGaussian()});

			alg.init(2);
			alg.setPoints(points,null);

			double[] where = new double[]{rand.nextGaussian(),rand.nextGaussian()};

			assertTrue(alg.findNearest(where, 10, found));

			ExhaustiveNeighbor exhaustive = new ExhaustiveNeighbor(2);
			exhaustive.setPoints(points);
			double[] expected = points.get( exhaustive.findClosest(where,1000) );

			assertTrue(expected == found.point);
		}
	}
}
