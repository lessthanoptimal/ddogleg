		// Easiest way to create a NN algorithm is using the factory below
		NearestNeighbor<Double> nn = FactoryNearestNeighbor.kdtree();

		// specify the dimension of each point
		nn.init(2);

		// Create data that's going to be searched
		List<double[]> points = new ArrayList<double[]>();
		List<Double> data = new ArrayList<Double>();

		// For sake of demonstration add a set of points along the line
		for( int i = 0; i < 10; i++ ) {
			double[] p = new double[]{i,i*2};
			points.add(p);
			data.add((double)i);
		}

		// Pass the points and associated data.  Internally a data structure is constructed that enables fast lookup.
		// This can be one of the more expensive operations, depending on which implementation is used.
		nn.setPoints(points,data);

		// declare storage for where to store the result
		NnData<Double> result = new NnData<Double>();

		// It will look for the closest point to [1.1,2.2] which will be [1,2]
		// The second parameter specifies the maximum distance away that it will consider for a neighbor
		// set to -1 to set to the largest possible value
		if( nn.findNearest(new double[]{1.1,2.2},-1,result) ) {
			System.out.println("Best match:");
			System.out.println("   point     = "+result.point[0]+" "+result.point[1]);
			System.out.println("   data      = "+result.data);
			System.out.println("   distance  = "+result.distance);
		} else {
			System.out.println("No match found");
		}
