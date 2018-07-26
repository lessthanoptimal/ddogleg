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
public class TrustRegionUpdateDogleg_F64<S extends DMatrix> implements TrustRegionBase_F64.ParameterUpdate<S> {
	// TODO consider more accurate intersection method in paper


	// the trust region instance which is using the update function
	protected TrustRegionBase_F64<S> owner;

	// minimum possible value from function being optimized
	protected double minimumFunctionValue;

	// used to solve positive definite systems
	protected LinearSolver<S,DMatrixRMaj> solver;

	// Gradient's direction
//	protected DMatrixRMaj direction = new DMatrixRMaj(1,1);
	// g'*B*g
	protected double gBg;

	// Solution to Gauss-Newton problem
	protected DMatrixRMaj stepGN = new DMatrixRMaj(1,1);
	protected double distanceGN; // length of GN solution

	protected DMatrixRMaj stepCauchy = new DMatrixRMaj(1,1);

	// is the hessian positive definite?
	protected boolean positiveDefinite;

	double distanceCauchy;

	// work space
	protected S tmp;

	/**
	 * Specifies internal algorithms
	 *
	 * @param solver Solver for positive definite systems
	 */
	public TrustRegionUpdateDogleg_F64(LinearSolver<S, DMatrixRMaj> solver) {
		this.solver = solver;
	}

	protected TrustRegionUpdateDogleg_F64(){}

	@Override
	public void initialize( TrustRegionBase_F64<S> owner , int numberOfParameters , double minimumFunctionValue) {
		this.owner = owner;
		this.minimumFunctionValue = minimumFunctionValue;
//		direction.reshape(numberOfParameters,1);
		stepGN.reshape(numberOfParameters,1);
		stepCauchy.reshape(numberOfParameters,1);
		tmp = owner.math.createMatrix();
	}

	@Override
	public void initializeUpdate() {
		// Scale the gradient vector to make it less likely to overflow/underflow
//		CommonOps_DDRM.divide(owner.gradient,owner.gradientNorm, direction);
		gBg = owner.math.innerProduct(owner.gradient,owner.hessian);

		if(UtilEjml.isUncountable(gBg))
			throw new OptimizationException("Uncountable. gBg="+gBg);

		// see if it's positive definite and Gauss-Newton can be solved
		if( gBg > 0 && solveGaussNewtonPoint(stepGN) ) {
			distanceCauchy = owner.gradientNorm*owner.gradientNorm/gBg;
			positiveDefinite = true;
			// p_gn = -||g||*inv(B)*direction
			CommonOps_DDRM.scale(-1, stepGN);
			distanceGN = NormOps_DDRM.normF(stepGN);
		} else {
			positiveDefinite = false;
		}
	}

	protected boolean solveGaussNewtonPoint(DMatrixRMaj pointGN ) {
		// Compute Gauss-Newton step and make sure the input hessian isn't modified
		S H;
		if( solver.modifiesA() ) {
			tmp.set(owner.hessian);
			H = tmp;
		} else {
			H = owner.hessian;
		}
		if( !solver.setA(H) ) {
			return false;
		}
		solver.solve(owner.gradient.copy(), pointGN); // todo remove copy

		return true;
	}

	@Override
	public boolean computeUpdate(DMatrixRMaj step, double regionRadius) {
		if( positiveDefinite ) {
			// If the GN solution is inside the trust region it should use that solution
			if( distanceGN <= regionRadius ) {
				step.set(stepGN);
				return distanceGN == regionRadius;
			}

			boolean maxStep;

			// of the Gauss-Newton solution is inside the trust region use that
			if( distanceGN <= regionRadius ) {
				step.set(stepGN);
				maxStep = distanceGN == regionRadius;
			} else if( distanceCauchy*owner.gradientNorm >= regionRadius ) {
				// if the trust region comes before the Cauchy point then perform the cauchy step
				maxStep = cauchyStep(regionRadius, step);
			} else {
				combinedStep(regionRadius, step);
				maxStep = true;
			}

			// since the GN solution is outside the region boundary all other solutions must be inside
			return maxStep;
		} else {
			// Cauchy step for negative semi-definite systems
			double tau = Math.min(1, Math.max(0,(owner.fx-minimumFunctionValue)/regionRadius) );
			CommonOps_DDRM.scale(-tau*regionRadius/owner.gradientNorm, owner.gradient,step);
			return tau == 1.0;
		}
	}

