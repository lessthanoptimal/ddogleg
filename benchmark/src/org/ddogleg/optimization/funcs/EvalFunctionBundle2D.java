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

package org.ddogleg.optimization.funcs;

import org.ddogleg.optimization.functions.FunctionNtoM;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 2D bundle adjustment. camera is specified by (x,y)  and landmarks by (x,y) observations are angles.
 *
 * @author Peter Abeles
 */
public abstract class EvalFunctionBundle2D<S extends DMatrix> implements
		EvalFuncLeastSquaresSchur<S> ,
		EvalFuncLeastSquares<S>
{

	Random rand;

	double length,depth;
	int numCamera,numLandmarks;

	double observations[];

	double optimal[];
	double initial[];

	public EvalFunctionBundle2D() {
		this(0xDEADBEF,20,10,20,10);
	}

	public EvalFunctionBundle2D(long seed , double length, double depth,
								int numCamera, int numLandmarks)
	{
		this.length = length;
		this.depth = depth;
		this.numCamera = numCamera;
		this.numLandmarks = numLandmarks;

		this.rand = new Random(seed);

		observations = new double[numCamera*numLandmarks];

		optimal = new double[2*(numCamera+numLandmarks)];
		initial = new double[2*(numCamera+numLandmarks)];

		List<Point2D> cameras = new ArrayList<>();
		List<Point2D> landmarks = new ArrayList<>();

		int index = 0;
		for (int i = 0; i < numCamera; i++) {
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

		for (int i = 0; i < optimal.length; i++) {
			initial[i] = optimal[i] + rand.nextGaussian()*0.5;
		}

		index = 0;
		for (int i = 0; i < numCamera; i++) {
			Point2D c = cameras.get(i);
			for (int j = 0; j < numLandmarks; j++, index++) {
				Point2D l = landmarks.get(j);
				observations[index] = Math.atan((l.y-c.y)/(l.x-c.x));

				if(UtilEjml.isUncountable(observations[index]))
					throw new RuntimeException("Egads");
			}
		}
	}

	@Override
	public FunctionNtoM getFunction() {
		return new Func();
	}

	@Override
	public double[] getInitial() {
		return initial;
	}

	@Override
	public double[] getOptimal() {
		return optimal;
	}


	public class Func implements FunctionNtoM
	{
		List<Point2D> cameras = new ArrayList<>();
		List<Point2D> landmarks = new ArrayList<>();

		@Override
		public void process(double[] input, double[] output) {
			decode(input,cameras, landmarks);

			int index = 0;
			for (int i = 0; i < numCamera; i++) {
				Point2D c = cameras.get(i);
				for (int j = 0; j < numLandmarks; j++, index++) {
					Point2D l = landmarks.get(j);
					output[index] = Math.atan((l.y-c.y)/(l.x-c.x))-observations[index];

					if(UtilEjml.isUncountable(output[index]))
						throw new RuntimeException("Egads");
				}
			}
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

	protected void decode(double[] input, List<Point2D> cameras , List<Point2D> landmarks ) {
		cameras.clear();
		landmarks.clear();
		int index = 0;
		for (int i = 0; i < numCamera; i++) {
			cameras.add( new Point2D(input[index++],input[index++]) );
		}
		for (int i = 0; i < numLandmarks; i++) {
			landmarks.add( new Point2D(input[index++],input[index++]) );
		}
	}

	protected static class Point2D {
		double x,y;

		public Point2D(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
