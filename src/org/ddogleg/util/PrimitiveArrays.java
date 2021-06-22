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

package org.ddogleg.util;

import org.ddogleg.struct.DogArray_I32;
import org.ddogleg.struct.DogArray_I8;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Various functions for manipulating primitive arrays
 *
 * @author Peter Abeles
 */
public class PrimitiveArrays {

	/**
	 * Finds the itersection of two sets. Uses a algorithm that requires linear time and memory. Manually
	 * determines the min and max values contained in both sets.
	 *
	 * @param setA Set A of integers. Unsorted.
	 * @param sizeA Number of elements in set A
	 * @param setB Set B of integers. Unsorted.
	 * @param sizeB Number of elements in set B
	 * @param work Work space
	 * @param results Output set that is the intersection. Sorted from least to greatest
	 */
	public static void intersection( int[] setA, int sizeA,
									 int[] setB, int sizeB,
									 DogArray_I32 results,
									 @Nullable DogArray_I8 work ) {
		// Handling the pathological case where enables safely accessing the first element in setA
		if (sizeA == 0 || sizeB == 0) {
			results.reset();
			return;
		}

		// Set the min/max to an actual element. This enables if else to be used below.
		int min = setA[0];
		int max = min;

		// Exhaustively search to find the minimum and maximum values
		for (int i = 1; i < sizeA; i++) {
			int v = setA[i];
			if (v < min)
				min = v;
			else if (v > max)
				max = v;
		}

		for (int i = 0; i < sizeB; i++) {
			int v = setB[i];
			if (v < min)
				min = v;
			else if (v > max)
				max = v;
		}
		intersection(setA, sizeA, setB, sizeB, min, max, results, work);
	}

	/**
	 * Finds the intersection of two sets. Uses a algorithm that requires linear time and memory.
	 *
	 * @param setA Set A of integers. Unsorted.
	 * @param sizeA Number of elements in set A
	 * @param setB Set B of integers. Unsorted.
	 * @param sizeB Number of elements in set B
	 * @param valueMin Minimum value in either set
	 * @param valueMax Maximum value in either set
	 * @param work Work space
	 * @param results Output set that is the intersection. Sorted from least to greatest
	 */
	public static void intersection( int[] setA, int sizeA,
									 int[] setB, int sizeB,
									 int valueMin, int valueMax,
									 DogArray_I32 results,
									 @Nullable DogArray_I8 work ) {
		work = countOccurrences(setA, sizeA, setB, sizeB, valueMin, valueMax, results, work);

		for (int i = 0; i < work.size; i++) {
			if (work.data[i] != 2)
				continue;
			results.add(i + valueMin);
		}
	}

	/**
	 * Finds the intersection of two sets. Uses a algorithm that requires linear time and memory.
	 *
	 * @param setA Set A of integers. Unsorted.
	 * @param sizeA Number of elements in set A
	 * @param setB Set B of integers. Unsorted.
	 * @param sizeB Number of elements in set B
	 * @param valueMin Minimum value in either set
	 * @param valueMax Maximum value in either set
	 * @param work Work space
	 * @param results Output set that is the intersection. Sorted from least to greatest
	 */
	public static void union( int[] setA, int sizeA,
							  int[] setB, int sizeB,
							  int valueMin, int valueMax,
							  DogArray_I32 results,
							  @Nullable DogArray_I8 work ) {
		work = countOccurrences(setA, sizeA, setB, sizeB, valueMin, valueMax, results, work);

		for (int i = 0; i < work.size; i++) {
			if (work.data[i] == 0)
				continue;
			results.add(i + valueMin);
		}
	}

	private static DogArray_I8 countOccurrences( int[] setA, int sizeA,
												 int[] setB, int sizeB,
												 int valueMin, int valueMax,
												 DogArray_I32 results,
												 @Nullable DogArray_I8 work ) {
		results.reset();
		results.reserve(Math.min(sizeA, sizeB));

		if (work == null)
			work = new DogArray_I8(valueMax - valueMin + 1);
		work.reset();
		work.resize(valueMax - valueMin + 1, (byte)0);

		for (int i = 0; i < sizeA; i++) {
			work.data[setA[i] - valueMin]++;
		}
		for (int i = 0; i < sizeB; i++) {
			work.data[setB[i] - valueMin]++;
		}
		return work;
	}

