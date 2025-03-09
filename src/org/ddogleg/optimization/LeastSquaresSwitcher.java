/*
 * Copyright (c) 2012-2024, Peter Abeles. All Rights Reserved.
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

import lombok.Getter;
import lombok.Setter;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ejml.data.*;
import org.ejml.ops.DConvertMatrixStruct;
import org.ejml.sparse.csc.CommonOps_DSCC;

import java.util.Objects;

/**
 * Class that lets you easily switch between different {@link GaussNewtonBase_F64} solvers and matrix formats.
 * You must first call {@link #setSolver} before you call {@link #setFunction}. Supports implementations
 * that can be specified using {@link ConfigNonLinearLeastSquares}.
 */
@SuppressWarnings({"unchecked", "rawtypes", "NullAway.Init"})
public class LeastSquaresSwitcher<S extends DMatrix> {
	// Cost function
	FunctionNtoM function;

	// compute the Jacobian
	ProcessJacobianOut functionJac;

	/** Solver being used */
	@Getter @Setter UnconstrainedLeastSquaresBase<S, ?> solver;

	/**
	 * Specifies the cost residual function and it's Jacobian. Must be called after the solver has been specified.
	 */
	public void setFunction( FunctionNtoM function, ProcessJacobianOut jacobian ) {
		Objects.requireNonNull(solver, "Must specify solver first");

		this.function = function;
		this.functionJac = jacobian;

		if (solver instanceof UnconstrainedLeastSquares sol) {
			if (sol.getJacobianType() == DMatrixRMaj.class) {
				sol.setFunction(function, new JacobianDDRM());
			} else if (sol.getJacobianType() == DMatrixSparseCSC.class) {
				sol.setFunction(function, new JacobianDSCC());
			} else {
				throw new IllegalArgumentException("Unknown Jacobian matrix type. " +
						sol.getJacobianType().getSimpleName());
			}
		} else if (solver instanceof UnconstrainedLeastSquaresSchur sol) {
			if (sol.getJacobianType() == DMatrixRMaj.class) {
				sol.setFunction(function, new SchurJacobianDDRM());
			} else if (sol.getJacobianType() == DMatrixSparseCSC.class) {
				sol.setFunction(function, new SchurJacobianDSCC());
			} else {
				throw new IllegalArgumentException("Unknown Jacobian matrix type. " +
						sol.getJacobianType().getSimpleName());
			}
		} else {
			throw new IllegalArgumentException("Unknown solver type: " + solver.getClass().getSimpleName());
		}
	}

	/**
	 * Used to specify the solver using a configuration which is then passed on to a factory
	 *
	 * @param config Which solver to user
	 * @param sparse Should it use a sparse matrix format?
	 * @param schur Should it use a schur decomposition?
	 */
	public void setSolver( ConfigNonLinearLeastSquares config, boolean sparse, boolean schur ) {
		if (schur) {
			if (sparse) {
				setSolver((UnconstrainedLeastSquaresSchur)FactoryOptimizationSparse.leastSquaresSchur(config));
			} else {
				setSolver((UnconstrainedLeastSquaresSchur)FactoryOptimization.leastSquaresSchur(config));
			}
		} else {
			if (sparse) {
				setSolver((UnconstrainedLeastSquares)FactoryOptimizationSparse.leastSquares(config));
			} else {
				setSolver((UnconstrainedLeastSquares)FactoryOptimization.leastSquares(config));
			}
		}
	}

	/// Convenience function for wrapping a matrix with [JacobianOut]. Useful for unit testing
	public static JacobianOut jacobianOutWrap( DMatrixRMaj output ) {
		return ( row0, col0, values, numRows, numCols ) -> {
			for (int row = 0, idx = 0; row < numRows; row++) {
				int outIdx = (row0 + row)*output.numCols + col0;
				for (int col = 0; col < numCols; col++) {
					output.data[outIdx++] += values[idx++];
				}
			}
		};
	}

	/** Jacobian implementation for dense matrix */
	class JacobianDDRM implements FunctionNtoMxN<DMatrixRMaj> {
		DMatrixRMaj output;

		public JacobianDDRM() {
			// NOTE: We can't use the convenience function because output is initially null and need to use
			// the current value
			functionJac.setJacobianOut(( row0, col0, values, numRows, numCols ) -> {
				for (int row = 0, idx = 0; row < numRows; row++) {
					int outIdx = (row0 + row)*output.numCols + col0;
					for (int col = 0; col < numCols; col++) {
						output.data[outIdx++] += values[idx++];
					}
				}
			});
		}

		@Override public void process( double[] input, DMatrixRMaj output ) {
			this.output = output;
			output.reshape(getNumOfOutputsM(), getNumOfInputsN());
			output.zero();

			functionJac.process(input);
		}

		@Override public DMatrixRMaj declareMatrixMxN() {
			return new DMatrixRMaj(getNumOfOutputsM(), getNumOfInputsN());
		}

		@Override public int getNumOfInputsN() {
			return function.getNumOfInputsN();
		}

		@Override public int getNumOfOutputsM() {
			return function.getNumOfOutputsM();
		}
	}

	/** Jacobian implementation for sparse matrix */
	class JacobianDSCC implements FunctionNtoMxN<DMatrixSparseCSC> {
		protected DMatrixSparseTriplet triplet = new DMatrixSparseTriplet(1, 1, 1);
		protected IGrowArray work = new IGrowArray();

		public JacobianDSCC() {
			functionJac.setJacobianOut(( row0, col0, values, numRows, numCols ) -> {
				for (int row = 0, idx = 0; row < numRows; row++) {
					for (int col = 0; col < numCols; col++) {
						double v = values[idx++];
						if (v == 0.0)
							continue;
						triplet.addItem(row0 + row, col0 + col, v);
					}
				}
			});
		}

