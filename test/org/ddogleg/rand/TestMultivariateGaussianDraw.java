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

package org.ddogleg.rand;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestMultivariateGaussianDraw
{
    public static int N = 6000;

    /**
     * Do a lot of draws on the distribution and see if a similar distribution is computed
     * in the end.
     */
    @Test
    public void testStatistics() {
        DMatrixRMaj orig_x = new DMatrixRMaj(new double[][]{{4},{-2}});
        DMatrixRMaj orig_P = new DMatrixRMaj(new double[][]{{6,-2},{-2,10}});

        MultivariateGaussianDraw dist = new MultivariateGaussianDraw(new Random(0xfeed),orig_x,orig_P);

        DMatrixRMaj draws[] = new DMatrixRMaj[N];

        // sample the distribution
        for( int i = 0; i < N; i++ ) {
            DMatrixRMaj x = new DMatrixRMaj(2,1);
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
        DMatrixRMaj comp_P = new DMatrixRMaj(2,2);
        DMatrixRMaj temp = new DMatrixRMaj(2,1);

        for( int i = 0; i < N; i++ ) {
            temp.set(0,0,draws[i].get(0,0)-raw_comp_x[0]);
            temp.set(1,0,draws[i].get(1,0)-raw_comp_x[1]);

            CommonOps_DDRM.multAddTransB(temp,temp,comp_P);
        }

        CommonOps_DDRM.scale(1.0/N,comp_P);

        MatrixFeatures_DDRM.isIdentical(comp_P,orig_P,0.3);
    }

}
