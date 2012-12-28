package org.ddogleg.nn.alg;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestAxisSplitRuleMax {
	@Test
	public void basic() {

		double[] var = new double[]{1,2,3,10,4,5,5,6};

		AxisSplitRuleMax alg = new AxisSplitRuleMax();
		alg.setDimension(var.length);
		int found = alg.select(var);
		assertEquals(3,found);
	}
}
