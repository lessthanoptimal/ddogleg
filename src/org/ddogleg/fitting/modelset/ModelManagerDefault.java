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

package org.ddogleg.fitting.modelset;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Default model manager.  Assumes the model class has a no argument constructor and a set function.
 *
 * @author Peter Abeles
 */
public class ModelManagerDefault<T> implements ModelManager<T> {

	Class type;
	Method copyMethod;

	public ModelManagerDefault(Class type) {
		this.type = type;

		try {
			copyMethod = type.getMethod("set",type);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Was expecting a data.set(Type a) method",e);
		}
	}

	@Override
	public T createModelInstance() {
		try {
			return (T)type.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void copyModel(T src, T dst) {
		try {
			copyMethod.invoke(dst,src);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
