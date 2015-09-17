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
 * Exhaustively computes all the permutations of a set, without recursion.  Designed to be memory and speed efficient.
 * </p>
 *
 * <p>
 * Example for the set "0123".
 * </p>
 * <pre>
 * {@code
 * 0123
 * 0132
 * 0213
 * 0231
 * 0321
 * 0312
 * 1023
 * 1032
 * 1203
 * 1230
 * 1320
 * 1302
 * 2103
 * 2130
 * 2013
 * 2031
 * 2301
 * 2310
 * 3120
 * 3102
 * 3210
 * 3201
 * 3021
 * 3012
 * }
 * </pre>
 *
 * @param <T>
 */
public class Permute< T >
{
	protected List<T> list;
	private int indexes[];
	private int counters[];

	private int total;
	private int permutation;

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
		indexes = new int[ list.size() ];
		counters = new int[ list.size() ];

		for( int i = 0; i < indexes.length ; i++ ) {
			counters[i] = indexes[i] = i;
		}

		total = 1;
		for( int i = 2; i <= indexes.length ; i++ ) {
			total *= i;
		}

		permutation = 0;
	}

	/**
	 * Returns the total number of permutations
	 */
	public int getTotalPermutations() {
		return total;
	}

	/**
	 * This will permute the list once
	 */
	public boolean next()
	{
		System.out.println("ENTER next()");
		printCounters();

		if( indexes.length <= 1 || permutation >= total-1 )
			return false;

		int N = indexes.length-2;
		int k = N;
		while( k <= N ) {
			System.out.println("  k = "+k+"  swap");
			swap(k, counters[k]++);//after
			if (counters[k] == indexes.length) {
				System.out.println("  k -= 1");
				k -= 1;
				if( k < 0 )
					throw new RuntimeException("BUG, should have been caught earlier");
			} else {
				System.out.println("  swap(counter[k],k)");
				swap(counters[k], k);  //before
				while( k < indexes.length-1 ) {
					k++;
					counters[k] = k;
				}
				printCounters();
			}
		}
		printCounters();
		System.out.println("EXIT next()");

		permutation++;
		return true;
	}

	private void printCounters() {
		for (int i = 0; i < counters.length; i++) {
			System.out.print(counters[i]+" ");
		}
		System.out.println();
	}

	/**
	 * This will undo a permutation.
	 */
	public boolean previous()
	{
		return false;
//		if( indexes.length <= 1 && permutation <= 0 )
//			return false;
//
//		System.out.println("ENTER previous()");
//		printCounters();
//
//		int N = indexes.length-2;
//		int k = N;
//
//		while( k <= N ) {
//			System.out.println("  k = " + k);
//			swap( k, counters[k]++);//after
//			if (counters[k] == indexes.length) {
//				k -= 1;
//				if( k < 0 )
//					throw new RuntimeException("BUG, should have been caught earlier");
//			} else {
//				swap(counters[k], k);  //before
//				while( k < indexes.length-1 ) {
//					k++;
//					counters[k] = k;
//				}
//			}
//		}
//
//		printCounters();
//		System.out.println("EXIT previous()");
//		return true;
	}

	private void swap( int i , int j ) {
		int val = indexes[i];
		indexes[i] = indexes[j];
		indexes[j] = val;
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
		return list.get(indexes[i]);
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

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<Integer>();

		for (int i = 0; i < 4; i++) {
			list.add(i);
		}

		Permute permute = new Permute(list);

		print(permute);
		while( permute.next() ) {
			print(permute);
		}

		System.out.println();
		System.out.println("Reverse");
		print(permute);
		while( permute.previous() ) {
			print(permute);
		}
	}

	private static void print( Permute permute ) {
		System.out.print(" * ");
		for (int i = 0; i < permute.size(); i++) {
			System.out.print(permute.get(i));
		}
		System.out.println();


	}
}
