package org.ddogleg.nn.alg;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Peter Abeles
 */
public class TestAxisSplitRuleRandomK {

	@Test
	public void basic() {



		AxisSplitRuleRandomK alg = new AxisSplitRuleRandomK(new Random(234),3);


		// results are random.  Test to see if only the expected numbers are returned
		int num10 = 0;
		int num11 = 0;
		int num12 = 0;

		for( int i = 0; i < 20; i++ ) {
			double[] var = new double[]{1,2,3,10,4,5,5,6,11,12};
			alg.setDimension(var.length);
			int found = alg.select(var);
			switch( found ) {
				case 3:
					num10++;
					break;
				case 8:
					num11++;
					break;

				case 9:
					num12++;
					break;

				default:
					fail("Unexpected value");
			}

		}

		assertTrue(num10 > 2);
		assertTrue(num11 > 2);
		assertTrue(num12 > 2);
	}

}
