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

import org.ejml.UtilEjml;
import org.ejml.alg.dense.mult.VectorVectorMult_D64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.NormOps_D64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestCauchyStep {

	RowMatrix_F64 J,x,residuals,gradient;

	public TestCauchyStep() {
		J = new RowMatrix_F64(3,2,true,1,0,0,Math.sqrt(2),0,0);

		x = new RowMatrix_F64(2,1,true,0.5,1.5);
		residuals = new RowMatrix_F64(3,1,true,-1,-2,-3);

		gradient = new RowMatrix_F64(2,1);
		CommonOps_D64.multTransA(J, residuals, gradient);
	}
	/**
	 * The optimal solution falls inside the trust region
	 */
	@Test
	public void computeStep_inside() {
		CauchyStep alg = new CauchyStep();
		alg.init(2,3);
		alg.setInputs(x,residuals,J,gradient,-1);
		
		RowMatrix_F64 step = new RowMatrix_F64(2,1);
		
		alg.computeStep(10,step);
		
		// empirical test to see if it is a local minimum
		double a =  cost(residuals,J,step,0);
		double b =  cost(residuals, J, step, 0.01);
		double c =  cost(residuals,J,step,-0.01);

		assertTrue(a < b);
		assertTrue(a < c);
	}
	
	public static double cost( RowMatrix_F64 residuals , RowMatrix_F64 J , RowMatrix_F64 h , double delta )
	{
		// adjust the value of h along the gradient's direction
		RowMatrix_F64 direction = h.copy();
		CommonOps_D64.scale(1.0/ NormOps_D64.normF(h),direction);
		
		h = h.copy();
		for( int i = 0; i < h.numRows; i++ )
			h.data[i] += delta*direction.data[i];
		
		RowMatrix_F64 B = new RowMatrix_F64(J.numCols,J.numCols);
		CommonOps_D64.multTransA(J,J,B);

		double left = VectorVectorMult_D64.innerProd(residuals, residuals);
		double middle = VectorVectorMult_D64.innerProdA(residuals, J, h);
		double right = VectorVectorMult_D64.innerProdA(h, B, h);

//		double cost =  0.5*left + middle + 0.5*right;
//
//		SimpleMatrix _r = SimpleMatrix.wrap(residuals);
//		SimpleMatrix _J = SimpleMatrix.wrap(J);
//		SimpleMatrix _h = SimpleMatrix.wrap(h);
//
//		double v = _r.plus(_J.mult(_h)).normF();
//
//		double alt = 0.5*v*v;
//
//		if( Math.abs(alt-cost) > 1e-8 )
//			throw new RuntimeException("Oh crap");
		
		return 0.5*left + middle + 0.5*right;
	}

	/**
	 * The optimal solution falls outside the trust region
	 */
	@Test
	public void computeStep_outside() {

		CauchyStep alg = new CauchyStep();
		alg.init(2,3);
		alg.setInputs(x,residuals,J,gradient,-1);

		RowMatrix_F64 step = new RowMatrix_F64(2,1);

		alg.computeStep(1,step);

		// make sure it on he trust region border
		double l = NormOps_D64.normF(step);
		assertTrue(Math.abs(l - 1) <= UtilEjml.EPS);
		
		// empirical test to see if it is a local minimum
		double a =  cost(residuals,J,step,0);
		double c =  cost(residuals,J,step,-0.01);

		assertTrue(a < c);
	}

	/**
	 * Check predicted reduction against direct computation
	 */
	@Test
	public void predictedReduction_inside() {
		CauchyStep alg = new CauchyStep();
		alg.init(2,3);
		alg.setInputs(x,residuals,J,gradient,-1);

		RowMatrix_F64 step = new RowMatrix_F64(2,1);

		alg.computeStep(10,step);

		// empirical calculation of the reduction
		double a =  VectorVectorMult_D64.innerProd(residuals,residuals)*0.5;
		double c =  cost(residuals,J,step,0);

		assertEquals(a-c,alg.predictedReduction(),1e-8);
	}

	/**
	 * Check predicted reduction against direct computation
	 */
	@Test
	public void predictedReduction_outside() {
		CauchyStep alg = new CauchyStep();
		alg.init(2,3);
		alg.setInputs(x,residuals,J,gradient,-1);

		RowMatrix_F64 step = new RowMatrix_F64(2,1);

		alg.computeStep(1,step);

		// empirical calculation of the reduction
		double a =  VectorVectorMult_D64.innerProd(residuals,residuals)*0.5;
		double c =  cost(residuals,J,step,0);

		assertEquals(a-c,alg.predictedReduction(),1e-8);
	}
}
