/*
 * Copyright (c) 2012, Peter Abeles. All Rights Reserved.
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
   If you have a list but you want to shuffle
   the first k-items in the list so that each combination
   of items can be found once (independent of order) this use
   this class.

   To access the elements in the list use this function list
   a list that is

   This algorithm I developed on my own and should be
   considered experimental.  This function is also
   not synchronized.

   example:

   List = 012345
   k = 3;

   012
   013
   014
   015
   023
   024
   025
   034
   035
   045
   123
   124
   125
   134
   135
   145
   234
   235
   245
   345
*/

public class Combinations< T >
{
	List<T> a; // the original unmolested list

	int N; // the total number of elements in "a"
	int k; // the number of elements in the active bucket
	int []bins; // which element in "a" is in which position in the list
	int c; // the last bin in "bins"
	long num_shuff = -1; // the number of shuffles, calculated when it is needed

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
	 * @param list List which is to be symbols
	 * @param bucketSize Size of the bucket
	 */
	public void init( List<T> list , int bucketSize ) {
		if( list.size() < bucketSize ) {
			throw new RuntimeException("There needs to be more than or equal to elements in the \"a\" that there are in the bucket");
		}

		this.k = bucketSize;
		this.c = bucketSize - 1;
		N = list.size();

		bins = new int[ bucketSize ];

		for( int i = 0; i < bins.length; i++ ) {
			bins[i] = i;
		}

		this.a = list;
		this.num_shuff = -1;
	}

	/**
	 This will shuffle the elements in and out of the
	 bins.  When all combinations have been exhausted
	 an ExhaustedException will be thrown.
	 */
	public void shuffle()
			throws ExhaustedException
	{
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

			if( allgood == false ) {
				throw new ExhaustedException();
			}
		}
	}

	public void unshuffle()
			throws ExhaustedException
	{
		for( int i = c; i >= 0; i-- ) {
			bins[i]--;

			if( i == 0 ) {
				if( bins[i] < 0 ) {
					bins[i] = 0;
					throw new ExhaustedException();
				}
				break;
			} else if( bins[i] <= bins[i-1] ) {
				bins[i] = N-1-(c-i);
			} else {
				break;
			}
		}
	}

	/**
	 * The number of combinations is, n!/(k!*(n-k)!, where n is number of elements, k is the number of bins, and
	 * ! is factorial.
	 *
	 * @return Total number
	 */
	public long numShuffles() {
		if( num_shuff != -1 )
			return num_shuff;

		// this could be speed up by caching intermediate values
		long fact_N_div_NmK = factorial(N,N-bins.length);
		long fac_K = factorial(bins.length);

		num_shuff = fact_N_div_NmK/fac_K;

		return num_shuff;
	}

	public int size() {
		return k;
	}

	public T get( int i ) {
		return a.get( bins[i] );
	}

	public List<T> getList( List<T> storage ) {
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
	 This returns all the items that are not currently inside the
	 'active bins'
	 THIS CAN BE MADE FASTER
	 */
	public List<T> getOutside( List<T> ret )
	{
		if( ret == null ) {
			ret = new ArrayList<T>();
		}

		ret.addAll( a );

		// bins specifies elements in order of first in 'a' to last
		for( int i = bins.length-1; i >= 0; i-- ) {
			ret.remove( bins[i] );
		}

		return ret;
	}

	/**
	 this exception is thrown when there are no mroe combinations
	 */
	public static class ExhaustedException
			extends Exception
	{
		public ExhaustedException(){

		}
	}

	public static long factorial( long n , int start)
	{
		long ret = 1;
		for (long i = start; i <= n; ++i) ret *= i;
		return ret;
	}

	public static long factorial(long n)
	{
		long ret = 1;
		for (long i = 1; i <= n; ++i) ret *= i;
		return ret;
	}
}
