/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

import org.ddogleg.combinatorics.Combinations;
import org.ddogleg.combinatorics.Permute;

import java.util.ArrayList;
import java.util.List;

/**
 * Example that demonstrates how to compute all the combinations and permutations of a set.
 *
 * @author Peter Abeles
 */
public class ExampleCombinatorics {

	public static void main( String[] args ) {
		List<String> words = new ArrayList<String>();
		words.add("cat");
		words.add("dog");
		words.add("bird");
		words.add("moose");

		// Demonstration of going through all of the sets combinations
		var combinations = new Combinations<String>(words, 2);

		long N = combinations.computeTotalCombinations();
		System.out.println("Total number of combinations = " + N + "\n");
		var bucket = new ArrayList<String>();
		int i = 0;
		do {
			combinations.getBucket(bucket);
			System.out.printf("i = %2d || ", i);
			for (String s : bucket) {
				System.out.print(s + " ");
			}
			System.out.println();
			i++;
		} while (combinations.next());

		System.out.println("\nReverse");
		i = 0;
		do {
			combinations.getBucket(bucket);

			System.out.printf("i = %2d || ", i);
			for (String s : bucket) {
				System.out.print(s + " ");
			}
			System.out.println();
			i++;
		} while (combinations.previous());

		// Demonstration of going through all of the sets permutations
		Permute<String> permute = new Permute<String>(words);

		N = permute.getTotalPermutations();
		System.out.println("\n\nTotal number of permutations = " + N + "\n");
		i = 0;
		do {
			permute.getPermutation(bucket);
			System.out.printf("i = %2d || ", i);
			for (String s : bucket) {
				System.out.print(s + " ");
			}
			System.out.println();
			i++;
		} while (permute.next());

		System.out.println("\nReverse");
		i = 0;
		do {
			permute.getPermutation(bucket);

			System.out.printf("i = %2d || ", i);
			for (String s : bucket) {
				System.out.print(s + " ");
			}
			System.out.println();
			i++;
		} while (permute.previous());
	}
}
