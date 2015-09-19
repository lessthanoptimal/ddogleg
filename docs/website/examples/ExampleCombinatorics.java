		List<String> words = new ArrayList<String>();
		words.add("cat");
		words.add("dog");
		words.add("bird");
		words.add("moose");

		// Demonstration of going through all of the sets combinations
		Combinations<String> combinations = new Combinations<String>(words,2);
		
		long N = combinations.computeTotalCombinations();
		System.out.println("Total number of combinations = "+N+"\n");
		List<String> bucket = new ArrayList<String>();
		int i = 0;
		do {
			combinations.getBucket(bucket);
			System.out.printf("i = %2d || ", i);
			for( String s : bucket ) {
				System.out.print(s+" ");
			}
			System.out.println();
			i++;
		} while( combinations.next() );

		System.out.println("\nReverse");
		i = 0;
		do {
			combinations.getBucket(bucket);
				
			System.out.printf("i = %2d || ", i);
			for( String s : bucket ) {
				System.out.print(s+" ");
			}
			System.out.println();
			i++;
		} while( combinations.previous() );

		// Demonstration of going through all of the sets permutations
		Permute<String> permute = new Permute<String>(words);

		N = permute.getTotalPermutations();
		System.out.println("\n\nTotal number of permutations = "+N+"\n");
		i = 0;
		do {
			permute.getPermutation(bucket);
			System.out.printf("i = %2d || ", i);
			for( String s : bucket ) {
				System.out.print(s+" ");
			}
			System.out.println();
			i++;
		} while( permute.next() );

		System.out.println("\nReverse");
		i = 0;
		do {
			permute.getPermutation(bucket);

			System.out.printf("i = %2d || ", i);
			for( String s : bucket ) {
				System.out.print(s+" ");
			}
			System.out.println();
			i++;
		} while( permute.previous() );
