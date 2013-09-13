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

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a new {@link KdTree KD-Tree} from a list of points and (optional) associated data. Which axis is used
 * to split the data and how to split the data is determined by the {@link AxisSplitter} passed in.
 *
 * A child node can be null if it is a leaf and there was no data in that direction.
 *
 * WARNING: A reference to the input points is saved.  Do not modify the input until the K-D Tree is no longer needed.
 * This reduced memory overhead significantly.
 *
 * @author Peter Abeles
 */
public class KdTreeConstructor<D> {

	// Number of elements/axes in each data point
	private int N;

	// selects which axis to split along and divides the set of points
	AxisSplitter<D> splitter;

	// Used to recycles memory and avoid GC calls
	KdTreeMemory memory;

	/**
	 * Constructor which allows for maximum configurable.
	 *
	 * @param memory Used to recycle data
	 * @param N Number of elements/axes in each data point
	 */
	public KdTreeConstructor( KdTreeMemory memory , int N , AxisSplitter<D> splitter ) {
		this.memory = memory;
		this.N = N;
		this.splitter = splitter;

		splitter.setDimension(N);
	}

	/**
	 * Creates canonical K-D Tree by selecting the maximum variance axis and splitting the points at the median.
	 *
	 * @param N N Number of elements/axes in each data point
	 */
	public KdTreeConstructor( int N ) {
		this(new KdTreeMemory(), N, new AxisSplitterMedian<D>(new AxisSplitRuleMax()));
	}

	/**
	 * Creates a new {@link KdTree} from the provided points.
	 *
	 * WARNING: Reference to each point is saved to reduce memory usage..
	 *
	 * @param points Data points.
	 * @param data (Optional) Data associated to each point.  Can be null.
	 * @return KdTre
	 */
	public KdTree construct( List<double[]> points ,
							 List<D> data )
	{
		KdTree tree = memory.requestTree();
		tree.N = N;

		if( points.size() == 1 ) {
			tree.root = createLeaf(points,data);
		} else if( points.size() > 1 ) {
			tree.root = computeBranch(points, data );
		}

		return tree;
	}

	/**
	 * Given the data inside this particular node, select a point for the node and
	 * compute the node's children
	 *
	 * @return The node associated with this region
	 */
	protected KdTree.Node computeBranch(List<double[]> points, List<D> data ) {

		// declare storage for the split data
		List<double[]> left = new ArrayList<double[]>(points.size()/2);
		List<double[]> right = new ArrayList<double[]>(points.size()/2);
		List<D> leftData,rightData;

		if( data == null ) {
			leftData = null; rightData = null;
		} else {
			leftData = new ArrayList<D>(points.size()/2);
			rightData = new ArrayList<D>(points.size()/2);
		}

		// perform the splitting
		splitter.splitData(points,data,left,leftData,right,rightData);

		// save the results into the current node and its children
		KdTree.Node node = memory.requestNode();

		node.split = splitter.getSplitAxis();
		node.point = splitter.getSplitPoint();
		node.data = splitter.getSplitData();

		// Compute the left and right children
		node.left = computeChild(left,leftData);
		// free memory
		left = null; leftData = null;
		node.right = computeChild(right,rightData);

		return node;
	}

	/**
	 * Creates a child by checking to see if it is a leaf or branch.
	 */
	protected KdTree.Node computeChild( List<double[]> points , List<D> data )
	{
		if( points.size() == 0 )
			return null;
		if( points.size() == 1 ) {
			return createLeaf(points,data);
		} else {
			return computeBranch(points,data);
		}
	}

	/**
	 * Convenient function for creating a leaf node
	 */
	private KdTree.Node createLeaf( List<double[]> points , List<D> data ) {
		D d = data == null ? null : data.get(0);
		return memory.requestNode(points.get(0),d);
	}
}
