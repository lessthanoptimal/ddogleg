package org.ddogleg.nn.alg;

/**
 * @author Peter Abeles
 */
public class TestKdTreeSearchStandard extends StandardKdTreeSearchTests {
	@Override
	public KdTreeSearch createAlg() {
		return new KdTreeSearchStandard();
	}
}
