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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ddogleg.optimization.functions.GradientLineFunction;
import org.ddogleg.optimization.impl.LineSearchMore94;
import org.ddogleg.optimization.impl.QuasiNewtonBFGS;

/**
 * Wrapper around {@link QuasiNewtonBFGS} for {@link UnconstrainedMinimization}.  For a description of what
 * the line parameters mean see {@link LineSearchMore94}.
 *
 * @author Peter Abeles
 */
public class QuasiNewtonBFGS_to_UnconstrainedMinimization implements UnconstrainedMinimization {

	// line search parameters
	private double line_gtol = 0.9;
	private double line_ftol = 1e-3;
	private double line_xtol = 0.1;

	QuasiNewtonBFGS alg;
	LineSearchMore94 lineSearch = new LineSearchMore94();

	@Override
	public void setFunction(FunctionNtoS function, FunctionNtoN gradient, double minFunctionValue) {
		lineSearch = new LineSearchMore94();

		GradientLineFunction gradLine;

		if( gradient == null ) {
			gradLine = new CachedNumericalGradientLineFunction(function);
		} else {
			gradLine = new CachedGradientLineFunction(function,gradient);
		}

		alg = new QuasiNewtonBFGS(gradLine,lineSearch,minFunctionValue);
	}

	@Override
	public void initialize(double[] initial, double ftol, double gtol) {
		lineSearch.setConvergence(line_ftol, line_gtol, line_xtol);
		alg.setConvergence(ftol,gtol,line_gtol);
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
	public String getWarning() {
		return alg.getWarning();
	}

	@Override
	public double getFunctionValue() {
		return alg.getFx();
	}

	@Override
	public boolean isUpdated() {
		return alg.isUpdatedParameters();
	}

	public double getLine_gtol() {
		return line_gtol;
	}

	public void setLine_gtol(double line_gtol) {
		this.line_gtol = line_gtol;
	}

	public double getLine_ftol() {
		return line_ftol;
	}

	public void setLine_ftol(double line_ftol) {
		this.line_ftol = line_ftol;
	}

	public double getLine_xtol() {
		return line_xtol;
	}

	public void setLine_xtol(double line_xtol) {
		this.line_xtol = line_xtol;
	}
}
