/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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
 * An implementation of the quick sort algorithm from Numerical Recipes Third Edition
 * that is specified for arrays of floats.
 *
 * A small amount of memory is declared for this sorting algorithm.
 *
 * This implementation seems to often perform slower than Shell sort.  A comment in Numerical
 * recipes about unnecessary array checks makes me think this is slow because java always
 * does a bounds check on arrays.
 *
 * This has slightly better performance than Arrays.sort(float[]).  Not noticeable in most applications.
 */
public class QuickSortObj_F32 {
	// an architecture dependent tuning parameter
	private int M = 7;
	private int NSTACK;

	private int istack[];

	public QuickSortObj_F32() {
		NSTACK = 65;
		istack = new int[NSTACK];
	}

	public QuickSortObj_F32(int NSTACK, int M) {
		this.M = M;
		this.NSTACK = NSTACK;

		istack = new int[NSTACK];
	}

	public void sort( SortableParameter_F32[] arr , int length )
	{
		int i,ir,j,k;
		int jstack = -1;
		int l = 0;
		// if I ever publish a book I will never use variable l in an algorithm with lots of 1

		SortableParameter_F32 a;

		ir=length-1;

		SortableParameter_F32 temp;

		for(;;) {
			if( ir-l < M) {
				for( j=l+1;j<=ir;j++) {
					a = arr[j];
					for( i=j-1; i>=l;i-- ) {
						if( arr[i].sortValue <= a.sortValue ) break;
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

				if( arr[l].sortValue > arr[ir].sortValue) {
					temp = arr[l];
					arr[l] = arr[ir];
					arr[ir] = temp;
				}
				if( arr[l+1].sortValue > arr[ir].sortValue) {
					temp = arr[l+1];
					arr[l+1] = arr[ir];
					arr[ir] = temp;
				}
				if( arr[l].sortValue > arr[l+1].sortValue) {
					temp = arr[l];
					arr[l] = arr[l+1];
					arr[l+1] = temp;
				}
				i=l+1;
				j=ir;
				a=arr[l+1];
				for(;;) {
					do { i++; } while( arr[i].sortValue < a.sortValue);
					do { j--; } while( arr[j].sortValue > a.sortValue);
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

	public void sort( SortableParameter_F32[] arr , int length , int indexes[] )
	{
		for( int i = 0; i < length; i++ ) {
			indexes[i] = i;
		}

		int i,ir,j,k;
		int jstack = -1;
		int l = 0;
		// if I ever publish a book I will never use variable l in an algorithm with lots of 1

		SortableParameter_F32 a;

		ir=length-1;

		int temp;

		for(;;) {
			if( ir-l < M) {
				for( j=l+1;j<=ir;j++) {
					a = arr[indexes[j]];
					temp = indexes[j];
					for( i=j-1; i>=l;i-- ) {
						if( arr[indexes[i]].sortValue <= a.sortValue ) break;
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

				if( arr[indexes[l]].sortValue > arr[indexes[ir]].sortValue) {
					temp = indexes[l];
					indexes[l] = indexes[ir];
					indexes[ir] = temp;
				}
				if( arr[indexes[l+1]].sortValue > arr[indexes[ir]].sortValue) {
					temp = indexes[l+1];
					indexes[l+1] = indexes[ir];
					indexes[ir] = temp;
				}
				if( arr[indexes[l]].sortValue > arr[indexes[l+1]].sortValue) {
					temp = indexes[l];
					indexes[l] = indexes[l+1];
					indexes[l+1] = temp;
				}
				i=l+1;
				j=ir;
				a=arr[indexes[l+1]];
				for(;;) {
					do { i++; } while( arr[indexes[i]].sortValue < a.sortValue);
					do { j--; } while( arr[indexes[j]].sortValue > a.sortValue);
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
