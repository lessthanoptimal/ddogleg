/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.fitting.modelset.lmeds;

import org.ddogleg.DDoglegConcurrency;
import org.ddogleg.fitting.modelset.DistanceFromModel;
import org.ddogleg.fitting.modelset.ModelGenerator;
import org.ddogleg.fitting.modelset.ModelManager;
import org.ddogleg.sorting.QuickSelect;
import org.ddogleg.struct.Factory;
import org.jetbrains.annotations.Nullable;
import pabeles.concurrency.GrowArray;

import java.util.List;
import java.util.Objects;

import static org.ddogleg.fitting.modelset.ransac.Ransac.addSelect;
import static org.ddogleg.fitting.modelset.ransac.Ransac.randomDraw;

/**
 * Concurrent version of {@link LeastMedianOfSquares}
 *
 * @author Peter Abeles
 */
public class LeastMedianOfSquares_MT<Model, Point> extends LeastMedianOfSquares<Model, Point> {
	// Storage for each thread's state
	final GrowArray<TrialHelper> helpers;

	//------------------- BEGIN LOCK OWNED
	final Object lock = new Object();
	// The trial which has the best. See code comments below
	volatile int bestMedianTrial;
	// The helper which contains the best solution
	volatile @Nullable TrialHelper bestHelper;
	//------------------- END LOCK OWNED

	/**
	 * @see LeastMedianOfSquares
	 */
	public LeastMedianOfSquares_MT( long randSeed, int totalCycles, double maxMedianError,
									double inlierFraction,
									ModelManager<Model> modelManager,
									Class<Point> pointType ) {
		super(randSeed, totalCycles, maxMedianError, inlierFraction, modelManager, pointType);

		// Initialize size is zero. so factories not being defined won't cause problems
		helpers = new GrowArray<>(TrialHelper::new, ( a ) -> a.initialize(matchToInput.length), (Class)TrialHelper.class);
	}

	public LeastMedianOfSquares_MT( long randSeed,
									int totalCycles,
									ModelManager<Model> modelManager,
									Class<Point> pointType ) {
		this(randSeed, totalCycles, Double.MAX_VALUE, 0, modelManager, pointType);
	}

	@Override
	public boolean process( List<Point> dataSet ) {
		if (dataSet.size() < sampleSize)
			return false;

		checkTrialGenerators();

		final int N = dataSet.size();

		// make sure the array is large enough.  If not declare a new one that is
		if (matchToInput.length < N) {
			matchToInput = new int[N];
		}

		bestMedian = Double.MAX_VALUE;

		helpers.reset();
		DDoglegConcurrency.loopFor(0, totalCycles, 1, helpers, ( helper, trial ) -> {
			// See RANSAC for a detailed description for why this is done. It's related to concurrency
			randomDraw(helper.selectedIdx, N, sampleSize, trialRNG.get(trial));
			addSelect(helper.selectedIdx, sampleSize, dataSet, helper.initialSample);

			if (!helper.modelGenerator.generate(helper.initialSample, helper.candidate))
				return;

			helper.modelDistance.setModel(helper.candidate);
			helper.modelDistance.distances(dataSet, helper.errors.data);

			double median = QuickSelect.select(helper.errors.data, (int)(N*errorFraction + 0.5), N);

			// see if it could be better and avoid the synchronize
			if (median > bestMedian) {
				return;
			}

			synchronized (lock) {
				// Need to do it again since it wasn't locked
				if (median > bestMedian)
					return;

				// There is a tie. Current results are only better if they come from an earlier trial
				// This is done to ensure identical results with single thread version.
				if (median == bestMedian && bestMedianTrial < trial)
					return;

				helper.swapModels();
				bestMedian = median;
				bestMedianTrial = trial;
				bestHelper = helper;
			}
		});

		// bestHelper can be null if every attempt to generate a model failed
		if (bestHelper == null)
			return false;

		super.helper = bestHelper;

		// if configured to do so compute the inlier set
		computeInlierSet(dataSet, N, Objects.requireNonNull(super.helper));

		// If bestMedian == MAX_VALUE that means no model was found. This needs to fail even if maxMedianError
		// has been set to MAX_VALUE.
		return bestMedian != Double.MAX_VALUE && bestMedian < maxMedianError;
	}

	@Override
	public void setModel( Factory<ModelGenerator<Model, Point>> factoryGenerator,
						  Factory<DistanceFromModel<Model, Point>> factoryDistance ) {
		this.factoryGenerator = factoryGenerator;
		this.factoryDistance = factoryDistance;

		// discard previous helpers since they are no longer valid
		helpers.releaseInternalArray();

		sampleSize = factoryGenerator.newInstance().getMinimumPoints();
	}
}
