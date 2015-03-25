/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.fitting.modelset;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class BenchmarkModelManager {

	public static final int N = 10;

	List<Data>  list = new ArrayList<Data>();
	Data src = new Data();

	public void init() {
		for (int i = 0; i < N; i++) {
			src.d[i] = i + 3.1;
		}
		for (int i = 0; i < 10000; i++) {
			list.add(new Data());
		}
	}

	public long perform(ModelManager<Data> mm, int numTrials) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < numTrials; i++) {
			for (int j = 0; j < list.size(); j++) {
				mm.copyModel(src,list.get(j));
			}
		}
		return System.currentTimeMillis()-start;
	}

	public static class Specialized implements ModelManager<Data> {

		@Override
		public Data createModelInstance() {
			return new Data();
		}

		@Override
		public void copyModel(Data src, Data dst) {
			dst.set(src);
		}
	}

	public static class Data {
		double d[] = new double[N];
		public Data() {
		}

		public void set( Data a ) {
			System.arraycopy(a.d,0,d,0,N);
		}
	}

	public static void main(String[] args) {
		BenchmarkModelManager benchmark = new BenchmarkModelManager();
		benchmark.init();
		int trial = 10000;
		System.out.println("Specialized = "+benchmark.perform(new Specialized(),trial));
		System.out.println("Default     = "+benchmark.perform(new ModelManagerDefault<Data>(Data.class),trial));

	}
}
