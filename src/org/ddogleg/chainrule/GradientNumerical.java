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

/// Given a [CROperatorForward] implementations of this interface will compute the gradient of its
/// inputs and parameters. Numerical differentiation is done using a symmetric sample, e.g.
/// `dx = [f(x+T)-f(x-T)]/T`
public interface GradientNumerical<T> {

	/// Overrides default settings for computing numerical gradient.
	///
	/// @param T Sampling distance used for numerical differentiation
	void configure( double T );

	/// Sets the function which will be differentiated and other parameters.
	///
	/// @param inputs Value of all inputs passed into the function
	/// @param function The function which is to be differentiated
	void setFunction( List<CRArray<T>> inputs, CROperatorForward<T> function );

	/// Performs numerical differentiation to compute the gradients of input and parameters. The value of parameters
	/// will be the current parameters stored in the function.
	///
	/// @param dout (Input) Output's Jacobian, computed from next layer.
	/// @param gradInput (Output) Storage for input gradients
	void differentiate( CRArray<T> dout, List<CRArray<T>> gradInput );
}
