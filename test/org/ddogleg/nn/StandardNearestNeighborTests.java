/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.nn;

import org.ddogleg.nn.alg.ExhaustiveNeighbor;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_F64;
import org.ddogleg.struct.GrowQueue_I32;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public abstract class StandardNearestNeighborTests {

	Random rand = new Random(234);

	NearestNeighbor<Double> alg;

	NnData found = new NnData();
	FastQueue<NnData<Double>> foundN = new FastQueue<NnData<Double>>((Class)NnData.class,true);

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
				points.add(randPoint(2));

			alg.init(2);
			alg.setPoints(points,null);

			double[] where = randPoint(2);

			assertTrue(alg.findNearest(where, 10, found));

			ExhaustiveNeighbor exhaustive = new ExhaustiveNeighbor(2);
			exhaustive.setPoints(points);
			double[] expected = points.get( exhaustive.findClosest(where,1000) );

			assertTrue(expected == found.point);
		}
	}

	private double[] randPoint( int dimen ) {
		double []ret = new double[dimen];
		for( int i = 0; i < dimen; i++ )
			ret[i] = rand.nextGaussian();
		return ret;
	}

	/**
	 * Make sure distances <= 0 are treated as having no limit
	 */
	@Test
	public void checkSetMaxDistance() {
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});
		points.add(new double[]{-1, 3});
		points.add(new double[]{0.9,4.5});

		double target[] = new double[]{1.1,3.9};

		alg.init(2);
		alg.setPoints(points, null);

		// should fail because the tolerance is too tight
		assertFalse(alg.findNearest(target, 0.01, found));
		// Should be treated as unconstrained
		assertTrue(alg.findNearest(target, 0, found));
		assertTrue(found.point==points.get(3));
		// should find a solution
		assertTrue(alg.findNearest(target, 10, found));
		assertTrue(found.point==points.get(3));
	}

	@Test
	public void findNearest_checkDistance() {
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});
		points.add(new double[]{-1, 3});
		points.add(new double[]{0.9,4.5});

		double target[] = new double[]{1.1,3.9};

		alg.init(2);
		alg.setPoints(points,null);

		assertTrue(alg.findNearest(target, 10, found));

		double d0 = found.point[0]-target[0];
		double d1 = found.point[1]-target[1];

		assertEquals(d0*d0 + d1*d1,found.distance,1e-8);
	}

	@Test
	public void findNearestN_checkDistance() {
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});
		points.add(new double[]{-1, 3});
		points.add(new double[]{0.9,4.5});

		double target[] = new double[]{1.1,3.9};

		alg.init(2);
		alg.setPoints(points,null);

		foundN.reset();
		alg.findNearest(target, 10, 1, foundN);
		assertEquals(1,foundN.size());

		double d0 = foundN.get(0).point[0]-target[0];
		double d1 = foundN.get(0).point[1]-target[1];

		assertEquals(d0*d0 + d1*d1,foundN.get(0).distance,1e-8);
	}

	@Test
	public void findNearestN_checkData() {
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});

		List<Double> data = new ArrayList<Double>();
		data.add(3.0);
		data.add(7.0);

		alg.init(2);
		alg.setPoints(points,data);

		foundN.reset();
		alg.findNearest(new double[]{6, 7}, 10, 1, foundN);

		assertTrue(data.get(1) == foundN.get(0).data);
	}

	@Test
	public void findNearestN_compareToNaive() {

		GrowQueue_I32 outputIndex = new GrowQueue_I32();
		GrowQueue_F64 outputDistance = new GrowQueue_F64();

		for( int i = 0; i < 200; i++ ) {
			int numPoints = 8 + rand.nextInt(100);
			int numNeighbors = 1 + rand.nextInt(10);

//			System.out.println("numPoints = "+numPoints);

			List<double[]> points = new ArrayList<double[]>();
			for( int j = 0; j < numPoints; j++ )
				points.add(randPoint(2));

			alg.init(2);
			alg.setPoints(points,null);

			double[] where = randPoint(2);

			foundN.reset();
			alg.findNearest(where, 10.0, numNeighbors, foundN);

			ExhaustiveNeighbor exhaustive = new ExhaustiveNeighbor(2);
			exhaustive.setPoints(points);

			outputIndex.reset();
			outputDistance.reset();
			exhaustive.findClosestN(where, 10.0, numNeighbors, outputIndex,outputDistance);

			assertEquals(foundN.size(),outputIndex.getSize());

			for( int j = 0; j < outputIndex.getSize(); j++ ) {
				double[] expected = points.get(outputIndex.get(j) );

				boolean failed = true;
				for( int k = 0; k < foundN.size(); k++ ) {
					if( foundN.get(k).point == expected ) {
						failed = false;
						break;
					}
				}

				assertFalse(failed);
			}
		}
	}

	/**
	 * Input data has duplicate values.  Make sure they are handled correctly
	 */
	@Test
	public void findNearestN_duplicates() {
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[]{3,4});
		points.add(new double[]{3,4});
		points.add(new double[]{3,4});
		points.add(new double[]{3,4});
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});

		List<Double> data = new ArrayList<Double>();
		data.add(3.0);
		data.add(3.1);
		data.add(3.2);
		data.add(3.3);
		data.add(3.4);
		data.add(7.0);

		alg.init(2);
		alg.setPoints(points,data);

		foundN.reset();
		alg.findNearest(new double[]{6, 7}, 50, 5, foundN);

		assertEquals(5,foundN.size());

		// each item should be unique
		for( int i = 0; i < foundN.size(); i++ ) {
			double[] a = foundN.get(i).point;
			for( int j = i+1; j < foundN.size(); j++ ) {
				if( a == foundN.get(j).point ) {
					fail("found duplicate");
				}
			}
		}
	}
}
