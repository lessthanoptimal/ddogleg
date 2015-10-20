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

package org.ddogleg.nn.alg;

import org.ddogleg.sorting.QuickSelect;

import java.util.List;

/**
 * Splits the points in K-D Tree node by selecting the axis with the largest variance.  The point with the median
 * value along that axis is the split point.  The data is segmented into left and right lists using the sorted list
 * used to find the median value.
 *
 * @author Peter Abeles
 */
public class AxisSplitterMedian<D> implements AxisSplitter<D> {

	// Number of elements/axes in each data point
	private int N;

	// storage for variance calculation
	private double mean[];
	private double var[];

	// storage for median calculation
	private double tmp[] = new double[1];
	private int indexes[] = new int[1];

	// using each axis's variance, selects which axis to split along
	// This abstraction was done so that random trees could use the same code
	AxisSplitRule splitRule;

	// storage for output
	int splitAxis;
	double[] splitPoint;
	D splitData;

	public AxisSplitterMedian(AxisSplitRule splitRule ) {
		this.splitRule = splitRule;
	}

	/**
	 * Defaults to selecting the split axis with maximum variance
	 */
	public AxisSplitterMedian() {
		this.splitRule = new AxisSplitRuleMax();
	}

	@Override
	public void setDimension(int N) {
		this.N = N;
		this.mean = new double[N];
		this.var = new double[N];

		if( splitRule == null )
			throw new RuntimeException("You must call setRule() before setDimension()");
		splitRule.setDimension(N);
	}

	@Override
	public void splitData(List<double[]> points, List<D> data,
						  List<double[]> left, List<D> leftData,
						  List<double[]> right, List<D> rightData) {
		computeAxisVariance(points);
		for (int i = 0; i < N; i++) {
			if( Double.isNaN(var[i])) {
				throw new RuntimeException("Variance is NaN.  Bad input data with NaN is the cause.");
			}
		}

		splitAxis = splitRule.select(var);

		// where the median is
		final int medianNum = points.size()/2;
		// sort until the median is found
		quickSelect(points, splitAxis,medianNum);

		splitPoint = points.get( indexes[medianNum] );

		// split into left and right lists.  Skip over the median point
		if( data == null ) {
			for( int i = 0; i < medianNum; i++ ) {
				left.add(points.get(indexes[i]));
			}
			for( int i = medianNum+1; i < points.size(); i++ ) {
				right.add(points.get(indexes[i]));
			}
			splitData = null;
		} else {

			for( int i = 0; i < medianNum; i++ ) {
				int index = indexes[i];
				left.add(points.get(index));
				leftData.add(data.get(index));
			}
			for( int i = medianNum+1; i < points.size(); i++ ) {
				int index = indexes[i];
				right.add(points.get(index));
				rightData.add(data.get(index));
			}
			splitData = data.get( indexes[medianNum] );
		}
	}

	@Override
	public double[] getSplitPoint() {
		return splitPoint;
	}

	@Override
	public D getSplitData() {
		return splitData;
	}

	@Override
	public int getSplitAxis() {
		return splitAxis;
	}

	/**
	 * Select the maximum variance as the split
	 */
	private void computeAxisVariance(List<double[]> points) {
		int numPoints = points.size();

		for( int i = 0; i < N; i++ ) {
			mean[i] = 0;
			var[i] = 0;
		}

		// compute the mean
		for( int i = 0; i < numPoints; i++ ) {
			double[] p = points.get(i);

			for( int j = 0; j < N; j++ ) {
				mean[j] += p[j];
			}
		}

		for( int i = 0; i < N; i++ ) {
			mean[i] /= numPoints;
		}

		// compute the variance * N
		for( int i = 0; i < numPoints; i++ ) {
			double[] p = points.get(i);

			for( int j = 0; j < N; j++ ) {
				double d = mean[j] - p[j];
				var[j] += d*d;
			}
		}
	}

	/**
	 * Uses quick-select to find the median value
	 */
	private void quickSelect(List<double[]> points, int splitAxis, int medianNum) {
		int numPoints = points.size();

		if( tmp.length < numPoints ) {
			tmp = new double[numPoints];
			indexes = new int[ numPoints ];
		}
		for( int i = 0; i < numPoints; i++ ) {
			tmp[i] = points.get(i)[splitAxis];
		}

		QuickSelect.selectIndex(tmp, medianNum, numPoints, indexes);
	}
}
