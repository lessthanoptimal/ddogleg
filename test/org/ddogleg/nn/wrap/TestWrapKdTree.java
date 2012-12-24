package org.ddogleg.nn.wrap;

import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.StandardNearestNeighborTests;

/**
 * @author Peter Abeles
 */
public class TestWrapKdTree extends StandardNearestNeighborTests {

	public TestWrapKdTree() {
		setAlg(FactoryNearestNeighbor.<Double>kdtree());
	}

}
