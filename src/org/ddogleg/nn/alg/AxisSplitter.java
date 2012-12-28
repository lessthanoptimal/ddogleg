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

import java.util.List;

/**
 * Selects which dimension the set of points should be split by, which point is used to split the lists, and splits
 * the lists into two sets.  A point goes into the left list if it has a value less than the split point and to
 * the right list if it has a value higher than.  The split point goes into neither list.  If multiple points have
 * the same value and one of them is the split point then all but one go into the left or right list.
 *
 * @author Peter Abeles
 */
public interface AxisSplitter<D> {

	public void setDimension( int N );

	/**
	 * Given the a set of points, select the axis to split the data along and select a point to divide the data.
	 * Points whput items below the threshold
	 * into left and above into right.  Data is optional and should be ignored if null. The selected
	 *
	 * @param points Input: Set of points.
	 * @param data Input: (Optional) Set of data associated with the points.  Can be null.
	 * @param left Output: Storage for points less than the split point.
	 * @param leftData Output: (Optional) Storage for data associated with left. Can be null.
	 * @param right Output: Storage for points more than the split point.
	 * @param rightData Output: (Optional) Storage for data associated with right. Can be null.
	 */
	public void splitData( List<double[]> points , List<D> data ,
						   List<double[]> left , List<D> leftData ,
						   List<double[]> right , List<D> rightData );

	/**
	 * Returns the point used to split the data
	 */
	public double[] getSplitPoint();

	/**
	 * Data associated with the split point
	 */
	public D getSplitData();

	/**
	 * The axis/dimension that the input list was split on
	 * @return
	 */
	public int getSplitAxis();
}
