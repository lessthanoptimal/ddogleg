/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.sorting;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestApproximateSort_F64 {

	Random rand = new Random(234);

	@Test
	public void sortIndex() {
		int numBins = 150;
		double tolerance = 15.0/numBins;
		double[] data = random(-5,10,4,200);
	    int indexes[] = new int[ 200 ];

		ApproximateSort_F64 alg = new ApproximateSort_F64(-5,10,numBins);

		alg.sortIndex(data,4,200,indexes);

		// see if the sort was to within the expected tolerance
		boolean used[] = new boolean[indexes.length];
		used[indexes[0]-4] = true;
		for( int i = 1; i < indexes.length; i++ ) {
			used[indexes[i]-4] = true;
			assertTrue(data[indexes[i]] > data[indexes[i - 1]] - tolerance);
		}

		// make sure everything was referenced
		for( int i = 0; i < indexes.length; i++ ) {
			assertTrue(used[i] );
		}
	}

	@Test
	public void sortObject() {
		int numBins = 150;
		double tolerance = 15.0/numBins;
		double[] data = random(-5,10,4,200);
		SortableParameter_F64[] objs = convert(data);

		ApproximateSort_F64 alg = new ApproximateSort_F64(-5,10,numBins);

		alg.sortObject(objs, 4, 200);

		// see if the sort was to within the expected tolerance
		for( int i = 5; i < objs.length; i++ ) {
			assertTrue( objs[i].sortValue > objs[i-1].sortValue -tolerance );
		}
	}

	public double[] random( double min , double max , int offset , int total ) {

		double[] ret = new double[ offset + total ];

		double range = max-min;
		for( int i = 0; i < total; i++ ) {
			ret[i+offset] = rand.nextDouble()*range + min;
		}

		return ret;
	}

	public SortableParameter_F64[] convert(double[] data ) {
		SortableParameter_F64[] ret = new SortableParameter_F64[data.length];

		for( int i = 0; i < data.length; i++ ) {
			ret[i] = new SortableParameter_F64(data[i]);
		}

		return ret;
	}
}
