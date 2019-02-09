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
	public static void fillCounting(int[] array , int offset, int length ) {
		for (int i = 0; i < length; i++) {
			array[i+offset] = i;
		}
	}

	public static int[] fillCounting(int length ) {
		int[] array = new int[length];
		fillCounting(array,0,length);
		return array;
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( byte []array , int offset , int length , Random rand ) {
		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length-i);
			byte tmp = array[offset+src+i];
			array[offset+src+i]=array[offset+i];
			array[offset+i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( short []array , int offset , int length , Random rand ) {
		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length-i);
			short tmp = array[offset+src+i];
			array[offset+src+i]=array[offset+i];
			array[offset+i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( int []array , int offset , int length , Random rand ) {
		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length-i);
			int tmp = array[offset+src+i];
			array[offset+src+i]=array[offset+i];
			array[offset+i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( long []array , int offset , int length , Random rand ) {
		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length-i);
			long tmp = array[offset+src+i];
			array[offset+src+i]=array[offset+i];
			array[offset+i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( float []array , int offset , int length , Random rand ) {
		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length-i);
			float tmp = array[offset+src+i];
			array[offset+src+i]=array[offset+i];
			array[offset+i] = tmp;
		}
	}

	/**
	 * Randomly shuffle the array
	 */
	public static void shuffle( double []array , int offset , int length , Random rand ) {
		for (int i = 0; i < length; i++) {
			int src = rand.nextInt(length-i);
			double tmp = array[offset+src+i];
			array[offset+src+i]=array[offset+i];
			array[offset+i] = tmp;
		}
	}
}
