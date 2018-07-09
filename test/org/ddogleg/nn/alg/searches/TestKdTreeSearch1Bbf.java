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

package org.ddogleg.nn.alg.searches;

import org.ddogleg.nn.alg.KdTreeSearch1;
import org.ddogleg.nn.alg.distance.KdTreeEuclideanSq_F64;

/**
 * @author Peter Abeles
 */
public class TestKdTreeSearch1Bbf extends StandardKdTreeSearch1Tests {
	@Override
	public KdTreeSearch1<double[]> createAlg() {
		// specify so many max nodes that it will be optimal
		return new KdTreeSearch1Bbf<>(new KdTreeEuclideanSq_F64(N),10000);
	}
}