	/**
	 * Computes the Cauchy step, truncates it if the regionRadius is less than the optimal step
	 * @param regionRadius
	 * @param step
	 */
	protected boolean cauchyStep(double regionRadius, DMatrixRMaj step) {
		double normRadius = regionRadius/owner.gradientNorm;

		boolean maxStep;
		double dist = distanceCauchy;
		if( dist >= normRadius ) {
			maxStep = true;
			dist = normRadius;
		} else {
			maxStep = false;
		}
		CommonOps_DDRM.scale(-dist, owner.gradient, step);

		return maxStep;
	}

	protected void combinedStep(double regionRadius, DMatrixRMaj step) {
		// find the Cauchy point
		CommonOps_DDRM.scale(-distanceCauchy, owner.gradient, stepCauchy);

		// compute the combined step
		double beta = combinedStep(stepCauchy,stepGN,regionRadius,step);
	}

	/**
	 * Combined step that is a linear interpolation between the cauchy and Gauss-Newton steps.
	 * Returns the 'beta' variable.
	 *
	 * phi(beta) = ||a + beta*(b-1)||^2 - radius^2
	 *
	 * where a = Cauchy and b = Gauss-Newton steps
	 *
	 * @return 'beta' from equation above
	 */
	protected static double combinedStep( DMatrixRMaj stepCauchy , DMatrixRMaj stepGN ,
										  double regionRadius , DMatrixRMaj step ) {
		// c = a'*(b-a)
		double c = 0;
		for( int i = 0; i < stepCauchy.numRows; i++ )
			c += stepCauchy.data[i]*(stepGN.data[i]-stepCauchy.data[i]);

		// solve phi(beta) = ||a + beta*(b-1)||^2 - radius^2

		// bma2 = ||b-a||^2
		// a2 = ||a||^2
		double bma2 = 0;
		double a2 = 0;
		for( int i = 0; i < stepCauchy.numRows; i++ ) {
			double a = stepCauchy.data[i];
			double d = stepGN.data[i]-a;
			bma2 += d*d;
			a2 += a*a;
		}

		double r2 = regionRadius*regionRadius;

		double beta;

		if( c <= 0 )
			beta = (-c+Math.sqrt(c*c + bma2*(r2-a2)))/bma2;
		else
			beta = (r2-a2)/(c+Math.sqrt(c*c + bma2*(r2-a2)));

		// step = a + beta*(b-a)
		step.zero();
		for( int i = 0; i < stepCauchy.numRows; i++ )
			step.data[i] = stepCauchy.data[i] + beta*(stepGN.data[i]-stepCauchy.data[i]);

		return beta;
	}

	/**
	 * Compute the fractional distance from P to GN that it intersects the region's boundary
	 */
	static double fractionToGN( double lengthP , double lengthGN , double lengthPtoGN, double region ) {

		// First triangle has 3 known sides
		double a=lengthGN,b=lengthP,c=lengthPtoGN;

		// Law of cosine to find angle for side GN (a.k.a 'a')
		double cosineA = (a*a - b*b - c*c)/(-2.0*b*c);
		double angleA = Math.acos(cosineA);

		// In the second triangle, that is now being considered, lengthP is known and the side which intersects
		// the region boundary is known, but we need to solve for the length from P to the intersection
		// with the boundary
		a=region;
		double angleB = Math.asin((b/a)*Math.sin(angleA));
		double angleC = Math.PI-angleA-angleB;
		c = Math.sqrt(a*a + b*b - 2.0*a*b*Math.cos(angleC));

		return c/lengthPtoGN;
	}
}
