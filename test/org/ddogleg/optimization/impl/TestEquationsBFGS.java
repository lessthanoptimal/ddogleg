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

package org.ddogleg.optimization.impl;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestEquationsBFGS {
	
	Random rand = new Random(234);

	@Test
	public void inverseUpdate() {
		int N = 6;
		DenseMatrix64F H = RandomMatrices_D64.createSymmetric(N,-1,1,rand);
		DenseMatrix64F s = RandomMatrices_D64.createRandom(N,1,-1,1,rand);
		DenseMatrix64F y = RandomMatrices_D64.createRandom(N,1,-1,1,rand);
		DenseMatrix64F tempV0 = new DenseMatrix64F(N,1);
		DenseMatrix64F tempV1 = new DenseMatrix64F(N,1);

		DenseMatrix64F expected = H.copy();
		DenseMatrix64F found = H.copy();

		EquationsBFGS.naiveInverseUpdate(expected, s, y);
		EquationsBFGS.inverseUpdate(found,s,y.copy(),tempV0,tempV1);

		assertTrue(MatrixFeatures_D64.isIdentical(expected, found, 1e-8));
	}


}
