/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.chainrule;

import java.util.Random;

@SuppressWarnings("unchecked")
public class FactoryCRArray {
	public static <T> CRArray<T> create( int length, Class<T> type ) {
		if (double[].class == type) {
			return (CRArray<T>)new CRArray<>(new double[length], 0);
		} else if (float[].class == type) {
			return (CRArray<T>)new CRArray<>(new float[length], 0);
		} else if (int[].class == type) {
			return (CRArray<T>)new CRArray<>(new int[length], 0);
		} else if (short[].class == type) {
			return (CRArray<T>)new CRArray<>(new short[length], 0);
		} else if (byte[].class == type) {
			return (CRArray<T>)new CRArray<>(new byte[length], 0);
		} else {
			throw new IllegalArgumentException("Unknown type: " + type.getSimpleName());
		}
	}

	public static <T> CRArray<T> random( Random rand, int length, Class<T> type ) {
		CRArray<T> array = create(length, type);

		if (double[].class == type) {
			var a = (double[])array.a;
			for (int i = 0; i < a.length; i++) {
				a[i] = rand.nextDouble();
			}
		} else if (float[].class == type) {
			var a = (float[])array.a;
			for (int i = 0; i < a.length; i++) {
				a[i] = rand.nextFloat();
			}
		} else if (int[].class == type) {
			var a = (int[])array.a;
			for (int i = 0; i < a.length; i++) {
				a[i] = rand.nextInt(1000);
			}
		} else if (short[].class == type) {
			var a = (short[])array.a;
			for (int i = 0; i < a.length; i++) {
				a[i] = (short)rand.nextInt(1000);
			}
		} else if (byte[].class == type) {
			var a = (byte[])array.a;
			for (int i = 0; i < a.length; i++) {
				a[i] = (byte)rand.nextInt(256);
			}
		} else {
			throw new IllegalArgumentException("Unknown type: " + type.getSimpleName());
		}

		return array;
	}
}
