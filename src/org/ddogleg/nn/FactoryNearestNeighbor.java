package org.ddogleg.nn;

import org.ddogleg.nn.alg.AxisSplitRuleRandomK;
import org.ddogleg.nn.alg.AxisSplitterMedian;
import org.ddogleg.nn.alg.KdTreeSearchBbf;
import org.ddogleg.nn.wrap.KdForestBbfSearch;
import org.ddogleg.nn.wrap.KdTreeNearestNeighbor;
import org.ddogleg.nn.wrap.WrapExhaustiveNeighbor;

import java.util.Random;

/**
 * Factory for creating implementations of {@link NearestNeighbor}.
 *
 * @author Peter Abeles
 */
public class FactoryNearestNeighbor {

	public static <D> NearestNeighbor<D> kdtree() {
		return new KdTreeNearestNeighbor<D>();
	}

	public static <D> NearestNeighbor<D> kdtree( int maxNodesSearched ) {
		return new KdTreeNearestNeighbor<D>(new KdTreeSearchBbf(maxNodesSearched),new AxisSplitterMedian<D>());
	}

	public static <D> NearestNeighbor<D> kdRandomForest( int maxNodesSearched , int numTrees , int numConsiderSplit ,
														 long randomSeed ) {

		Random rand = new Random(randomSeed);

		return new KdForestBbfSearch<D>(numTrees,maxNodesSearched,
				new AxisSplitterMedian<D>(new AxisSplitRuleRandomK(rand,numConsiderSplit)));
	}

	public static <D> NearestNeighbor<D> exhaustive() {
		return new WrapExhaustiveNeighbor<D>();
	}
}
