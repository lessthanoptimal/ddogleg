/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.chainrule.sym;

import org.ddogleg.DDoglegJUnit;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.optimization.LeastSquaresSwitcher;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// The element-wise math wrappers: each has to reach the right operator with the right sizes, and
/// its parameters' Jacobian columns have to land where the feed offsets say
public class TestSymOps extends DDoglegJUnit {
	static final double[] A = {1.0, -2.0, 0.5};
	static final double[] B = {4.0, 0.25, -1.0};
	static final double S = -4.0;

	/// Builds `op(a, b)` with `a` at column 0 and `b` right after it, runs it forwards and
	/// backwards, and returns output plus Jacobian
	static class Evaluated {
		final double[] out;
		final DMatrixRMaj jacobian;

		Evaluated( double[] out, DMatrixRMaj jacobian ) {
			this.out = out;
			this.jacobian = jacobian;
		}
	}

	Evaluated evaluate( java.util.function.BinaryOperator<Sym> op, double[] valuesB ) {
		var params = new double[A.length + valuesB.length];
		System.arraycopy(A, 0, params, 0, A.length);
		System.arraycopy(valuesB, 0, params, A.length, valuesB.length);

		Sym a = Sym.param("a", A.length);
		Sym b = Sym.param("b", valuesB.length);
		SymFunction fun = new SymGraphBuilder().build(op.apply(a, b));
		fun.feed(a, params).offset = 0;
		fun.feed(b, params).offset = A.length;
		fun.mapParamColumns();

		var out = new double[A.length];
		fun.run(new org.ddogleg.chainrule.CRArray<>(out, 0));

		var jacobian = new DMatrixRMaj(A.length, params.length);
		fun.jacobianTo(LeastSquaresSwitcher.jacobianOutWrap(jacobian));
		fun.jacobianRow = 0;
		fun.runJacobian();
		return new Evaluated(out, jacobian);
	}

	@Test void mult() {
		Evaluated e = evaluate(SymOps::mult, B);
		for (int i = 0; i < A.length; i++) {
			assertEquals(A[i]*B[i], e.out[i], 1e-12, "output " + i);
			assertEquals(B[i], e.jacobian.get(i, i), 1e-12, "dA " + i);
			assertEquals(A[i], e.jacobian.get(i, A.length + i), 1e-12, "dB " + i);
		}
	}

	@Test void divide() {
		Evaluated e = evaluate(SymOps::divide, B);
		for (int i = 0; i < A.length; i++) {
			assertEquals(A[i]/B[i], e.out[i], 1e-12, "output " + i);
			assertEquals(1.0/B[i], e.jacobian.get(i, i), 1e-12, "dA " + i);
			assertEquals(-A[i]/(B[i]*B[i]), e.jacobian.get(i, A.length + i), 1e-12, "dB " + i);
		}
	}

	@Test void multVS() {
		Evaluated e = evaluate(SymOps::multVS, new double[]{S});
		for (int i = 0; i < A.length; i++) {
			assertEquals(A[i]*S, e.out[i], 1e-12, "output " + i);
			assertEquals(S, e.jacobian.get(i, i), 1e-12, "dA " + i);
			assertEquals(A[i], e.jacobian.get(i, A.length), 1e-12, "dS " + i);
		}
	}

	@Test void divideDamped() {
		final double beta = 0.5;
		Evaluated e = evaluate(( a, b ) -> SymOps.divideDamped(a, b, beta), new double[]{S + 5});
		double damped = beta + S + 5;
		for (int i = 0; i < A.length; i++) {
			assertEquals(A[i]/damped, e.out[i], 1e-12, "output " + i);
			assertEquals(1.0/damped, e.jacobian.get(i, i), 1e-12, "dA " + i);
			assertEquals(-A[i]/(damped*damped), e.jacobian.get(i, A.length), 1e-12, "dS " + i);
		}
	}

	@Test void divideVS() {

		Evaluated e = evaluate(SymOps::divideVS, new double[]{S});
		for (int i = 0; i < A.length; i++) {
			assertEquals(A[i]/S, e.out[i], 1e-12, "output " + i);
			assertEquals(1.0/S, e.jacobian.get(i, i), 1e-12, "dA " + i);
			assertEquals(-A[i]/(S*S), e.jacobian.get(i, A.length), 1e-12, "dS " + i);
		}
	}
}
