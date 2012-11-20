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

package org.ddogleg.sorting;

import java.util.*;

/**
 * A class that's used to compare the speed of various sorting algorithms.
 */
public class BenchMarkSort
{
    public void process( int num ) {
        double[] data;
        long before;
        long after;

//        data = createRandom(new Random(0x344),num);
//        before = System.currentTimeMillis();
//        StraightInsertionSort.sort(data);
//        after = System.currentTimeMillis();
//        System.out.println("Straight Insertion = "+(after-before));

        data = createRandom(new Random(0x344),num);
        before = System.currentTimeMillis();
        ShellSort.sort(data);
        after = System.currentTimeMillis();
        System.out.println("Shell = "+(after-before));

        data = createRandom(new Random(0x344),num);
        QuickSort_F64 quicksort = new QuickSort_F64();
        before = System.currentTimeMillis();
        quicksort.sort(data,data.length);
        after = System.currentTimeMillis();
        System.out.println("Quicksort = "+(after-before));

        data = createRandom(new Random(0x344),num);
        before = System.currentTimeMillis();
        Arrays.sort(data);
        after = System.currentTimeMillis();
        System.out.println("Array.sort = "+(after-before));

        List<Double> temp = createList(new Random(0x344),num);
        before = System.currentTimeMillis();
        Collections.sort(temp);
        after = System.currentTimeMillis();
        System.out.println("Collections.sort = "+(after-before));


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

    public static List<Double> createList( Random rand , final int num ) {
        List<Double> ret = new ArrayList<Double>(num);

        for( int i = 0; i < num; i++ ) {
            ret.add((rand.nextDouble()-0.5)*2000.0);
        }

        return ret;
    }

    public static void main( String []args ) {
        BenchMarkSort bench = new BenchMarkSort();

        bench.process(1000000);
    }

}
