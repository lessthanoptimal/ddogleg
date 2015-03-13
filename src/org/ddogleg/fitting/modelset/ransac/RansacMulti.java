/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.fitting.modelset.ransac;

import org.ddogleg.fitting.modelset.DistanceFromModel;
import org.ddogleg.fitting.modelset.ModelGenerator;
import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.fitting.modelset.ModelMatcherMulti;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * <p>
 * Modification of {@link Ransac RANSAC} that finds the best fit model and model parameters to a set of data.  A model
 * is a mathematical description of a specific object type (e.g. circle, square) and model parameters is the description
 * (e.g. radius and center point for a circle).  The minimum (or user specified) set of points is drawn from the
 * set of all points.  Then for each model parameters are estimated.  The model and parameter pair with the largest
 * inliers set is returned.
 * </p>
 *
 * <p>
 * To change the default behavior of the class for specific applications the child class can override internal
 * functions.  Suggestions are shown below.
 * </p>
 * <ul>
 * <li>{@link #checkExitIteration}: Override to provide custom logic for when the RANSAC iteration should stop</li>
 * <li>{@link #selectMatchSet}: Override to provide custom for how the inlier set is found.  Be sure to
 * set up matchToInput[] correctly.</li>
 * </ul>
 *
 * @author Peter Abeles
 */
public class RansacMulti<Point> implements ModelMatcherMulti<Point> {
	// how many points are drawn to generate the model
	protected int sampleSize;

	// used to randomly select points/samples
	protected Random rand;

	// list of Points passed in by the user
	protected List<Point> dataSet;

	// list of points which are a candidate for the best fit set
	protected List<Point> candidatePoints = new ArrayList<Point>();

	// list of samples from the best fit model
	protected List<Point> bestFitPoints = new ArrayList<Point>();

	// List of information on objects which can fit the data
	protected List<ObjectType> objectTypes;
	// Storage for object model parameters which bestFitParam can be set to
	protected List<Object> objectParam = new ArrayList<Object>();
	// Storage for object model parameters which are used to compute candidate models
	protected List<Object> objectCandidateParam = new ArrayList<Object>();

	// the best model found so far
	protected Object bestFitParam;
	// the index of the model which is the best fit
	protected int bestFitModelIndex;

	// which iteration is it on
	protected int iteration;
	// the maximum number of iterations it will perform
	protected int maxIterations;

	// the set of points which were initially sampled
	protected FastQueue<Point> initialSample;

	// list of indexes converting it from match set to input list
	protected int []matchToInput = new int[1];
	protected int []bestMatchToInput = new int[1];

	/**
	 * Creates a new instance of the ransac algorithm.  The number of points sampled will default to the
	 * minimum number.  To override this default invoke {@link #setSampleSize(int)}.
	 *
	 * @param randSeed		 The random seed used by the random number generator.
	 * @param maxIterations	The maximum number of iterations the RANSAC algorithm will perform.
	 * @param objectTypes Description of the different types of objects it can detect
	 * @param typePoint Class of Point
	 */
	public RansacMulti(long randSeed,
					   int maxIterations,
					   List<ObjectType> objectTypes,
					   Class<Point> typePoint) {
		this.rand = new Random(randSeed);
		this.maxIterations = maxIterations;
		this.objectTypes = objectTypes;

		// Set the sample size to be the largest
		this.sampleSize = 0;
		for( int i = 0; i < objectTypes.size(); i++ ) {
			ObjectType o = objectTypes.get(i);

			// add data to store solutions
			objectParam.add( o.modelManager.createModelInstance() );
			objectCandidateParam.add( o.modelManager.createModelInstance() );

			// see if the user wants to use the default or not
			if( o.sampleSize <= 0 ) {
				o.sampleSize = o.modelGenerator.getMinimumPoints();
			}

			if( o.sampleSize > sampleSize ) {
				sampleSize = o.sampleSize;
			}
		}

		initialSample = new FastQueue<Point>(typePoint,false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean process(List<Point> dataSet ) {
		this.dataSet = dataSet;

		// see if it has the minimum number of points
		if (dataSet.size() < sampleSize )
			return false;

		// configure internal data structures
		initialize(dataSet);

		// iterate until it has exhausted all iterations or stop if the entire data set
		// is in the inlier set
		for (iteration = 0; checkExitIteration(); iteration++) {
			// sample the a small set of points
			initialSample.reset();
			Ransac.randomDraw(dataSet, sampleSize, initialSample.toList(), rand);

			// try fitting all the different kinds of objects
			for( int j = 0; j < objectTypes.size(); j++ ) {

				ObjectType model = objectTypes.get(j);
				Object param = objectCandidateParam.get(j);

				// adjust the list size to what was requested by this particular model
				initialSample.size = model.sampleSize;

				// get the candidate(s) for this sample set
				if( model.modelGenerator.generate(initialSample.toList(), param ) ) {

					// see if it can find a model better than the current best one
					selectMatchSet(model.modelDistance, model.thresholdFit, param);

					// save this results
					if (bestFitPoints.size() < candidatePoints.size()) {
						bestFitModelIndex = j;
						objectCandidateParam.set(j,objectParam.get(j));
						objectParam.set(j,param);
						setBestModel(param);
					}
				}
			}
		}

		return bestFitPoints.size() > 0;
	}

	/**
	 * Checks to see if it should stop the RANSAC iterations.  A child class can override this class to perform
	 * a custom behavior.  The default code is shown below:
	 *
	 * <pre>
	 * {@code iteration < maxIterations && bestFitPoints.size() != dataSet.size()}
	 * </pre>
	 *
	 * @return if true RANSAC should continue iterating if false then RANSAC will stop.
	 */
	protected boolean checkExitIteration() {
		return iteration < maxIterations && bestFitPoints.size() != dataSet.size();
	}

	/**
	 * Initialize internal data structures before performing RANSAC iterations
	 */
	protected void initialize( List<Point> dataSet ) {
		bestFitPoints.clear();

		if( dataSet.size() > matchToInput.length ) {
			matchToInput = new int[ dataSet.size() ];
			bestMatchToInput = new int[ dataSet.size() ];
		}
	}

	/**
	 * Exhaustively searches through the list of points contained in 'dataSet' for the set of inliers which match
	 * the provided model.  It keeps track of the mapping between the index of the inlier list and the 'dataSet' list
	 * using the matchToInput[] array.   If there is no corresponding (can't happen by default) match then -1
	 * should be set in matchToInput..
	 *
	 * @param modelDistance Computes
	 */
	protected <Model>void selectMatchSet( DistanceFromModel<Model,Point> modelDistance ,
										  double threshold, Model param) {
		candidatePoints.clear();
		modelDistance.setModel(param);

		for (int i = 0; i < dataSet.size(); i++) {
			Point point = dataSet.get(i);

			double distance = modelDistance.computeDistance(point);
			if (distance < threshold) {
				matchToInput[candidatePoints.size()] = i;
				candidatePoints.add(point);
			}
		}
	}

	/**
	 * Turns the current candidates into the best ones.
	 */
	protected void setBestModel( Object param ) {
		List<Point> tempPts = candidatePoints;
		candidatePoints = bestFitPoints;
		bestFitPoints = tempPts;

		int tempIndex[] = matchToInput;
		matchToInput = bestMatchToInput;
		bestMatchToInput = tempIndex;

		bestFitParam = param;
	}

	public List<Point> getMatchSet() {
		return bestFitPoints;
	}

	@Override
	public int getInputIndex(int matchIndex) {
		return bestMatchToInput[matchIndex];
	}

	@Override
	public double getFitQuality() {
		return bestFitPoints.size();
	}

	@Override
	public Object getModelParameters() {
		return bestFitParam;
	}

	@Override
	public int getModelIndex() {
		return bestFitModelIndex;
	}

	public int getInlierSize() {
		return bestFitPoints.size();
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	protected List<Point> getCandidatePoints() {
		return candidatePoints;
	}

	protected FastQueue<Point> getInitialSample() {
		return initialSample;
	}

	@Override
	public int getMinimumSize() {
		return sampleSize;
	}

	/**
	 * Override the number of points that are sampled and used to generate models.  If this value
	 * is not set it defaults to the minimum number.
	 *
	 * @param sampleSize Number of sample points.
	 */
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	public int getIteration() {
		return iteration;
	}

	/**
	 * Describes a model and RANSAC fit parameters for specific type of object.
	 */
	public static class ObjectType<Model,Point>
	{
		/** how close a point needs to be considered part of the model */
		public double thresholdFit;
		/**
		 *  The number of points it samples when generating a set of model parameters.
		 *  if &le; 0 then the minimum number will be used
 		 */
		public int sampleSize = -1;
		/** generates an initial model given a set of points */
		public ModelGenerator<Model,Point> modelGenerator;
		/** computes the distance a point is from the model */
		public DistanceFromModel<Model,Point> modelDistance;
		/** Used to create new models and copy models */
		public ModelManager<Model> modelManager;
	}
}