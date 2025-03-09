/*
 * Copyright (c) 2012-2024, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.math.HessianSchurComplement_DSCC;
import org.ejml.data.DMatrix;

/**
 * <p>
 * A variant on {@link UnconstrainedLeastSquares} for solving large scale systems which can be simplified using the
 * Schur Complement. The approximate Hessian matrix (J'*J) is assumed to have the
 * following block triangle form: [A B;C D]. The system being solved for
 * is as follows: [A B;C D] [x_1;x_2] = [b_1;b_2]. See {@link HessianSchurComplement_DSCC} for more details.
 *
 * </p>
 *
 * @author Peter Abeles
 * @see HessianSchurComplement_DSCC
 * @see SchurJacobian
 */
public interface UnconstrainedLeastSquaresSchur<S extends DMatrix>
		extends IterativeOptimization, UnconstrainedLeastSquaresBase<S, SchurJacobian<S>> {}
