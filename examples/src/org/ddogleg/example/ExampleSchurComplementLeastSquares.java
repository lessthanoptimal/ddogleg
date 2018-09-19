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

package org.ddogleg.example;

import org.ddogleg.optimization.FactoryOptimizationSparse;
import org.ddogleg.optimization.UnconstrainedLeastSquaresSchur;
import org.ddogleg.optimization.UtilOptimize;
import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ddogleg.optimization.functions.SchurJacobian;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.ConvertDMatrixStruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example for how to implement sparse Schur Complement for non-linear least squares. This is an invented
 * problem created for DDogleg to stress the system in a similar way to how Bundle Adjustment does, a problem
 * from computer vision, while being small enough to run very fast. For example, during optimization the Jacobian
 * will become singular and that needs to be handled correctly.
 * 
 * The problem takes place in a 2D world. Cameras and landmarks are described by a 2D coordinate. Each camera
 * can see every landmark with only a bearings measurement. Given a set of observations and an initial estimate
 * of each camera and landmark's location estimate the actual locations.
 *
 * @author Peter Abeles
 */
public class ExampleSchurComplementLeastSquares {

	public static void main(String[] args)
	{
		//------------------------------------------------------------------
		// Randomly generating the world
		//
		Random rand = new Random(0xDEADBEEF);
		final int numCameras = 10;
		final int numLandmarks = 40;

		// Cameras are placed along a line. This specifies the length of the line and how spread out the cameras
		// will be
		final double length = 10;

		// how far away the landmark line is from the camera line
		final double depth = 20;

		double[] observations = new double[numCameras*numLandmarks];

		double[] optimal = new double[2*(numCameras+numLandmarks)];
		double[] initial = new double[optimal.length];

		// Randomly create the world and observations
		List<Point2D> cameras = new ArrayList<>();
		List<Point2D> landmarks = new ArrayList<>();

		int index = 0;
		for (int i = 0; i < numCameras; i++) {
			double x = 2*(rand.nextDouble()-0.5)*length;
			cameras.add( new Point2D(0,x) );

			optimal[index++] = 0;
			optimal[index++] = x;
		}
		for (int i = 0; i < numLandmarks; i++) {
			double y = 2*(rand.nextDouble()-0.5)*length;
			landmarks.add( new Point2D(depth,y) );

			optimal[index++] = depth;
			optimal[index++] = y;
		}

		//------------------------------------------------------------------
		// Creating noisy initial estimate
		//
		for (int i = 0; i < optimal.length; i++) {
			initial[i] = optimal[i] + rand.nextGaussian()*5; // this is a lot of error!
		}

		//------------------------------------------------------------------
		// Creating perfect observations.
		//   Unrealistic, but we know if it hit the optimal solution or not
		index = 0;
		for (int i = 0; i < numCameras; i++) {
			Point2D c = cameras.get(i);
			for (int j = 0; j < numLandmarks; j++, index++) {
				Point2D l = landmarks.get(j);
				observations[index] = Math.atan((l.y-c.y)/(l.x-c.x));

				// sanity check
				if(UtilEjml.isUncountable(observations[index]))
					throw new RuntimeException("Egads");
			}
		}

		//------------------------------------------------------------------
		// Create the optimizer and optimize!
		//
		UnconstrainedLeastSquaresSchur<DMatrixSparseCSC> optimizer =
				FactoryOptimizationSparse.levenbergMarquardtSchur(null);

		// Send to standard out progress information
		optimizer.setVerbose(System.out,0);

		// For large sparse systems it's strongly recommended that you use an analytic Jacobian. While this might
		// change in the future after a redesign, there isn't a way to efficiently compute the numerical Jacobian
		FuncGradient funcGrad = new FuncGradient(numCameras,numLandmarks,observations);
		optimizer.setFunction(funcGrad,funcGrad);

		// provide it an extremely crude initial estimate of the line equation
		optimizer.initialize(initial,1e-12,1e-12);

		// iterate 500 times or until it converges.
		// Manually iteration is possible too if more control over is required
		UtilOptimize.process(optimizer,500);

		// see how accurately it found the solution. Optimally this value would be zero
		System.out.println("Final Error = "+optimizer.getFunctionValue());
	}


	/**
	 * Implements the residual function and the gradient.
	 */
	public static class FuncGradient
			implements FunctionNtoM, SchurJacobian<DMatrixSparseCSC>
	{
		int numCameras,numLandmarks;
		
		// observations of each landmark as seen from each camera as an angle measurement
		// 2D array. cameras = rows, landmarks = columns
		double observations[];
		
		List<Point2D> cameras = new ArrayList<>();
		List<Point2D> landmarks = new ArrayList<>();

		public FuncGradient(int numCameras, int numLandmarks,
							double observations[] ) {
			this.numCameras = numCameras;
			this.numLandmarks = numLandmarks;
			this.observations = observations;
		}

		/**
		 * The function.
		 * 
		 * @param input Parameters for input model.
		 * @param output Storage for the output give the model.
		 */
		@Override
		public void process(double[] input, double[] output) {
			decode(numCameras,numLandmarks,input,cameras, landmarks);

			int index = 0;
			for (int i = 0; i < cameras.size(); i++) {
				Point2D c = cameras.get(i);
				for (int j = 0; j < landmarks.size(); j++, index++) {
					Point2D l = landmarks.get(j);
					output[index] = Math.atan((l.y-c.y)/(l.x-c.x))-observations[index];

					if(UtilEjml.isUncountable(output[index]))
						throw new RuntimeException("Egads");
				}
			}
		}

		@Override
		public int getNumOfInputsN() {
			return 2*numCameras + 2*numLandmarks;
		}

		@Override
		public int getNumOfOutputsM() {
			return numCameras*numLandmarks;
		}

		/**
		 * The Jaoobian. Split in a left and right hand side for the Schur Complement.
		 * 
		 * @param input Vector with input parameters.
		 * @param left (Output) left side of jacobian. Will be resized to fit.
		 * @param right (Output) right side of jacobian. Will be resized to fit.
		 */
		@Override
		public void process(double[] input, DMatrixSparseCSC left, DMatrixSparseCSC right) {
			decode(numCameras,numLandmarks,input,cameras, landmarks);

			int N = numCameras*numLandmarks;
			DMatrixSparseTriplet tripletLeft = new DMatrixSparseTriplet(N,2*numCameras,1);
			DMatrixSparseTriplet tripletRight = new DMatrixSparseTriplet(N,2*numLandmarks,1);

			int output = 0;
			for (int i = 0; i < numCameras; i++) {
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
				for (int j = 0; j < numCameras; j++) {
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

			ConvertDMatrixStruct.convert(tripletLeft,left);
			ConvertDMatrixStruct.convert(tripletRight,right);
		}
	}

	protected static void decode( int numCameras , int numLandmarks, 
								  double[] input, List<Point2D> cameras , List<Point2D> landmarks ) {
		cameras.clear();
		landmarks.clear();
		int index = 0;
		for (int i = 0; i < numCameras; i++) {
			cameras.add( new Point2D(input[index++],input[index++]) );
		}
		for (int i = 0; i < numLandmarks; i++) {
			landmarks.add( new Point2D(input[index++],input[index++]) );
		}
	}

	static class Point2D {
		double x,y;

		public Point2D(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