		@Override public void process( double[] input, DMatrixSparseCSC output ) {
			triplet.reshape(getNumOfOutputsM(), getNumOfInputsN());
			triplet.zero();

			functionJac.process(input);

			// Convert it from the working sparse format into one that's efficient for computations
			DConvertMatrixStruct.convert(triplet, output, work);
			CommonOps_DSCC.duplicatesAdd(output, work);
		}

		@Override public DMatrixSparseCSC declareMatrixMxN() {
			return new DMatrixSparseCSC(getNumOfOutputsM(), getNumOfInputsN());
		}

		@Override public int getNumOfInputsN() {
			return function.getNumOfInputsN();
		}

		@Override public int getNumOfOutputsM() {
			return function.getNumOfOutputsM();
		}
	}

	/** Jacobian implementation for dense matrix in a Schur decomposition */
	class SchurJacobianDDRM implements SchurJacobian<DMatrixRMaj> {
		DMatrixRMaj left;
		DMatrixRMaj right;
		protected int split;

		public SchurJacobianDDRM() {
			functionJac.setJacobianOut(( row0, col0, values, numRows, numCols ) -> {
				DMatrixRMaj out;
				if (col0 >= split) {
					col0 -= split;
					out = right;
				} else {
					out = left;
				}

				for (int row = 0, idx = 0; row < numRows; row++) {
					int outIdx = (row0 + row)*out.numCols + col0;
					for (int col = 0; col < numCols; col++) {
						out.data[outIdx++] += values[idx++];
					}
				}
			});
		}

		@Override public void process( double[] input, DMatrixRMaj left, DMatrixRMaj right ) {
			// Initializes output Jacobian to zero. Adjustments are on left side
			int outputSize = getNumOfOutputsM();
			int inputSize = getNumOfInputsN();
			int split = functionJac.getSchurSplit();

			if (split < 0 || split > inputSize)
				throw new RuntimeException("Split out of range. split=" + split);

			this.left = left;
			this.right = right;
			left.reshape(outputSize, split);
			right.reshape(outputSize, getNumOfInputsN() - split);
			left.zero();
			right.zero();

			functionJac.process(input);
		}

		@Override public int getNumOfInputsN() {
			return function.getNumOfInputsN();
		}

		@Override public int getNumOfOutputsM() {
			return function.getNumOfOutputsM();
		}
	}

	/** Jacobian implementation for sparse matrix in a Schur decomposition */
	class SchurJacobianDSCC implements SchurJacobian<DMatrixSparseCSC> {
		// Used to specify sparse jacobian. Data structure is fast to write but slow to read
		protected DMatrixSparseTriplet leftTriplet = new DMatrixSparseTriplet(1, 1, 1);
		protected DMatrixSparseTriplet rightTriplet = new DMatrixSparseTriplet(1, 1, 1);
		protected IGrowArray work = new IGrowArray();
		protected int split;

		public SchurJacobianDSCC() {
			functionJac.setJacobianOut(( row0, col0, values, numRows, numCols ) -> {
				DMatrixSparseTriplet out;
				if (col0 >= split) {
					col0 -= split;
					out = rightTriplet;
				} else {
					out = leftTriplet;
				}

				for (int row = 0, idx = 0; row < numRows; row++) {
					for (int col = 0; col < numCols; col++) {
						double v = values[idx++];
						if (v == 0.0)
							continue;

						out.addItem(row + row0, col + col0, v);
					}
				}
			});
		}

		@Override public void process( double[] input, DMatrixSparseCSC left, DMatrixSparseCSC right ) {
			// Initializes output Jacobian to zero. Adjustments are on left side
			int outputSize = getNumOfOutputsM();
			split = functionJac.getSchurSplit();

			leftTriplet.zero();
			rightTriplet.zero();
			leftTriplet.reshape(outputSize, split);
			rightTriplet.reshape(outputSize, getNumOfInputsN() - split);

			functionJac.process(input);

			// Convert it from the working sparse format into one that's efficient for computations
			DConvertMatrixStruct.convert(leftTriplet, left, work);
			DConvertMatrixStruct.convert(rightTriplet, right, work);

			CommonOps_DSCC.duplicatesAdd(left, work);
			CommonOps_DSCC.duplicatesAdd(right, work);
		}

		@Override public int getNumOfInputsN() {
			return function.getNumOfInputsN();
		}

		@Override public int getNumOfOutputsM() {
			return function.getNumOfOutputsM();
		}
	}

	/**
	 * High level interface for implementing the Jacobian. Main feature is the ability to pass in {@link JacobianOut}
	 * which allows you to write values into the Jacobian.
	 */
	public interface ProcessJacobianOut {
		/** When called it will compute the Jacobian */
		void process( double[] input );

		/** If a Schur decomposition is used, where should it split the Jacobian into a left and right matrix? */
		int getSchurSplit();

		/** Used to pass in the Jacobian storage interface */
		void setJacobianOut( JacobianOut out );
	}

	/** Abstracts writing to the Jacobian so that {@link ProcessJacobianOut} doesn't need to know the format */
	public interface JacobianOut {
		/**
		 * Adds the local matrix into the system Jacobian. Assumes the matrix is stored in a row-major format.
		 *
		 * @param row Row in system Jacobian it should start writing to
		 * @param col Column in system Jacobian it should start writing to
		 * @param values Element values in local matrix
		 * @param numRows Number of rows in local matrix
		 * @param numCols Number of columns in local matrix
		 */
		void addMatrixToJacobian( int row, int col, double[] values, int numRows, int numCols );
	}
}
