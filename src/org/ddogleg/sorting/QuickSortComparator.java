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

package org.ddogleg.sorting;

import java.util.Comparator;

/**
 * An implementation of the quick sort algorithm from Numerical Recipes Third Edition
 * that is specified for arrays of floats.
 *
 * A small amount of memory is declared for this sorting algorithm.
 *
 * This implementation of QuickSort allows you to use a Comparator.  Useful when you want to ensure that no
 * extra memory is declared each time you sort.  This is possible when using built in methods.
 */
public class QuickSortComparator<T> {
	// an architecture dependent tuning parameter
	private int M = 7;
	private int NSTACK;

	private int istack[];

	Comparator<T> comparator;

	public QuickSortComparator(Comparator<T> comparator) {
		this(65,7,comparator);
	}

	public QuickSortComparator(int NSTACK, int M, Comparator<T> comparator) {
		this.M = M;
		this.NSTACK = NSTACK;

		istack = new int[NSTACK];
		this.comparator = comparator;
	}

	public void sort( T[] arr , int length )
	{
		int i,ir,j,k;
		int jstack = -1;
		int l = 0;
		// if I ever publish a book I will never use variable l in an algorithm with lots of 1
		T a;

		ir=length-1;

		T temp;

		for(;;) {
			if( ir-l < M) {
				for( j=l+1;j<=ir;j++) {
					a = arr[j];
					for( i=j-1; i>=l;i-- ) {
						if( comparator.compare(arr[i],a) <= 0 ) break;
						arr[i+1]=arr[i];
					}
					arr[i+1]=a;
				}
				if(jstack < 0) break;

				ir = istack[jstack--];
				l=istack[jstack--];

			} else {
				k=(l+ir)>>>1;
				temp = arr[k];
				arr[k] = arr[l+1];
				arr[l+1] = temp;

				if( comparator.compare(arr[l], arr[ir]) > 0) {
					temp = arr[l];
					arr[l] = arr[ir];
					arr[ir] = temp;
				}
				if( comparator.compare(arr[l+1], arr[ir]) > 0) {
					temp = arr[l+1];
					arr[l+1] = arr[ir];
					arr[ir] = temp;
				}
				if( comparator.compare(arr[l], arr[l+1]) > 0) {
					temp = arr[l];
					arr[l] = arr[l+1];
					arr[l+1] = temp;
				}
				i=l+1;
				j=ir;
				a=arr[l+1];
				for(;;) {
					do { i++; } while( comparator.compare(arr[i], a) < 0);
					do { j--; } while( comparator.compare(arr[j], a) > 0);
					if(j < i ) break;
					temp = arr[i];
					arr[i] = arr[j];
					arr[j] = temp;
				}
				arr[l+1]=arr[j];
				arr[j]=a;
				jstack += 2;

				if( jstack >= NSTACK )
					throw new RuntimeException("NSTACK too small");
				if( ir-i+1 >= j-l ) {
					istack[jstack] = ir;
					istack[jstack-1] = i;
					ir=j-1;
				} else {
					istack[jstack] = j-1;
					istack[jstack-1] = l;
					l=i;
				}
			}
		}
	}

	public void sort( T[] arr , int length , int indexes[] )
	{
		for( int i = 0; i < length; i++ ) {
			indexes[i] = i;
		}

		int i,ir,j,k;
		int jstack = -1;
		int l = 0;
		// if I ever publish a book I will never use variable l in an algorithm with lots of 1

		T a;

		ir=length-1;

		int temp;

		for(;;) {
			if( ir-l < M) {
				for( j=l+1;j<=ir;j++) {
					a = arr[indexes[j]];
					temp = indexes[j];
					for( i=j-1; i>=l;i-- ) {
						if( comparator.compare( arr[indexes[i]],a) <= 0 ) break;
						indexes[i+1]=indexes[i];
					}
					indexes[i+1]=temp;
				}
				if(jstack < 0) break;

				ir = istack[jstack--];
				l=istack[jstack--];

			} else {
				k=(l+ir)>>>1;
				temp = indexes[k];
				indexes[k] = indexes[l+1];
				indexes[l+1] = temp;

				if( comparator.compare(arr[indexes[l]] , arr[indexes[ir]]) > 0 ) {
					temp = indexes[l];
					indexes[l] = indexes[ir];
					indexes[ir] = temp;
				}
				if( comparator.compare(arr[indexes[l+1]], arr[indexes[ir]]) > 0 ) {
					temp = indexes[l+1];
					indexes[l+1] = indexes[ir];
					indexes[ir] = temp;
				}
				if( comparator.compare(arr[indexes[l]], arr[indexes[l+1]]) > 0 ) {
					temp = indexes[l];
					indexes[l] = indexes[l+1];
					indexes[l+1] = temp;
				}
				i=l+1;
				j=ir;
				a=arr[indexes[l+1]];
				for(;;) {
					do { i++; } while( comparator.compare(arr[indexes[i]], a ) < 0 );
					do { j--; } while( comparator.compare(arr[indexes[j]], a ) > 0 );
					if(j < i ) break;
					temp = indexes[i];
					indexes[i] = indexes[j];
					indexes[j] = temp;
				}
				temp = indexes[l+1];
				indexes[l+1]=indexes[j];
				indexes[j]=temp;
				jstack += 2;

				if( jstack >= NSTACK )
					throw new RuntimeException("NSTACK too small");
				if( ir-i+1 >= j-l ) {
					istack[jstack] = ir;
					istack[jstack-1] = i;
					ir=j-1;
				} else {
					istack[jstack] = j-1;
					istack[jstack-1] = l;
					l=i;
				}
			}
		}
	}
}
