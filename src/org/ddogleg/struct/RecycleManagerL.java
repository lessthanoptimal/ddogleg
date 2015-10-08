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

package org.ddogleg.struct;

import java.util.ArrayList;

/**
 * {@link RecycleManager} which maintains a used list.  Does not allow you to recycle individual elements
 *
 * @author Peter Abeles
 */
public class RecycleManagerL<T> extends RecycleManager<T> {
	protected ArrayList<T> used = new ArrayList<T>();

	public RecycleManagerL(Class<T> targetClass) {
		super(targetClass);
	}

	public ArrayList<T> getUsed() {
		return used;
	}

	/**
	 * Puts all elements in used into unused and clears the used list
	 */
	public void recycleAll() {
		unused.addAll(used);
		used.clear();
	}

	@Override
	public void recycleInstance(T object) {
		throw new IllegalArgumentException("Can't recycle individual elements if keeping track of used list");
	}

	@Override
	public T requestInstance() {
		T n = super.requestInstance();
		used.add(n);
		return n;
	}
}
