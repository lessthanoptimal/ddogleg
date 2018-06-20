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

import java.util.List;

/**
 * Convenience class for swapping elements in arrays and lists.
 *
 * @author Peter Abeles
 */
public class SwapElement {
	public static void swap( byte[] array , int a , int b ) {
		byte tmp = array[a];
		array[a] = array[b];
		array[b] = tmp;
	}

	public static void swap( char[] array , int a , int b ) {
		char tmp = array[a];
		array[a] = array[b];
		array[b] = tmp;
	}

	public static void swap( short[] array , int a , int b ) {
		short tmp = array[a];
		array[a] = array[b];
		array[b] = tmp;
	}

	public static void swap( int[] array , int a , int b ) {
		int tmp = array[a];
		array[a] = array[b];
		array[b] = tmp;
	}

	public static void swap( long[] array , int a , int b ) {
		long tmp = array[a];
		array[a] = array[b];
		array[b] = tmp;
	}

	public static void swap( float[] array , int a , int b ) {
		float tmp = array[a];
		array[a] = array[b];
		array[b] = tmp;
	}

	public static void swap( double[] array , int a , int b ) {
		double tmp = array[a];
		array[a] = array[b];
		array[b] = tmp;
	}

	public static <T> void swap(List<T> list, int a , int b ) {
		T tmp = list.get(a);
		list.set(a,list.get(b));
		list.set(b,tmp);
	}

}
