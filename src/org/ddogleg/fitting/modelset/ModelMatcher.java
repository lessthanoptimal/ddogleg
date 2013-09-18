/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.fitting.modelset;

import java.util.List;


/**
 * <p>
 * Given a set of points and it finds a set of model parameters which fit the data robustly.  Some of the points are
 * assumed to be noise and are pruned.  The set of points which fit the found parameters and their index in the
 * input list are returned.
 * </p>
 *
 * @param <Model> Type of model being fitted.
 * @param <Point> Type of data point being fitted.
 *
 * @author Peter Abeles
 */
public interface ModelMatcher<Model, Point> {

	/**
	 * Finds a set of points from the provided list that are a good fit for the internal model and
	 * computes the fit parameters for the model.
	 *
	 * @param dataSet Set of points (with noise) that are to be fit.
	 * @return true if it successfully found a solution or false if not.
	 */
	public boolean process(List<Point> dataSet );

	/**
	 * Model for the match set
	 *
	 * @return model.
	 */
	public Model getModelParameters();

	/**
	 * A set of points which match the provided parameters.
	 *
	 * @return List of points in the match set.
	 */
	public List<Point> getMatchSet();

	/**
	 * For an item in the match set, return the index of the item in the original input set.
	 *
	 * @param matchIndex Index of an element in the match set.
	 * @return Index of the same element in the original input list.
	 */
	public int getInputIndex( int matchIndex );

	/**
	 * Returns the metric used to evaluate the quality of fit.  Meaning is implementation specific.  Larger
	 * or smaller values could be preferred depending on implementation..
	 *
	 * @return Quality of fit to matched set of points
	 */
	public double getFitQuality();

	/**
	 * This is the minimum number of observations which can be input and produce a valid model.
	 *
	 * @return Minimum number of sample points
	 */
	public int getMinimumSize();
}
