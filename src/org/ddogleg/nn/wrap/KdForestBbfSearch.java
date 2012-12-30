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
 * K-D tree search which searches through multiple trees.  The search is performed using a Best-Bin-First approach
 *
 * @author Peter Abeles
 */
public class KdForestBbfSearch<D> implements NearestNeighbor<D> {

	// set of K-D trees which are to be searched
	KdTree[]forest;

	// creates the set of K-D trees given the same input
	KdTreeConstructor<D> constructor;

	KdTreeSearchBbf search;

	AxisSplitter<D> splitter;

	KdTreeMemory memory = new KdTreeMemory();

	public KdForestBbfSearch(int numberOfTrees,
							 int maxNodesSearched,
							 AxisSplitter<D> splitter) {
		this.forest = new KdTree[ numberOfTrees ];
		this.splitter = splitter;
		this.search = new KdTreeSearchBbf(maxNodesSearched);
	}


	@Override
	public void init(int pointDimension) {
		constructor = new KdTreeConstructor<D>(memory,pointDimension,splitter);
	}

	@Override
	public void setPoints(List<double[]> points, List<D> data) {
		if( forest[0] != null ) {
			for( int i = 0; i < forest.length; i++ )
				memory.recycleGraph(forest[i]);
		}
		for( int i = 0; i < forest.length; i++ )
			forest[i] = constructor.construct(points,data);
		search.setTrees(forest);
	}

	@Override
	public boolean findNearest(double[] point, double maxDistance, NnData<D> result) {
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
