/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchMarkSelect {
    public void process( int num ) {
        double[] data;
        Comparable[] compData;
        int[] indexes;
        long before;
        long after;

//        data = createRandom(new Random(0x344),num);
//        before = System.currentTimeMillis();
//        StraightInsertionSort.sort(data);
//        after = System.currentTimeMillis();
//        System.out.println("Straight Insertion = "+(after-before));

        data = createRandom(new Random(0x344),num);
        compData = new Comparable[data.length];
        for( int i = 0; i < compData.length; i++ ) {
            compData[i] = data[i];
        }
        indexes = new int[ data.length ];

        before = System.currentTimeMillis();
        QuickSelect.select(data,13,data.length);
        after = System.currentTimeMillis();
        System.out.println("Select double = "+(after-before));

        data = createRandom(new Random(0x344),num);
        before = System.currentTimeMillis();
        QuickSelect.selectIndex(data,13,data.length,indexes);
        after = System.currentTimeMillis();
        System.out.println("Select double with indexes = "+(after-before));

        before = System.currentTimeMillis();
        QuickSelect.select(compData,13,data.length);
        after = System.currentTimeMillis();
        System.out.println("Select comparable = "+(after-before));
    }

    public static double[] createRandom( Random rand , final int num ) {
        double[] ret = new double[ num ];

        for( int i = 0; i < num; i++ ) {
            ret[i] = (rand.nextDouble()-0.5)*2000.0;
        }

        return ret;
    }

    public static void main( String []args ) {
        BenchMarkSelect bench = new BenchMarkSelect();

        bench.process(10000000);
    }
}
