/*
 * Copyright (c) 2012-2017, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.solver.Polynomial;
import org.ddogleg.solver.PolynomialRoots;
import org.ejml.data.Complex_F64;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around {@link FindRealRootsSturm} for {@link PolynomialRoots}.
 *
 * @author Peter Abeles
 */
public class WrapRealRootsSturm implements PolynomialRoots {

	// raw algorithm for finding real roots
	FindRealRootsSturm alg;

	// avoid creating memory by recycling
	List<Complex_F64> roots = new ArrayList<Complex_F64>();
	Complex_F64[] storage;

	public WrapRealRootsSturm(FindRealRootsSturm alg) {
		this.alg = alg;

		storage = new Complex_F64[alg.getMaxRoots()];
		for( int i = 0; i < storage.length; i++ )
			storage[i] = new Complex_F64();
	}

	@Override
	public boolean process(Polynomial poly) {
		try {
			alg.process(poly);
		} catch( RuntimeException e ) {
			return false;
		}

		roots.clear();

		double found[] = alg.getRoots();
		int N = alg.getNumberOfRoots();

		for( int i = 0; i < N; i++ ) {
			Complex_F64 c = storage[i];
			c.real = found[i];
			c.imaginary = 0;
			roots.add(c);
		}

		return true;
	}

	@Override
	public List<Complex_F64> getRoots() {
		return roots;
	}
}
