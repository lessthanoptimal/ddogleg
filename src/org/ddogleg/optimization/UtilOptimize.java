/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

/**
 * Performs common optimization tasks.  Such as iterating until convergence.
 *
 * @author Peter Abeles
 */
public class UtilOptimize {

	/**
	 * Iterate until the line search converges or the maximum number of iterations has been exceeded.
	 *
	 * The maximum number of steps is specified.  A step is defined as the number of times the
	 * optimization parameters are changed.
	 *
	 * @param search Search algorithm
	 * @param maxSteps Maximum number of steps.
	 * @return Value returned by {@link IterativeOptimization#iterate}
	 */
	public static boolean process( IterativeOptimization search , int maxSteps ) {
		for( int i = 0; i < maxSteps; i++ ) {
			boolean converged = step(search);
			if( converged ) {
				return search.isConverged();
			}
		}

		return true;
	}

	/**
	 * Performs a single step by iterating until the parameters are updated.
	 *
	 * @param search Search algorithm
	 * @return Value returned by {@link IterativeOptimization#iterate}
	 */
	public static boolean step( IterativeOptimization search ) {
		for( int i = 0; i < 10000; i++ ) {
			boolean converged = search.iterate();
			if( converged || !search.isUpdated() ) {
				return converged;
			}
		}
		throw new RuntimeException("After 10,000 iterations it failed to take a step! Probably a bug.");
	}
}
