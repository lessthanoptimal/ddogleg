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

package org.ddogleg.nn.wrap;

import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.nn.alg.ExhaustiveNeighbor;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_F64;
import org.ddogleg.struct.GrowQueue_I32;

import java.util.List;

/**
 * Wrapper around {@link org.ddogleg.nn.alg.ExhaustiveNeighbor} for {@link NearestNeighbor}
 *
 * @author Peter Abeles
 */
public class WrapExhaustiveNeighbor<D> implements NearestNeighbor<D> {

	ExhaustiveNeighbor alg = new ExhaustiveNeighbor();
	List<double[]> points;
	List<D> data;

	GrowQueue_I32 outputIndex = new GrowQueue_I32();
	GrowQueue_F64 outputDistance = new GrowQueue_F64();

	@Override
	public void init( int N ) {
		alg.setN(N);
	}

	@Override
	public void setPoints(List<double[]> points, List<D> data) {
		alg.setPoints(points);
		this.points = points;
		this.data = data;
	}

	@Override
	public boolean findNearest(double[] point, double maxDistance, NnData<D> result) {
		if( maxDistance < 0 )
			maxDistance = Double.MAX_VALUE;

		int index = alg.findClosest(point,maxDistance);
		if( index >= 0 ) {
			result.point = points.get(index);
			result.distance = alg.getBestDistance();
			if( data != null )
				result.data = data.get(index);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void findNearest(double[] point, double maxDistance, int numNeighbors, FastQueue<NnData<D>> result) {
		if( maxDistance < 0 )
			maxDistance = Double.MAX_VALUE;

		outputIndex.reset();
		outputDistance.reset();
		alg.findClosestN(point,maxDistance,numNeighbors,outputIndex,outputDistance);

		for( int i = 0; i < outputIndex.size; i++ ) {
			int index = outputIndex.get(i);
			NnData<D> r = result.grow();
			r.distance = outputDistance.get(i);
			r.point = points.get(index);
			if( data != null )
				r.data = data.get( index );
		}
	}
}
