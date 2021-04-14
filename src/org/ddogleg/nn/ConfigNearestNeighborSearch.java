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

package org.ddogleg.nn;

/**
 * Generic configuration for all {@link NearestNeighbor} algorithms.
 *
 * @author Peter Abeles
 */
public class ConfigNearestNeighborSearch {
	/** Which search algorithm */
	public Type type = Type.EXHAUSTIVE;

	/** Configuration for {@link Type#RANDOM_FOREST} */
	public final RandomForest randomForest = new RandomForest();

	/** Configuration for {@link Type#KD_TREE} */
	public final KDTree kdtree = new KDTree();

	/** Seed used by random number generator */
	public long randomSeed = 0x42;

	public void checkValidity() {
		randomForest.checkValidity();
		kdtree.checkValidity();
	}

	public void setTo( ConfigNearestNeighborSearch src ) {
		this.type = src.type;
		this.randomForest.setTo(src.randomForest);
		this.kdtree.setTo(src.kdtree);
		this.randomSeed = src.randomSeed;
	}

	public static class RandomForest {
		/** Maximum number of nodes it will search. Controls speed and accuracy. */
		public int maxNodesSearched = 2000;
		/** Number of trees that are considered. Tune this. */
		public int numTrees = 10;
		/** Number of nodes that are considered when generating a tree. Must be less than the point's dimension. */
		public int numConsiderSplit = 5;

		public void checkValidity() {
			if (maxNodesSearched < 0)
				throw new IllegalArgumentException("maxNodesSearched can't be negative");

			if (numTrees <= 0)
				throw new IllegalArgumentException("numTrees must be positive");

			if (numConsiderSplit <= 0)
				throw new IllegalArgumentException("numConsiderSplit must be positive");
		}

		public void setTo( RandomForest src ) {
			this.maxNodesSearched = src.maxNodesSearched;
			this.numTrees = src.numTrees;
			this.numConsiderSplit = src.numConsiderSplit;
		}
	}

	public static class KDTree {
		/** Maximum number of nodes it will search. Controls speed and accuracy. */
		public int maxNodesSearched = Integer.MAX_VALUE;

		public void checkValidity() {
			if (maxNodesSearched < 0)
				throw new IllegalArgumentException("maxNodesSearched can't be negative");
		}

		public void setTo( KDTree src ) {
			this.maxNodesSearched = src.maxNodesSearched;
		}
	}

	public enum Type {
		EXHAUSTIVE,
		RANDOM_FOREST,
		KD_TREE,
		VP_TREE
	}
}
