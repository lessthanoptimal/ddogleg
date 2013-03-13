/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

/**
 * Implemenation of the shell sort algorithm from Numerical Recipes Third Edition.
 *
 * Is a O(N^{3/2}) sorting algorithm
 *
 * No additional memory allocation is performed
 *
 * Only recommended for less than 50 elements
 */
public class ShellSort {
	/**
	 * Sorts data into ascending order
	 */
	public static void sort( double[] data )
	{
		int i,j;
		int inc=1;
		double v;

		do {
			inc *= 3;
			inc++;
		} while( inc <= data.length );

		do {
			inc /= 3;

			for( i=inc; i < data.length; i++ ) {
				v=data[i];
				j=i;
				while( data[j-inc] > v ) {
					data[j] = data[j-inc];
					j -= inc;
					if( j < inc ) break;
				}
				data[j]=v;
			}
		} while( inc > 1 );
	}
}
