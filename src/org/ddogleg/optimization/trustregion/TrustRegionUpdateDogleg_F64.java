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
import org.ejml.dense.row.SpecializedOps_DDRM;
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

	// distance to the unconstrained Cauchy point
	double distanceCauchy;

	// work space
	protected S tmp0;
	protected DMatrixRMaj tmp1 = new DMatrixRMaj(1,1);

	// The predicted amount that the quadratic model will be reduced by this step
	double predictedReduction;

	// This is the length of the step f-norm of p
	double stepLength;

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
		tmp0 = owner.math.createMatrix();
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
			positiveDefinite = true;
			// length of the Cauchy step when computed without constraints
			distanceCauchy = owner.gradientNorm*(owner.gradientNorm*owner.gradientNorm/gBg);
			// p_gn = -inv(B)*g
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
			tmp0.set(owner.hessian);
			H = tmp0;
		} else {
			H = owner.hessian;
		}
		if( !solver.setA(H) ) {
			return false;
		}
		DMatrixRMaj B;
		if( solver.modifiesB() ) {
			tmp1.set(owner.gradient);
			B = tmp1;
		} else {
			B = owner.gradient;
		}
		solver.solve(B, pointGN);

		return true;
	}

	@Override
	public void computeUpdate(DMatrixRMaj step, double regionRadius) {
		if( positiveDefinite ) {
			//  If the GN solution is inside the trust region it should use that solution
			if( distanceGN <= regionRadius ) {
				step.set(stepGN);
				predictedReduction = owner.computePredictedReduction(stepGN);
				stepLength = distanceGN;
			} else if( distanceCauchy >= regionRadius ) {
				// if the trust region comes before the Cauchy point then perform the cauchy step
				cauchyStep(regionRadius, step);
			} else {
				// the solution lies on the line connecting Cauchy and GN
				combinedStep(regionRadius, step);
			}

		} else {
			// Cauchy step for negative semi-definite systems
			stepLength = Math.min(regionRadius, Math.max(0,(owner.fx-minimumFunctionValue)) );
			CommonOps_DDRM.scale(-stepLength/owner.gradientNorm, owner.gradient,step);
			double f = stepLength/owner.gradientNorm;
			predictedReduction = stepLength*owner.gradientNorm - 0.5*f*f*gBg;
		}
	}

	@Override
	public double getPredictedReduction() {
		return predictedReduction;
	}

	@Override
	public double getStepLength() {
		return stepLength;
	}

	/**
	 * Computes the Cauchy step, This is only called if the Cauchy point lies after or on the trust region
	 * @param regionRadius (Input) Trust region size
	 * @param step (Output) The step
	 */
	protected void cauchyStep(double regionRadius, DMatrixRMaj step) {

		double gn = owner.gradientNorm;
		double dist = regionRadius/gn;
		CommonOps_DDRM.scale(-dist, owner.gradient, step);
		stepLength = regionRadius; // it touches the trust region
		predictedReduction = dist*gn*gn - 0.5*dist*dist*gBg;
	}

	protected void combinedStep(double regionRadius, DMatrixRMaj step) {
		// find the Cauchy point
		CommonOps_DDRM.scale(-distanceCauchy/owner.gradientNorm, owner.gradient, stepCauchy);
		stepLength = regionRadius; // touches the trust region

		double distancePtoGN = SpecializedOps_DDRM.diffNormF(stepCauchy,stepGN);

		double f = fractionCauchyToGN(distanceCauchy,distanceGN,distancePtoGN,regionRadius);

		CommonOps_DDRM.add(1-f,stepCauchy,f,stepGN,step);
		predictedReduction = owner.computePredictedReduction(step);
	}

	/**
	 * Compute the fractional distance from P to GN where the point intersects the region's boundary
	 */
	static double fractionCauchyToGN(double lengthCauchy , double lengthGN , double lengthPtoGN, double region ) {

		// First triangle has 3 known sides
		double a=lengthGN,b=lengthCauchy,c=lengthPtoGN;

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
