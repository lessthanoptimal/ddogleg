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

package org.ddogleg.struct;

/**
 * Specifies which strategy is used to select internal array size when adding new blocks. If a fixed sized
 * block is always used this can be memory inefficient, but is faster as array creation and copy can be avoided.
 */
public enum BigDogGrowth {
	/**
	 * Blocks always have a fixed size.
	 */
	FIXED,
	/**
	 * The first block will grow as needed but all blocks later on are fixed. The idea being that initially
	 * a block can be much bigger than needed initially, but after the first block the amount of memory wasted
	 * is relatively small.
	 */
	GROW_FIRST,
	/**
	 * Every new block will start out as small as possible. Most memory efficient approach but will be
	 * wasteful as smaller arrays will be continuously created and copied as the array grows.
	 */
	GROW
}
