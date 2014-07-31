/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.nn.alg.*;
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

	/**
	 * Performs an optimal {@link NearestNeighbor} search using K-D tree. Distance measure is Euclidean squared.
	 *
	 * @see KdTreeNearestNeighbor
	 * @see AxisSplitterMedian
	 *
	 * @param <D> Associated data type.
	 * @return {@link NearestNeighbor} implementation
	 */
	public static <D> NearestNeighbor<D> kdtree() {
		return new KdTreeNearestNeighbor<D>();
	}

	/**
	 * Performs an approximate {@link NearestNeighbor} search using K-D tree.  Node are searched in Best-Bin-First
	 * order.  Distance measure is Euclidean squared.
	 *
	 * @see KdTreeNearestNeighbor
	 * @see org.ddogleg.nn.alg.KdTreeSearch1Bbf
	 * @see AxisSplitterMedian
	 *
	 * @param maxNodesSearched Maximum number of nodes it will search.  Controls speed and accuracy.
	 * @param <D> Associated data type.
	 * @return {@link NearestNeighbor} implementation
	 */
	public static <D> NearestNeighbor<D> kdtree( int maxNodesSearched ) {
		return new KdTreeNearestNeighbor<D>(new KdTreeSearch1Bbf(maxNodesSearched),
				new KdTreeSearchNBbf(maxNodesSearched),new AxisSplitterMedian<D>());
	}

	/**
	 * Approximate {@link NearestNeighbor} search which uses a set of randomly generated K-D trees and a Best-Bin-First
	 * search.  Designed to work in high dimensional space. Distance measure is Euclidean squared.
	 *
	 * @see KdForestBbfSearch
	 * @see AxisSplitterMedian
	 *
	 * @param maxNodesSearched  Maximum number of nodes it will search.  Controls speed and accuracy.
	 * @param numTrees Number of trees that are considered.  Try 10 and tune.
	 * @param numConsiderSplit Number of nodes that are considered when generating a tree.  Must be less than the
	 *                         point's dimension.  Try 5
	 * @param randomSeed Seed used by random number generator
	 * @param <D> Associated data type.
	 * @return {@link NearestNeighbor} implementation
	 */
	public static <D> NearestNeighbor<D> kdRandomForest( int maxNodesSearched , int numTrees , int numConsiderSplit ,
														 long randomSeed ) {

		Random rand = new Random(randomSeed);

		return new KdForestBbfSearch<D>(numTrees,maxNodesSearched,
				new AxisSplitterMedian<D>(new AxisSplitRuleRandomK(rand,numConsiderSplit)));
	}

	/**
	 * Performs an optimal {@link NearestNeighbor} by exhaustively consider all possible solutions.
	 * Distance measure is Euclidean squared.
	 *
	 * @see org.ddogleg.nn.alg.ExhaustiveNeighbor
	 *
	 * @param <D> Associated data type.
	 * @return {@link NearestNeighbor} implementation
	 */
	public static <D> NearestNeighbor<D> exhaustive() {
		return new WrapExhaustiveNeighbor<D>();
	}

	/**
	 * {@link VpTree Vantage point} tree implementation for nearest neighbor search.  Slower than KD-Tree on
	 * random data, but faster than it for some pathological cases.
	 *
	 * @see VpTree
	 *
	 * @param randSeed Random seed
	 * @param <D> Associated data type.
	 * @return {@link NearestNeighbor} implementation
	 */
	public static <D> NearestNeighbor<D> vptree( long randSeed ) {
		return new VpTree<D>(randSeed);
	}
}
