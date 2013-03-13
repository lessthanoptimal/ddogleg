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
 * An implementation of the straight insert sort algorithm.  This is a O(N^2) algorithm
 * and this implemenation originally came from NUmerical Recipes Third Edition.
 * page 420.
 *
 * No additional data structures need to be declared.  Elements are swapped in the list
 *
 * Only recommended for less than 20 elements
 */
public class StraightInsertionSort 
{
	/**
	 * Sorts data into ascending order
	 */
	public static void sort( double[] data )
	{
		int i=0,j;
		final int n = data.length;
		double a;

		// by doing the ugly exception catching it was 13% faster
		// on data set of 100000
		try {
			for( j =1; ; j++ ) {
				a=data[j];
				try {
					for( i=j; data[i-1] > a;i-- ) {
						data[i]=data[i-1];
					}
				}catch( ArrayIndexOutOfBoundsException e ) {}
				data[i]=a;
			}
		}catch( ArrayIndexOutOfBoundsException e ) {}
	}
}
