		Random rand = new Random(234);

		//------------------------ Create Observations
		// define a line in 2D space as the tangent from the origin
		double lineX = -2.1;
		double lineY = 1.3;
		List<Point2D> points = generateObservations(rand, lineX, lineY);

		//------------------------ Compute the solution
		// Let it know how to compute the model and fit errors
		ModelManager<Line2D> manager = new LineManager();
		ModelGenerator<Line2D,Point2D> generator = new LineGenerator();
		DistanceFromModel<Line2D,Point2D> distance = new DistanceFromLine();

		// RANSAC or LMedS work well here
		ModelMatcher<Line2D,Point2D> alg =
				new Ransac<Line2D,Point2D>(234234,manager,generator,distance,500,0.01);
//		ModelMatcher<Line2D,Point2D> alg =
//				new LeastMedianOfSquares<Line2D, Point2D>(234234,100,0.1,0.5,generator,distance);

		if( !alg.process(points) )
			throw new RuntimeException("Robust fit failed!");

		// let's look at the results
		Line2D found = alg.getModelParameters();

		// notice how all the noisy points were removed and an accurate line was estimated?
		System.out.println("Found line   "+found);
		System.out.println("Actual line   x = "+lineX+" y = "+lineY);
		System.out.println("Match set size = "+alg.getMatchSet().size());
