/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.solver;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.LinearSolverFactory;
import org.ejml.interfaces.linsol.LinearSolver;

/**
 * <p></p>Fits the coefficients for a quadratic polynomial to a set of even spaced data in an array.</p>
 *
 * <p>y = a*x<sup>2</sup> + b*x + c</p>
 *
 * <p>
 * The coefficients (a,b,c) of the polynomial are found the solving a system of linear
 * equations that minimizes the least squares error.
 * </p>
 *
 * @author Peter Abeles
 */
public class FitQuadratic1D {

	LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.leastSquares(10,3);

	DenseMatrix64F A = new DenseMatrix64F(1,3);
	DenseMatrix64F x = new DenseMatrix64F(3,1);
	DenseMatrix64F y = new DenseMatrix64F(1,1);

	/**
	 * Computes polynomial coefficients for the given data.
	 *
	 * @param length Number of elements in data with relevant data.
	 * @param data Set of observation data.
	 * @return true if successful or false if it fails.
	 */
	public boolean process( int offset , int length , double ...data ) {
		if( data.length < 3 )
			throw new IllegalArgumentException("At least three points");

		A.reshape(data.length,3);
		y.reshape(data.length,1);

		int indexDst = 0;
		int indexSrc = offset;
		for( int i = 0; i < length; i++ ) {
			double d = data[indexSrc++];

			A.data[indexDst++] = i*i;
			A.data[indexDst++] = i;
			A.data[indexDst++] = 1;

			y.data[i] = d;
		}

		if( !solver.setA(A) )
			return false;

		solver.solve(y,x);

		return true;
	}

	/**
	 *
	 * @return The coefficients [a,b,c]
	 */
	public double[] getCoefficients() {
		return x.data;
	}
}
