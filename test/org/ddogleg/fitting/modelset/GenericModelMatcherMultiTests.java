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

package org.ddogleg.fitting.modelset;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Generic tests for implementations of {@link ModelMatcherMulti}.  Performs all the tests for
 * the single model equivalent plus additional tests to see if it is selecting the correct model.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"NullAway.Init"})
public abstract class GenericModelMatcherMultiTests extends GenericModelMatcherTests{
	/**
	 * Wrapper to provide support for single ModelMatcher tests
	 */
	@Override
	public ModelMatcher<double[],Double> createModelMatcher(
			ModelManager<double[]> manager,
			DistanceFromModel<double[],Double> distance,
			ModelGenerator<double[],Double> generator,
			ModelFitter<double[],Double> fitter,
			int minPoints, double fitThreshold)
	{
		List<ModelInfo> models = new ArrayList<ModelInfo>();

		ModelInfo model0 = new ModelInfo();
		model0.manager = manager;
		model0.distance = distance;
		model0.generator = generator;
		model0.minPoints = minPoints;
		model0.fitThreshold = fitThreshold;
		model0.fitter = fitter;

		models.add( model0 );

		return (ModelMatcher)createModelMatcher(models);
	}

	/**
	 * Create two models.  One will generate larger errors than the other.  See if it selects the right one.
	 */
	@Test
	public void checkSelectModel() {
		List<ModelInfo> models = new ArrayList<ModelInfo>();

		ModelInfo model0 = new ModelInfo();
		model0.manager = new DoubleArrayManager(1);
		model0.distance = new RandomDistanceModel(5,0.3);
		model0.generator = new DoNothingModelFitter(2);
		model0.minPoints = -1;
		model0.fitThreshold = 0.1;

		ModelInfo model1 = new ModelInfo();
		model1.manager = new DoubleArrayManager(1);
		model1.distance = new RandomDistanceModel(0,0.05);
		model1.generator = new DoNothingModelFitter(3);
		model1.minPoints = -1;
		model1.fitThreshold = 0.1;

		models.add(model0);
		models.add(model1);

		List<Double> points = new ArrayList<Double>();
		for( int i = 0; i < 100; i++ ) {
			points.add(0.1+i);
		}

		ModelMatcherMulti<Double> alg = createModelMatcher(models);

		assertTrue(alg.process(points));

		assertEquals(1,alg.getModelIndex());
	}

	public abstract ModelMatcherMulti<Double> createModelMatcher( List<ModelInfo> models );

	public static class ModelInfo {
		public ModelManager<double[]> manager;
		public DistanceFromModel<double[],Double> distance;
		public ModelGenerator<double[],Double> generator;
		public ModelFitter<double[],Double> fitter;
		public int minPoints;
		public double fitThreshold;
	}
}
