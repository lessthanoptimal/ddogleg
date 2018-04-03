/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

import java.io.Serializable;

/**
 * Convergence paramters for {@link UnconstrainedMinimization} and {@link UnconstrainedLeastSquares}.
 *
 * @author Peter Abeles
 */
public class ConvergeOptParam implements Serializable
{
	public int maxIterations = 1000;
	public double gtol = 0;
	public double ftol = 0;

	public ConvergeOptParam(){}

	public ConvergeOptParam( ConvergeOptParam original ){
		this.maxIterations = original.maxIterations;
		this.gtol = original.gtol;
		this.ftol = original.ftol;
	}

	public ConvergeOptParam(int maxIterations, double gtol, double ftol) {
		this.maxIterations = maxIterations;
		this.gtol = gtol;
		this.ftol = ftol;
	}
}
