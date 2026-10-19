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

/// Computes the gradient in the backwards direction and is used when optimizing. If the parameters are assumed to be
/// known then no gradient will be produced for them since they are not being optimized. By default, parameters are not
/// assumed to be known.
public interface CROperatorBackwardsGrad<T> extends CROperatorForward<T> {
	/// Computes the derivatives of all the inputs and parameters to this function .
	///
	/// Mathematical explanation of how the partial derivatives are computed:
	/// <pre>
	/// @Out(...)/@x = @f(x,y)/@x * @Out/@f
	/// </pre>
	///
	/// Where Out is the final output, 'f' is this function with two inputs (x,y). '@Out/@f' is dout.
	///
	/// @param dout (Input) Derivative of output conditional on this function, computed from next layer.
	/// @param gradientInputs (Output) Gradient for all inputs which are not constant. If constant then the corresponding
	/// element is not modified. List will be filled in already.
	void gradient( CRArray<T> dout, List<CRArray<T>> gradientInputs );
}
