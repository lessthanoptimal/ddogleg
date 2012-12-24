package org.ddogleg.nn.wrap;

import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.StandardNearestNeighborTests;

/**
 * @author Peter Abeles
 */
public class TestWrapExhaustiveNeighbor extends StandardNearestNeighborTests {

	public TestWrapExhaustiveNeighbor() {
		setAlg(FactoryNearestNeighbor.<Double>exhaustive());
	}

}
