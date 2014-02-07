/*
 * Copyright (c) 2012-2014, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.sorting;

/**
 * Class which can be extended and allows the object to be sorted faster than a generic Comparable
 *
 * @author Peter Abeles
 */
public class SortableParameter_F32 {
	public float sortValue;

	public SortableParameter_F32(float sortValue) {
		this.sortValue = sortValue;
	}

	public SortableParameter_F32() {
	}
}
