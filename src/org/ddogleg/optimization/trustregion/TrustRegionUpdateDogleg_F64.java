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

package org.ddogleg.optimization.trustregion;

import org.ddogleg.optimization.OptimizationException;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.ejml.interfaces.linsol.LinearSolver;

/**
 * <p>
 * Approximates the optimal solution to the Trust Region's sub-problem using a piecewise linear approach [1,2].
 * The first part of the "dogleg" is a line along the steepest descent line. The second part moves towards the
 * solution to unconstrained sub-problem, a.k.a. Gauss-Newton solution. Positive-definite Hessians are handled
 * using standard equations. Negative semi-definite systems will take a Cauchy step to avoid blowing up, see [2].
 * A more elegant solution to negative definite systems is outline in [1] but hasn't been implemented yet due
 * to complexity.
 * </p>
 *
 * <ul>
 * <li>[1] Jorge Nocedal,and Stephen J. Wright "Numerical Optimization" 2nd Ed. Springer 2006</li>
 * <li>[2] Peter Abeles, "DDogleg Technical Report: Nonlinear Optimization R1", July 2018</li>
 * </ul>
 *
 * @author Peter Abeles
 */
public abstract class TrustRegionUpdateDogleg_F64<S extends DMatrix> implements TrustRegionBase_F64.ParameterUpdate<S> {

	// the trust region instance which is using the update function
	private TrustRegionBase_F64<S> owner;

	// minimum possible value from function being optimized
	private double minimumFunctionValue;

	// used to solve positive definite systems
	LinearSolver<S,DMatrixRMaj> solver;

	// Gradient's direction
	private DMatrixRMaj direction = new DMatrixRMaj(1,1);
	// g'*B*g
	private double gBg;

	// Solution to Gauss-Newton problem
	DMatrixRMaj pointGN = new DMatrixRMaj(1,1);
	double gn_length; // length of GN solution

	// is the hessian positive definite?
	boolean positiveDefinite;

	/**
	 * Specifies internal algorithms
	 *
	 * @param solver Solver for positive definite systems
	 */
	public TrustRegionUpdateDogleg_F64(LinearSolver<S, DMatrixRMaj> solver) {
		this.solver = solver;
	}

	@Override
	public void initialize( TrustRegionBase_F64<S> owner , int numberOfParameters , double minimumFunctionValue) {
		this.owner = owner;
		this.minimumFunctionValue = minimumFunctionValue;
		direction.reshape(numberOfParameters,1);
		pointGN.reshape(numberOfParameters,1);
	}

	@Override
	public void initializeUpdate() {
		// Scale the gradient vector to make it less likely to overflow/underflow
		CommonOps_DDRM.divide(owner.gradient,owner.gradientNorm, direction);
		gBg = owner.math.innerProduct(direction,owner.hessian);

		if(UtilEjml.isUncountable(gBg))
			throw new OptimizationException("Uncountable. gBg="+gBg);

		positiveDefinite = gBg > 0;

		if( positiveDefinite ) {
			// Compute Gauss-Newton step
			solveGaussNewtonPoint(pointGN);
			CommonOps_DDRM.scale(-1, pointGN);
			gn_length = NormOps_DDRM.normF(pointGN);
		}
	}

	protected void solveGaussNewtonPoint(DMatrixRMaj pointGN ) {
		// Compute Gauss-Newton step
		if( !solver.setA(owner.hessian) ) {
			throw new OptimizationException("Solver failed!");
		}
		solver.solve(owner.gradient, pointGN);
	}

	@Override
	public boolean computeUpdate(DMatrixRMaj p, double regionRadius) {
		if( positiveDefinite ) {
			// If the GN solution is inside the trust region it should use that solution
			if( gn_length <= regionRadius ) {
				p.set(pointGN);
				return gn_length == regionRadius;
			}

			// Does the pu point lie inside the trust region?
			double pu_length = owner.gradientNorm/gBg;
			if( pu_length < regionRadius ) {
				// Compute point 'pn'
				// pu = -((g'*g)/(g'*B*g))*g
				// Since g has been normalized g'*g = 1
				// this undoes the change in scale
				CommonOps_DDRM.scale(-owner.gradientNorm/gBg, direction,p);
				// vector from p to GN in p
				CommonOps_DDRM.subtract(pointGN,p,p);
				double length_p_to_gn = NormOps_DDRM.normF(p);

				// it does, so find the intersection of the second segment with the region's boundary
				double f = fractionToGN(pu_length, gn_length,length_p_to_gn,regionRadius);

				// starting from GN instead of P because P has been over written
				CommonOps_DDRM.add(pointGN,1.0-f,p,p);
			} else {
				// find location that the first segment hits the region's boundary
				// this is easy since direction is a unit vector
				CommonOps_DDRM.scale(regionRadius, direction,p);
			}

			// since the GN solution is outside the region boundary all other solutions must be inside
			return true;
		} else {
			// Cauchy step for negative semi-definite systems
			double tau = Math.min(1, Math.max(0,(owner.fx-minimumFunctionValue)/regionRadius) );
			CommonOps_DDRM.scale(tau*regionRadius, direction,p);
			return true;
		}
	}

	/**
	 * Compute the fractional distance from P to GN that it intersects the region's boundary
	 */
	static double fractionToGN( double lengthP , double lengthGN , double lengthPtoGN, double region ) {

		// First triangle has 3 known sides
		double a=lengthGN,b=lengthP,c=lengthPtoGN;

		// Law of cosine to find angle for side GN (a.k.a 'a')
		double cosineA = (a*a + b*b + c*c)/(2.0*a*b);

		// In the second triangle, that is now being considered, lengthP is known and the side which intersects
		// the region boundary is known, but we need to solve for the length from P to the intersection
		// with the boundary
		a=region;
		c=Math.sqrt(a*a + b*b -2.0*a*b*cosineA);

		return c/lengthPtoGN;
	}
}
