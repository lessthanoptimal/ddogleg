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

package org.ddogleg.clustering.misc;

import org.ddogleg.struct.DogLambdas;
import org.ddogleg.struct.LArrayAccessor;

import java.util.List;

/**
 * Wrapper around {@link List} for {@link LArrayAccessor}.
 *
 * @author Peter Abeles
 */
public class ListAccessor<P> implements LArrayAccessor<P> {
	List<P> list;
	DogLambdas.Copy<P> copier;
	Class<P> type;

	public ListAccessor(List<P> list, DogLambdas.Copy<P> copier, Class<P> type ) {
		this.list = list;
		this.copier = copier;
		this.type = type;
	}

	@Override public P getTemp( int index ) {
		return list.get(index);
	}

	@Override public void getCopy( int index, P dst ) {
		copier.copy(list.get(index), dst);
	}

	@Override public void copy( P src, P dst ) {
		copier.copy(src, dst);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override public Class<P> getElementType() {
		return type;
	}
}
