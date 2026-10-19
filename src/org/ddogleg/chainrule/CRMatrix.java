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

/// References an index in an array that represents a 2D matrix. Dense row major format.
@SuppressWarnings("unchecked")
@Setter @Getter public class CRMatrix<A> {
	/// Reference to the array
	@SuppressWarnings("NullAway.Init") public A data;

	/// First index in the sub array
	public int offset = 0;

	/// Rows and columns in the 2D matrix
	public int rows, cols;

	public CRMatrix( A array, int offset, int rows, int cols ) {
		this.data = array;
		this.offset = offset;
		this.rows = rows;
		this.cols = cols;
	}

	public CRMatrix() {}

	public static CRMatrix<double[]> createD( int rows, int cols ) {
		return new CRMatrix<>(new double[rows*cols], 0, rows, cols);
	}

	/// True if this is an invalid matrix. Can be used to indicate if this matrix should not be used
	public boolean isInvalid() {
		return rows < 0 || cols < 0;
	}

	/// Marks the matrix as invalid
	public void markInvalid() {rows = cols = -1;}

	/// Resets the matrix with zeros for rows and cols
	public void reset() {rows = cols = 0;}

	/// Sets the value of this to be identical to 'src' and returns a refernece to 'this'
	public CRMatrix<A> setTo( CRMatrix<A> src ) {
		reshape(src.rows, src.cols);
		read(src.data, 0);

		return this;
	}

	/// Ensure that the array has at least this many elements in it
	public CRMatrix<A> reserve( int length ) {
		// see if it needs to allocate a new array
		int arrLength = Array.getLength(data);
		if (arrLength >= length)
			return this;
		data = (A)Array.newInstance(data.getClass().getComponentType(), length);
		return this;
	}

	/// Resizes the array and makes sure the data array is large enough
	public CRMatrix<A> reshape( int rows, int cols ) {
		// Don't resize matrices with an offset since they probably are a reference to another array
		// this won't case the case where the reference is external and offset is zero
		if (offset != 0)
			throw new IllegalArgumentException("Can't resize Matrix because there is an offset");

		reserve(rows*cols);
		shape(rows, cols);
		return this;
	}

	public void wrap( A array, int offset, int rows, int cols ) {
		this.data = array;
		this.offset = offset;
		this.rows = rows;
		this.cols = cols;
	}

	/// Copies values into the passed in 'dest' array
	///
	/// @param dest Reference to the destination array
	/// @param offset Offset in the dest array where it should start writing
	public void write( A dest, int offset ) {
		System.arraycopy(this.data, this.offset, dest, offset, rows*cols);
	}

	/// Copies the passed in array into the referenced array
	///
	/// @param src Reference to the source array
	/// @param offset Offset in the src array where it should start
	public void read( A src, int offset ) {
		System.arraycopy(src, offset, this.data, this.offset, rows*cols);
	}

	/// Assigns a new value to the offset and returns a reference to 'this' allowing operations to be chained.
	public CRMatrix<A> setOffset( int offset ) {
		this.offset = offset;
		return this;
	}

	/// Array access for type double
	public void d( int row, int col, double value ) {
		((double[])data)[offset + row*cols + col] = value;
	}

	/// Array access for type double
	public double d( int row, int col ) {
		return ((double[])data)[offset + row*cols + col];
	}

	/// Array access for type double
	public void f( int row, int col, float value ) {
		((float[])data)[offset + row*cols + col] = value;
	}

	/// Array access for type float
	public float f( int row, int col ) {
		return ((float[])data)[offset + row*cols + col];
	}

	/// Array access for type double
	public void i( int row, int col, int value ) {
		((int[])data)[offset + row*cols + col] = value;
	}

	/// Array access for type int
	public int i( int row, int col ) {
		return ((int[])data)[offset + row*cols + col];
	}

	/// Array access for type double
	public void s( int row, int col, short value ) {
		((short[])data)[offset + row*cols + col] = value;
	}

	/// Array access for type short
	public short s( int row, int col ) {
		return ((short[])data)[offset + row*cols + col];
	}

	/// Array access for type double
	public void b( int row, int col, byte value ) {
		((byte[])data)[offset + row*cols + col] = value;
	}

	/// Array access for type byte
	public byte b( int row, int col ) {
		return ((byte[])data)[offset + row*cols + col];
	}

	public String toStringD( String format ) {
		var d = (double[])data;

		var buffer = new StringBuilder();
		buffer.append("[");
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				buffer.append(' ');
				buffer.append(String.format(format, d[offset + i*cols + j]));
			}
			if (i < rows - 1)
				buffer.append("; ");
		}
		buffer.append(" ]");
		return buffer.toString();
	}

	/// Creates a string representation of a 2D matrix. zeros are printed as dots.
	public String toStringM( String format ) {
		var d = (double[])data;

		// See how big the spacing is
		int wordLength = String.format(format, -1.932323123123).length();

		var buffer = new StringBuilder();

		buffer.append("CRMatrix ").append(rows).append("x").append(cols).append('\n');
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				double v = d[offset + i*cols + j];
				if (v == 0.0)
					buffer.append(String.format("%" + wordLength + "s", "."));
				else {
					String word = String.format(format, v);
					for (int idxLetter = 0; idxLetter < wordLength-word.length() ; idxLetter++) {
						buffer.append(' ');
					}
					buffer.append(word);
				}
				if (j < cols -1)
					buffer.append(" ");
			}
			if (i < rows - 1)
				buffer.append("\n");
		}
		return buffer.toString();
	}

	public boolean isEquals( CRMatrix<?> compare, double tol ) {
		if (data.getClass() != compare.data.getClass())
			return false;

		int length = rows*cols;

		if (data instanceof double[] aa) {
			var bb = (double[])compare.data;
			for (int i = 0; i < length; i++) {
				if (Math.abs(aa[offset + i] - bb[compare.offset + i]) > tol)
					return false;
			}
			return true;
		} else if (data instanceof float[] aa) {
			var bb = (float[])compare.data;
			for (int i = 0; i < length; i++) {
				if (Math.abs(aa[offset + i] - bb[compare.offset + i]) > tol)
					return false;
			}
		} else if (data instanceof int[] aa) {
			var bb = (int[])compare.data;
			for (int i = 0; i < length; i++) {
				if (Math.abs(aa[offset + i] - bb[compare.offset + i]) > tol)
					return false;
			}
		} else {
			throw new IllegalArgumentException("Add support for this type");
		}
		return true;
	}

	/// true if one element is NaN in the matrix
	public boolean isNaN() {
		int length = rows*cols;

		if (data instanceof double[] aa) {
			for (int i = 0; i < length; i++) {
				if (Double.isNaN(aa[offset + i]))
					return true;
			}
			return false;
		} else if (data instanceof float[] aa) {
			for (int i = 0; i < length; i++) {
				if (Float.isNaN(aa[offset + i]))
					return true;
			}
			return false;
		}
		return false;
	}

	/// Creates a copy of the original array and this wrapper. Uses reflections.
	public CRMatrix<A> copy() {
		A c = (A)Array.newInstance(this.data.getClass().getComponentType(), Array.getLength(this.data));
		System.arraycopy(this.data, 0, c, 0, Array.getLength(this.data));
		return new CRMatrix<>(c, this.offset, rows, cols);
	}

	/// Number of elements in the matrix
	public int elementCount() {
		return rows*cols;
	}

	public void shape( int rows, int cols ) {
		this.rows = rows;
		this.cols = cols;
	}
}
