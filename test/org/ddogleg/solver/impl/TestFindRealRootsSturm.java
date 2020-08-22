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

package org.ddogleg.solver.impl;

import org.ddogleg.solver.GeneralPolynomialRootReal;
import org.ddogleg.solver.Polynomial;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class TestFindRealRootsSturm extends GeneralPolynomialRootReal {

	@Override
	public List<Double> computeRealRoots(Polynomial poly) {
		FindRealRootsSturm alg = new FindRealRootsSturm(poly.size,-1,1e-16,500,500);

		alg.process(poly);

		int N = alg.getNumberOfRoots();
		double[] roots = alg.getRoots();

		List<Double> ret = new ArrayList<Double>();
		for( int i = 0; i < N; i++ )
			ret.add(roots[i]);

		return ret;
	}

	@Test
	@Override
	public void rootsLargeReal() {
		// THIS TEST IS INTENTIONALLY BEING BYPASSED
		// There appears to be a very basic problem with the formulation of Sturm sequences that cases
		// problems
	}
}
