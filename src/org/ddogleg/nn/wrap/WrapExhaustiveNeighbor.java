package org.ddogleg.nn.wrap;

import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.nn.alg.ExhaustiveNeighbor;

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
		int index = alg.findClosest(point,maxDistance);
		if( index >= 0 ) {
			result.point = points.get(index);
			if( data != null )
				result.data = data.get(index);
			return true;
		} else {
			return false;
		}
	}
}