	/**
	 * Sets each element within range to a number counting up
	 */
	public static void fillCounting( int[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		for (int i = 0; i < length; i++) {
			array[i + offset] = i;
		}
	}

	public static int[] fillCounting( int length ) {
		int[] array = new int[length];
		fillCounting(array, 0, length);
		return array;
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( byte[] array, int offset, int length, Random rand ) {
		sanityCheckRange(array.length, offset, length);

		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length - i);
			byte tmp = array[offset + src + i];
			array[offset + src + i] = array[offset + i];
			array[offset + i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( short[] array, int offset, int length, Random rand ) {
		sanityCheckRange(array.length, offset, length);

		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length - i);
			short tmp = array[offset + src + i];
			array[offset + src + i] = array[offset + i];
			array[offset + i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( int[] array, int offset, int length, Random rand ) {
		sanityCheckRange(array.length, offset, length);

		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length - i);
			int tmp = array[offset + src + i];
			array[offset + src + i] = array[offset + i];
			array[offset + i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( long[] array, int offset, int length, Random rand ) {
		sanityCheckRange(array.length, offset, length);

		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length - i);
			long tmp = array[offset + src + i];
			array[offset + src + i] = array[offset + i];
			array[offset + i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( float[] array, int offset, int length, Random rand ) {
		sanityCheckRange(array.length, offset, length);

		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length - i);
			float tmp = array[offset + src + i];
			array[offset + src + i] = array[offset + i];
			array[offset + i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( double[] array, int offset, int length, Random rand ) {
		sanityCheckRange(array.length, offset, length);

		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length - i);
			double tmp = array[offset + src + i];
			array[offset + src + i] = array[offset + i];
			array[offset + i] = tmp;
		}
	}

	/**
	 * Returns the value of the element with the minimum value
	 */
	public static int min( byte[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		byte min = array[offset];
		for (int i = 1; i < length; i++) {
			byte tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}

	/**
	 * Returns the value of the element with the minimum value
	 */
	public static int min( short[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		short min = array[offset];
		for (int i = 1; i < length; i++) {
			short tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}

	/**
	 * Returns the value of the element with the minimum value
	 */
	public static int min( int[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		int min = array[offset];
		for (int i = 1; i < length; i++) {
			int tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}

	/**
	 * Returns the value of the element with the minimum value
	 */
	public static long min( long[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		long min = array[offset];
		for (int i = 1; i < length; i++) {
			long tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}

	/**
	 * Returns the value of the element with the minimum value
	 */
	public static float min( float[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		float min = array[offset];
		for (int i = 1; i < length; i++) {
			float tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}

	/**
	 * Returns the value of the element with the minimum value
	 */
	public static double min( double[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double min = array[offset];
		for (int i = 1; i < length; i++) {
			double tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}

	/**
	 * Returns the index of the element with the minimum value
	 */
	public static int minIdx( byte[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		byte min = array[offset];
		int index = 0;
		for (int i = 1; i < length; i++) {
			byte tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
				index = i;
			}
		}
		return offset + index;
	}

	/**
	 * Returns the index of the element with the minimum value
	 */
	public static int minIdx( int[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		int min = array[offset];
		int index = 0;
		for (int i = 1; i < length; i++) {
			int tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
				index = i;
			}
		}
		return offset + index;
	}

	/**
	 * Returns the index of the element with the minimum value
	 */
	public static int minIdx( float[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		float min = array[offset];
		int index = 0;
		for (int i = 1; i < length; i++) {
			float tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
				index = i;
			}
		}
		return offset + index;
	}

	/**
	 * Returns the index of the element with the minimum value
	 */
	public static int minIdx( double[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double min = array[offset];
		int index = 0;
		for (int i = 1; i < length; i++) {
			double tmp = array[offset + i];
			if (tmp < min) {
				min = tmp;
				index = i;
			}
		}
		return offset + index;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static int max( byte[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		byte max = array[offset];
		for (int i = 1; i < length; i++) {
			byte tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static int max( short[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		short max = array[offset];
		for (int i = 1; i < length; i++) {
			short tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static int max( int[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		int max = array[offset];
		for (int i = 1; i < length; i++) {
			int tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static long max( long[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		long max = array[offset];
		for (int i = 1; i < length; i++) {
			long tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static float max( float[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		float max = array[offset];
		for (int i = 1; i < length; i++) {
			float tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static double max( double[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double max = array[offset];
		for (int i = 1; i < length; i++) {
			double tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static int maxIdx( byte[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		byte max = array[offset];
		int index = 0;
		for (int i = 1; i < length; i++) {
			byte tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
				index = i;
			}
		}
		return offset + index;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static int maxIdx( int[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		int max = array[offset];
		int index = 0;
		for (int i = 1; i < length; i++) {
			int tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
				index = i;
			}
		}
		return offset + index;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static int maxIdx( float[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		float max = array[offset];
		int index = 0;
		for (int i = 1; i < length; i++) {
			float tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
				index = i;
			}
		}
		return offset + index;
	}

	/**
	 * Returns the value of the element with the maximum value
	 */
	public static int maxIdx( double[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double max = array[offset];
		int index = 0;
		for (int i = 1; i < length; i++) {
			double tmp = array[offset + i];
			if (tmp > max) {
				max = tmp;
				index = i;
			}
		}
		return offset + index;
	}

	/**
	 * Finds the first index in 'array' for which val is not &le; val
	 *
	 * @param offset First index in the array
	 * @param length Number of elements in the array
	 * @param val The value for which the lower bound is being searched for.
	 * @return lower bound index
	 */
	public static int lowerBound( byte[] array, int offset, int length, int val ) {
		sanityCheckRange(array.length, offset, length);

		int count = length;
		int first = offset;
		while (count > 0) {
			int step = count/2;
			int idx = first + step;
			if (array[idx] < val) {
				first = idx + 1;
				count -= step + 1;
			} else {
				count = step;
			}
		}
		return first;
	}

	/**
	 * Finds the first index in 'array' for which val is not &le; val
	 *
	 * @param array unsigned byte array
	 * @param offset First index in the array
	 * @param length Number of elements in the array
	 * @param val The value for which the lower bound is being searched for.
	 * @return lower bound index
	 */
	public static int lowerBoundU( byte[] array, int offset, int length, int val ) {
		sanityCheckRange(array.length, offset, length);

		int count = length;
		int first = offset;
		while (count > 0) {
			int step = count/2;
			int idx = first + step;
			if ((array[idx] & 0xFF) < val) {
				first = idx + 1;
				count -= step + 1;
			} else {
				count = step;
			}
		}
		return first;
	}

	/**
	 * Finds the first index in 'array' for which val is not &le; val
	 *
	 * @param offset First index in the array
	 * @param length Number of elements in the array
	 * @param val The value for which the lower bound is being searched for.
	 * @return lower bound index
	 */
	public static int lowerBound( short[] array, int offset, int length, int val ) {
		sanityCheckRange(array.length, offset, length);

		int count = length;
		int first = offset;
		while (count > 0) {
			int step = count/2;
			int idx = first + step;
			if (array[idx] < val) {
				first = idx + 1;
				count -= step + 1;
			} else {
				count = step;
			}
		}
		return first;
	}

	/**
	 * Finds the first index in 'array' for which val is not &le; val
	 *
	 * @param offset First index in the array
	 * @param length Number of elements in the array
	 * @param val The value for which the lower bound is being searched for.
	 * @return lower bound index
	 */
	public static int lowerBound( int[] array, int offset, int length, int val ) {
		sanityCheckRange(array.length, offset, length);

		int count = length;
		int first = offset;
		while (count > 0) {
			int step = count/2;
			int idx = first + step;
			if (array[idx] < val) {
				first = idx + 1;
				count -= step + 1;
			} else {
				count = step;
			}
		}
		return first;
	}

	/**
	 * Finds the first index in 'array' for which val is not &le; val
	 *
	 * @param offset First index in the array
	 * @param length Number of elements in the array
	 * @param val The value for which the lower bound is being searched for.
	 * @return lower bound index
	 */
	public static int lowerBound( float[] array, int offset, int length, float val ) {
		sanityCheckRange(array.length, offset, length);

		int count = length;
		int first = offset;
		while (count > 0) {
			int step = count/2;
			int idx = first + step;
			if (array[idx] < val) {
				first = idx + 1;
				count -= step + 1;
			} else {
				count = step;
			}
		}
		return first;
	}

	/**
	 * Finds the first index in 'array' for which val is not &le; val
	 *
	 * @param offset First index in the array
	 * @param length Number of elements in the array
	 * @param val The value for which the lower bound is being searched for.
	 * @return lower bound index
	 */
	public static int lowerBound( double[] array, int offset, int length, double val ) {
		sanityCheckRange(array.length, offset, length);

		int count = length;
		int first = offset;
		while (count > 0) {
			int step = count/2;
			int idx = first + step;
			if (array[idx] < val) {
				first = idx + 1;
				count -= step + 1;
			} else {
				count = step;
			}
		}
		return first;
	}

	/**
	 * Computes the sum of the array and stores the result in a double
	 */
	public static double sumD( byte[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double sum = 0.0;
		for (int i = 0; i < length; i++) {
			sum += array[offset + i];
		}
		return sum;
	}

	/**
	 * Computes the sum of the array and stores the result in a double
	 */
	public static double sumD( short[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double sum = 0.0;
		for (int i = 0; i < length; i++) {
			sum += array[offset + i];
		}
		return sum;
	}

	/**
	 * Computes the sum of the array and stores the result in a double
	 */
	public static double sumD( int[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double sum = 0.0;
		for (int i = 0; i < length; i++) {
			sum += array[offset + i];
		}
		return sum;
	}

	/**
	 * Computes the sum of the array and stores the result in a double
	 */
	public static double sumD( long[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double sum = 0.0;
		for (int i = 0; i < length; i++) {
			sum += array[offset + i];
		}
		return sum;
	}

	/**
	 * Computes the sum of the array and stores the result in a double
	 */
	public static double sumD( float[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double sum = 0.0;
		for (int i = 0; i < length; i++) {
			sum += array[offset + i];
		}
		return sum;
	}

	/**
	 * Computes the sum of the array and stores the result in a double
	 */
	public static double sumD( double[] array, int offset, int length ) {
		sanityCheckRange(array.length, offset, length);

		double sum = 0.0;
		for (int i = 0; i < length; i++) {
			sum += array[offset + i];
		}
		return sum;
	}

	/**
	 * Recursively computes a result from an array. Previous results are feedback into the current value being
	 * considered.
	 */
	public static double feedbackIdxDOp( byte[] array, int offset, int length, FeedbackIdxD op ) {
		sanityCheckRange(array.length, offset, length);

		double result = 0.0;
		for (int i = 0; i < length; i++) {
			result = op.process(i, array[i + offset], result);
		}
		return result;
	}

	/**
	 * Recursively computes a result from an array. Previous results are feedback into the current value being
	 * considered.
	 */
	public static double feedbackIdxDOp( short[] array, int offset, int length, FeedbackIdxD op ) {
		sanityCheckRange(array.length, offset, length);

		double result = 0.0;
		for (int i = 0; i < length; i++) {
			result = op.process(i, array[i + offset], result);
		}
		return result;
	}

	/**
	 * Recursively computes a result from an array. Previous results are feedback into the current value being
	 * considered.
	 */
	public static double feedbackIdxDOp( int[] array, int offset, int length, FeedbackIdxD op ) {
		sanityCheckRange(array.length, offset, length);

		double result = 0.0;
		for (int i = 0; i < length; i++) {
			result = op.process(i, array[i + offset], result);
		}
		return result;
	}

	/**
	 * Recursively computes a result from an array. Previous results are feedback into the current value being
	 * considered.
	 */
	public static double feedbackIdxDOp( long[] array, int offset, int length, FeedbackIdxD op ) {
		sanityCheckRange(array.length, offset, length);

		double result = 0.0;
		for (int i = 0; i < length; i++) {
			result = op.process(i, array[i + offset], result);
		}
		return result;
	}

	/**
	 * Recursively computes a result from an array. Previous results are feedback into the current value being
	 * considered.
	 */
	public static double feedbackIdxDOp( float[] array, int offset, int length, FeedbackIdxD op ) {
		sanityCheckRange(array.length, offset, length);

		double result = 0.0;
		for (int i = 0; i < length; i++) {
			result = op.process(i, array[i + offset], result);
		}
		return result;
	}

	/**
	 * Recursively computes a result from an array. Previous results are feedback into the current value being
	 * considered.
	 */
	public static double feedbackIdxDOp( double[] array, int offset, int length, FeedbackIdxD op ) {
		sanityCheckRange(array.length, offset, length);

		double result = 0.0;
		for (int i = 0; i < length; i++) {
			result = op.process(i, array[i + offset], result);
		}
		return result;
	}

	private static void sanityCheckRange( int arrayLength, int offset, int length ) {
		if (length <= 0)
			throw new IllegalArgumentException("length must be positive. length=" + length);
		if (offset < 0 || offset >= arrayLength)
			throw new IllegalArgumentException("offset is invalid. offset=" + offset);
	}

	@FunctionalInterface
	public interface FeedbackIdxD {
		double process( int idx, double value, double previous );
	}
}
