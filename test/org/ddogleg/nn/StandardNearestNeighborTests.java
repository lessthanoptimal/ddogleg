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

package org.ddogleg.nn;

import org.ddogleg.nn.alg.ExhaustiveNeighbor;
import org.ddogleg.nn.alg.distance.KdTreeEuclideanSq_F64;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_F64;
import org.ddogleg.struct.GrowQueue_I32;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public abstract class StandardNearestNeighborTests {

	private Random rand = new Random(234);

	public int N = 2;
	private KdTreeEuclideanSq_F64 distance = new KdTreeEuclideanSq_F64(N);
	private NearestNeighbor<double[]> alg;

	private NnData<double[]> found = new NnData<>();
	private FastQueue<NnData<double[]>> foundN = new FastQueue<>(NnData::new);

	public void setAlg(NearestNeighbor<double[]> alg) {
		this.alg = alg;
	}

	@Test
	void findNearest_zero() {
		List<double[]> points = new ArrayList<>();

		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();
		assertFalse(search.findNearest(new double[]{1, 2}, 10, found));
	}

	@Test
	void findNearest_one() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});

		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();
		assertTrue(search.findNearest(new double[]{1, 2}, 10, found));

		assertSame(points.get(0), found.point);
	}

	@Test
	void findNearest_two() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});

		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();
		assertTrue(search.findNearest(new double[]{6, 7}, 10, found));

		assertSame(points.get(1), found.point);
	}

	@Test
	void findNearest_checkData() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});

		alg.setPoints(points,true);
		NearestNeighbor.Search<double[]> search = alg.createSearch();
		assertTrue(search.findNearest(new double[]{6, 7}, 10, found));

		assertEquals(1, found.index);
	}

	@Test
	void findNearest_compareToNaive() {
		for( int numPoints = 10; numPoints <= 100; numPoints += 10 ) {
//			System.out.println("numPoints = "+numPoints);

			List<double[]> points = new ArrayList<>();
			for( int i = 0; i < numPoints; i++ )
				points.add(randPoint(2));

			alg.setPoints(points,false);

			double[] where = randPoint(2);

			NearestNeighbor.Search<double[]> search = alg.createSearch();
			assertTrue(search.findNearest(where, 10, found));

			ExhaustiveNeighbor<double[]> exhaustive = new ExhaustiveNeighbor<>(distance);
			exhaustive.setPoints(points);
			double[] expected = points.get( exhaustive.findClosest(where,1000) );

			assertSame(expected, found.point);
		}
	}

	private double[] randPoint( int dimen ) {
		double []ret = new double[dimen];
		for( int i = 0; i < dimen; i++ )
			ret[i] = rand.nextGaussian();
		return ret;
	}

	/**
	 * Make sure distances < 0 are treated as having no limit and other properties
	 */
	@Test
	void checkSetMaxDistance() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});
		points.add(new double[]{-1, 3});
		points.add(new double[]{0.9,4.5});

		double[] target = new double[]{1.1,3.9};

		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();

		// should fail because the tolerance is too tight
		assertFalse(search.findNearest(target, 0.01, found));
		// Should search for a perfect match
		assertFalse(search.findNearest(target, 0, found));
		assertTrue(search.findNearest(points.get(3), 0, found));
		assertSame(found.point, points.get(3));
		// Should be treated as unconstrained
		assertTrue(search.findNearest(target, -1, found));
		assertSame(found.point, points.get(3));
		// should find a solution
		assertTrue(search.findNearest(target, 10, found));
		assertSame(found.point, points.get(3));
	}

	/**
	 * Makes sure the maximum distance is inclusive for findNearest()
	 */
	@Test
	void checkSetMaxDistance_inclusive() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});
		points.add(new double[]{-1, 3});
		points.add(new double[]{0.9,4.5});

		double[] target = new double[]{-2,3};

		
		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();

		assertTrue(search.findNearest(target, 1.00000001, found));
		assertSame(found.point, points.get(2));
		assertTrue(search.findNearest(target, 1, found));
		assertSame(found.point, points.get(2));
		assertFalse(search.findNearest(target, 0.99999999, found));
	}

	/**
	 * Makes sure the maximum distance is inclusive for findNearestN()
	 */
	@Test
	void checkSetMaxDistance_inclusiveN() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});
		points.add(new double[]{-1, 3});
		points.add(new double[]{-3, 3});
		points.add(new double[]{0.9,4.5});

		double[] target = new double[]{-2,3};

		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();

		foundN.reset();
		search.findNearest(target, 1.00000001, 2, foundN);
		assertEquals(2, foundN.size());
		foundN.reset();
		search.findNearest(target, 1, 2, foundN);
		assertEquals(2, foundN.size());
		foundN.reset();
		search.findNearest(target, 0.99999999, 2, foundN);
		assertEquals(0, foundN.size());
	}

	// make sure the return distance is correct
	@Test
	void findNearest_checkDistance() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});
		points.add(new double[]{-1, 3});
		points.add(new double[]{0.9,4.5});

		double[] target = new double[]{1.1,3.9};

		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();

		assertTrue(search.findNearest(target, 10, found));

		double d0 = found.point[0]-target[0];
		double d1 = found.point[1]-target[1];

		assertEquals(d0*d0 + d1*d1,found.distance,1e-8);
	}

	// make sure the return distance is correct
	@Test
	void findNearestN_checkDistance() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});
		points.add(new double[]{-1, 3});
		points.add(new double[]{0.9,4.5});

		double[] target = new double[]{1.1,3.9};

		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();

		foundN.reset();
		search.findNearest(target, 10, 1, foundN);
		assertEquals(1,foundN.size());

		double d0 = foundN.get(0).point[0]-target[0];
		double d1 = foundN.get(0).point[1]-target[1];

		assertEquals(d0*d0 + d1*d1,foundN.get(0).distance,1e-8);
	}

	@Test
	void findNearestN_checkData() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});

		alg.setPoints(points,true);
		NearestNeighbor.Search<double[]> search = alg.createSearch();

		foundN.reset();
		search.findNearest(new double[]{6, 7}, 10, 1, foundN);

		assertEquals(1, foundN.get(0).index);
	}

	/**
	 * Makes sure the algorithm first clears the results
	 */
	@Test
	void findNearestN_resultsCleared() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});

		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();

		foundN.grow();
		search.findNearest(new double[]{6, 7}, 10, 1, foundN);

		assertEquals(1,foundN.size);
	}

	@Test
	void findNearestN_compareToNaive() {

		GrowQueue_I32 outputIndex = new GrowQueue_I32();
		GrowQueue_F64 outputDistance = new GrowQueue_F64();

		for( int i = 0; i < 200; i++ ) {
			int numPoints = 8 + rand.nextInt(100);
			int numNeighbors = 1 + rand.nextInt(10);

//			System.out.println("numPoints = "+numPoints);

			List<double[]> points = new ArrayList<>();
			for( int j = 0; j < numPoints; j++ )
				points.add(randPoint(2));

			alg.setPoints(points,false);
			NearestNeighbor.Search<double[]> search = alg.createSearch();

			double[] where = randPoint(2);

			foundN.reset();
			search.findNearest(where, 10.0, numNeighbors, foundN);

			// see if it found more points than expected
			assertTrue(foundN.size <= numNeighbors);

			ExhaustiveNeighbor<double[]> exhaustive = new ExhaustiveNeighbor<>(distance);
			exhaustive.setPoints(points);

			outputIndex.reset();
			outputDistance.reset();
			exhaustive.findClosestN(where, 10.0, numNeighbors, outputIndex,outputDistance);

			assertEquals(outputIndex.size(),foundN.size());

			for( int j = 0; j < outputIndex.size(); j++ ) {
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
	void findNearestN_duplicates() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{3,4});
		points.add(new double[]{3,4});
		points.add(new double[]{3,4});
		points.add(new double[]{3,4});
		points.add(new double[]{3,4});
		points.add(new double[]{6,8});

		
		alg.setPoints(points,false);
		NearestNeighbor.Search<double[]> search = alg.createSearch();

		foundN.reset();
		search.findNearest(new double[]{6, 7}, 50, 5, foundN);

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

	/**
	 * Compare single threaded results to multi-threaded results. This won't catch all problems but will
	 * catch glaring errors
	 */
	@Test
	void threadSafe() {
		int count = 200;
		List<double[]> points = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			points.add( new double[]{rand.nextGaussian(),rand.nextGaussian()});
		}

		List<double[]> targets = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			targets.add( new double[]{rand.nextGaussian(),rand.nextGaussian()});
		}

		double[][] results = new double[count][];
		List<NearestNeighbor.Search<double[]>> searches = new ArrayList<>();
		for (int i = 0; i < results.length; i++) {
			searches.add(alg.createSearch());
		}

		alg.setPoints(targets,false);

		// runs in a parallel and blocks until all threads are done
		IntStream.range(0, searches.size())
				.parallel().forEach(i -> {
					NearestNeighbor.Search<double[]> s = searches.get(i);
					NnData<double[]> r = new NnData<>();
					assertTrue(s.findNearest(targets.get(i),Double.MAX_VALUE,r));
					results[i] = r.point;
				});

		// compare results to single thread version
		NearestNeighbor.Search<double[]> search = searches.get(0);
		NnData<double[]> r = new NnData<>();
		for (int i = 0; i < searches.size(); i++) {
			search.findNearest(targets.get(i),Double.MAX_VALUE,r);
			assertSame(results[i], r.point);
		}
	}
}
