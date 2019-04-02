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

package org.ddogleg.nn.wrap;

import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.alg.*;
import org.ddogleg.nn.alg.searches.KdTreeSearch1Bbf;
import org.ddogleg.nn.alg.searches.KdTreeSearchNBbf;

import java.util.List;

/**
 * K-D tree search which searches through multiple trees.  The search is performed using a Best-Bin-First approach
 *
 * @author Peter Abeles
 */
public class KdForestBbfNearestNeighbor<P> implements NearestNeighbor<P> {

	// set of K-D trees which are to be searched
	KdTree[]forest;

	// creates the set of K-D trees given the same input
	KdTreeConstructor<P> constructor;

	AxisSplitter<P> splitter;

	KdTreeMemory<P> memory = new KdTreeMemory<>();

	// saved for searches
	int maxNodesSearched;
	KdTreeDistance<P> distance;

	public KdForestBbfNearestNeighbor(int numberOfTrees,
									  int maxNodesSearched,
									  KdTreeDistance<P> distance ,
									  AxisSplitter<P> splitter) {
		this.forest = new KdTree[ numberOfTrees ];
		this.splitter = splitter;
		this.maxNodesSearched = maxNodesSearched;
		this.distance = distance;

		this.constructor = new KdTreeConstructor<P>(memory,splitter);
	}

	@Override
	public void setPoints(List<P> points , boolean trackIndicies ) {
		if( forest[0] != null ) {
			for( int i = 0; i < forest.length; i++ )
				memory.recycleGraph(forest[i]);
		}
		for( int i = 0; i < forest.length; i++ )
			forest[i] = constructor.construct(points,trackIndicies);
	}

	@Override
	public Search<P> createSearch() {
		return new InternalSearch();
	}

	private class InternalSearch extends KdTreeInternalSearch<P> {

		InternalSearch() {
			super(new KdTreeSearch1Bbf<>(distance,maxNodesSearched),
					new KdTreeSearchNBbf<>(distance,maxNodesSearched));
		}

		@Override
		void setTree() {
			((KdTreeSearch1Bbf)search1).setTrees(forest);
			((KdTreeSearchNBbf)searchN).setTrees(forest);
		}
	}
}
