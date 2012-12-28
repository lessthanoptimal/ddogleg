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

package org.ddogleg.nn.alg;

/**
 * Selects which axis the data should be split along when given a list of variances.
 *
 * @author Peter Abeles
 */
public interface AxisSplitRule {

	/**
	 * Specifies the point's dimension
	 *
	 * @param N dimension
	 */
	public void setDimension( int N );

	/**
	 * Selects the index for splitting using the provided variances.  The input list can be modified.
	 * @param variance List of variances for each dimension in the point
	 * @return The selected split axis
	 */
	public int select( double []variance );
}
