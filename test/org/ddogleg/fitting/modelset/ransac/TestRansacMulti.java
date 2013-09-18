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

package org.ddogleg.fitting.modelset.ransac;

import org.ddogleg.fitting.modelset.GenericModelMatcherMultiTests;
import org.ddogleg.fitting.modelset.ModelMatcherMulti;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestRansacMulti extends GenericModelMatcherMultiTests {

	public TestRansacMulti() {
		configure(0.9, 0.05, true);
	}

	@Override
	public ModelMatcherMulti<Double> createModelMatcher(List<ModelInfo> models) {

		List<RansacMulti.ObjectType> rm = new ArrayList<RansacMulti.ObjectType>();

		for( int i = 0; i < models.size(); i++ ) {
			RansacMulti.ObjectType rm0 = new RansacMulti.ObjectType();
			ModelInfo mo = models.get(i);

			rm0.modelGenerator = mo.generator;
			rm0.modelDistance = mo.distance;
			rm0.thresholdFit = mo.fitThreshold;
			rm0.sampleSize = mo.minPoints;

			rm.add(rm0);
		}

		return new RansacMulti<Double>(344, 2000, rm, Double.class );
	}

	/**
	 * See if it will select models with more of the correct points in it
	 */
	@Test
	public void selectMatchSet() {
		double modelVal = 50;

		List<Integer> dataSet = new ArrayList<Integer>();

		for (int i = 0; i < 200; i++) {
			dataSet.add(i);
		}

		List<RansacMulti.ObjectType> objectTypes = new ArrayList<RansacMulti.ObjectType>();

		RansacMulti.ObjectType obj0 = new RansacMulti.ObjectType();
		obj0.modelDistance = new TestRansac.DebugModelStuff((int) modelVal);
		obj0.modelGenerator = new TestRansac.DebugModelStuff((int) modelVal);
		obj0.thresholdFit = 1;

		objectTypes.add(obj0);

		RansacMulti<Integer> ransac = new RansacMulti<Integer>(234,20,objectTypes,Integer.class);
		ransac.setSampleSize(5);
		// declare the array so it doesn't blow up when accessed
		ransac.matchToInput = new int[ dataSet.size()];
		double param[] = new double[]{modelVal};

		ransac.selectMatchSet(obj0.modelDistance,dataSet, 4, param);

		assertTrue(ransac.candidatePoints.size() == 7);
	}


}
