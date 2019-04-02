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
import org.ddogleg.nn.alg.searches.KdTreeSearch1Standard;
import org.ddogleg.nn.alg.searches.KdTreeSearchNStandard;

import java.util.List;

/**
 * Wrapper around {@link KdTree} for {@link NearestNeighbor}
 *
 * @author Peter Abeles
 */
public class KdTreeNearestNeighbor<P> implements NearestNeighbor<P> {

	// tree being searched
	KdTree tree;
	// creates a tree from data
	KdTreeConstructor<P> constructor;
	// searches the tree for the nearest neighbor
	KdTreeSearch1<P> search1;
	// searches the tree for the N nearest neighbors
	KdTreeSearchN<P> searchN;
	// Used internally during tree construction
	AxisSplitter<P> splitter;

	// used to recycle memory
	KdTreeMemory<P> memory = new KdTreeMemory<>();

	public KdTreeNearestNeighbor(KdTreeSearch1<P> search1, KdTreeSearchN<P> searchN, AxisSplitter<P> splitter) {
		this.search1 = search1;
		this.searchN = searchN;
		this.splitter = splitter;
		constructor = new KdTreeConstructor<>(memory,splitter);
	}

	public KdTreeNearestNeighbor( KdTreeDistance<P> distance ) {
		this( new KdTreeSearch1Standard<>(distance), new KdTreeSearchNStandard<>(distance), new AxisSplitterMedian<>(distance));
	}

	@Override
	public void setPoints(List<P> points, boolean trackIndicies) {
		if( tree != null )
			memory.recycleGraph(tree);
		tree = constructor.construct(points,trackIndicies);
	}

	@Override
	public Search<P> createSearch() {
		return new InternalSearch(search1.copy(),searchN.copy());
	}

	private class InternalSearch extends KdTreeInternalSearch<P> {

		InternalSearch(KdTreeSearch1<P> search1, KdTreeSearchN<P> searchN) {
			super(search1, searchN);
		}

		@Override
		void setTree() {
			((KdTreeSearch1Standard)search1).setTree(tree);
			((KdTreeSearchNStandard)searchN).setTree(tree);
		}
	}
}
