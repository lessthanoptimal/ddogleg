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

package org.ddogleg.optimization.quasinewton;

import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.MatrixFeatures_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.equation.Equation;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestEquationsBFGS {
	
	Random rand = new Random(234);

	@Test
	public void update() {
		Equation eq = new Equation();
		eq.process("H=rand(5,5)");
		eq.process("y=rand(5,1)");
		eq.process("s=rand(5,1)");
		eq.process("tmp=y'*s");
		eq.process("p=1.0/tmp(0,0)");
		eq.process("I=eye(5)");
		eq.process("H_k= (I - p*y*s')*H*(I - p*s*y') + p*y*y'");

		DMatrixRMaj H = eq.lookupDDRM("H");
		DMatrixRMaj y = eq.lookupDDRM("y");
		DMatrixRMaj s = eq.lookupDDRM("s");
		DMatrixRMaj H_k = eq.lookupDDRM("H_k");

		DMatrixRMaj found = H.copy();
		EquationsBFGS.update(found,s,y,
				new DMatrixRMaj(1,1),new DMatrixRMaj(1,1));


		assertTrue(MatrixFeatures_DDRM.isIdentical(H_k,found, UtilEjml.TEST_F64));
	}

	/**
	 * Naive but easy to visually verify implementation of the inverse BFGS update.  Primarily
	 * for testing purposes.
	 *
	 * @param H inverse matrix being updated
	 * @param s change in state
	 * @param y change in gradient
	 */
	public static void naiveInverseUpdate(DMatrixRMaj H,
										  DMatrixRMaj s,
										  DMatrixRMaj y)
	{
		SimpleMatrix _y = new SimpleMatrix(y);
		SimpleMatrix _s = new SimpleMatrix(s);
		SimpleMatrix B = new SimpleMatrix(H);
		SimpleMatrix I = SimpleMatrix.identity(_y.getNumElements());

		double p = 1.0/_y.dot(_s);

		SimpleMatrix A1 = I.minus(_s.mult(_y.transpose()).scale(p));
		SimpleMatrix A2 = I.minus(_y.mult(_s.transpose()).scale(p));
		SimpleMatrix SS = _s.mult(_s.transpose()).scale(p);
		SimpleMatrix M = A1.mult(B).mult(A2).plus(SS);

		H.set(M.getMatrix());
	}

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

		naiveInverseUpdate(expected, s, y);
		EquationsBFGS.inverseUpdate(found,s,y.copy(),tempV0,tempV1);

		assertTrue(MatrixFeatures_DDRM.isIdentical(expected, found, 1e-8));
	}


}
