package org.ddogleg.nn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.ddogleg.nn.BenchmarkNearestNeighbor.createData;

/**
 * @author Peter Abeles
 */
public class BenchmarkNearestNeighborCorrect {

	int dimen;
	List<double[]> cloud;
	List<double[]> searchSet;
	double[] solutions[];
	double maxDistance;
	NnData result = new NnData();

	NearestNeighbor exhaustive = FactoryNearestNeighbor.exhaustive();

	private double computeCorrectness( NearestNeighbor alg ) {
		alg.init(dimen);
		alg.setPoints(cloud,null);

		int numCorrect = 0;
		for( int i = 0; i < searchSet.size(); i++ ) {
			double []p = searchSet.get(i);
			if( alg.findNearest(p,maxDistance,result) ) {
				if( solutions[i] == result.point )
					numCorrect++;
			}
		}

		return 100.0*numCorrect/searchSet.size();
	}


	public List<Subject> createAlg() {
		List<Subject> ret = new ArrayList<Subject>();

//		ret.add( new Subject(FactoryNearestNeighbor.exhaustive(),"Exhaustive"));
//		ret.add( new Subject(FactoryNearestNeighbor.kdtree(),"kdtree"));
		ret.add( new Subject(FactoryNearestNeighbor.kdtree(200),"kdtree P "));

		return ret;
	}

	public void evaluateDataSet( int dimen , int cloudSize , int searchSize ) {
		Random rand = new Random(234);

		this.dimen = dimen;
		this.cloud = createData(rand,cloudSize,dimen);
		this.searchSet = createData(rand,searchSize,dimen);
		this.solutions = new double[ searchSize ][];
		this.maxDistance = 10;

		System.out.println("Computing solutions");
		exhaustive.init(dimen);
		exhaustive.setPoints(cloud,null);
		for( int i = 0; i < searchSize; i++ ) {
			exhaustive.findNearest(searchSet.get(i),maxDistance,result);
			solutions[i] = result.point;
		}

		System.out.println("K = "+dimen+"  cloud = "+cloudSize);
		for( Subject alg : createAlg() ) {
			System.out.printf("%20s = %4.1f\n",alg.name,computeCorrectness(alg.alg));
		}
	}


	private static class Subject {
		public String name;
		public NearestNeighbor alg;

		private Subject(NearestNeighbor alg,String name) {
			this.name = name;
			this.alg = alg;
		}
	}


	// TODO have a search set
	// TODO Compute correct solution using exhaustive
	public static void main( String args[] ) {
		BenchmarkNearestNeighborCorrect app = new BenchmarkNearestNeighborCorrect();

//		app.evaluateDataSet(3,30);
//		app.evaluateDataSet(3,300);
//		app.evaluateDataSet(3,600);
//		app.evaluateDataSet(5,10000);
		app.evaluateDataSet(10,100000,1000);
		app.evaluateDataSet(20,100000,1000);
		app.evaluateDataSet(60,100000,1000);
		app.evaluateDataSet(120,100000,1000);

		try {
			synchronized ( app ) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {}
	}
}
