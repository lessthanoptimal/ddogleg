/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.nn.alg.distance;

import org.ddogleg.nn.alg.KdTreeDistance;

/**
 * Euclidian squared distance
 *
 * @author Peter Abeles
 */
public class KdTreeEuclideanSq_U8 implements KdTreeDistance<byte[]> {
	@Override
	public double compute(byte[] a, byte[] b) {
		int sum = 0;

		final int N = a.length;
		for (int i = 0; i < N; i++) {
			double d = (a[i]&0xFF)-(b[i]&0xFF);
			sum += d*d;
		}

		return sum;
	}

	@Override
	public double valueAt(byte[] point, int index) {
		return point[index]&0xFF;
	}
}
