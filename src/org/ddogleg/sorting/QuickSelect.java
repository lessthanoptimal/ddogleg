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
 * An implementation of the quick select algorithm from Numerical Recipes Third Edition
 * that is specified for arrays of doubles.  See page 433.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class QuickSelect {

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
	public static Comparable select( Comparable[]data , int k , int maxIndex ) {

		int i,j,mid;
		int n = maxIndex;
		Comparable a;
		int l = 0;
		int ir = n-1;

		Comparable temp;

		for(;;) {
			if( ir <= l+1 ) {
				if( ir == l+1 && data[ir].compareTo(data[l]) < 0 ) {
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

				if( data[l].compareTo(data[ir]) > 0 ) {
					temp = data[l];
					data[l] = data[ir];
					data[ir] = temp;
				}

				if( data[lp1].compareTo( data[ir] ) > 0 ) {
					temp = data[lp1];
					data[lp1] = data[ir];
					data[ir] = temp;
				}

				if( data[l].compareTo(data[lp1]) > 0 ) {
					temp = data[lp1];
					data[lp1] = data[l];
					data[l] = temp;
				}

				i=lp1;
				j=ir;
				a=data[lp1];

				for(;;) {
					do i++; while(data[i].compareTo(a) < 0);
					do j--; while (data[j].compareTo(a) > 0);
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
}