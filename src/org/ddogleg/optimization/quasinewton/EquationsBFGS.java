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

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.mult.VectorVectorMult_DDRM;

/**
 * <p>
 * Equations for updating the approximate Hessian matrix using BFGS equations.
 * </p>
 *
 * <p>Forward:</p>
 * <pre>
 *  B(k+1) = B(k) + [B(k)*s*s'*B(k)]/[s'*B*s]
 *                + y*y'/[y'*s]
 * </pre>
 *
 * <p>Inverse:</p>
 * <pre>
 *  H(k+1) = (I-p*s*y')*H(k)*(I-p*y*s') + p*s*s'
 * </pre>
 *
 * <ul>
 * <li>B = symmetric positive definite forward n by n matrix.</li>
 * <li>H = symmetric positive definite inverse n by n matrix.</li>
 * <li>s = x(k+1)-x(k) vector change in state.</li>
 * <li>y = x'(k+1)-x'(k) vector change in gradient.</li>
 * <li>p = 1/(y'*s) {@code >} 0</li>
 * </ul>
 *
 *
 * @author Peter Abeles
 */
public class EquationsBFGS {

	/**
	 * DFP Hessian update equation. See class description for equations
	 *
	 * @param H symmetric inverse matrix being updated
	 * @param s change in state (new - old)
	 * @param y change in gradient (new - old)
	 * @param tempV0 Storage vector
	 */
	public static void update(DMatrixRMaj H ,
							  DMatrixRMaj s ,
							  DMatrixRMaj y ,
							  DMatrixRMaj tempV0, DMatrixRMaj tempV1) {
		double p = VectorVectorMult_DDRM.innerProd(y,s);
		if( p == 0 )
			return;

		p = 1.0/p;

		double sBs = VectorVectorMult_DDRM.innerProdA(s,H,s);
		if( sBs == 0 )
			return;

		CommonOps_DDRM.mult(H,s,tempV0);
		CommonOps_DDRM.multTransA(s,H,tempV1);

		VectorVectorMult_DDRM.rank1Update(-p, H , tempV0, y);
		VectorVectorMult_DDRM.rank1Update(-p, H , y, tempV1);
		VectorVectorMult_DDRM.rank1Update(p*(p*sBs+1), H , y, y);
	}

	/**
	 * BFGS inverse hessian update equation that orders the multiplications to minimize the number of operations.
	 *
	 * @param H symmetric inverse matrix being updated
	 * @param s change in state
	 * @param y change in gradient
	 * @param tempV0 Storage vector of length N
	 * @param tempV1 Storage vector of length N
	 */
	public static void inverseUpdate( DMatrixRMaj H , DMatrixRMaj s , DMatrixRMaj y ,
									  DMatrixRMaj tempV0, DMatrixRMaj tempV1)
	{
		double alpha = VectorVectorMult_DDRM.innerProdA(y,H,y);
		double p = 1.0/VectorVectorMult_DDRM.innerProd(s,y);

		CommonOps_DDRM.mult(H,y,tempV0);
		CommonOps_DDRM.multTransA(y, H, tempV1);

		VectorVectorMult_DDRM.rank1Update(-p, H , tempV0, s);
		VectorVectorMult_DDRM.rank1Update(-p, H , s, tempV1);
		VectorVectorMult_DDRM.rank1Update(p*alpha*p+p, H , s, s);
	}

	/**
	 *
	 *
	 * <p>
	 * [1] D. Byatt and I. D. Coope and C. J. Price, "Effect of limited precision on the BFGS quasi-Newton algorithm"
	 * Proc. of 11th Computational Techniques and Applications Conference CTAC-2003
	 * </p>
	 *
	 * @param C
	 * @param d
	 * @param y
	 * @param tempV0
	 */
	public static void conjugateUpdateD( DMatrixRMaj C , DMatrixRMaj d , DMatrixRMaj y ,
										 double step, DMatrixRMaj tempV0 )
	{
		DMatrixRMaj z = tempV0;

		CommonOps_DDRM.multTransA(C, y, z);
		
		double dTd = VectorVectorMult_DDRM.innerProd(d,d);
		double dTz = VectorVectorMult_DDRM.innerProd(d,z);
		
		double middleScale = -dTd/dTz;
		double rightScale = dTd/Math.sqrt(-dTd*dTz/step);
		
		int N = d.getNumElements();
		for( int i = 0; i < N; i++ ) {
			d.data[i] += middleScale*z.data[i] + rightScale*d.data[i];
		}
	}

	/**
	 *
	 *
	 * <p>
	 * [1] D. Byatt and I. D. Coope and C. J. Price, "Effect of limited precision on the BFGS quasi-Newton algorithm"
	 * Proc. of 11th Computational Techniques and Applications Conference CTAC-2003
	 * </p>
	 *
	 * @param C
	 * @param d
	 * @param y
	 * @param tempV0
	 */
	public static void conjugateUpdateC( DMatrixRMaj C , DMatrixRMaj d , DMatrixRMaj y ,
										 double step, DMatrixRMaj tempV0 , DMatrixRMaj tempV1)
	{
		DMatrixRMaj z = tempV0;
		DMatrixRMaj d_bar = tempV1;

		CommonOps_DDRM.multTransA(C,y,z);

		double dTd = VectorVectorMult_DDRM.innerProd(d,d);
		double dTz = VectorVectorMult_DDRM.innerProd(d,z);

		double middleScale = -dTd/dTz;
		double rightScale = dTd/Math.sqrt(-dTd*dTz/step);

		int N = d.getNumElements();
		for( int i = 0; i < N; i++ ) {
			d.data[i] += middleScale*z.data[i] + rightScale*d.data[i];
		}
	}
}
