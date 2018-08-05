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

package org.ddogleg.optimization;

import org.ddogleg.optimization.functions.FunctionNtoM;

/**
 * Fixed output for unit tests
 *
 * @author Peter Abeles
 */
public class MockFunctionNtoM implements FunctionNtoM {

	double[] output;
	int N;

	public MockFunctionNtoM(double[] output , int N ) {
		this.N = N;
		this.output = output;
	}

	@Override
	public void process(double[] input, double[] output) {
		System.arraycopy(this.output,0,output,0,this.output.length);
	}

	@Override
	public int getNumOfInputsN() {
		return N;
	}

	@Override
	public int getNumOfOutputsM() {
		return output.length;
	}
}
