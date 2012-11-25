/*
 * Copyright (c) 2012, Peter Abeles. All Rights Reserved.
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
	 * @param search Search algorithm
	 * @param maxIterations Maximum number of iterations
	 * @return True if it converged to a solution
	 */
	public static boolean process( LineSearch search , int maxIterations ) {
		for( int i = 0; i < maxIterations; i++ ) {
			if( search.iterate() ) {
				return search.isConverged();
			}
		}

		return true;
	}

	/**
	 * Iterate until the search algorithm converges or the maximum number of iterations has been exceeded.
	 *
	 * @param alg Search algorithm
	 * @param maxIterations Maximum number of iterations
	 * @return True if it converged to a solution
	 */
	public static boolean process( UnconstrainedMinimization alg , int maxIterations ) {
		for( int i = 0; i < maxIterations; i++ ) {
			if( alg.iterate() ) {
				return alg.isConverged();
			}
		}

		return true;
	}

	/**
	 * Iterate until the search algorithm converges or the maximum number of iterations has been exceeded.
	 *
	 * @param alg Search algorithm
	 * @param maxIterations Maximum number of iterations
	 * @return True if it converged to a solution
	 */
	public static boolean process( UnconstrainedLeastSquares alg , int maxIterations ) {
		for( int i = 0; i < maxIterations; i++ ) {
			if( alg.iterate() ) {
				return alg.isConverged();
			}
		}

		return true;
	}
}
