package org.ddogleg.nn.wrap;

import org.ddogleg.nn.StandardNearestNeighborTests;
import org.ddogleg.nn.alg.AxisSplitRule;
import org.ddogleg.nn.alg.AxisSplitRuleRandomK;
import org.ddogleg.nn.alg.AxisSplitterMedian;

import java.util.Random;

/**
 * @author Peter Abeles
 */
public class TestKdForestBbfSearch extends StandardNearestNeighborTests {

	public TestKdForestBbfSearch() {
		// set the max nodes so it that it will produce perfect results
		AxisSplitRule rule = new AxisSplitRuleRandomK(new Random(234),1);
		setAlg(new KdForestBbfSearch(5,10000,new AxisSplitterMedian(rule)));
	}
}
