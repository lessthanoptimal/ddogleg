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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestApproximateSort_F32 {

	Random rand = new Random(234);

	@Test
	public void computeRange_primitive() {
		ApproximateSort_F32 alg = new ApproximateSort_F32(12);

		float[] data = random(-5,10,4,200);
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;

		for( int i = 4; i < data.length; i++ ) {
			if( data[i] < min )
				min = data[i];
			if( data[i] > max )
				max = data[i];
		}

		alg.computeRange(data,4,200);
		assertEquals(min,alg.minValue,1e-8);
		assertEquals(max,alg.maxValue,1e-8);
	}

	@Test
	public void computeRange_object() {
		ApproximateSort_F32 alg = new ApproximateSort_F32(12);

		float[] data = random(-5,10,4,200);
		SortableParameter_F32[] objs = convert(data);
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;

		for( int i = 4; i < data.length; i++ ) {
			if( data[i] < min )
				min = data[i];
			if( data[i] > max )
				max = data[i];
		}

		alg.computeRange(objs,4,200);
		assertEquals(min,alg.minValue,1e-8);
		assertEquals(max,alg.maxValue,1e-8);
	}

	@Test
	public void sortIndex() {
		int numBins = 150;
		float tolerance = 15.0f/numBins;
		float[] data = random(-5,10,4,200);
	    int indexes[] = new int[ 200 ];

		ApproximateSort_F32 alg = new ApproximateSort_F32(-5,10,numBins);

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
		float tolerance = 15.0f/numBins;
		float[] data = random(-5,10,4,200);
		SortableParameter_F32[] objs = convert(data);

		ApproximateSort_F32 alg = new ApproximateSort_F32(-5,10,numBins);

		alg.sortObject(objs, 4, 200);

		// see if the sort was to within the expected tolerance
		for( int i = 5; i < objs.length; i++ ) {
			assertTrue( objs[i].sortValue > objs[i-1].sortValue -tolerance );
		}
	}

	public float[] random( float min , float max , int offset , int total ) {

		float[] ret = new float[ offset + total ];

		float range = max-min;
		for( int i = 0; i < total; i++ ) {
			ret[i+offset] = rand.nextFloat()*range + min;
		}

		return ret;
	}

	public SortableParameter_F32[] convert(float[] data ) {
		SortableParameter_F32[] ret = new SortableParameter_F32[data.length];

		for( int i = 0; i < data.length; i++ ) {
			ret[i] = new SortableParameter_F32(data[i]);
		}

		return ret;
	}
}
