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

package org.ddogleg.chainrule.sym;

import org.ddogleg.chainrule.JacobianSerialChain;

/// A node that needs more than its inputs bound: an operator whose parameters are chosen per
/// evaluation, from data the expression cannot name. The builder gives it the block it landed on
/// and the function being built, and it registers whatever hooks and captures it needs.
///
/// This is what keeps composites out of the builder. A bilinear grid, for instance, picks the four
/// knots surrounding a pixel and hands their parameter slices to its operator, then publishes those
/// same slices as Jacobian columns — all of which it does through this interface, so nothing about
/// grids appears here or in [SymGraphBuilder].
public interface SymExtension {
	/// Registers pre-forward hooks and moving Jacobian columns. Called once, before the graph is
	/// fixated, with the terminals already resolved to their handles
	void install( JacobianSerialChain<double[]> chains, int block, SymFunction function );

	/// True when the extension supplies this input itself, so the builder must not demand that the
	/// expression bind it
	default boolean ownsInput( int slot ) {
		return false;
	}
}
