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
 * A O(N) sorting routine for integer valued elements with a known upper and lower bound.   This performance can
 * be obtained since it does not compare elements and instead create a histogram.
 *
 * @author Peter Abeles
 */
public class CountingSort {

	GrowQueue_I32 histogram = new GrowQueue_I32();

	FastQueue<GrowQueue_I32> histIndexes = new FastQueue<GrowQueue_I32>(GrowQueue_I32.class,true);

	int minValue,maxValue;

	public CountingSort() {}

	public CountingSort(int minValue, int maxValue) {
		setRange(minValue,maxValue);
	}

	/**
	 * Specify the data range
	 *
	 * @param minValue Minimum allowed value.  (inclusive)
	 * @param maxValue Maximum allowed value.  (inclusive)
	 */
	public void setRange( int minValue , int maxValue ) {
		this.maxValue = maxValue;
		this.minValue = minValue;

		histogram.resize(maxValue-minValue+1);
		histIndexes.resize(maxValue-minValue+1);
	}

	/**
	 * Sorts the data in the array.
	 * @param data Data which is to be sorted.  Sorted data is written back into this same array
	 * @param begin First element to be sorted (inclusive)
	 * @param end Last element to be sorted (exclusive)
	 */
	public void sort( int data[] , int begin , int end ) {
		histogram.fill(0);

		for( int i = begin; i < end; i++ ) {
			histogram.data[data[i]-minValue]++;
		}

		// over wrist the input data with sorted elements
		int index = begin;
		for( int i = 0; i < histogram.size; i++ ) {
			int N = histogram.get(i);
			int value = i+minValue;
			for( int j = 0; j < N; j++ ) {
				data[index++] = value;
			}
		}
	}

	/**
	 * Sort routine which does not modify the input array.  Input and output arrays can be the same instance.
	 *
	 * @param input (Input) Data which is to be sorted. Not modified.
	 * @param startIndex First element in input list
	 * @param output (Output) Sorted data. Modified.
	 * @param length Number of elements
	 */
	public void sort( int input[] , int startIndex ,int output[] , int startOutput , int length ) {
		histogram.fill(0);

		for( int i = 0; i < length; i++ ) {
			histogram.data[input[i+startIndex]-minValue]++;
		}

		// over wrist the input data with sorted elements
		int index = startOutput;
		for( int i = 0; i < histogram.size; i++ ) {
			int N = histogram.get(i);
			int value = i+minValue;
			for( int j = 0; j < N; j++ ) {
				output[index++] = value;
			}
		}
	}

	/**
	 * Sort routine which does not modify the input array and instead maintains a list of indexes.
	 *
	 * @param input (Input) Data which is to be sorted. Not modified.
	 * @param start First element in input list
	 * @param length Length of the input list
	 * @param indexes Number of elements
	 */
	public void sortIndex( int input[] , int start , int length , int indexes[] ) {
		for( int i = 0; i < length; i++ )
			indexes[i] = i;

		for( int i = 0; i < histogram.size; i++ ) {
			histIndexes.get(i).reset();
		}

		for( int i = 0; i < length; i++ ) {
			int indexInput = i+start;
			histIndexes.data[input[indexInput]-minValue].add(indexInput);
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
}
