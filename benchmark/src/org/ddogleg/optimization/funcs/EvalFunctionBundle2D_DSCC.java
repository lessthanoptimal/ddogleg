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

package org.ddogleg.optimization.funcs;

import org.ddogleg.optimization.functions.FunctionNtoMxN;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ddogleg.optimization.wrap.SchurJacobian_to_NtoMxN;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.DConvertMatrixStruct;

import java.util.ArrayList;
import java.util.List;

/**
 * 2D bundle adjustment. camera is specified by (x,y)  and landmarks by (x,y) observations are angles.
 *
 * @author Peter Abeles
 */
public class EvalFunctionBundle2D_DSCC extends EvalFunctionBundle2D<DMatrixSparseCSC>
{
	public EvalFunctionBundle2D_DSCC() {
	}

	public EvalFunctionBundle2D_DSCC(long seed, double length, double depth, int numCamera, int numLandmarks) {
		super(seed, length, depth, numCamera, numLandmarks);
	}

	@Override
	public FunctionNtoMxN<DMatrixSparseCSC> getJacobian() {
		return new SchurJacobian_to_NtoMxN.DSCC(getJacobianSchur());
	}

	@Override
	public SchurJacobian<DMatrixSparseCSC> getJacobianSchur() {
		return new Jacobian();
	}

	public class Jacobian implements SchurJacobian<DMatrixSparseCSC> {
		List<Point2D> cameras = new ArrayList<>();
		List<Point2D> landmarks = new ArrayList<>();

		@Override
		public void process(double[] input, DMatrixSparseCSC left, DMatrixSparseCSC right) {
			decode(input,cameras, landmarks);

			int N = numCamera*numLandmarks;
			DMatrixSparseTriplet tripletLeft = new DMatrixSparseTriplet(N,2*numCamera,1);
			DMatrixSparseTriplet tripletRight = new DMatrixSparseTriplet(N,2*numLandmarks,1);

			int output = 0;
			for (int i = 0; i < numCamera; i++) {
				Point2D c = cameras.get(i);
				for (int j = 0; j < numLandmarks; j++, output++) {
					Point2D l = landmarks.get(j);
					double top = l.y-c.y;
					double bottom = l.x-c.x;
					double slope = top/bottom;

					double a = 1.0/(1.0+slope*slope);

					double dx = top/(bottom*bottom);
					double dy = -1.0/bottom;

					tripletLeft.addItemCheck(output,i*2+0,a*dx);
					tripletLeft.addItemCheck(output,i*2+1,a*dy);
				}
			}

			for (int i = 0; i < numLandmarks; i++) {
				Point2D l = landmarks.get(i);
				for (int j = 0; j < numCamera; j++) {
					Point2D c = cameras.get(j);
					double top = l.y-c.y;
					double bottom = l.x-c.x;
					double slope = top/bottom;

					double a = 1.0/(1.0+slope*slope);

					double dx = -top/(bottom*bottom);
					double dy = 1.0/bottom;

					output = j*numLandmarks + i;
					tripletRight.addItemCheck(output,i*2+0,a*dx);
					tripletRight.addItemCheck(output,i*2+1,a*dy);
				}
			}

			DConvertMatrixStruct.convert(tripletLeft,left);
			DConvertMatrixStruct.convert(tripletRight,right);
		}

		@Override
		public int getNumOfInputsN() {
			return 2*numCamera + 2*numLandmarks;
		}

		@Override
		public int getNumOfOutputsM() {
			return numCamera*numLandmarks;
		}
	}
}
