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

package org.ddogleg.chainrule.grid;

import java.util.Arrays;

/// A grid with a set of coefficients at every vertex that act as a set of parameters for a function, such as
/// a polynomial or spline. The shape lives in [#config] and the coefficients stored in a flat array,
/// laid out knot by knot in row major order.
///
/// Storage format
/// * name: string terminating with '\n'
/// * config: yaml serialized `config` on a single line terminating with '\n'
/// * length: string terminating with '\n'
/// * parameters: big endian double array
public class GridCoefND {
	/// Shape of the grid. Says how long [#parameters] has to be
	public final ConfigGridCoefND config = new ConfigGridCoefND();

	/// Coefficients for every knot, `config.dof()` of them
	public double[] parameters = new double[0];

	public GridCoefND() {}

	public GridCoefND( ConfigGridCoefND config ) {
		configure(config);
	}

	/// Sets the shape and sizes the coefficients to match, leaving them at zero
	public GridCoefND configure( ConfigGridCoefND config ) {
		return configure(config, 2);
	}

	/// [#configure] with the per-axis knot minimum given, so a nearest-neighbor field can store a 1x1 grid
	public GridCoefND configure( ConfigGridCoefND config, int minAxis ) {
		config.checkValid(minAxis);
		this.config.setTo(config);
		this.parameters = new double[config.dof()];
		return this;
	}

	public GridCoefND setTo( GridCoefND src ) {
		this.config.setTo(src.config);
		this.parameters = src.parameters.clone();
		return this;
	}

	public GridCoefND reset() {
		config.reset();
		parameters = new double[0];
		return this;
	}

	/// True if every field matches exactly. Coefficients are compared bit for bit, since this is used
	/// to check that saving and loading changed nothing
	public boolean isIdentical( GridCoefND o ) {
		if (!config.isIdentical(o.config))
			return false;
		return Arrays.equals(parameters, o.parameters);
	}

}
