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

package org.ddogleg.fitting.modelset.ransac;

import org.ddogleg.DDoglegConcurrency;
import org.ddogleg.fitting.modelset.DistanceFromModel;
import org.ddogleg.fitting.modelset.ModelGenerator;
import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.struct.Factory;
import org.jetbrains.annotations.Nullable;
import pabeles.concurrency.GrowArray;

import java.util.List;
import java.util.Objects;

/**
 * Concurrent implementation of {@link Ransac}. It will produce identical results when given the same seed.
 *
 * @author Peter Abeles
 */
public class Ransac_MT<Model, Point> extends Ransac<Model, Point> {

	// Storage for each thread's state
	final GrowArray<TrialHelper> helpers;

	//------------------- BEGIN LOCK OWNED
	final Object lock = new Object();
	// Size of the best model across all threads
	// NOTE: Reading from an int is safe across threads on 32-bit and 64-bit machines.
	volatile int bestInlierSize;
	// The trial which has the best. See code comments below
	volatile int bestInlierTrial;
	// The helper which contains the best solution
	volatile @Nullable TrialHelper bestHelper;
	//------------------- END LOCK OWNED

	public Ransac_MT( long randSeed, int maxIterations, double thresholdFit,
					  ModelManager<Model> modelManager,
					  Class<Point> pointType ) {
		super(randSeed, maxIterations, thresholdFit, modelManager, pointType);

		// This should be safe even though the factories aren't defined size the initial size will be zero
		helpers = new GrowArray<>(TrialHelper::new, TrialHelper::reset, TrialHelper.class);
	}

	@Override
	public boolean process( List<Point> dataSet ) {
		// see if it has the minimum number of points
		if (dataSet.size() < sampleSize)
			return false;

		Objects.requireNonNull(factoryDistance, "Must specify the model");

		// make sure there is a RNG for each trial
		checkTrialGenerators();

		bestInlierSize = -1;
		bestInlierTrial = -1;
		bestHelper = null;

		// iterate until it has exhausted all iterations or stop if the entire data set
		// is in the inlier set
		DDoglegConcurrency.loopFor(0, maxIterations, 1, helpers, ( helper, trial ) -> {
			// See comment in single threaded code. This convoluted way of sampling is needed to ensure
			// single and multi threaded code produce the same results.
			randomDraw(helper.selectedIdx, dataSet.size(), sampleSize, trialRNG.get(trial));
			addSelect(helper.selectedIdx, sampleSize, dataSet, helper.initialSample);

			// get the candidate(s) for this sample set
			if (!helper.modelGenerator.generate(helper.initialSample, helper.candidateParam))
				return;

			// NOTE: A design requirement is that produce identical results to the single thread version.
			//       That means that if there are multiple models with the same number of inliers then the
			//       model which was generated in a lower trial number is selected

			// the global best inlier size is set to -1 so that if a model just as good is found now
			// we can compare trial numbers to see who the winner is
			int threshold = Math.max(bestInlierSize - 1, helper.bestFitPoints.size());

			// see if it can find a model better than the current best one
			if (!helper.selectMatchSet(dataSet, threshold, thresholdFit, helper.candidateParam))
				return;

			// See if the global best is the winner
			if (bestInlierSize > helper.candidatePoints.size())
				return;

			synchronized (lock) {
				// Need to do it again since previously it wasn't locked and might have changed
				if (bestInlierSize > helper.candidatePoints.size())
					return;

				// There is a tie. Current results are only better if they come from an earlier trial
				if (bestInlierSize == helper.candidatePoints.size() && bestInlierTrial < trial)
					return;

				// Record the new champion
				bestInlierSize = helper.candidatePoints.size();
				bestInlierTrial = trial;
				bestHelper = helper;
			}

			// Save the best results
			helper.swapCandidateWithBest();
		});

		// Set the winner to the helper with the best results
		TrialHelper result = super.helper = Objects.requireNonNull(bestHelper);

		return result.bestFitPoints.size() > 0;
	}

	@Override
	public void setModel( Factory<ModelGenerator<Model, Point>> factoryGenerator,
						  Factory<DistanceFromModel<Model, Point>> factoryDistance ) {
		this.factoryGenerator = factoryGenerator;
		this.factoryDistance = factoryDistance;

		// discard previous helpers since they are no longer valid
		helpers.releaseInternalArray();

		// Figure out how many points to sample
		helpers.resize(1);
		sampleSize = helpers.get(0).modelGenerator.getMinimumPoints();
	}
}
