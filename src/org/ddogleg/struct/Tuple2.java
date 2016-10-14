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

package org.ddogleg.struct;

/**
 * Simple data structure for passing a pair of data.
 *
 * @author Peter Abeles
 */
public class Tuple2<A,B> {
	public A data0;
	public B data1;

	public Tuple2(A data0, B data1) {
		this.data0 = data0;
		this.data1 = data1;
	}

	public Tuple2() {
	}

	public A getData0() {
		return data0;
	}

	public void setData0(A data0) {
		this.data0 = data0;
	}

	public B getData1() {
		return data1;
	}

	public void setData1(B data1) {
		this.data1 = data1;
	}
}

