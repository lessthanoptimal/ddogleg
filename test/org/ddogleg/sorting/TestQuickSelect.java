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

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;


public class TestQuickSelect {
    Random rand = new Random(0xFF);


    /**
     * Creates a random array then sorts it.  It then requests that the specified element be found
     * and compares it against the sorted array.
     */
    @Test
    public void testWithQuickSort() {
        Comparable orig[] = new Comparable[100];
        Comparable copy[] = new Comparable[orig.length];
        Comparable sorted[] = new Comparable[orig.length];

        for( int i = 0; i < orig.length; i++ ) {
            orig[i] = rand.nextDouble();
            sorted[i] = orig[i];
        }

        Arrays.sort(sorted);

        for( int i = 0; i < orig.length; i++ ) {
            System.arraycopy(orig,0,copy,0,orig.length);
            QuickSelect.select(copy,i,copy.length);

            assertEquals(sorted[i],copy[i]);
        }

    }

}