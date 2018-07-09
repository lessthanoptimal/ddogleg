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

package org.ddogleg.nn.alg;

import org.ddogleg.struct.GrowQueue_I32;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Selects which dimension the set of points should be split by, which point is used to split the lists, and splits
 * the lists into two sets.  A point goes into the left list if it has a value less than the split point and to
 * the right list if it has a value higher than.  The split point goes into neither list.  If multiple points have
 * the same value and one of them is the split point then all but one go into the left or right list.
 *
 * @author Peter Abeles
 */
public interface AxisSplitter<P> {
	/**
	 * Given the a set of points, select the axis to split the data along and select a point to divide the data.
	 * Points whput items below the threshold
	 * into left and above into right.  Data is optional and should be ignored if null. The selected
	 *
	 * @param points Input: Set of points.
	 * @param indexes Input: (Optional) Option index asssociated with points.  Can be null.
	 * @param left Output: Storage for points less than the split point.
	 * @param leftIndexes Output: (Optional) Storage for indexes associated with left. Can be null.
	 * @param right Output: Storage for points more than the split point.
	 * @param righrIndexes Output: (Optional) Storage for indexes associated with right. Can be null.
	 */
	void splitData( List<P> points , @Nullable GrowQueue_I32 indexes ,
					List<P> left , @Nullable GrowQueue_I32 leftIndexes ,
					List<P> right , @Nullable GrowQueue_I32 righrIndexes );

	/**
	 * Returns the point used to split the data
	 */
	P getSplitPoint();

	/**
	 * Index associated with the split point
	 */
	int getSplitIndex();

	/**
	 * The axis/dimension that the input list was split on
	 */
	int getSplitAxis();

	/**
	 * Number of elements in a point
	 */
	int getPointLength();
}
