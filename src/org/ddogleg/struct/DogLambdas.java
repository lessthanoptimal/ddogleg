/*
 * Copyright (c) 2012-2024, Peter Abeles. All Rights Reserved.
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
 * Common lambdas used in DDogleg
 *
 * @author Peter Abeles
 */
public interface DogLambdas {
	@FunctionalInterface interface NewInstance<P> {
		P newInstance();
	}

	@FunctionalInterface interface Copy<P> {
		void copy( P src, P dst );
	}

	@FunctionalInterface interface AssignIdx_B {
		boolean assign( int idx );
	}

	@FunctionalInterface interface AssignIdx_I8 {
		byte assign( int idx );
	}

	@FunctionalInterface interface AssignIdx_I16 {
		short assign( int idx );
	}

	@FunctionalInterface interface AssignIdx_I32 {
		int assign( int idx );
	}

	@FunctionalInterface interface AssignIdx_I64 {
		long assign( int idx );
	}

	@FunctionalInterface interface AssignIdx_F32 {
		float assign( int idx );
	}

	@FunctionalInterface interface AssignIdx_F64 {
		double assign( int idx );
	}
}
