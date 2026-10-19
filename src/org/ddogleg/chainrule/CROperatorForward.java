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

import java.util.Map;

/// Operator in forward direction. Takes in N inputs of [CRArray] and produces a single output. Can be chained
/// together using [JacobianSerialChain]. Typically, the first input is the output of the previous element
/// in the chain.
///
/// - Input: Set of encoded structures, e.g. 3D point, SE3, curve, ... etc.
/// - Settings: High level configurations, e.g. 3D or homogenous points.
/// - Output: Resulting output, a single array
public interface CROperatorForward<T> {
	/// Specifies the value of an input's vector.
	///
	/// The implementation can safely save the array `input` points at along with its offset. Neither the
	/// values at that offset nor the array itself will change until after [#forward] or a derived function
	/// such as [CROperatorBackwardsJac#jacobian] has been called. The `input` reference itself cannot be
	/// saved: the caller is free to recycle the [CRArray] the moment the call returns, including pointing
	/// it at another offset.
	///
	/// @param which Which input
	/// @param input The input's value. This reference can't be saved internally but the array and offset can be.
	void setInput( int which, CRArray<T> input );

	/// Returns the specified input's vector size
	int getInputSize( int which );

	/// Number of inputs
	int getInputCount();

	/// This function is called after an iteration and will normalize an input's values. The meaning of the values
	/// should not change.
	default void normalizeAfterStep( int which, CRArray<T> params ) {}

	/// Retrieve a serialized description of the settings
	default Map<String, String> getSettings() {return Map.of();}

	/// Reconfigures the block using the specified settings
	default void setSettings( Map<String, String> settings ) {}

	/// Performs forward pass of the function. Must have already updated [#setInput(int, CRArray)].
	///
	/// @param output (Output) Output vector. Modified.
	void forward( CRArray<T> output );

	/// Returns the output vector's size
	int getOutputSize();

	/// Primitive data type for the array
	Class<T> getArrayType();
}
