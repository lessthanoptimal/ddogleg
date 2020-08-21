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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ddogleg.optimization.functions.GradientLineFunction;
import org.ddogleg.optimization.quasinewton.LineSearchMore94;
import org.ddogleg.optimization.quasinewton.QuasiNewtonBFGS;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;

/**
 * Wrapper around {@link QuasiNewtonBFGS} for {@link UnconstrainedMinimization}.  For a description of what
 * the line parameters mean see {@link LineSearchMore94}.
 *
 * @author Peter Abeles
 */
public class QuasiNewtonBFGS_to_UnconstrainedMinimization implements UnconstrainedMinimization {

	QuasiNewtonBFGS alg;

	public QuasiNewtonBFGS_to_UnconstrainedMinimization(QuasiNewtonBFGS alg) {
		this.alg = alg;
	}

	@Override
	public void setFunction(FunctionNtoS function, FunctionNtoN gradient, double minFunctionValue) {
		GradientLineFunction gradLine;

		if( gradient == null ) {
			gradLine = new CachedNumericalGradientLineFunction(function);
		} else {
			gradLine = new CachedGradientLineFunction(function,gradient);
		}

		alg.setFunction(gradLine,minFunctionValue);
	}

	@Override
	public void initialize(double[] initial, double ftol, double gtol) {
		alg.setConvergence(ftol,gtol);
		alg.initialize(initial);
	}

	@Override
	public double[] getParameters() {
		return alg.getParameters();
	}

	@Override
	public boolean iterate() throws OptimizationException {
		return alg.iterate();
	}

	@Override
	public boolean isConverged() {
		return alg.isConverged();
	}

	@Override
	public void setVerbose(@Nullable PrintStream verbose, int level) {
		alg.setVerbose(verbose,level);
	}

	@Override
	public double getFunctionValue() {
		return alg.getFx();
	}

	@Override
	public boolean isUpdated() {
		return alg.isUpdatedParameters();
	}
}
