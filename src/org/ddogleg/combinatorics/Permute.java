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

package org.ddogleg.combinatorics;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * Exhaustively computes all the permutations of a set, without recursion.  Designed to be memory and speed efficient.
 * </p>
 *
 * <p>
 * <pre>
 * Partially worked example for the set "1234".
 *
 * 1234
 * 2134
 * 3124
 * 1324
 * 2314
 * 3214
 * 3241
 * 2341
 * 4321
 * </pre>
 * </p>
 *
 * <p>
 * Algorithm was originally lifted from some website years ago.  Here is its pseudo-code.
 * <pre>
 * let a[] represent an arbitrary list of objects to permute
 *   let N equal the length of a[]
 *   create an integer array p[] of size N+1 to control the iteration
 *   initialize p[0] to 0, p[1] to 1, p[2] to 2, ..., p[N] to N
 *   initialize index variable i to 1
 *   while (i < N) do {
 *      decrement p[i] by 1
 *      if i is odd, then let j = p[i] otherwise let j = 0
 *      swap(a[j], a[i])
 *      let i = 1
 *      while (p[i] is equal to 0) do {
 *         let p[i] = i
 *         increment i by 1
 *      } // end while (p[i] is equal to 0)
 *   } // end while (i < N)
 * </pre>
 * </p>
 *
 * @param <T>
 */
public class Permute< T >
{
	protected List<T> list;
	private int bins[];
	protected int N; // this is 'i' in the pseudocode above
	private int end; // total number of permutations

	/**
	 Permute the elements in the list provided
	 */
	public Permute(List<T> list) {
		init( list );
	}

	public Permute() {
	}

	/**
	 * Initializes the permutation for a new list
	 *
	 * @param list List which is to be permuted.
	 */
	private void init( List<T> list ) {
		this.list = list;
		bins = new int[ list.size() + 1 ];

		for( int i = 0; i < bins.length ; i++ ) {
			bins[i] = i;
		}

		N=1;
		end = 1;
		for( int i = 1; i < bins.length ; i++ ) {
			end *= i;
		}

	}

	/**
	 * Returns the total number of permutations
	 */
	public int getTotalPermutations() {
		return end;
	}

	/**
	 * This will permute the list once
	 */
	public boolean next()
	{
		if( bins[N] >= list.size() ) {
			return false;
		}

		bins[ N ]--;

		int j = N % 2 * bins[N]; // if N is odd then j = bin[N] else 0
		swap( N , j );

		N = 1;
		while( bins[N] == 0 ) {
			bins[N] = N;
			N++;
		}

		return true;
	}

	private void swap( int a , int b ) {
		if( a > b ) {
			int t = a;
			a = b;
			b = t;
		}

		// now swap that bin and left most
		T t = list.get(a);
		list.set( a , list.get(b) );
		list.set( b , t );
	}

	/**
	 * This will undo a permutation.
	 */
	public boolean previous()
	{
		if( N == 1 ) {
			for( int i = 0; i < bins.length; i++ ) {
				if( bins[i] != i ) {
					bins[i]++;
					N = i;
					break;
				}
			}
			if( N == 1 )
				return false;
		} else {
			for( int i = 2; i < N; i++ ) {
				bins[i] = 0;
			}
			N = 1;
		}

		int j = N % 2 * (bins[N] - 1); // if N is odd then j = bin[N] else 0
		swap( N , j );

		return true;
	}

	/**
	 * Returns the size of the list being premuted
	 *
	 * @return list size
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Returns element 'i' in the current permutation
	 *
	 * @param i index
	 * @return element in permuted list
	 */
	public T get( int i ) {
		return list.get(bins[i]);
	}

	/**
	 * Returns a list containing the current permutation.
	 *
	 * @param storage Optional storage.  If null a new list will be declared.
	 * @return Current permutation
	 */
	public List<T> getAll( List<T> storage ) {
		if( storage == null )
			storage = new ArrayList<T>();
		else
			storage.clear();

		for( int i = 0; i < list.size(); i++ ) {
			storage.add(get(i));
		}

		return storage;
	}
}
