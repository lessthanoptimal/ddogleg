/*
 * Copyright (c) 2012-2022, Peter Abeles. All Rights Reserved.
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

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

/**
 * A double linked list.  Internal data structures are recycled to minimize creation of new memory.
 *
 * @author Peter Abeles
 */
public class DogLinkedList<T> {
	// first element in the list
	protected @Nullable Element<T> first;
	// last element in the list
	protected @Nullable Element<T> last;
	// total number of elements in the list
	protected int size;

	// recycled elements.  It is assumed that all elements inside of here have all parameters set to null already
	protected final ArrayDeque<Element<T>> available = new ArrayDeque<>();

	/**
	 * Puts the linked list back into its initial state.  Elements are saved for later use.
	 */
	public void reset() {
		Element<T> e = first;
		while( e != null ) {
			Element<T> n = e.next;
			e.clear();
			available.add( e );
			e = n;
			// This is possible if the list is cyclical
			if (e == first)
				break;
		}
		first = last = null;
		size = 0;
	}

	/**
	 * Checks to see if there are no elements in the list
	 * @return true if empty or false if not
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Returns the N'th element when counting from the from or from the back
	 *
	 * @param index Number of elements away from the first or last element. Must be positive.
	 * @return if true then the number of elements will be from first otherwise last
	 */
	@SuppressWarnings("NullAway")
	public Element<T> getElement( int index , boolean fromFront ) {
		if( index > size || index < 0 ) {
			throw new IllegalArgumentException("index is out of bounds");
		}
		if( fromFront ) {
			Element<T> e = first;
			for( int i = 0; i < index; i++ ) {
				if( e == null )
					throw new IllegalArgumentException("Element "+i+" is null");
				e = e.next;
			}
			return e;
		} else {
			Element<T> e = last;
			for( int i = 0; i < index; i++ ) {
				if( e == null )
					throw new IllegalArgumentException("Element "+i+" is null");
				e = e.prev;
			}
			return e;
		}
	}

	/**
	 * Adds the element to the front of the list.
	 *
	 * @param object Object being added.
	 * @return The element it was placed inside of
	 */
	public Element<T> pushHead( T object ) {
		Element<T> e = requestNew();
		e.object = object;

		if( first == null ) {
			first = last = e;
		} else {
			e.next = first;
			first.prev = e;
			first = e;
		}
		size++;

		return e;
	}

	/**
	 * Adds the element to the back of the list.
	 *
	 * @param object Object being added.
	 * @return The element it was placed inside of
	 */
	public Element<T> pushTail( T object ) {
		Element<T> e = requestNew();
		e.object = object;

		if( last == null ) {
			first = last = e;
		} else {
			e.prev = last;
			last.next = e;
			last = e;
		}
		size++;

		return e;
	}

	/**
	 * Inserts the object into a new element after the provided element.
	 *
	 * @param previous Element which will be before the new one
	 * @param object The object which goes into the new element
	 * @return The new element
	 */
	public Element<T> insertAfter( Element<T> previous , T object ) {
		Element<T> e = requestNew();
		e.object = object;
		e.prev = previous;
		e.next = previous.next;
		if( e.next != null ) {
			e.next.prev = e;
		} else {
			last = e;
		}
		previous.next = e;
		size++;
		return e;
	}

	/**
	 * Inserts the object into a new element before the provided element.
	 *
	 * @param next Element which will be after the new one
	 * @param object The object which goes into the new element
	 * @return The new element
	 */
	public Element<T> insertBefore( Element<T> next , T object ) {
		Element<T> e = requestNew();
		e.object = object;
		e.prev = next.prev;
		e.next = next;

		if( e.prev != null ) {
			e.prev.next = e;
		} else {
			first = e;
		}
		next.prev = e;
		size++;
		return e;
	}

	/**
	 * Swaps the location of the two elements
	 *
	 * @param a Element
	 * @param b Element
	 */
	public void swap( Element<T> a , Element<T> b ) {
		if (a.next == b) {
			if( a.prev != null ) {
				a.prev.next = b;
			}
			if( b.next != null ) {
				b.next.prev = a;
			}
			Element<T> tmp = a.prev;
			a.prev = b;
			a.next = b.next;
			b.prev = tmp;
			b.next = a;
			if( first == a )
				first = b;
			if( last == b )
				last = a;
		} else if (a.prev == b) {
			if( a.next != null ) {
				a.next.prev = b;
			}
			if( b.prev != null ) {
				b.prev.next = a;
			}
			Element<T> tmp = a.next;
			a.next = b;
			a.prev = b.prev;
			b.prev = a;
			b.next = tmp;

			if( first == b )
				first = a;
			if( last == a )
				last = b;
		} else {
			if (a.next != null) {
				a.next.prev = b;
			}
			if (a.prev != null) {
				a.prev.next = b;
			}
			if (b.next != null) {
				b.next.prev = a;
			}
			if (b.prev != null) {
				b.prev.next = a;
			}
			Element<T> tempNext = b.next;
			Element<T> tempPrev = b.prev;
			b.next = a.next;
			b.prev = a.prev;
			a.next = tempNext;
			a.prev = tempPrev;

			if (a.next == null)
				last = a;
			else if (b.next == null)
				last = b;
			if (a.prev == null)
				first = a;
			else if (b.prev == null)
				first = b;
		}
	}

