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
 * <p>
 * QuickSelect searches for the k-th largest item in the list.  While doing this search
 * it will sort the list partially.  all the items below k will have a value less than it
 * and all the items more than k will have a value greater than it.  However the values
 * above and below can be unsorted.  QuickSelect is faster than QuickSort of you don't
 * need a fully sorted list.
 * </p>
 * <p>
 * An implementation of the quick select algorithm from Numerical Recipes Third Edition
 * that is specified for arrays of doubles.  See page 433.
 * </p>
 *
 *
 * @author Peter Abeles
 */
public class QuickSelectArray {

   /**
	 * Sorts the array such that the values in the array up to and including
	 * 'k' are sorted the least to greatest.  This implies that the array
	 * itself is modified. For convinience the 'k' element is returned.
	 *
	 * @param data The unsorted list
	 * @param k The element of the sorted list that is to be found
	 * @param maxIndex Only element up to this value are considered
	 * @return the 'k'th largest element
	 */
	public static float select( float []data , int k , int maxIndex ) {

		int i,j,mid;
		int n = maxIndex;
		float a;
		int l = 0;
		int ir = n-1;

		float temp;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}
				return data[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				temp = data[mid];
				data[mid] = data[lp1];
				data[lp1] = temp;

				if( data[l] > data[ir] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}

				if( data[lp1] > data[ir] ) {
					temp = data[lp1];
					data[lp1] = data[ir];
					data[ir] = temp;
				}

				if( data[l] > data[lp1] ) {
					temp = data[lp1];
					data[lp1] = data[l];
					data[l] = temp;
				}

				i=lp1;
				j=ir;
				a=data[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					temp = data[i];
					data[i] = data[j];
					data[j] = temp;
				}
				data[lp1] = data[j];
				data[j] = a;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	/**
	 * <p>
	 * Returns the original index of the 'k' largest element in the list.
	 * </p>
	 * 
	 * <p>
	 * Note: There is additional overhead since the values of indexes needs to be set
	 * </p>
	 * 
	 * @param indexes Temporary storage and is overwritten
	 */
	public static int selectIndex( float []data , int k , int maxIndex ,  int []indexes) {

		for( int i = 0; i < maxIndex; i++ ) {
			indexes[i] = i;
		}

		int i,j,mid;
		int n = maxIndex;
		float a;
		int indexA;
		int l = 0;
		int ir = n-1;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					swap(data,indexes,l,ir);
				}
				return indexes[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				swap(data,indexes,mid,lp1);

				if( data[l] > data[ir] ) {
					swap(data,indexes,l,ir);
				}

				if( data[lp1] > data[ir] ) {
					swap(data,indexes,lp1,ir);
				}

				if( data[l] > data[lp1] ) {
					swap(data,indexes,lp1,l);
				}

				i=lp1;
				j=ir;
				a=data[lp1];
				indexA=indexes[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					swap(data,indexes,i,j);
				}
				data[lp1] = data[j];
				data[j] = a;
				indexes[lp1] = indexes[j];
				indexes[j] = indexA;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	private static void swap( float[] data , int []indexes, int a , int b )
	{
		float tempD = data[a];
		int tempI = indexes[a];

		data[a] = data[b];
		indexes[a] = indexes[b];

		data[b] = tempD;
		indexes[b] = tempI;
	}

   /**
	 * Sorts the array such that the values in the array up to and including
	 * 'k' are sorted from the least to greatest.  This implies that the array
	 * itself is modified. For convenience the value of the 'k'th element is returned.
	 *
	 * @param data The unsorted list.  Is modified.
	 * @param k The element of the sorted list that is to be found
	 * @param maxIndex Only element up to this value are considered
	 * @return the 'k'th largest element
	 */
	public static double select( double []data , int k , int maxIndex ) {

		int i,j,mid;
		int n = maxIndex;
		double a;
		int l = 0;
		int ir = n-1;

		double temp;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}
				return data[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				temp = data[mid];
				data[mid] = data[lp1];
				data[lp1] = temp;

				if( data[l] > data[ir] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}

				if( data[lp1] > data[ir] ) {
					temp = data[lp1];
					data[lp1] = data[ir];
					data[ir] = temp;
				}

				if( data[l] > data[lp1] ) {
					temp = data[lp1];
					data[lp1] = data[l];
					data[l] = temp;
				}

				i=lp1;
				j=ir;
				a=data[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					temp = data[i];
					data[i] = data[j];
					data[j] = temp;
				}
				data[lp1] = data[j];
				data[j] = a;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	/**
	 * <p>
	 * Returns the original index of the 'k' largest element in the list.
	 * </p>
	 * 
	 * <p>
	 * Note: There is additional overhead since the values of indexes needs to be set
	 * </p>
	 *
	 * @param data (Modified) The unsorted list.
	 * @param k The element of the sorted list that is to be found
	 * @param maxIndex Only element up to this value are considered
	 * @param indexes (Modified) Work space which contains indexes of the original list
	 * @return the 'k'th largest element
	 */
	public static int selectIndex( double []data , int k , int maxIndex ,  int []indexes) {

		for( int i = 0; i < maxIndex; i++ ) {
			indexes[i] = i;
		}

		int i,j,mid;
		int n = maxIndex;
		double a;
		int indexA;
		int l = 0;
		int ir = n-1;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					swap(data,indexes,l,ir);
				}
				if( k >= indexes.length )
					System.out.println("Crap");
				return indexes[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				swap(data,indexes,mid,lp1);

				if( data[l] > data[ir] ) {
					swap(data,indexes,l,ir);
				}

				if( data[lp1] > data[ir] ) {
					swap(data,indexes,lp1,ir);
				}

				if( data[l] > data[lp1] ) {
					swap(data,indexes,lp1,l);
				}

				i=lp1;
				j=ir;
				a=data[lp1];
				indexA=indexes[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					swap(data,indexes,i,j);
				}
				data[lp1] = data[j];
				data[j] = a;
				indexes[lp1] = indexes[j];
				indexes[j] = indexA;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	private static void swap( double[] data , int []indexes, int a , int b )
	{
		double tempD = data[a];
		int tempI = indexes[a];

		data[a] = data[b];
		indexes[a] = indexes[b];

		data[b] = tempD;
		indexes[b] = tempI;
	}

   /**
	* Sorts the array such that the values in the array up to and including
	* 'k' are sorted from the least to greatest.  This implies that the array
	* itself is modified. For convenience the value of the 'k'th element is returned.
	 *
	 * @param data The unsorted list.  Is modified.
	 * @param k The element of the sorted list that is to be found
	 * @param maxIndex Only element up to this value are considered
	 * @return the 'k'th largest element
	 */
	public static long select( long []data , int k , int maxIndex ) {

		int i,j,mid;
		int n = maxIndex;
		long a;
		int l = 0;
		int ir = n-1;

		long temp;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}
				return data[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				temp = data[mid];
				data[mid] = data[lp1];
				data[lp1] = temp;

				if( data[l] > data[ir] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}

				if( data[lp1] > data[ir] ) {
					temp = data[lp1];
					data[lp1] = data[ir];
					data[ir] = temp;
				}

				if( data[l] > data[lp1] ) {
					temp = data[lp1];
					data[lp1] = data[l];
					data[l] = temp;
				}

				i=lp1;
				j=ir;
				a=data[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					temp = data[i];
					data[i] = data[j];
					data[j] = temp;
				}
				data[lp1] = data[j];
				data[j] = a;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	/**
	 * <p>
	 * Returns the original index of the 'k' largest element in the list.
	 * </p>
	 * 
	 * <p>
	 * Note: There is additional overhead since the values of indexes needs to be set
	 * </p>
	 * 
	 * @param indexes Temporary storage and is overwritten
	 */
	public static int selectIndex( long []data , int k , int maxIndex ,  int []indexes) {

		for( int i = 0; i < maxIndex; i++ ) {
			indexes[i] = i;
		}

		int i,j,mid;
		int n = maxIndex;
		long a;
		int indexA;
		int l = 0;
		int ir = n-1;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					swap(data,indexes,l,ir);
				}
				return indexes[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				swap(data,indexes,mid,lp1);

				if( data[l] > data[ir] ) {
					swap(data,indexes,l,ir);
				}

				if( data[lp1] > data[ir] ) {
					swap(data,indexes,lp1,ir);
				}

				if( data[l] > data[lp1] ) {
					swap(data,indexes,lp1,l);
				}

				i=lp1;
				j=ir;
				a=data[lp1];
				indexA=indexes[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					swap(data,indexes,i,j);
				}
				data[lp1] = data[j];
				data[j] = a;
				indexes[lp1] = indexes[j];
				indexes[j] = indexA;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	private static void swap( long[] data , int []indexes, int a , int b )
	{
		long tempD = data[a];
		int tempI = indexes[a];

		data[a] = data[b];
		indexes[a] = indexes[b];

		data[b] = tempD;
		indexes[b] = tempI;
	}

   /**
	 * Sorts the array such that the values in the array up to and including
	* Sorts the array such that the values in the array up to and including
	* 'k' are sorted from the least to greatest.  This implies that the array
	* itself is modified. For convenience the value of the 'k'th element is returned.
	 *
	 * @param data The unsorted list.  Is modified.
	 * @param k The element of the sorted list that is to be found
	 * @param maxIndex Only element up to this value are considered
	 * @return the 'k'th largest element
	 */
	public static int select( int []data , int k , int maxIndex ) {

		int i,j,mid;
		int n = maxIndex;
		int a;
		int l = 0;
		int ir = n-1;

		int temp;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}
				return data[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				temp = data[mid];
				data[mid] = data[lp1];
				data[lp1] = temp;

				if( data[l] > data[ir] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}

				if( data[lp1] > data[ir] ) {
					temp = data[lp1];
					data[lp1] = data[ir];
					data[ir] = temp;
				}

				if( data[l] > data[lp1] ) {
					temp = data[lp1];
					data[lp1] = data[l];
					data[l] = temp;
				}

				i=lp1;
				j=ir;
				a=data[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					temp = data[i];
					data[i] = data[j];
					data[j] = temp;
				}
				data[lp1] = data[j];
				data[j] = a;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	/**
	 * <p>
	 * Returns the original index of the 'k' largest element in the list.
	 * </p>
	 * 
	 * <p>
	 * Note: There is additional overhead since the values of indexes needs to be set
	 * </p>
	 * 
	 * @param indexes Temporary storage and is overwritten
	 */
	public static int selectIndex( int []data , int k , int maxIndex ,  int []indexes) {

		for( int i = 0; i < maxIndex; i++ ) {
			indexes[i] = i;
		}

		int i,j,mid;
		int n = maxIndex;
		int a;
		int indexA;
		int l = 0;
		int ir = n-1;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					swap(data,indexes,l,ir);
				}
				return indexes[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				swap(data,indexes,mid,lp1);

				if( data[l] > data[ir] ) {
					swap(data,indexes,l,ir);
				}

				if( data[lp1] > data[ir] ) {
					swap(data,indexes,lp1,ir);
				}

				if( data[l] > data[lp1] ) {
					swap(data,indexes,lp1,l);
				}

				i=lp1;
				j=ir;
				a=data[lp1];
				indexA=indexes[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					swap(data,indexes,i,j);
				}
				data[lp1] = data[j];
				data[j] = a;
				indexes[lp1] = indexes[j];
				indexes[j] = indexA;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	private static void swap( int[] data , int []indexes, int a , int b )
	{
		int tempD = data[a];
		int tempI = indexes[a];

		data[a] = data[b];
		indexes[a] = indexes[b];

		data[b] = tempD;
		indexes[b] = tempI;
	}

   /**
	 * Sorts the array such that the values in the array up to and including
	* Sorts the array such that the values in the array up to and including
	* 'k' are sorted from the least to greatest.  This implies that the array
	* itself is modified. For convenience the value of the 'k'th element is returned.
	*
	 * @param data The unsorted list.  Is modified.
	 * @param k The element of the sorted list that is to be found
	 * @param maxIndex Only element up to this value are considered
	 * @return the 'k'th largest element
	 */
	public static short select( short []data , int k , int maxIndex ) {

		int i,j,mid;
		int n = maxIndex;
		short a;
		int l = 0;
		int ir = n-1;

		short temp;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}
				return data[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				temp = data[mid];
				data[mid] = data[lp1];
				data[lp1] = temp;

				if( data[l] > data[ir] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}

				if( data[lp1] > data[ir] ) {
					temp = data[lp1];
					data[lp1] = data[ir];
					data[ir] = temp;
				}

				if( data[l] > data[lp1] ) {
					temp = data[lp1];
					data[lp1] = data[l];
					data[l] = temp;
				}

				i=lp1;
				j=ir;
				a=data[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					temp = data[i];
					data[i] = data[j];
					data[j] = temp;
				}
				data[lp1] = data[j];
				data[j] = a;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	/**
	 * <p>
	 * Returns the original index of the 'k' largest element in the list.
	 * </p>
	 * 
	 * <p>
	 * Note: There is additional overhead since the values of indexes needs to be set
	 * </p>
	 * 
	 * @param indexes Temporary storage and is overwritten
	 */
	public static int selectIndex( short []data , int k , int maxIndex ,  int []indexes) {

		for( int i = 0; i < maxIndex; i++ ) {
			indexes[i] = i;
		}

		int i,j,mid;
		int n = maxIndex;
		short a;
		int indexA;
		int l = 0;
		int ir = n-1;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					swap(data,indexes,l,ir);
				}
				return indexes[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				swap(data,indexes,mid,lp1);

				if( data[l] > data[ir] ) {
					swap(data,indexes,l,ir);
				}

				if( data[lp1] > data[ir] ) {
					swap(data,indexes,lp1,ir);
				}

				if( data[l] > data[lp1] ) {
					swap(data,indexes,lp1,l);
				}

				i=lp1;
				j=ir;
				a=data[lp1];
				indexA=indexes[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					swap(data,indexes,i,j);
				}
				data[lp1] = data[j];
				data[j] = a;
				indexes[lp1] = indexes[j];
				indexes[j] = indexA;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	private static void swap( short[] data , int []indexes, int a , int b )
	{
		short tempD = data[a];
		int tempI = indexes[a];

		data[a] = data[b];
		indexes[a] = indexes[b];

		data[b] = tempD;
		indexes[b] = tempI;
	}

   /**
	* Sorts the array such that the values in the array up to and including
	* 'k' are sorted from the least to greatest.  This implies that the array
	* itself is modified. For convenience the value of the 'k'th element is returned.
	 *
	 * @param data The unsorted list.  Is modified.
	 * @param k The element of the sorted list that is to be found
	 * @param maxIndex Only element up to this value are considered
	 * @return the 'k'th largest element
	 */
	public static byte select( byte []data , int k , int maxIndex ) {

		int i,j,mid;
		int n = maxIndex;
		byte a;
		int l = 0;
		int ir = n-1;

		byte temp;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}
				return data[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				temp = data[mid];
				data[mid] = data[lp1];
				data[lp1] = temp;

				if( data[l] > data[ir] ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}

				if( data[lp1] > data[ir] ) {
					temp = data[lp1];
					data[lp1] = data[ir];
					data[ir] = temp;
				}

				if( data[l] > data[lp1] ) {
					temp = data[lp1];
					data[lp1] = data[l];
					data[l] = temp;
				}

				i=lp1;
				j=ir;
				a=data[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					temp = data[i];
					data[i] = data[j];
					data[j] = temp;
				}
				data[lp1] = data[j];
				data[j] = a;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	/**
	 * <p>
	 * Returns the original index of the 'k' largest element in the list.
	 * </p>
	 * 
	 * <p>
	 * Note: There is additional overhead since the values of indexes needs to be set
	 * </p>
	 * 
	 * @param indexes Temporary storage and is overwritten
	 */
	public static int selectIndex( byte []data , int k , int maxIndex ,  int []indexes) {

		for( int i = 0; i < maxIndex; i++ ) {
			indexes[i] = i;
		}

		int i,j,mid;
		int n = maxIndex;
		byte a;
		int indexA;
		int l = 0;
		int ir = n-1;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir] < data[l] ) {
					swap(data,indexes,l,ir);
				}
				return indexes[k];
			} else {
				mid = (l+ir) >> 1;

				int lp1 = l+1;
				swap(data,indexes,mid,lp1);

				if( data[l] > data[ir] ) {
					swap(data,indexes,l,ir);
				}

				if( data[lp1] > data[ir] ) {
					swap(data,indexes,lp1,ir);
				}

				if( data[l] > data[lp1] ) {
					swap(data,indexes,lp1,l);
				}

				i=lp1;
				j=ir;
				a=data[lp1];
				indexA=indexes[lp1];

				for(;;) {
					do i++; while(data[i]<a);
					do j--; while (data[j]>a);
					if( j < i) break;
					swap(data,indexes,i,j);
				}
				data[lp1] = data[j];
				data[j] = a;
				indexes[lp1] = indexes[j];
				indexes[j] = indexA;
				if( j >= k ) ir=j-1;
				if( j <= k ) l=i;
			}
		}
	}

	private static void swap( byte[] data , int []indexes, int a , int b )
	{
		byte tempD = data[a];
		int tempI = indexes[a];

		data[a] = data[b];
		indexes[a] = indexes[b];

		data[b] = tempD;
		indexes[b] = tempI;
	}


}
