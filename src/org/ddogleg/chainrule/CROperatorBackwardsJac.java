/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.chainrule;

import java.util.List;

/// Computes the Jacobian in the backwards direction and is used when optimizing. If the parameters are assumed to be
/// known then no Jacobian will be produced for them since they are not being optimized. By default, parameters are not
/// assumed to be known.
public interface CROperatorBackwardsJac<T> extends CROperatorForward<T> {
	/// Computes the Jacobian of all the inputs and parameters to this function.
	///
	/// Mathematical explanation of how the partial derivatives are computed:
	/// ```
	/// @Out(..)/@x = [@f1(x,y)/@x @f1(x,y)/@y ; @f2(x,y)/@x ...  ] * @???/@f
	/// ```
	///
	/// Where Out is the final output, 'f' is this function with two inputs (x,y). '@Out/@f' is dout.
	///
	/// @param dout (Input) Jacobian of output conditional on this function, computed from next layer.
	/// @param jacobianInputs (Output) Storage for all the Jacobian inputs. All matrices must be pre-declared and
	/// included in the list. If a matrix is marked as invalid, then the jacobian is skipped for that input.
	void jacobian( CRMatrix<T> dout, List<CRMatrix<T>> jacobianInputs );
}
