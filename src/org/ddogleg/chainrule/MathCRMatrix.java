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

import java.util.Random;

/// Specifies standard math operations on [CRMatrix].
public interface MathCRMatrix<T> {
	/// Performs matrix multiplication. out = a\*b
	void mult( CRMatrix<T> a, CRMatrix<T> b, CRMatrix<T> out );

	/// Adds src into dst. Needed when a value feeds more than one consumer and the derivatives
	/// arriving from each have to be summed
	void addTo( CRMatrix<T> dst, CRMatrix<T> src );

	/// Fills the diagonal elements with the specified value
	void diag( double value, CRMatrix<T> m );

	/// Fills all elements in the matrix with zero
	default void zero( CRMatrix<T> m ) {fill(m, 0.0);}

	/// Fills every element with the specified value
	void fill( CRMatrix<T> m, double value );

	/// Fills all elements in the array with zero
	void zero( CRArray<T> m, int length );

	/// Fills the matrix with uniformly distributed random values.
	///
	/// @param rand Random number generator
	/// @param min Lower extent of random values
	/// @param max Upper extent of random values
	/// @param output Matrix which is filled
	void random( Random rand, double min, double max, CRMatrix<T> output );

	/// Creates a matrix with random values
	CRMatrix<T> random( Random rand, double min, double max, int rows, int cols );

	/// Creates a random array
	CRArray<T> random( Random rand, double min, double max, int length );

	/// Returns true if the matrix contains at least one uncountable number
	boolean isUncountable( CRMatrix<T> m );

	/// Allocates a new array with the specified length
	CRArray<T> createArray( int length );

	/// Creates a new matrix with the specified shape
	CRMatrix<T> createMatrix( int rows, int cols );
}
