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

import java.util.*;

/**
 * A class that's used to compare the speed of various sorting algorithms.
 */
public class BenchMarkSort
{
	public void process( int num ) {
		double[] data;
		SortableParameter_F64[] obj;
		long before;
		long after;

//        data = createRandom(new Random(0x344),num);
//        before = System.currentTimeMillis();
//        StraightInsertionSort.sort(data);
//        after = System.currentTimeMillis();
//        System.out.println("Straight Insertion = "+(after-before));

		data = createRandom(new Random(0x344), num);
		before = System.currentTimeMillis();
		ShellSort.sort(data);
		after = System.currentTimeMillis();
		System.out.println("Shell = "+(after-before));

		data = createRandom(new Random(0x344), num);
		QuickSort_F64 quicksort = new QuickSort_F64();
		before = System.currentTimeMillis();
		quicksort.sort(data,data.length);
		after = System.currentTimeMillis();
		System.out.println("Quicksort = "+(after-before));

		data = createRandom(new Random(0x344), num);
		int indexes[] = new int[ num ];
		before = System.currentTimeMillis();
		quicksort.sort(data,data.length,indexes);
		after = System.currentTimeMillis();
		System.out.println("Quicksort Indexes = "+(after-before));

		data = createRandom(new Random(0x344), num);
		ApproximateSort_F64 approx = new ApproximateSort_F64(2000);
		before = System.currentTimeMillis();
		approx.computeRange(data, 0, data.length);
//		approx.setRange(-1000,1000);
		approx.sortIndex(data,0,data.length,indexes);
		after = System.currentTimeMillis();
		System.out.println("Approx Indexes = "+(after-before));

		obj = createRandomObj(new Random(0x344), num);
		QuickSortObj_F64 quicksortObj = new QuickSortObj_F64();
		before = System.currentTimeMillis();
		quicksortObj.sort(obj,obj.length);
		after = System.currentTimeMillis();
		System.out.println("QuicksortObj = "+(after-before));

		data = createRandom(new Random(0x344), num);
		before = System.currentTimeMillis();
		Arrays.sort(data);
		after = System.currentTimeMillis();
		System.out.println("Array.sort = "+(after-before));

		List<Foo> temp = createList(new Random(0x344),num);
		Foo[] tempArr = temp.toArray(new Foo[0]);

		before = System.currentTimeMillis();
		Collections.sort(temp);
		after = System.currentTimeMillis();
		System.out.println("Collections.sort = "+(after-before));

		before = System.currentTimeMillis();
		QuickSortComparator<Foo> sortComparator = new QuickSortComparator<Foo>(new FooComparator());
		sortComparator.sort(tempArr,tempArr.length);
		after = System.currentTimeMillis();
		System.out.println("QuickSortComparator = "+(after-before));
	}

	public static List<Double> makeList( double[] data ) {
		List<Double> ret = new ArrayList<Double>(data.length);

		for( Double d : data ) {
			ret.add(d);
		}

		return ret;
	}

	public static double[] createRandom( Random rand , final int num ) {
		double[] ret = new double[ num ];

		for( int i = 0; i < num; i++ ) {
			ret[i] = (rand.nextDouble()-0.5)*2000.0;
		}

		return ret;
	}

	public static SortableParameter_F64[] createRandomObj(Random rand, final int num) {
		SortableParameter_F64[] ret = new SortableParameter_F64[ num ];

		for( int i = 0; i < num; i++ ) {
			ret[i] = new SortableParameter_F64();
			ret[i].sortValue = (rand.nextDouble()-0.5)*2000.0;
		}

		return ret;
	}

	public static int[] createRandom_S32( Random rand , final int num ) {
		int[] ret = new int[ num ];

		for( int i = 0; i < num; i++ ) {
			ret[i] = rand.nextInt(2000)-1000;
		}

		return ret;
	}

	public static List<Foo> createList( Random rand , final int num ) {
		List<Foo> ret = new ArrayList<Foo>(num);

		for( int i = 0; i < num; i++ ) {
			ret.add( new Foo((rand.nextDouble()-0.5)*2000.0));
		}

		return ret;
	}

	private static class Foo implements Comparable<Foo> {

		double value;

		private Foo(double value) {
			this.value = value;
		}

		@Override
		public int compareTo(Foo o) {
			if( value < o.value )
				return -1;
			else if( value > o.value )
				return 1;
			else
				return 0;
		}
	}

	private static class FooComparator implements Comparator<Foo> {

		@Override
		public int compare(Foo o1, Foo o2) {
			if( o1.value < o2.value )
				return -1;
			else if( o1.value > o2.value )
				return 1;
			else
				return 0;
		}
	}

	public static void main( String []args ) {
		BenchMarkSort bench = new BenchMarkSort();

		bench.process(3000000);
	}

}
