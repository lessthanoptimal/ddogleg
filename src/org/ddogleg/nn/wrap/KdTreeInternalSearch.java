/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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
import org.ddogleg.nn.alg.KdTree;
import org.ddogleg.nn.alg.KdTreeResult;
import org.ddogleg.nn.alg.KdTreeSearch1;
import org.ddogleg.nn.alg.KdTreeSearchN;
import org.ddogleg.struct.DogArray;

/**
 * @author Peter Abeles
 */
public abstract class KdTreeInternalSearch<P> implements NearestNeighbor.Search<P> {
	KdTreeSearch1<P> search1;
	KdTreeSearchN<P> searchN;

	// storage for multiple results
	DogArray<KdTreeResult> found = new DogArray<>(KdTreeResult::new);

	KdTreeInternalSearch( KdTreeSearch1<P> search1, KdTreeSearchN<P> searchN )
	{
		this.search1 = search1;
		this.searchN = searchN;
	}

	/**
	 * Specifies the tree which the internal searches use
	 */
	abstract void setTree();

	@Override
	public boolean findNearest(P point, double maxDistance, NnData<P> result) {
		setTree();
		if( maxDistance < 0 )
			search1.setMaxDistance(Double.MAX_VALUE);
		else
			search1.setMaxDistance(maxDistance);
		KdTree.Node found = search1.findNeighbor(point);
		if( found == null )
			return false;

		result.point = (P)found.point;
		result.index = found.index;
		result.distance = search1.getDistance();

		return true;
	}

	@Override
	public void findNearest(P point, double maxDistance, int numNeighbors, DogArray<NnData<P>> results) {
		setTree();
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
