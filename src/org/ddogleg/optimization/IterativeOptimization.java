/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * <p>
 * Interface for iterative optimization classes.  One iteration is performed in the search by
 * invoking the {@link #iterate()} function.  When the optimization has terminated iterate() will
 * return true.  All implementers of this class must terminate within a finite number of steps. The
 * process can terminate because it has converged or no more progress can be made.
 * </p>
 *
 * <p>
 * Implementers of this class will provide a function that returns the best set of parameters found
 * so far.  This allows the progress to be terminated early if it is taking an excessive amount of time.
 * All implementations are also {@link Serializable}, allowing intermediate progress to be saved and
 * resumed.
 * </p>
 *
 * @see UtilOptimize
 *
 * @author Peter Abeles
 */
public interface IterativeOptimization extends Serializable {

	/**
	 * <p>
	 * Updates the search. If the search has terminated true is returned.  After the
	 * search has terminated invoke {@link #isConverged} to see if a solution has been
	 * converged to or if it stopped for some other reason.
	 * </p>
	 * <p>
	 * NOTE: The optimization parameters might not be modified after iterate() is called.  An internal book keeping
	 * step might have been done.  To see if parameters have changed call {@link #isUpdated()}.
	 * </p>
	 * @return true if it has converged or that no more progress can be made.
	 */
	boolean iterate() throws OptimizationException;

	/**
	 * True if the parameter(s) being optimized have been updated
	 *
	 * @return True if parameters have been updated
	 */
	boolean isUpdated();

	/**
	 * Indicates if iteration stopped due to convergence or not.
	 *
	 * @return True if iteration stopped because it converged.
	 */
	boolean isConverged();

	/**
	 * If set to a non-null output then extra information will be printed to the specified stream.
	 *
	 * @param out Stream that is printed to. Set to null to disable
	 * @param level (Future use) Parameter which can be used to specify level of verbose output. Set to zero for now.
	 */
	void setVerbose(@Nullable PrintStream out , int level );
}
