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
import org.ddogleg.nn.NnData;
import org.ddogleg.nn.alg.*;
import org.ddogleg.nn.alg.searches.KdTreeSearch1Standard;
import org.ddogleg.nn.alg.searches.KdTreeSearchNStandard;
import org.ddogleg.struct.FastQueue;

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
	KdTreeSearch1<P> search;
	// searches the tree for the N nearest neighbors
	KdTreeSearchN<P> searchN;
	// Used internally during tree construction
	AxisSplitter<P> splitter;

	// storage for multiple results
	FastQueue<KdTreeResult> found = new FastQueue<KdTreeResult>(KdTreeResult.class,true);

	// used to recycle memory
	KdTreeMemory<P> memory = new KdTreeMemory<>();

	public KdTreeNearestNeighbor(KdTreeSearch1<P> search, KdTreeSearchN<P> searchN, AxisSplitter<P> splitter) {
		this.search = search;
		this.searchN = searchN;
		this.splitter = splitter;
	}

	public KdTreeNearestNeighbor( KdTreeDistance<P> distance ) {
		this( new KdTreeSearch1Standard<>(distance), new KdTreeSearchNStandard<>(distance), new AxisSplitterMedian<>(distance));
		constructor = new KdTreeConstructor<>(memory,splitter);
	}

	@Override
	public void setPoints(List<P> points, boolean trackIndicies) {
		if( tree != null )
			memory.recycleGraph(tree);
		tree = constructor.construct(points,trackIndicies);
		search.setTree(tree);
		searchN.setTree(tree);
	}

	@Override
	public boolean findNearest( P point , double maxDistance , NnData<P> result ) {
		if( maxDistance < 0 )
			search.setMaxDistance(Double.MAX_VALUE);
		else
			search.setMaxDistance(maxDistance);
		KdTree.Node found = search.findNeighbor(point);
		if( found == null )
			return false;

		result.point = (P)found.point;
		result.index = found.index;
		result.distance = search.getDistance();

		return true;
	}

	@Override
	public void findNearest(P point, double maxDistance, int numNeighbors, FastQueue<NnData<P>> results) {
		results.reset();

		if( maxDistance <= 0 )
			searchN.setMaxDistance(Double.MAX_VALUE);
		else
			searchN.setMaxDistance(maxDistance);

		found.reset();
		searchN.findNeighbor(point, numNeighbors, found);

		for( int i = 0; i < found.size; i++ ) {
			KdTreeResult k = found.get(i);
			NnData<P> r = results.grow();

			r.point = (P)k.node.point;
			r.index = k.node.index;
			r.distance = k.distance;
		}
	}
}
