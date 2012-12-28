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

package org.ddogleg.nn;

import org.ddogleg.nn.alg.AxisSplitRuleRandomK;
import org.ddogleg.nn.alg.AxisSplitterMedian;
import org.ddogleg.nn.alg.KdTreeSearchBbf;
import org.ddogleg.nn.wrap.KdForestBbfSearch;
import org.ddogleg.nn.wrap.KdTreeNearestNeighbor;
import org.ddogleg.nn.wrap.WrapExhaustiveNeighbor;

import java.util.Random;

/**
 * Factory for creating implementations of {@link NearestNeighbor}.
 *
 * @author Peter Abeles
 */
public class FactoryNearestNeighbor {

	public static <D> NearestNeighbor<D> kdtree() {
		return new KdTreeNearestNeighbor<D>();
	}

	public static <D> NearestNeighbor<D> kdtree( int maxNodesSearched ) {
		return new KdTreeNearestNeighbor<D>(new KdTreeSearchBbf(maxNodesSearched),new AxisSplitterMedian<D>());
	}

	public static <D> NearestNeighbor<D> kdRandomForest( int maxNodesSearched , int numTrees , int numConsiderSplit ,
														 long randomSeed ) {

		Random rand = new Random(randomSeed);

		return new KdForestBbfSearch<D>(numTrees,maxNodesSearched,
				new AxisSplitterMedian<D>(new AxisSplitRuleRandomK(rand,numConsiderSplit)));
	}

	public static <D> NearestNeighbor<D> exhaustive() {
		return new WrapExhaustiveNeighbor<D>();
	}
}