	/**
	 * Removes the element from the list and saves the element data structure for later reuse.
	 * @param element The item which is to be removed from the list
	 */
	public void remove( Element<T> element ) {
		if( element.next == null ) {
			last = element.prev;
		} else {
			element.next.prev = element.prev;
		}
		if( element.prev == null ) {
			first = element.next;
		} else {
			element.prev.next = element.next;
		}
		size--;
		element.clear();
		available.add(element);
	}

	/**
	 * Removes the first element from the list
	 * @return The object which was contained in the first element
	 */
	public T removeHead() {
		if( first == null )
			throw new IllegalArgumentException("Empty list");

		T ret = first.getObject();
		Element<T> e = first;
		available.add(first);

		if( first.next != null ) {
			first.next.prev = null;
			first = first.next;
		} else {
			// there's only one element in the list
			first = last = null;
		}
		e.clear();
		size--;
		return ret;
	}

	/**
	 * Removes the last element from the list
	 * @return The object which was contained in the last element
	 */
	public T removeTail() {
		if( last == null )
			throw new IllegalArgumentException("Empty list");

		T ret = last.getObject();
		Element<T> e = last;
		available.add(last);

		if( last.prev != null ) {
			last.prev.next = null;
			last = last.prev;
		} else {
			// there's only one element in the list
			first = last = null;
		}
		e.clear();
		size--;
		return ret;
	}

	/**
	 * Returns the first element which contains 'object' starting from the head.
	 * @param object Object which is being searched for
	 * @return First element which contains object or null if none can be found
	 */
	public @Nullable Element<T> find( T object ) {
		Element<T> e = first;
		while( e != null ) {
			if( e.object == object ) {
				return e;
			}
			e = e.next;
		}
		return null;
	}

	/**
	 * Returns the first element in the list
	 * @return first element
	 */
	public @Nullable Element<T> getHead() {
		return first;
	}

	/**
	 * Returns the last element in the list
	 * @return last element
	 */
	public @Nullable Element<T> getTail() {
		return last;
	}

	/** Returns the value in the head */
	public T getFirst() {
		return Objects.requireNonNull(first).object;
	}

	/** Returns the value in the trail */
	public T getLast() {
		return Objects.requireNonNull(last).object;
	}

	/**
	 * Add all elements in list into this linked list
	 * @param list List
	 */
	public void addAll( List<T> list ) {
		if( list.isEmpty() )
			return;

		Element<T> a = requestNew();
		a.object = list.get(0);

		if( first == null ) {
			first = a;
		} else if( last != null ) {
			last.next = a;
			a.prev = last;
		}

		for (int i = 1; i < list.size(); i++) {
			Element<T> b = requestNew();
			b.object = list.get(i);

			a.next = b;
			b.prev = a;
			a = b;
		}

		last = a;
		size += list.size();
	}

	/**
	 * Adds the specified elements from array into this list
	 * @param array The array
	 * @param first First element to be added
	 * @param length The number of elements to be added
	 */
	public void addAll( T[] array , int first , int length ) {
		if( length <= 0 )
			return;

		Element<T> a = requestNew();
		a.object = array[first];

		if( this.first == null ) {
			this.first = a;
		} else if( last != null ) {
			last.next = a;
			a.prev = last;
		}

		for (int i = 1; i < length; i++) {
			Element<T> b = requestNew();
			b.object =  array[first+i];

			a.next = b;
			b.prev = a;
			a = b;
		}

		last = a;
		size += length;
	}

	/**
	 * Returns the number of elements in the list
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns a new element.  If there are old elements available those are returned, otherwise a new one is returned.
	 *
	 * @return Unused element.
	 */
	protected Element<T> requestNew () {
		if( available.isEmpty() ) {
			return new Element<>();
		} else {
			return available.pop();
		}
	}

	@SuppressWarnings("NullAway")
	public static class Element<T>
	{
		public @Nullable @Getter @Setter Element<T> next;
		public @Nullable @Getter @Setter Element<T> prev;
		public @Getter @Setter T object;

		public void clear() {
			next = null;
			prev = null;
			object = null;
		}
	}
}
