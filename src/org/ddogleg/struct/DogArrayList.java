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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Wrapper around queue which allows it to act as a {@link List}.
 *
 * @author Peter Abeles
 */
public class DogArrayList<T> implements List<T> , Serializable {
	DogArray<T> array;

	public DogArrayList( DogArray<T> array ) {
		this.array = array;
	}

	@Override
	public int size() {
		return array.size;
	}

	@Override
	public boolean isEmpty() {
		return array.size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		return array.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new MyIterator();
	}

	@Override
	public Object[] toArray() {
		Object[] ret = new Object[array.size];

		System.arraycopy(array.data,0,ret,0, array.size);

		return ret;
	}

	@Override
	public <A> A[] toArray(A[] a) {
		System.arraycopy(array.data,0,a,0, array.size);
		return a;
	}

	@Override
	public boolean add(T t) {
		throw new RuntimeException("Add is not supported by FastQueue. You need FastArray instead");
	}

	@Override
	public boolean remove(Object o) {
		throw new RuntimeException("Not all list operations are supposed.");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for( Object o : c ) {
			if( !contains(o) )
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new RuntimeException("Add is not supported by FastQueue. You need FastArray instead");
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new RuntimeException("Not all list operations are supposed.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("Not all list operations are supposed.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("Not all list operations are supposed.");
	}

	@Override
	public void clear() {
		array.reset();
	}

	@Override
	public T get(int index) {
		return array.data[index];
	}

	@Override
	public T set(int index, T element) {
		throw new RuntimeException("Set is not supported by FastQueue. You need FastArray instead");
	}

	@Override
	public void add(int index, T element) {
		throw new RuntimeException("Not all list operations are supposed.");
	}

	@Override
	public T remove(int index) {
		throw new RuntimeException("Not all list operations are supposed.");
	}

	@Override
	public int indexOf(Object o) {
		return array.indexOf((T)o);
	}

	@Override
	public int lastIndexOf(Object o) {
		for(int i = array.size-1; i >= 0; i-- ) {
			if( array.data[i].equals(o) )
				return i;
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		return new MyIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new RuntimeException("Not supported");
	}

	public class MyIterator implements ListIterator<T>
	{
		int index = 0;

		@Override
		public boolean hasNext() {
			return index < array.size;
		}

		@Override
		public T next() {
			return array.data[index++];
		}

		@Override
		public boolean hasPrevious() {
			return index > 0;
		}

		@Override
		public T previous() {
			return array.data[--index];
		}

		@Override
		public int nextIndex() {
			return index;
		}

		@Override
		public int previousIndex() {
			return index-1;
		}

		@Override
		public void remove() {
			throw new RuntimeException("Not all list operations are supposed.");
		}

		@Override
		public void set(T t) {
			array.data[index-1] = t;
		}

		@Override
		public void add(T t) {
			throw new RuntimeException("Add is not supported by FastQueue. Use FastArray instead");
		}
	}

	public DogArray<T> getArray() {
		return array;
	}

	public void setArray( DogArray<T> array ) {
		this.array = array;
	}
}
