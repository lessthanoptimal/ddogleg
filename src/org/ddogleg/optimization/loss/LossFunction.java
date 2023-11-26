/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.optimization.loss;

import org.ddogleg.optimization.functions.FunctionNtoS;

/**
 * <p>Residual loss function for regression. The standard squared loss can be sensitive to outliers. This function
 * enables robust loss functions to be used instead which are less sensitive to. Most implementations will attempt
 * to behave like squared error when an observation is not an outlier. Therefor, the following should be true
 * f(0) = 0.
 * </p>
 *
 * <p>All implementations should return a scalar which is &ge 0. A value of zero would indicate no errors.</p>
 *
 * @author Peter Abeles
 */
public interface LossFunction extends FunctionNtoS {

	/**
	 * Passes in the current residuals at the start of an iteration. If a loss function is dynamically computed
	 * and conditional on the residuals, here's where it should be done
	 *
	 * @return true if the loss function has changed and the cost needs to be recomputed.
	 */
	default boolean fixate( double[] residuals ) {return false;}

	/** Number of elements in the residual */
	int getNumberOfFunctions();

	void setNumberOfFunctions( int value );

	@Override default int getNumOfInputsN() {
		return getNumberOfFunctions();
	}
}
