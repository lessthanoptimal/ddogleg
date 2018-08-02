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

/**
 * <p>
 * The Cauchy point is an approximate solution to the Trust Region subproblem. It's direction is found by solving
 * a linear version of the subproblem and he point's location along the direction is found by minimizing the sub
 * problem along the direction.
 * </p>
 *
 * <p>The Hessian does not need to be invertible making it more robust. All though the convergence is slower
 * than other approaches.</p>
 *
 * @author Peter Abeles
 */
public class TrustRegionUpdateCauchy_F64<S extends DMatrix> implements TrustRegionBase_F64.ParameterUpdate<S>
{
	// the trust region instance which is using the update function
	private TrustRegionBase_F64<S> owner;

	// minimum possible value from function being optimized
	private double minimumFunctionValue;

	// direction of the gradient
	DMatrixRMaj direction = new DMatrixRMaj(1,1);
	// g'*B*g
	double gBg;

	// The predicted amount that the quadratic model will be reduced by this step
	double predictedReduction;

	// This is the length of the step f-norm of p
	double stepLength;

	boolean verbose=false;

	@Override
	public void initialize( TrustRegionBase_F64<S> owner , int numberOfParameters , double minimumFunctionValue) {
		this.owner = owner;
		this.minimumFunctionValue = minimumFunctionValue;
		direction.reshape(numberOfParameters,1);
	}

	@Override
	public void initializeUpdate() {
		// use the direction instead of gradient for reduced overflow/underflow issues
		CommonOps_DDRM.divide(owner.gradient,owner.gradientNorm,direction);
		gBg = owner.math.innerProductVectorMatrix(direction,owner.hessian);

		if(UtilEjml.isUncountable(gBg))
			throw new OptimizationException("Uncountable. gBg="+gBg);
	}

	@Override
	public void computeUpdate(DMatrixRMaj step, double regionRadius) {
		double gnorm = owner.gradientNorm;

		if( gBg <= 0 ) {
			if( verbose )
				System.out.println("  not-positive definite. dBd <= 0");
			// always decreasing so take the largest possible step to the region's boundary
			// At the same time don't try to jump past the smallest possible value for the function
			stepLength = regionRadius;
		} else {
			if( verbose )
				System.out.println("  normal step");
			// find the distance of the minimum point
			stepLength = Math.min(regionRadius,gnorm/gBg);
		}

		CommonOps_DDRM.scale(-stepLength,direction,step);

		// compute predicted reduction
		predictedReduction = stepLength*(owner.gradientNorm - 0.5*stepLength*gBg);

	}

	@Override
	public double getPredictedReduction() {
		return predictedReduction;
	}

	@Override
	public double getStepLength() {
		return stepLength;
	}

	@Override
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}
