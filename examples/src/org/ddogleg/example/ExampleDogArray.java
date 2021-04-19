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

package org.ddogleg.example;

import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.DogArray_F32;

/**
 * DogArrays and BigDogArrays are a dynamic array that will automatically grow and add new instances. They are
 * designed to make recycling memory easy by managing the life cycle entirely from creating to re-initializing
 * the data structures. Many convenience functions are also provided and it they support functional APIs. Access
 * to the raw underlying internal data structures is also provided as this is intended for high performance
 * applications. Implementations for object and primitive data structures are provided.
 *
 * The main difference between DogArrays and BigDogArrays is that DogArrays are a continuous chunk of memory
 * while BigDogArrays are composed of multiple arrays, enabling it to scale to very very large arrays.
 *
 * While not covered here, FastArrays are dynamic object arrays that do not manage the creation or recycling
 * of memory that are also available.
 *
 * @author Peter Abeles
 */
public class ExampleDogArray {
	public static void main( String[] args ) {
		// This will create an array of Point2D. New instances are created using the default constructor
		// and when recycled they will be assigned values of zero.
		// The second argument (reset lambda) is optional. If not provided then the state is not changed
		// when recycled
		var arrayObject = new DogArray<>(Point2D::new, ( p ) -> p.setTo(0, 0));

		// Resize the array so that it has 5 elements
		arrayObject.resize(2);
		// Let's resize it to make it larger, but provide a lambda to initialize these values instead of the default
		// (idx = the index, p = an element in the array)
		arrayObject.resize(6, ( idx, p ) -> p.setTo(idx, 1));
		// Print so we can see what it looks like. Instead of the traditional for loop, let's use the functional API
		arrayObject.forEach(p -> System.out.println(p.x + " " + p.y));

		// When the order doesn't matter, you can "remove" elements with remove swap. This has a O(1) complexity
		// but will swap the first for the last objects
		arrayObject.removeSwap(1);
		System.out.println("After removeSwap");
		arrayObject.forIdx(( idx, p ) -> System.out.println("p[" + idx + "] = " + p.x + " " + p.y));

		// While remove will shift every element, maintaining their order but in O(N) time
		arrayObject.remove(0);
		System.out.println("After remove");
		arrayObject.forIdx(( idx, p ) -> System.out.println("p[" + idx + "] = " + p.x + " " + p.y));

		// It's also possible to treat it like a list. Note that toList() does not declare any memory and recycles
		// the returned object. This is very important in threaded applications.
		for (Point2D p : arrayObject.toList()) {
			p.y += 10;
		}

		// Make it easy to see which output belongs to the code below
		System.out.println("------------ Primitive Array\n");

		// There are primitive versions that should (in theory) support the same API when it makes sense
		var arrayPrimitive = new DogArray_F32();

		// This will fill the first 5 elements with (0.5, 1.5, 2.5, ... )
		arrayPrimitive.resize(5, ( idx ) -> idx + 0.5f);
		// This will fill the new elements (5 to 7) with -1
		arrayPrimitive.resize(8, -1);
		// If you shrink the array size then no values are changed
		arrayPrimitive.resize(7);

		// Let's print it out
		System.out.print("[ ");
		arrayPrimitive.forEach(v -> System.out.print(v + ", "));
		System.out.println(" ]");

		// If you want to access elements in the reverse order, then getTail() can do that
		System.out.println("tail[0]=" + arrayPrimitive.getTail(0) + " tail[2]=" + arrayPrimitive.getTail(2));

		// There are also a few different variants of indexOf() for when you need to search and get the index
		System.out.println("2.5 is at " + arrayPrimitive.indexOf(2.5f));

		// Not previously discussed, but reserve will ensure that there is enough memory preallocate to support
		// an array of the specified size before it needs to declare a new array internally
		arrayPrimitive.reserve(20);
		System.out.println("Reserve: array.size=" + arrayPrimitive.size + " data.length=" + arrayPrimitive.data.length);

		// If you are dealing with errors then its easy to get percentile errors by sorting then using get fraction
		arrayPrimitive.sort();
		System.out.println("35% = " + arrayPrimitive.getFraction(0.35));
	}
}
