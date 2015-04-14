/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.rand;

import java.util.Random;

/**
 * Used to draw numbers from an uniform distibution.
 */
public class UniformDraw {
	double min;
	double max;
	Random rand;

	public UniformDraw( Random rand  , double min , double max )
	{
		if( max < min ) {
			throw new IllegalArgumentException("max must be greater than or equal min");
		} else if( Double.isInfinite(min) || Double.isInfinite(max)) {
			throw new IllegalArgumentException("Must be finite");
		} else if( Double.isNaN(min) || Double.isNaN(max)) {
			throw new IllegalArgumentException("Must not be NaN");
		}

		this.min = min;
		this.max = max;
		this.rand = rand;
	}

	public UniformDraw( double min , double max )
	{
		if( max < min ) {
			throw new IllegalArgumentException("max must be greater than or equal min");
		} else if( Double.isInfinite(min) || Double.isInfinite(max)) {
			throw new IllegalArgumentException("Must be finite");
		} else if( Double.isNaN(min) || Double.isNaN(max)) {
			throw new IllegalArgumentException("Must not be NaN");
		}

		this.min = min;
		this.max = max;
	}

	public double getMin() {
		return min;
	}

	public void setRand(Random rand) {
		this.rand = rand;
	}

	public double getMax() {
		return max;
	}

	public double next() {
		return rand.nextDouble()*(max-min)+min;
	}

	public static double draw( Random rand , double min , double max  )
	{
		return rand.nextDouble()*(max-min)+min;
	}


}
