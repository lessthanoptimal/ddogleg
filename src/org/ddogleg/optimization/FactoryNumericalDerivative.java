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

package org.ddogleg.optimization;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ddogleg.optimization.impl.NumericalGradientForward;
import org.ddogleg.optimization.impl.NumericalJacobianForward_DDRM;
import org.ddogleg.optimization.impl.NumericalJacobianForward_DSCC;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;

/**
 * Functions for creating numerical derivatives
 *
 * @author Peter Abeles
 */
public class FactoryNumericalDerivative {
	public static <D extends DMatrix> FunctionNtoMxN<D> jacobianForwards(FunctionNtoM func, Class<D> type ) {
		if( type == DMatrixRMaj.class ) {
			return (FunctionNtoMxN)new NumericalJacobianForward_DDRM(func);
		} else if( type == DMatrixSparseCSC.class ) {
			return (FunctionNtoMxN)new NumericalJacobianForward_DSCC(func);
		} else {
			throw new RuntimeException("Matrix type unknown/not supported. "+type.getSimpleName());
		}
	}

	public static FunctionNtoN gradientForwards(FunctionNtoS func ) {
		return new NumericalGradientForward(func);
	}
}
