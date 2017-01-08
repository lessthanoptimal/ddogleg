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

import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.MatrixFeatures_R64;
import org.ejml.ops.RandomMatrices_R64;
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
		RowMatrix_F64 H = RandomMatrices_R64.createSymmetric(N,-1,1,rand);
		RowMatrix_F64 s = RandomMatrices_R64.createRandom(N,1,-1,1,rand);
		RowMatrix_F64 y = RandomMatrices_R64.createRandom(N,1,-1,1,rand);
		RowMatrix_F64 tempV0 = new RowMatrix_F64(N,1);
		RowMatrix_F64 tempV1 = new RowMatrix_F64(N,1);

		RowMatrix_F64 expected = H.copy();
		RowMatrix_F64 found = H.copy();

		EquationsBFGS.naiveInverseUpdate(expected, s, y);
		EquationsBFGS.inverseUpdate(found,s,y.copy(),tempV0,tempV1);

		assertTrue(MatrixFeatures_R64.isIdentical(expected, found, 1e-8));
	}


}
