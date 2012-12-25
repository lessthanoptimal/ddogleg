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
public class WrapKdTree<D> implements NearestNeighbor<D> {

	KdTree tree;
	KdTreeConstructor<D> constructor;
	KdTreeSearch ops;

	KdTreeMemory memory = new KdTreeMemory();

	public WrapKdTree(KdTreeSearch ops) {
		this.ops = ops;
	}

	public WrapKdTree() {
		this( new KdTreeSearchStandard());
	}

	@Override
	public void init( int N ) {
		constructor = new KdTreeConstructor<D>(memory,N);
	}

	@Override
	public void setPoints(List<double[]> points, List<D> data) {
		if( tree != null )
			memory.recycleGraph(tree);
		tree = constructor.construct(points,data);
		ops.setTree(tree);
	}

	@Override
	public boolean findNearest( double[] point , double maxDistance , NnData<D> result ) {
		ops.setMaxDistance(maxDistance);
		KdTree.Node found = ops.findClosest(point);
		if( found == null )
			return false;

		result.point = found.point;
		result.data = (D)found.data;

		return true;
	}
}
