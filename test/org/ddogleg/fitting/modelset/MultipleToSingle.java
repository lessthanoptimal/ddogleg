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
 * Converts a {@link ModelMatcherMulti} into a {@link ModelMatcher} for testing purposes.
 *
 * @author Peter Abeles
 */
public class MultipleToSingle implements ModelMatcher {

	ModelMatcherMulti alg;

	@Override
	public boolean process(List dataSet) {
		return alg.process(dataSet);
	}

	@Override
	public Object getModelParameters() {
		return alg.getModelParameters();
	}

	@Override
	public List getMatchSet() {
		return alg.getMatchSet();
	}

	@Override
	public int getInputIndex(int matchIndex) {
		return alg.getInputIndex(matchIndex);
	}

	@Override
	public double getFitQuality() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int getMinimumSize() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
