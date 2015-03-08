/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.combinatorics;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Computes the combinations of size k given a set S of size N.  This can be done in the forward or reverse direction.
 * A combination is defined is a unique subset of a list in which order does not mater.  This is different from
 * a permutation where order does matter.
 * </p>
 *
 * <p>
 * The bucket is used to refer to the output set of size k that is the current combination.  Elements outside the
 * bucket are all elements not currently in the bucket.
 * </p>
 *
 * <p>Below is an example of a combination.</p>
 * <pre>
 * List = 012345
 * k = 3;
 *
 * 012
 * 013
 * 014
 * 015
 * 023
 * 024
 * 025
 * 034
 * 035
 * 045
 * 123
 * 124
 * 125
 * 134
 * 135
 * 145
 * 234
 * 235
 * 245
 * 345
 * </pre>
 **/
public class Combinations< T >
{
	protected List<T> a; // the original unmolested list

	protected int N; // the total number of elements in "a"
	protected int k; // the number of elements in the active bucket
	protected int []bins; // which element in "a" is in which position in the list
	protected int c; // the last bin in "bins"

	// indicates if it is 0=at beginning, 1=middle, 2=end
	// 0 or 2 indicate that it is actually one past the extreme, which needs to be taken into account
	protected int state;

	/**
	 * Constructor where the list and combinations is specified
	 *
	 * @param a List of symbols
	 * @param bucketSize Size of the bucket
	 */
	public Combinations(List<T> a, int bucketSize) {
		init( a , bucketSize );
	}

	public Combinations(){
	}

	/**
	 * Initialize with a new list and bucket size
	 *
	 * @param list List which is to be symbols
	 * @param bucketSize Size of the bucket
	 */
	public void init( List<T> list , int bucketSize ) {
		if( list.size() < bucketSize ) {
			throw new RuntimeException("There needs to be more than or equal to elements in the 'list' that there are in the bucket");
		}

		this.k = bucketSize;
		this.c = bucketSize - 1;
		N = list.size();

		bins = new int[ bucketSize ];

		for( int i = 0; i < bins.length; i++ ) {
			bins[i] = i;
		}

		this.a = list;
		state = 1;
	}

	/**
	 * This will shuffle the elements in and out of the
	 * bins.  When all combinations have been exhausted
	 * an ExhaustedException will be thrown.
	 *
	 * @return true if the next combination was successfully found or false is it has been exhausted
	 */
	public boolean next()
	{
		if( state == 2 )
			return false;
		else
			state = 1;

		bins[c]++;

		if( bins[c] >= N ) {
			boolean allgood = false;
			for( int i = c-1; i >= 0; i-- ) {
				bins[i]++;

				if( bins[i] <= (N-(k-i)) ) {
					allgood = true;
					for( int j = i + 1; j < k; j++ ) {
						bins[j] = bins[j-1] + 1;
					}
					break;
				}
			}

			if( !allgood ) {
				state = 2;
				// put it back into the last combination
				for( int j = 0; j < bins.length; j++ ) {
					bins[j] = j+N-bins.length;
				}
			}

			return allgood;
		}
		return true;
	}

	/**
	 * Undoes the previous combination computed by {@link #next()}.
	 *
	 * @return true if the combination was successfully undone or false is is back to the original state
	 */
	public boolean previous()
	{
		// see if it is back to the start already
		if( state == 0 )
			return false;
		else
			state = 1;

		for( int i = c; i >= 0; i-- ) {
			bins[i]--;

			if( i == 0 ) {
				if( bins[0] < 0 ) {
					state = 0;
					// put it back into its first combination
					for( int j = 0; j < bins.length; j++ ) {
						bins[j] = j;
					}
					return false;
				}
				break;
			} else if( bins[i] <= bins[i-1] ) {
				bins[i] = N-1-(c-i);
			} else {
				break;
			}
		}
		return true;
	}

	/**
	 * The number of combinations is, n!/(k!*(n-k)!, where n is number of elements, k is the number of bins, and
	 * ! is factorial.
	 *
	 * @return Total number
	 */
	public long computeTotalCombinations() {

		long numerator = a.size();
		long denominator = k;

		for( int i = 1; i < k; i++ ) {
			numerator *= a.size()-i;
			denominator *= k-i;
		}

		return numerator/denominator;
	}

	/**
	 * Returns the size of the bucket/output set
	 *
	 * @return Size of bucket
	 */
	public int getBucketSize() {
		return k;
	}

	/**
	 * Returns element 'i' in the bucket.
	 *
	 * @param i which element
	 * @return the element
	 */
	public T get( int i ) {
		return a.get( bins[i] );
	}

	/**
	 * Extracts the entire bucket.  Will add elements to the provided list or create a new one
	 *
	 * @param storage Optional storage.  If null a list is created.  clear() is automatically called.
	 * @return List containing the bucket
	 */
	public List<T> getBucket(List<T> storage) {
		if( storage == null )
			storage = new ArrayList<T>();
		else
			storage.clear();
		
		
		for( int i = 0; i < bins.length; i++ ) {
			storage.add( a.get( bins[i] ) );
		}
		
		return storage;
	}

	/**
	 * This returns all the items that are not currently inside the bucket
	 *
	 * @param storage Optional storage.  If null a list is created.  clear() is automatically called.
	 * @return List containing the bucket
	 */
	public List<T> getOutside( List<T> storage )
	{
		if( storage == null ) {
			storage = new ArrayList<T>();
		}  else {
			storage.clear();
		}

		storage.addAll(a);

		// bins specifies elements in order of first in 'a' to last
		for( int i = bins.length-1; i >= 0; i-- ) {
			storage.remove(bins[i]);
		}

		return storage;
	}
}
