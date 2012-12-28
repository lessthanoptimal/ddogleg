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

/**
 * @author Peter Abeles
 */
public class UtilGaussian {

    public static double SQRT_2_PI = Math.sqrt(2*Math.PI);

    public static double computePDF( double mean , double sigma , double sample ) {
        double delta = sample-mean;

        return Math.exp( - delta*delta/(2.0*sigma*sigma))/(sigma*SQRT_2_PI);
    }

	public static double derivative1( double mean , double sigma , double sample ) {
		double delta = sample-mean;

		double a = Math.exp( - delta*delta/(2.0*sigma*sigma))/(sigma*SQRT_2_PI);
		double c = -delta/(sigma*sigma);

		return c*a;
	}

	public static double derivative2( double mean , double sigma , double sample ) {
		double delta = sample-mean;

		double sigma2 = sigma*sigma;
		double exp = Math.exp( - delta*delta/(2.0*sigma*sigma))/(sigma*SQRT_2_PI);
		double c = -delta/sigma2;

		return (c*c-1.0/sigma2)*exp;
	}

	public static double derivative3( double mean , double sigma , double sample ) {
		double delta = sample-mean;

		double sigma2 = sigma*sigma;
		double exp = Math.exp( - delta*delta/(2.0*sigma*sigma))/(sigma*SQRT_2_PI);
		double c = -delta/sigma2;
		return c*(c*c - 3.0/sigma2)*exp;
	}

	public static double derivative4( double mean , double sigma , double sample ) {
		double delta = sample-mean;

		double sigma2 = sigma*sigma;
		double exp = Math.exp( - delta*delta/(2.0*sigma*sigma))/(sigma*SQRT_2_PI);
		double c = -delta/sigma2;
		return (3.0/(sigma2*sigma2) + c*c*c*c - 6*c*c/sigma2)*exp;
	}
}
