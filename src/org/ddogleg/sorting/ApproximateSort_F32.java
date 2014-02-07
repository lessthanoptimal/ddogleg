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

import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;

/**
 * Counting sort for floating point numbers.  Sorting accuracy will be to within range/numBins.
 *
 * @author Peter Abeles
 */
public class ApproximateSort_F32 {

	FastQueue<GrowQueue_I32> histIndexes = new FastQueue<GrowQueue_I32>(GrowQueue_I32.class,true);
	FastQueue<SortableParameter_F32> histObjs[] = new FastQueue[0];

	double minValue,maxValue,divisor;
	int numBins;

	public ApproximateSort_F32(int numBins) {
		this.numBins = numBins;
	}

	public ApproximateSort_F32(int minValue, int maxValue, int numBins) {
		this.numBins = numBins;
		setRange(minValue,maxValue);
	}

	/**
	 * Specify the data range
	 *
	 * @param minValue Minimum allowed value.  (inclusive)
	 * @param maxValue Maximum allowed value.  (inclusive)
	 */
	public void setRange( float minValue , float maxValue ) {
		this.maxValue = maxValue;
		this.minValue = minValue;

		divisor = 1.00001*(maxValue-minValue)/numBins;

		histIndexes.resize(numBins);

		if( histObjs.length < numBins ) {
		histObjs = new FastQueue[ numBins ];
			for (int i = 0; i < numBins; i++) {
				histObjs[i] = new FastQueue<SortableParameter_F32>(SortableParameter_F32.class,true);
			}
		}
	}

	/**
	 * Examines the list and computes the range from it
	 */
	public void computeRange( float input[] , int start , int length ) {
		if( length == 0 ) {
			divisor = 0;
			return;
		}

		float min,max;

		min = max = input[start];

		for( int i = 1; i < length; i++ ) {
			float val = input[start+i];
			if( val < min )
				min = val;
			else if( val > max )
				max = val;
		}

		setRange(min,max);
	}

	/**
	 * Examines the list and computes the range from it
	 */
	public void computeRange( SortableParameter_F32 input[] , int start , int length ) {
		if( length == 0 ) {
			divisor = 0;
			return;
		}

		float min,max;

		min = max = input[start].sortValue;

		for( int i = 1; i < length; i++ ) {
			float val = input[start+i].sortValue;
			if( val < min )
				min = val;
			else if( val > max )
				max = val;
		}

		setRange(min,max);
	}

	/**
	 * Sort routine which does not modify the input array and instead maintains a list of indexes.
	 *
	 * @param input (Input) Data which is to be sorted. Not modified.
	 * @param start First element in input list
	 * @param length Length of the input list
	 * @param indexes Number of elements
	 */
	public void sortIndex( float input[] , int start , int length , int indexes[] ) {
		for( int i = 0; i < length; i++ )
			indexes[i] = i;

		for( int i = 0; i < histIndexes.size; i++ ) {
			histIndexes.get(i).reset();
		}

		for( int i = 0; i < length; i++ ) {
			int indexInput = i+start;
			int discretized = (int)((input[indexInput]-minValue)/divisor);
			histIndexes.data[discretized].add(indexInput);
		}

		// over wrist the input data with sorted elements
		int index = 0;
		for( int i = 0; i < histIndexes.size; i++ ) {
			GrowQueue_I32 matches = histIndexes.get(i);
			for( int j = 0; j < matches.size; j++ ) {
				indexes[index++] = matches.data[j];
			}
		}
	}

	/**
	 * Sorts the input list
	 *
	 * @param input (Input) Data which is to be sorted. Not modified.
	 * @param start First element in input list
	 * @param length Length of the input list
	 */
	public void sortObject( SortableParameter_F32 input[] , int start , int length ) {

		for( int i = 0; i < histIndexes.size; i++ ) {
			histObjs[i].reset();
		}

		for( int i = 0; i < length; i++ ) {
			int indexInput = i+start;
			SortableParameter_F32 p = input[indexInput];
			int discretized = (int)((p.sortValue-minValue)/divisor);
			histObjs[discretized].add(p);
		}

		// over wrist the input data with sorted elements
		int index = start;
		for( int i = 0; i < histIndexes.size; i++ ) {
			FastQueue<SortableParameter_F32>  matches = histObjs[i];
			for( int j = 0; j < matches.size; j++ ) {
				input[index++] = matches.data[j];
			}
		}
	}
}
