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

package org.ddogleg.optimization.wrap;

import org.ddogleg.optimization.LineSearch;
import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.UnconstrainedMinimization;
import org.ddogleg.optimization.functions.FunctionNtoN;
import org.ddogleg.optimization.functions.FunctionNtoS;
import org.ddogleg.optimization.functions.GradientLineFunction;
import org.ddogleg.optimization.impl.LineSearchMore94;
import org.ddogleg.optimization.impl.QuasiNewtonBFGS;

/**
 * @author Peter Abeles
 */
public class WrapQuasiNewtonBFGS implements UnconstrainedMinimization {

	// line search parmeters
	private static final double line_gtol = 0.9;
	private static final double line_ftol = 1e-3;
	private static final double line_xtol = 0.1;

	QuasiNewtonBFGS alg;

	@Override
	public void setFunction(FunctionNtoS function, FunctionNtoN gradient, double minFunctionValue) {
		LineSearch lineSearch = new LineSearchMore94(line_ftol,line_gtol,line_xtol);

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
}
