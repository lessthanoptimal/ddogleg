/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ddogleg.stats;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestUtilGaussian {

	/**
	 * Test the PDF based on some of its properties
	 */
	@Test
	public void computePDF() {
		double max = UtilGaussian.computePDF(2,1.5,2);

		assertTrue( UtilGaussian.computePDF(2,1.5,1) < max );
		assertTrue( UtilGaussian.computePDF(2,1.5,3) < max );
		assertEquals(UtilGaussian.computePDF(2, 1.5, 1),UtilGaussian.computePDF(2, 1.5, 3),1e-8 );

		assertTrue( UtilGaussian.computePDF(2,2,2) < max );
		assertTrue( UtilGaussian.computePDF(2,1,2) > max );
	}

	@Test
	public void derivative1() {

		// numerical derivative
		double expected = deriv1(1.5, 2, 1.8);

		assertEquals(expected,UtilGaussian.derivative1(1.5, 2, 1.8),1e-4);
	}

	@Test
	public void derivative2() {
		// numerical derivative.. test is very crude
		double expected = deriv2(1.5, 2, 1.8);

		assertEquals(expected, UtilGaussian.derivative2(1.5, 2, 1.8), 1e-3);
	}

	@Test
	public void derivative3() {
		// numerical derivative.. test is very crude
		double expected = deriv3(1.5, 2, 1.8);

		assertEquals(expected, UtilGaussian.derivative3(1.5, 2, 1.8), 1e-3);
	}

	@Test
	public void derivative4() {
		// numerical derivative.. test is very crude
		double expected = deriv4(1.5, 2, 1.8);

		assertEquals(expected, UtilGaussian.derivative4(1.5, 2, 1.8), 0.1);
	}

	public static double normal( double mu , double sigma ,double x ) {
		double delta = x-mu;
		double a = -delta*delta/(2*sigma*sigma);
		double b = 2*Math.PI*sigma*sigma;

		return (1.0/Math.sqrt(b))*Math.exp(a);
	}

	public static double deriv1( double mu , double sigma ,double x) {
		return (normal(mu,sigma,x+1e-10) - normal(mu,sigma,x))/1e-10;
	}
	public static double deriv2( double mu , double sigma ,double x) {
		return (deriv1(mu,sigma,x+1e-3) - deriv1(mu,sigma,x-1e-3))/2e-3;
	}
	public static double deriv3( double mu , double sigma ,double x) {
		double d = 1e-1;
		return (deriv2(mu,sigma,x+d) - deriv2(mu,sigma,x-d))/(2*d);
	}
	public static double deriv4( double mu , double sigma ,double x) {
		double d = 0.1;
		return (deriv3(mu, sigma, x + d) - deriv3(mu, sigma, x - d))/(2*d);
	}
}
