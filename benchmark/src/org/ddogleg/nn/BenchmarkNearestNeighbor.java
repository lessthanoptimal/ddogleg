package org.ddogleg.nn;

import org.ddogleg.Performer;
import org.ddogleg.ProfileOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class BenchmarkNearestNeighbor {

	int dimen;
	List<double[]> cloud;
	List<double[]> searchSet;
	double maxDistance;
	NnData result = new NnData();

	public class SetAndSearch implements Performer {

		NearestNeighbor alg;
		String name;

		public SetAndSearch(NearestNeighbor alg, String name) {
			this.alg = alg;
			this.name = name;

			alg.init(dimen);
			alg.setPoints(cloud,null);
		}

		@Override
		public void process() {
//			alg.init(dimen);
//			alg.setPoints(cloud,null);

			for( double[]p : searchSet ) {
				alg.findNearest(p,maxDistance,result);
			}
		}

		@Override
		public String getName() {
			return name;
		}
	}

	public List<Performer> createAlg() {
		List<Performer> ret = new ArrayList<Performer>();

		ret.add( new SetAndSearch(FactoryNearestNeighbor.exhaustive(),"Exhaustive"));
		ret.add( new SetAndSearch(FactoryNearestNeighbor.kdtree(),"kdtree"));
		ret.add( new SetAndSearch(FactoryNearestNeighbor.kdtree(200),"kdtree P"));

		return ret;
	}

	public void evaluateDataSet( int dimen , int cloudSize , int searchSize ) {
		Random rand = new Random(234);

		this.dimen = dimen;
		this.cloud = createData(rand,cloudSize,dimen);
		this.searchSet = createData(rand,searchSize,dimen);
		this.maxDistance = 10;

		System.out.println("K = "+dimen+"  cloud = "+cloudSize+"  search = "+searchSize);
		for( Performer alg : createAlg() ) {
			ProfileOperation.printOpsPerSec(alg,100);
		}
	}

	public static List<double[]> createData( Random rand , int size , int k ) {
		List<double[]> ret = new ArrayList<double[]>();

		for( int i = 0; i < size; i++ ) {
			double []d = new double[ k ];
			for( int j = 0; j < k; j++ ) {
				d[j] = rand.nextDouble()*3;
			}
			ret.add(d);
		}
		return ret;
	}


	public static void main( String args[] ) {
		BenchmarkNearestNeighbor app = new BenchmarkNearestNeighbor();

		app.evaluateDataSet(3,30,20);
		app.evaluateDataSet(3,300,200);
		app.evaluateDataSet(3,600,500);
		app.evaluateDataSet(5,10000,10000);
		app.evaluateDataSet(10,10000,10000);
		app.evaluateDataSet(20,10000,10000);
		app.evaluateDataSet(60,10000,10000);
		app.evaluateDataSet(120,10000,10000);

	}
}
