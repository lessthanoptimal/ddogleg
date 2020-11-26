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

import org.ddogleg.struct.DogArray_I32;
import org.jetbrains.annotations.Nullable;

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
public class KdTreeConstructor<P> {

	// selects which axis to split along and divides the set of points
	AxisSplitter<P> splitter;

	// Used to recycles memory and avoid GC calls
	KdTreeMemory<P> memory;

	/**
	 * Constructor which allows for maximum configurable.
	 *
	 * @param memory Used to recycle data
	 */
	public KdTreeConstructor(KdTreeMemory<P> memory , AxisSplitter<P> splitter ) {
		this.memory = memory;
		this.splitter = splitter;
	}

	/**
	 * Creates canonical K-D Tree by selecting the maximum variance axis and splitting the points at the median.
	 *
	 */
	public KdTreeConstructor( KdTreeDistance<P> distance ) {
		this(new KdTreeMemory<>(), new AxisSplitterMedian<>(distance, new AxisSplitRuleMax()));
	}

	/**
	 * Creates a new {@link KdTree} from the provided points.
	 *
	 * WARNING: Reference to each point is saved to reduce memory usage..
	 *
	 * @param points Data points.
	 * @return KdTre
	 */
	public KdTree construct(List<P> points , boolean trackIndexes )
	{
		DogArray_I32 indexes = null;
		if( trackIndexes ) {
			indexes = new DogArray_I32();
			indexes.resize(points.size());
			for (int i = 0; i < indexes.size; i++) {
				indexes.data[i] = i;
			}
		}

		KdTree tree = memory.requestTree(splitter.getPointLength());

		if( points.size() == 1 ) {
			tree.root = createLeaf(points,indexes);
		} else if( points.size() > 1 ) {
			tree.root = computeBranch(points, indexes );
		}

		return tree;
	}

	/**
	 * Given the data inside this particular node, select a point for the node and
	 * compute the node's children
	 *
	 * @return The node associated with this region
	 */
	protected KdTree.Node computeBranch(List<P> points, @Nullable DogArray_I32 indexes)
	{
		// declare storage for the split data
		List<P> left = new ArrayList<>(points.size()/2);
		List<P> right = new ArrayList<>(points.size()/2);
		DogArray_I32 leftIndexes,rightIndexes;

		if( indexes == null ) {
			leftIndexes = null; rightIndexes = null;
		} else {
			leftIndexes = new DogArray_I32(points.size()/2);
			rightIndexes = new DogArray_I32(points.size()/2);
		}

		// perform the splitting
		splitter.splitData(points,indexes,left,leftIndexes,right,rightIndexes);

		// save the results into the current node and its children
		KdTree.Node node = memory.requestNode();

		node.split = splitter.getSplitAxis();
		node.point = splitter.getSplitPoint();
		node.index = splitter.getSplitIndex();

		// Compute the left and right children
		node.left = computeChild(left,leftIndexes);
		// free memory
		left = null; leftIndexes = null;
		node.right = computeChild(right,rightIndexes);

		return node;
	}

	/**
	 * Creates a child by checking to see if it is a leaf or branch.
	 */
	protected @Nullable KdTree.Node computeChild(List<P> points , @Nullable DogArray_I32 indexes )
	{
		if( points.size() == 0 )
			return null;
		if( points.size() == 1 ) {
			return createLeaf(points,indexes);
		} else {
			return computeBranch(points,indexes);
		}
	}

	/**
	 * Convenient function for creating a leaf node
	 */
	private KdTree.Node createLeaf(List<P> points , @Nullable DogArray_I32 indexes ) {
		int index = indexes == null ? -1 : indexes.get(0);
		return memory.requestNode(points.get(0),index);
	}
}
