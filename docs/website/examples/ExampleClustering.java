		List<double[]> points = new ArrayList<double[]>();

		// create 3 clusters drawn from a uniform square distribution
		points.addAll( createCluster(5,7,2,100) );
		points.addAll( createCluster(1,2,1,120) );
		points.addAll( createCluster(4,5,1.5,300) );

		// remove any structure from the point's ordering
		Collections.shuffle(points);

		ComputeClusters<double[]> cluster = FactoryClustering.kMeans_F64(null,1000,100, 1e-8);
//		ComputeClusters<double[]> cluster = FactoryClustering.gaussianMixtureModelEM_F64(1000, 1e-8);

		cluster.init(2, rand.nextLong());

		// visualization stuff
		Gui gui = new Gui(points);

		JFrame frame = new JFrame();
		frame.add(gui,BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// Run the cluster algorithm again each time the user clicks the window
		// This allows you to see how stable the clusters are
		while( true ) {

			cluster.process(points, 3);

			AssignCluster<double[]> assignment = cluster.getAssignment();
			gui.update(assignment);

			while( !clicked ) {
				Thread.yield();
			}
			clicked = false;
		}
