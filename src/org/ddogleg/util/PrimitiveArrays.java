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

import java.util.Random;

/**
 * Various functions for manipulating primitive arrays
 *
 * @author Peter Abeles
 */
public class PrimitiveArrays {

	/**
	 * Sets each element within range to a number counting up
	 */
	public static void fillCounting( int[] array, int offset, int length ) {
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
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
	public static int min( short[] array, int offset, int length ) {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
	public static int min( int[] array, int offset, int length ) {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
		long min = Long.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
		float min = Float.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
		double min = Double.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
		int min = Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < length; i++) {
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
	public static int minIdx( int[] array, int offset, int length ) {
		int min = Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < length; i++) {
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
		float min = Float.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < length; i++) {
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
		double min = Double.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < length; i++) {
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
		int max = -Integer.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
	public static int max( short[] array, int offset, int length ) {
		int max = -Integer.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
	public static int max( int[] array, int offset, int length ) {
		int max = -Integer.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
		long max = -Long.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
		float max = -Float.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < length; i++) {
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
		int max = -Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < length; i++) {
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
	public static int maxIdx( int[] array, int offset, int length ) {
		int max = -Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < length; i++) {
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
		float max = -Float.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < length; i++) {
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
		double max = -Double.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < length; i++) {
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
		double result = 0.0;
		for (int i = 0; i < length; i++) {
			result = op.process(i, array[i + offset], result);
		}
		return result;
	}

	@FunctionalInterface
	public interface FeedbackIdxD {
		double process( int idx, double value, double previous );
	}
}
