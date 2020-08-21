/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.sorting.QuickSelect;
import org.ddogleg.struct.GrowQueue_I32;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Splits the points in K-D Tree node by selecting the axis with the largest variance.  The point with the median
 * value along that axis is the split point.  The data is segmented into left and right lists using the sorted list
 * used to find the median value.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class AxisSplitterMedian<P> implements AxisSplitter<P> {

	// Number of elements/axes in each data point
	private final int N;

	// storage for variance calculation
	private final double[] mean;
	private final double[] var;

	// storage for median calculation
	private double[] tmp = new double[1];
	private int[] indexes = new int[1];

	// using each axis's variance, selects which axis to split along
	// This abstraction was done so that random trees could use the same code
	AxisSplitRule splitRule;

	KdTreeDistance<P> distance;

	// storage for output
	int splitAxis;
	P splitPoint;
	int splitIndex;

	public AxisSplitterMedian(KdTreeDistance<P> distance,
							  AxisSplitRule splitRule ) {
		this.distance = distance;
		this.splitRule = splitRule;
		this.N = distance.length();

		this.mean = new double[N];
		this.var = new double[N];

		splitRule.setDimension(N);
	}

	/**
	 * Defaults to selecting the split axis with maximum variance
	 */
	public AxisSplitterMedian(KdTreeDistance<P> distance) {
		this(distance,new AxisSplitRuleMax());
	}

	@Override
	public void splitData(List<P> points, @Nullable GrowQueue_I32 indexes,
						  List<P> left, @Nullable GrowQueue_I32 leftIndexes,
						  List<P> right, @Nullable GrowQueue_I32 rightIndexes) {
		computeAxisVariance(points);
		for (int i = 0; i < N; i++) {
			if( Double.isNaN(var[i])) {
				throw new RuntimeException("Variance is NaN.  Bad input is the cause. mean[i]="+mean[i]+" i="+i+" points.size="+points.size());
			}
		}

		splitAxis = splitRule.select(var);

		// where the median is
		final int medianNum = points.size()/2;
		// sort until the median is found
		quickSelect(points, splitAxis,medianNum);

		splitPoint = points.get( this.indexes[medianNum] );

		// split into left and right lists.  Skip over the median point
		if( indexes == null ) {
			for( int i = 0; i < medianNum; i++ ) {
				left.add(points.get(this.indexes[i]));
			}
			for( int i = medianNum+1; i < points.size(); i++ ) {
				right.add(points.get(this.indexes[i]));
			}
		} else {
			Objects.requireNonNull(leftIndexes);
			Objects.requireNonNull(rightIndexes);
			leftIndexes.reset();
			rightIndexes.reset();

			for( int i = 0; i < medianNum; i++ ) {
				int index = this.indexes[i];
				left.add(points.get(index));
				leftIndexes.add(indexes.get(index));
			}
			for( int i = medianNum+1; i < points.size(); i++ ) {
				int index = this.indexes[i];
				right.add(points.get(index));
				rightIndexes.add(indexes.get(index));
			}
			splitIndex = indexes.get( this.indexes[medianNum] );
		}
	}

	@Override
	public P getSplitPoint() {
		return splitPoint;
	}

	@Override
	public int getSplitIndex() {
		return splitIndex;
	}

	@Override
	public int getSplitAxis() {
		return splitAxis;
	}

	@Override
	public int getPointLength() {
		return N;
	}

	/**
	 * Select the maximum variance as the split
	 */
	private void computeAxisVariance(List<P> points) {
		int numPoints = points.size();

		for( int i = 0; i < N; i++ ) {
			mean[i] = 0;
			var[i] = 0;
		}

		// compute the mean
		for( int i = 0; i < numPoints; i++ ) {
			P p = points.get(i);

			for( int j = 0; j < N; j++ ) {
				mean[j] += distance.valueAt(p,j);
			}
		}

		for( int i = 0; i < N; i++ ) {
			mean[i] /= numPoints;
		}

		// compute the variance * N
		for( int i = 0; i < numPoints; i++ ) {
			P p = points.get(i);

			for( int j = 0; j < N; j++ ) {
				double d = mean[j] - distance.valueAt(p,j);
				var[j] += d*d;
			}
		}
	}

	/**
	 * Uses quick-select to find the median value
	 */
	private void quickSelect(List<P> points, int splitAxis, int medianNum) {
		int numPoints = points.size();

		if( tmp.length < numPoints ) {
			tmp = new double[numPoints];
			indexes = new int[ numPoints ];
		}
		for( int i = 0; i < numPoints; i++ ) {
			tmp[i] = distance.valueAt(points.get(i),splitAxis);
		}

		QuickSelect.selectIndex(tmp, medianNum, numPoints, indexes);
	}
}
