		// Select which algorithm to use
		PolynomialRoots finder = PolynomialOps.createRootFinder(6, RootFinderType.EVD);

		// Create an arbitrary 3rd order polynomial
		// f(x) = 2 + 0.2*x + 5*x^2 + 3*x^3
		Polynomial poly = Polynomial.wrap( 2 , 0.2 , 5 , 3 );

		// Find the roots
		if( !finder.process(poly) )
			throw new RuntimeException("Failed to find solution!");

		// Print the solution
		List<Complex_F64> roots = finder.getRoots();

		System.out.println("Total roots found: "+roots.size());

		for( Complex_F64 c : roots ) {
			if( !c.isReal() ) {
				System.out.println("root is imaginary: "+c);
				continue;
			}

			double value = poly.evaluate(c.real);
			System.out.println("Polynomial value at "+c.real+" is "+value);
		}
