/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.clustering.kmeans;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestAssignKMeans_F64 {

	@Test
	public void serialize() {

		List<double[]> clusters = new ArrayList<double[]>();

		clusters.add( new double[]{10,0,0});
		clusters.add( new double[]{0,0,10});

		AssignKMeans_F64 alg = new AssignKMeans_F64(clusters);

		byte[] encoded = save(alg);

//		AssignKMeans_F64 found = load( encoded );
	}

	public static byte[] save( Object o ) {
		try {
			FileOutputStream fileOut = new FileOutputStream("junk.txt");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(o);
			out.close();
			return byteStream.toByteArray();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T load( byte[] data ) {
		try {
			ByteArrayInputStream fileIn = new ByteArrayInputStream(data);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			T obj = (T)in.readObject();
			in.close();
			return obj;
		} catch(IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void assign() {

		List<double[]> clusters = new ArrayList<double[]>();

		clusters.add( new double[]{10,0,0});
		clusters.add( new double[]{0,0,10});

		AssignKMeans_F64 alg = new AssignKMeans_F64(clusters);

		assertEquals(1,alg.assign(new double[]{0,0,9}));
		assertEquals(0, alg.assign(new double[]{12, 0, 0}));
	}

	@Test
	public void assign_soft() {

		List<double[]> clusters = new ArrayList<double[]>();

		clusters.add( new double[]{10,0,0});
		clusters.add( new double[]{5,0,0});

		AssignKMeans_F64 alg = new AssignKMeans_F64(clusters);

		double histogram[] = new double[2];

		alg.assign(new double[]{10,0,0},histogram);
		assertEquals(1.0, histogram[0], 1e-8);
		assertEquals(0.0, histogram[1],1e-8);

		// see if much more weight is given to the second one
		alg.assign(new double[]{6, 0, 0}, histogram);
		assertTrue(histogram[0]*10 < histogram[1]);

		// this is actually a difficult case for using this type of distance metric
		// one cluster is much farther away and as a result the weight is equality split between the two closer points
		// which might not be desirable
		clusters.add( new double[]{5000,0,0});
		histogram = new double[3];
		alg.assign(new double[]{6,0,0},histogram);
		assertTrue(histogram[0]/30.0 > histogram[2]);
		assertEquals(histogram[0], histogram[1], 0.01);

	}

	@Test
	public void copy() {
		List<double[]> clusters = new ArrayList<double[]>();

		clusters.add( new double[]{10,0,0});
		clusters.add( new double[]{0,0,10});

		AssignKMeans_F64 original = new AssignKMeans_F64(clusters);
		AssignKMeans_F64 copy = (AssignKMeans_F64)original.copy();

		assertEquals(original.clusters.size(),copy.clusters.size());

		for (int i = 0; i < original.clusters.size(); i++) {
			double[] o = original.clusters.get(i);
			double[] c = copy.clusters.get(i);

			assertTrue(o!=c);

			for (int j = 0; j < o.length; j++) {
				assertEquals(o[j],c[j],1e-8);
			}
		}

	}
}
