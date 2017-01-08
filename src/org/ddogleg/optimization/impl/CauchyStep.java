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

import org.ejml.alg.dense.mult.VectorVectorMult_R64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_R64;
import org.ejml.ops.NormOps_R64;

/**
 * <p>
 * Selects the optimal point along the gradient line within the trust region's constraint.
 * </p>
 *
 * <p>
 * The negative definite case is not considered because it is impossible when the Hessian is
 * approximated by squaring the Jacobian.  For a matrix to be negative definite there must be a
 * vector 'x' which will produce a negative result:<br>
 * {@code x'*H*x < 0  --> x'*J'*J*x --> (J*x)'*(J*x)}<br>
 * which is clearly always &ge; 0
 * </p>
 *
 * @author Peter Abeles
 */
public class CauchyStep implements TrustRegionStep {

	// square of the Jacobian
	private RowMatrix_F64 B = new RowMatrix_F64(1,1);
	private RowMatrix_F64 gradient;


	private double gBg;
	private double gnorm;

	private boolean maxStep;

	private double predicted;

	public void init( int numParam , int numFunctions ) {
		B.reshape(numParam,numParam);
	}

	@Override
	public void setInputs(  RowMatrix_F64 x , RowMatrix_F64 residuals , RowMatrix_F64 J ,
							RowMatrix_F64 gradient , double fx )
	{
		this.gradient = gradient;
		CommonOps_R64.multInner(J, B);

		gBg = VectorVectorMult_R64.innerProdA(gradient, B, gradient);
		gnorm = NormOps_R64.normF(gradient);
	}

	/**
	 *
	 * Computes the Cauchy step.  See comment in class description for why negative definite case
	 * is not considered.
	 *
	 * @param regionRadius
	 * @param step
	 */
	@Override
	public void computeStep( double regionRadius , RowMatrix_F64 step) {

		double dist;

		double normRadius = regionRadius/gnorm;

		if( gBg == 0 ) {
			dist = normRadius;
			maxStep = true;
		} else {
			// find the distance of the minimum point
			dist = gnorm*gnorm/gBg;
			// use the border or dist, which ever is closer
			if( dist >= normRadius ) {
				maxStep = true;
				dist = normRadius;
			} else {
				maxStep = false;
			}
		}

		CommonOps_R64.scale(-dist,gradient,step);

		// compute predicted reduction
		predicted = dist*gnorm*gnorm - 0.5*dist*dist*gBg;
	}

	@Override
	public double predictedReduction() {
		return predicted;
	}

	@Override
	public boolean isMaxStep() {
		return maxStep;
	}
}
