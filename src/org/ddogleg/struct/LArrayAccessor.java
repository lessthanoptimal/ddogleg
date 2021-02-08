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

/**
 * Provides access to the elements inside very large or small arrays. Designed to provide efficient access if the
 * data is compressed or not. As a convenience, a function is provided for creating a copy of the elements.
 *
 * @author Peter Abeles
 */
public interface LArrayAccessor<P> {
	/**
	 * Returns an instance of P which has the value of the element at 'index'. Note that the accessor will
	 * own the data type which is returned and can modify it on the next call.
	 *
	 * <p>This design is intended to be efficient when a massive array that's compressed and a very small array
	 * which is not compressed is used.</p>
	 */
	P getTemp( int index );

	/** Copies the element at 'index' into 'dst'. Only use if a copy is required. */
	void getCopy( int index , P dst );

	/** Copies src into dst */
	void copy(P src, P dst);

	/** Number of elements in the set */
	int size();

	/**
	 * Data type of element
	 */
	Class<P> getElementType();
}
