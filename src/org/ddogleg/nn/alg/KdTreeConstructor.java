package org.ddogleg.nn.alg;

import org.ddogleg.sorting.QuickSelectArray;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Creates a new {@link KdTree KD-Tree} from a list of points and (optional) associated data. The axis with the largest
 * variance is used to split the point set at each node.
 *
 * WARNING: A reference to the input points is saved.  Do not modify the input until the KD-Tree is no longer needed.
 * This reduced memory overhead significantly.
 *
 * @author Peter Abeles
 */
// TODO insert node
public class KdTreeConstructor<D> {

	// Number of elements/axes in each data point
	private int N;

	// storage for variance calvulation
	private double mean[] = new double[N];
	private double var[] = new double[N];

	// storage for median calculation
	private double tmp[] = new double[1];
	private int indexes[] = new int[1];

	// Used to recycles memory and avoid GC calls
	KdTreeMemory memory;

	/**
	 * Declares internal storage
	 *
	 * @param memory Used to recycle data
	 * @param N Number of elements/axes in each data point
	 */
	public KdTreeConstructor( KdTreeMemory memory , int N ) {
		this.memory = memory;
		this.N = N;
		mean = new double[N];
		var = new double[N];
	}

	public KdTreeConstructor( int N ) {
		this(new KdTreeMemory(),N);
	}

	/**
	 * Creates a new {@link KdTree} from the provided points.
	 *
	 * WARNING: Reference to each point is saved to reduce memory usage..
	 *
	 * @param points Data points.
	 * @param data (Optional) Data associated to each point.  Can be null.
	 * @return KdTre
	 */
	public KdTree construct( List<double[]> points ,
							 List<D> data )
	{
		KdTree tree = memory.requestTree();
		tree.N = N;

		if( points.size() == 1 ) {
			tree.root = createLeaf(points,data);
		} else if( points.size() > 1 ) {
			tree.root = computeBranch(points, data );
		}

		return tree;
	}

	private KdTree.Node computeBranch(List<double[]> points, List<D> data ) {

		// split the points along the axis with the highest variance at its median value
		int split = selectSplitAxis(points);
		// where the median is
		final int medianNum = points.size()/2;
		// sort until the median is found
		quickSelect(points, split,medianNum);

		List<double[]> left = new ArrayList<double[]>(points.size()/2);
		List<double[]> right = new ArrayList<double[]>(points.size()/2);
		List<D> leftData,rightData;

		KdTree.Node node = memory.requestNode();
		node.split = split;
		node.point = points.get( indexes[medianNum] );

		// split into left and right lists.  Skip over the median point
		if( data == null ) {
			for( int i = 0; i < medianNum; i++ ) {
				left.add(points.get(indexes[i]));
			}
			for( int i = medianNum+1; i < points.size(); i++ ) {
				right.add(points.get(indexes[i]));
			}
			leftData = rightData = null;
		} else {
			node.data = data.get( indexes[medianNum] );

			leftData = new ArrayList<D>();
			rightData = new ArrayList<D>();

			for( int i = 0; i < medianNum; i++ ) {
				int index = indexes[i];
				left.add(points.get(index));
				leftData.add(data.get(index));
			}
			for( int i = medianNum+1; i < points.size(); i++ ) {
				int index = indexes[i];
				right.add(points.get(index));
				rightData.add(data.get(index));
			}
		}

		// Compute the left and right children
		node.left = computeChild(left,leftData,node);
		// free memory
		left = null; leftData = null;
		node.right = computeChild(right,rightData,node);

		return node;
	}

	/**
	 * Creates a child by checking to see if it is a leaf or branch.
	 */
	private KdTree.Node computeChild( List<double[]> points , List<D> data , KdTree.Node parent )
	{
		if( points.size() == 0 )
			// avoid a null node by making the parent the leaf too
			return memory.requestNode(parent.point,parent.data);
		if( points.size() == 1 ) {
			return createLeaf(points,data);
		} else {
			return computeBranch(points,data);
		}
	}

	/**
	 * Convenient function for creating a leaf node
	 */
	private KdTree.Node createLeaf( List<double[]> points , List<D> data ) {
		D d = data == null ? null : data.get(0);
		return memory.requestNode(points.get(0),d);
	}

	/**
	 * Select the maximum variance as the split
	 */
	private int selectSplitAxis(List<double[]> points) {
		int numPoints = points.size();

		for( int i = 0; i < N; i++ ) {
			mean[i] = 0;
			var[i] = 0;
		}

		// compute the mean
		for( int i = 0; i < numPoints; i++ ) {
			double[] p = points.get(i);

			for( int j = 0; j < N; j++ ) {
				mean[j] += p[j];
			}
		}

		for( int i = 0; i < N; i++ ) {
			mean[i] /= numPoints;
		}

		// compute the variance * N
		for( int i = 0; i < numPoints; i++ ) {
			double[] p = points.get(i);

			for( int j = 0; j < N; j++ ) {
				double d = mean[j] - p[j];
				var[j] += d*d;
			}
		}

		// select the index with the maximum variance
		int split = -1;
		double bestVar = 0;
		for( int i = 0; i < N; i++ ) {
			if( var[i] > bestVar ) {
				split = i;
				bestVar = var[i];
			}
		}

		return split;
	}

	/**
	 * Uses quick-select to find the median value
	 */
	private void quickSelect(List<double[]> points, int splitAxis, int medianNum) {
		int numPoints = points.size();

		if( tmp.length < numPoints ) {
			tmp = new double[numPoints];
			indexes = new int[ numPoints ];
		}
		for( int i = 0; i < numPoints; i++ ) {
			tmp[i] = points.get(i)[splitAxis];
		}

		QuickSelectArray.selectIndex(tmp, medianNum, numPoints, indexes);
	}
}
