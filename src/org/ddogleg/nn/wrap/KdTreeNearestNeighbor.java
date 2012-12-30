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

package org.ddogleg.nn.wrap;

import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.nn.alg.*;

import java.util.List;

/**
 * Wrapper around {@link KdTree} for {@link NearestNeighbor}
 *
 * @author Peter Abeles
 */
public class KdTreeNearestNeighbor<D> implements NearestNeighbor<D> {

	// tree being searched
	KdTree tree;
	// creates a tree from data
	KdTreeConstructor<D> constructor;
	// searches the tree for the nearest neighbor
	KdTreeSearch search;
	// Used internally during tree construction
	AxisSplitter<D> splitter;

	// used to recycle memory
	KdTreeMemory memory = new KdTreeMemory();

	public KdTreeNearestNeighbor(KdTreeSearch search, AxisSplitter<D> splitter) {
		this.search = search;
		this.splitter = splitter;
	}

	public KdTreeNearestNeighbor() {
		this( new KdTreeSearchStandard(), new AxisSplitterMedian<D>());
	}

	@Override
	public void init( int N ) {
		constructor = new KdTreeConstructor<D>(memory,N,splitter);
	}

	@Override
	public void setPoints(List<double[]> points, List<D> data) {
		if( tree != null )
			memory.recycleGraph(tree);
		tree = constructor.construct(points,data);
		search.setTree(tree);
	}

	@Override
	public boolean findNearest( double[] point , double maxDistance , NnData<D> result ) {
		if( maxDistance <= 0 )
			search.setMaxDistance(Double.MAX_VALUE);
		else
			search.setMaxDistance(maxDistance);
		KdTree.Node found = search.findClosest(point);
		if( found == null )
			return false;

		result.point = found.point;
		result.data = (D)found.data;
		result.distance = search.getDistance();

		return true;
	}
}
