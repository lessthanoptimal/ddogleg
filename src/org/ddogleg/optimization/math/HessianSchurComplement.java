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

package org.ddogleg.optimization.math;

import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;

/**
 * <P>Given the already computed Jacobian (broken up into a left and right side) compute the decomposed
 * approximate Hessian matrix, i.e. [A B, B D]</P>
 *
 * <pre>
 *     A=L'*L
 *     B=L'*R
 *     D=R*R
 * </pre>
 *
 * <p>Where L and R are the left and right hand side of the Jacobian, respectively</p>
 *
 * @author Peter Abeles
 */
public interface HessianSchurComplement<S extends DMatrix> extends HessianMath
{
	/**
	 * Given the left and right hand side of the Jacobian compute the Hessian.
	 *
	 * @param jacLeft (input) Jacobian left side
	 * @param jacRight (input) Jacobian right side
	 */
	void computeHessian(S jacLeft , S jacRight);

	/**
	 * Computes the gradient given the Jacobian and the residuals.
	 * @param jacLeft (input) Jacobian left side
	 * @param jacRight (input) Jacobian right side
	 * @param residuals (Input) residuals
	 * @param gradient (Output) gradient
	 */
	void computeGradient(S jacLeft , S jacRight ,
						 DMatrixRMaj residuals, DMatrixRMaj gradient);

	/**
	 * Creates a matrix of the same type that this interface can process
	 * @return matrx
	 */
	S createMatrix();
}
