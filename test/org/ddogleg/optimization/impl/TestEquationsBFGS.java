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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
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
		DMatrixRMaj H = RandomMatrices_DDRM.symmetric(N,-1,1,rand);
		DMatrixRMaj s = RandomMatrices_DDRM.rectangle(N,1,-1,1,rand);
		DMatrixRMaj y = RandomMatrices_DDRM.rectangle(N,1,-1,1,rand);
		DMatrixRMaj tempV0 = new DMatrixRMaj(N,1);
		DMatrixRMaj tempV1 = new DMatrixRMaj(N,1);

		DMatrixRMaj expected = H.copy();
		DMatrixRMaj found = H.copy();

		EquationsBFGS.naiveInverseUpdate(expected, s, y);
		EquationsBFGS.inverseUpdate(found,s,y.copy(),tempV0,tempV1);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found, 1e-8));
	}


}
