/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.chainrule;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;

/// References an index in an array. Used to abstract reading and writing array types.
@SuppressWarnings("unchecked")
@Setter @Getter public class CRArray<A> {
	/// Reference to the array
	@SuppressWarnings("NullAway.Init") public A a;

	/// First index in the sub array
	public int offset = 0;

	public CRArray( A array, int offset ) {
		this.a = array;
		this.offset = offset;
	}

	public CRArray() {}

	public static CRArray<double[]> arrayD( double... values ) {
		return new CRArray<>(values, 0);
	}

	public CRArray<A> wrap( A array, int offset ) {
		this.a = array;
		this.offset = offset;
		return this;
	}

	public boolean isInvalid() {
		return offset < 0;
	}

	public void markInvalid() {
		offset = -1;
	}

	/// Copies values into the passed in 'dest' array
	///
	/// @param dest Reference to the destination array
	/// @param offset Offset in the dest array where it should start writing
	/// @param length Number of elements to copy
	public void write( A dest, int offset, int length ) {
		System.arraycopy(this.a, this.offset, dest, offset, length);
	}

	/// Copies the passed in array into the referenced array
	///
	/// @param src Reference to the source array
	/// @param offset Offset in the src array where it should start
	/// @param length Number of elements to copy
	public void read( A src, int offset, int length ) {
		System.arraycopy(src, offset, this.a, this.offset, length);
	}

	/// Assigns a new value to the offset and returns a reference to 'this' allowing operations to be chained.
	public CRArray<A> setOffset( int offset ) {
		this.offset = offset;
		return this;
	}

	/// Array access for type double
	public void d( int index, double value ) {
		((double[])a)[offset + index] = value;
	}

	/// Array access for type double
	public double d( int index ) {
		return ((double[])a)[offset + index];
	}

	/// Array access for type double
	public void f( int index, float value ) {
		((float[])a)[offset + index] = value;
	}

	/// Array access for type float
	public float f( int index ) {
		return ((float[])a)[offset + index];
	}

	/// Array access for type double
	public void i( int index, int value ) {
		((int[])a)[offset + index] = value;
	}

	/// Array access for type int
	public int i( int index ) {
		return ((int[])a)[offset + index];
	}

	/// Array access for type double
	public void s( int index, short value ) {
		((short[])a)[offset + index] = value;
	}

	/// Array access for type short
	public short s( int index ) {
		return ((short[])a)[offset + index];
	}

	/// Array access for type double
	public void b( int index, byte value ) {
		((byte[])a)[offset + index] = value;
	}

	/// Array access for type byte
	public byte b( int index ) {
		return ((byte[])a)[offset + index];
	}

	/// Ensure that the array has at least this many elements in it
	public CRArray<A> reserve( int length ) {
		// see if it needs to allocate a new array
		int arrLength = Array.getLength(a);
		if (arrLength >= length)
			return this;
		a = (A)Array.newInstance(a.getClass().getComponentType(), length);
		return this;
	}

	/// Converts the array into a String using the provided printf format for each element
	///
	/// @param format How each element is converted into a string
	/// @param length Number of elements
	/// @return The string
	public String toStringD( String format, int length ) {
		var d = (double[])a;

		var buffer = new StringBuilder();
		buffer.append("[");
		for (int i = 0; i < length; i++) {
			buffer.append(' ');
			buffer.append(String.format(format, d[offset + i]));
		}
		buffer.append(" ]");
		return buffer.toString();
	}

	/// Checks to see if the two arrays are equals to each other to within the specified tolerance and over the
	/// specified number of elements
	///
	/// @param compare Array to compare against
	/// @param length Number of elements to compare
	/// @param tol Tolerance for equality
	/// @return true if equal
	public boolean isEquals( CRArray<?> compare, int length, double tol ) {
		if (a.getClass() != compare.a.getClass())
			return false;

		if (a instanceof double[]) {
			for (int i = 0; i < length; i++) {
				if (Math.abs(d(i) - compare.d(i)) > tol)
					return false;
			}
			return true;
		} else if (a instanceof float[]) {
			for (int i = 0; i < length; i++) {
				if (Math.abs(f(i) - compare.f(i)) > tol)
					return false;
			}
		} else if (a instanceof int[]) {
			for (int i = 0; i < length; i++) {
				if (Math.abs(i(i) - compare.i(i)) > tol)
					return false;
			}
		} else {
			throw new IllegalArgumentException("Add support for this type");
		}
		return true;
	}

	/// Creates a copy of the original array and this wrapper. Uses reflections.
	public CRArray<A> copy() {
		A c = (A)Array.newInstance(this.a.getClass().getComponentType(), Array.getLength(this.a));
		System.arraycopy(this.a, 0, c, 0, Array.getLength(this.a));
		return new CRArray<>(c, this.offset);
	}
}
