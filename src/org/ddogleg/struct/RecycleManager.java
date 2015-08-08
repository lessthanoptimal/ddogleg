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

import java.util.Stack;

/**
 * Simple class which helps minimize declaring new objects by helping you recycle them.
 *
 * @author Peter Abeles
 */
public class RecycleManager<T> {

	protected Class<T> targetClass;
	protected Stack<T> unused = new Stack<T>();

	public RecycleManager(Class<T> targetClass) {
		this.targetClass = targetClass;
	}

	protected RecycleManager() {
	}

	/**
	 * Either returns a recycled instance or a new one.
	 */
	public T requestInstance() {
		T a;
		if( unused.size() > 0 ) {
			a = unused.pop();
		} else {
			a = createInstance();
		}
		return a;
	}

	/**
	 * Call when an instance is no longer needed and can be recycled
	 */
	public void recycleInstance( T object ) {
		unused.add(object);
	}

	/**
	 * Creates a new instance using the class.  overload this to handle more complex constructors
	 */
	protected T createInstance() {
		try {
			return targetClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the stack containing all the unused instances.
	 */
	public Stack<T> getUnused() {
		return unused;
	}
}
