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

import java.util.List;
import java.util.Stack;

/**
 * A double linked list.  Internal data structures are recycled to minimize creation of new memory.
 *
 * @author Peter Abeles
 */
public class LinkedList<T> {

	// first element in the list
	Element first;
	// last element in the list
	Element last;
	// total number of elements in the list
	int size;

	// recycled elements.  It is assumed that all elements inside of here have all parameters set to null already
	Stack<Element> available = new Stack<Element>();

	/**
	 * Puts the linked list back into its initial state.  Elements are saved for later use.
	 */
	public void reset() {
		Element e = first;
		while( e != null ) {
			Element n = e.next;
			e.clear();
			available.add( e );
			e = n;
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

	public Element getElement( int index , boolean fromFront ) {
		if( index > size || index < 0 ) {
			throw new IllegalArgumentException("index is out of bounds");
		}
		if( fromFront ) {
			Element e = first;
			for( int i = 0; i < index; i++ ) {
				e = e.next;
			}
			return e;
		} else {
			Element e = last;
			for( int i = 0; i < index; i++ ) {
				e = e.previous;
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
	public Element pushHead( T object ) {
		Element e = requestNew();
		e.object = object;

		if( first == null ) {
			first = last = e;
		} else {
			e.next = first;
			first.previous = e;
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
	public Element pushTail( T object ) {
		Element e = requestNew();
		e.object = object;

		if( last == null ) {
			first = last = e;
		} else {
			e.previous = last;
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
	public Element insertAfter( Element previous , T object ) {
		Element e = requestNew();
		e.object = object;
		e.previous = previous;
		e.next = previous.next;
		if( e.next != null ) {
			e.next.previous = e;
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
	public Element insertBefore( Element next , T object ) {
		Element e = requestNew();
		e.object = object;
		e.previous = next.previous;
		e.next = next;

		if( e.previous != null ) {
			e.previous.next = e;
		} else {
			first = e;
		}
		next.previous = e;
		size++;
		return e;
	}

	/**
	 * Swaps the location of the two elements
	 *
	 * @param a Element
	 * @param b Element
	 */
	public void swap( Element a , Element b ) {
		if (a.next == b) {
			if( a.previous != null ) {
				a.previous.next = b;
			}
			if( b.next != null ) {
				b.next.previous = a;
			}
			Element tmp = a.previous;
			a.previous = b;
			a.next = b.next;
			b.previous = tmp;
			b.next = a;
			if( first == a )
				first = b;
			if( last == b )
				last = a;
		} else if (a.previous == b) {
			if( a.next != null ) {
				a.next.previous = b;
			}
			if( b.previous != null ) {
				b.previous.next = a;
			}
			Element tmp = a.next;
			a.next = b;
			a.previous = b.previous;
			b.previous = a;
			b.next = tmp;

			if( first == b )
				first = a;
			if( last == a )
				last = b;
		} else {
			if (a.next != null) {
				a.next.previous = b;
			}
			if (a.previous != null) {
				a.previous.next = b;
			}
			if (b.next != null) {
				b.next.previous = a;
			}
			if (b.previous != null) {
				b.previous.next = a;
			}
			Element tempNext = b.next;
			Element tempPrev = b.previous;
			b.next = a.next;
			b.previous = a.previous;
			a.next = tempNext;
			a.previous = tempPrev;

			if (a.next == null)
				last = a;
			else if (b.next == null)
				last = b;
			if (a.previous == null)
				first = a;
			else if (b.previous == null)
				first = b;
		}
	}

	/**
	 * Removes the element from the list and saves the element data structure for later reuse.
	 * @param element The item which is to be removed from the list
	 */
	public void remove( Element element ) {
		if( element.next == null ) {
			last = element.previous;
		} else {
			element.next.previous = element.previous;
		}
		if( element.previous == null ) {
			first = element.next;
		} else {
			element.previous.next = element.next;
		}
		size--;
		element.clear();
		available.push(element);
	}

	/**
	 * Removes the first element from the list
	 * @return The object which was contained in the first element
	 */
	public Object removeHead() {
		if( first == null )
			throw new IllegalArgumentException("Empty list");

		Object ret = first.getObject();
		Element e = first;
		available.push(first);

		if( first.next != null ) {
			first.next.previous = null;
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
	 * @return The object which was contained in the lsat element
	 */
	public Object removeTail() {
		if( last == null )
			throw new IllegalArgumentException("Empty list");

		Object ret = last.getObject();
		Element e = last;
		available.add(last);

		if( last.previous != null ) {
			last.previous.next = null;
			last = last.previous;
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
	public Element find( T object ) {
		Element e = first;
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
	public Element getHead() {
		return first;
	}

	/**
	 * Returns the last element in the list
	 * @return last element
	 */
	public Element getTail() {
		return last;
	}

	/**
	 * Add all elements in list into this linked list
	 * @param list List
	 */
	public void addAll( List<T> list ) {
		if( list.isEmpty() )
			return;

		Element a = requestNew();
		a.object = list.get(0);

		if( first == null ) {
			first = a;
		} else if( last != null ) {
			last.next = a;
			a.previous = last;
		}

		for (int i = 1; i < list.size(); i++) {
			Element b = requestNew();
			b.object = list.get(i);

			a.next = b;
			b.previous = a;
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

		Element a = requestNew();
		a.object = array[first];

		if( this.first == null ) {
			this.first = a;
		} else if( last != null ) {
			last.next = a;
			a.previous = last;
		}

		for (int i = 1; i < length; i++) {
			Element b = requestNew();
			b.object =  array[first+i];

			a.next = b;
			b.previous = a;
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
	protected Element requestNew () {
		if( available.isEmpty() ) {
			return new Element();
		} else {
			return available.pop();
		}
	}

	public static class Element
	{
		public Element next;
		public Element previous;
		public Object object;

		public void clear() {
			next = null;
			previous = null;
			object = null;
		}

		public Element getNext() {
			return next;
		}

		public void setNext(Element next) {
			this.next = next;
		}

		public Element getPrevious() {
			return previous;
		}

		public void setPrevious(Element previous) {
			this.previous = previous;
		}

		public Object getObject() {
			return object;
		}

		public void setObject(Object object) {
			this.object = object;
		}
	}
}
