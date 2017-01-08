/*
 * Copyright (c) 2012-2017, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.rand;

import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.MatrixFeatures_R64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;


public class TestMultivariateGaussianDraw
{
    public static int N = 6000;

    /**
     * Do a lot of draws on the distribution and see if a similar distribution is computed
     * in the end.
     */
    @Test
    public void testStatistics() {
        RowMatrix_F64 orig_x = new RowMatrix_F64(new double[][]{{4},{-2}});
        RowMatrix_F64 orig_P = new RowMatrix_F64(new double[][]{{6,-2},{-2,10}});

        MultivariateGaussianDraw dist = new MultivariateGaussianDraw(new Random(0xfeed),orig_x,orig_P);

        RowMatrix_F64 draws[] = new RowMatrix_F64[N];

        // sample the distribution
        for( int i = 0; i < N; i++ ) {
            RowMatrix_F64 x = new RowMatrix_F64(2,1);
            draws[i] = dist.next(x);
        }

        // compute the statistics
        double raw_comp_x[] = new double[2];

        // find the mean
        for( int i = 0; i < N; i++ ) {
            raw_comp_x[0] += draws[i].get(0,0);
            raw_comp_x[1] += draws[i].get(1,0);
        }

        raw_comp_x[0] /= N;
        raw_comp_x[1] /= N;

        assertEquals(4,raw_comp_x[0],0.1);
        assertEquals(-2.0,raw_comp_x[1],0.1);

        // now the covariance
        RowMatrix_F64 comp_P = new RowMatrix_F64(2,2);
        RowMatrix_F64 temp = new RowMatrix_F64(2,1);

        for( int i = 0; i < N; i++ ) {
            temp.set(0,0,draws[i].get(0,0)-raw_comp_x[0]);
            temp.set(1,0,draws[i].get(1,0)-raw_comp_x[1]);

            CommonOps_R64.multAddTransB(temp,temp,comp_P);
        }

        CommonOps_R64.scale(1.0/N,comp_P);

        MatrixFeatures_R64.isIdentical(comp_P,orig_P,0.3);
    }

}
