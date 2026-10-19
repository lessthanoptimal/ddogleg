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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.ddogleg.chainrule.ops.CROperatorBilinearSplineND;
import org.ejml.MapFormattable;
import org.ejml.MapPrintFormat;
import org.ejml.MapPrintReflect;

import java.util.function.Consumer;

/// Extends [ConfigGridCoefND] to include min and max values needed by the spline to define its range. Used by
/// [org.ddogleg.chainrule.ops.CROperatorBilinearSplineND].
@Accessors(chain = true, fluent = true)
public class ConfigGridSplineND implements MapFormattable {

	@Getter public ConfigGridCoefND grid = new ConfigGridCoefND();

	/// Range of allowed values
	@Getter @Setter public double min = 1, max = -1;

	public ConfigGridSplineND() {reset();}

	public ConfigGridSplineND configure( Consumer<ConfigGridSplineND> op ) {
		op.accept(this);
		return this;
	}

	/// Makes sure valid values have been entered for all parameters
	public void checkValid() {
		grid.checkValid();
		if (!(min <= max))
			throw new IllegalArgumentException("min can't be more than max");
	}

	/// Resets parameters to an invalid value so you will know if they have been initialized
	public ConfigGridSplineND reset() {
		grid.reset();
		min = 1;
		max = -1;
		return this;
	}

	public ConfigGridSplineND setTo( ConfigGridSplineND src ) {
		grid.setTo(src.grid);
		this.min = src.min;
		this.max = src.max;
		return this;
	}

	/// True if every field matches exactly
	public boolean isIdentical( ConfigGridSplineND o ) {
		return grid.isIdentical(o.grid) && min == o.min && max == o.max;
	}

	@Override public String formatMap( MapPrintFormat format ) {
		return MapPrintReflect.formatMap(this, format);
	}

	@Override public String toString() { return MapPrintFormat.DEFAULT.toString(this); }
}
