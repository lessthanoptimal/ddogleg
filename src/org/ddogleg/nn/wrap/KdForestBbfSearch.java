package org.ddogleg.nn.wrap;

import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.nn.alg.*;

import java.util.List;

/**
 * @author Peter Abeles
 */
public class KdForestBbfSearch<D> implements NearestNeighbor<D> {

	KdTree[]forest;

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
		search.setMaxDistance(maxDistance);
		KdTree.Node found = search.findClosest(point);
		if( found == null )
			return false;

		result.point = found.point;
		result.data = (D)found.data;

		return true;
	}
}
