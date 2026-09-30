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

package org.ddogleg.optimization.math;

import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.decomposition.DecompositionInterface;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.interfaces.linsol.LinearSolverSparse;

/**
 * Wraps a solver so that setA() is computed on a copy of the input and then the input is filled with NaN.
 * Solutions are still correct. Used to detect code which reads a matrix after passing it to a solver which
 * declares {@link #modifiesA()}.
 */
public class ScrambleInputSolver<S extends DMatrix> implements LinearSolverSparse<S, DMatrixRMaj> {
	LinearSolver<S, DMatrixRMaj> solver;

	public ScrambleInputSolver( LinearSolver<S, DMatrixRMaj> solver ) {
		this.solver = solver;
	}

	@Override public boolean setA( S A ) {
		S copy = A.copy();
		for (int row = 0; row < A.getNumRows(); row++) {
			for (int col = 0; col < A.getNumCols(); col++) {
				A.set(row, col, Double.NaN);
			}
		}
		return solver.setA(copy);
	}

	@SuppressWarnings("unchecked")
	@Override public void solveSparse( S B, S X ) {((LinearSolverSparse<S, DMatrixRMaj>)solver).solveSparse(B, X);}

	@Override public void solve( DMatrixRMaj B, DMatrixRMaj X ) {solver.solve(B, X);}

	@Override public double quality() {return solver.quality();}

	@Override public boolean modifiesA() {return true;}

	@Override public boolean modifiesB() {return solver.modifiesB();}

	@Override public void setStructureLocked( boolean locked ) {}

	@Override public boolean isStructureLocked() {return false;}

	@Override public <D extends DecompositionInterface> D getDecomposition() {return solver.getDecomposition();}

	/** Dense variant, needed by constructors which take {@link LinearSolverDense} */
	public static class Dense extends ScrambleInputSolver<DMatrixRMaj> implements LinearSolverDense<DMatrixRMaj> {
		public Dense( LinearSolverDense<DMatrixRMaj> solver ) {super(solver);}

		@Override public void invert( DMatrixRMaj A_inv ) {throw new UnsupportedOperationException();}
	}
}
