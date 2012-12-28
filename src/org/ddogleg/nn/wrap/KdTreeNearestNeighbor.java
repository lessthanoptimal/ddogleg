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
		search.setMaxDistance(maxDistance);
		KdTree.Node found = search.findClosest(point);
		if( found == null )
			return false;

		result.point = found.point;
		result.data = (D)found.data;

		return true;
	}
}
