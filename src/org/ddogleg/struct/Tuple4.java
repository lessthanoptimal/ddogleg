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

package org.ddogleg.struct;

/**
 * Simple data structure for passing a quad of data.
 *
 * @author Peter Abeles
 */
public class Tuple4<A,B,C,D> {
	public A d0;
	public B d1;
	public C d2;
	public D d3;

	public Tuple4(A d0, B d1, C d2, D d3) {
		this.d0 = d0;
		this.d1 = d1;
		this.d2 = d2;
		this.d3 = d3;
	}

	public Tuple4() {
	}

	public A getD0() {
		return d0;
	}

	public void setD0(A d0) {
		this.d0 = d0;
	}

	public B getD1() {
		return d1;
	}

	public void setD1(B d1) {
		this.d1 = d1;
	}

	public void setD2(C d2) {
		this.d2 = d2;
	}

	public C getD2() {
		return d2;
	}

	public D getD3() {
		return d3;
	}

	public void setD3(D d3) {
		this.d3 = d3;
	}
}

