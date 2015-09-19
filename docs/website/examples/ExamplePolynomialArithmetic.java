		Polynomial a = Polynomial.wrap(5,-2,3,0.5,30.4);
		Polynomial b = Polynomial.wrap(-0.4,8.4,-2.3);

		System.out.println("a = "+a);
		System.out.println("b = "+b);
		System.out.println("a + b = "+PolynomialOps.add(a,b,null));
		System.out.println("a * b = "+PolynomialOps.multiply(a, b, null));

		// Declare storage for the quotient and the remainder
		Polynomial q = new Polynomial(10);
		Polynomial r = new Polynomial(10);
		PolynomialOps.divide(a, b, q,r);
		System.out.println("a / b = ( "+q+" , "+r+" )");
		System.out.println("Derivative a = "+PolynomialOps.derivative(a,null));
