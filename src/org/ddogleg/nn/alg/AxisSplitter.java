package org.ddogleg.nn.alg;

import java.util.List;

/**
 * @author Peter Abeles
 */
public interface AxisSplitter<D> {

	public void setDimension( int N );

	/**
	 * Given the a set of points, select the axis to split the data along and select a point to divide the data.
	 * Points whput items below the threshold
	 * into left and above into right.  Data is optional and should be ignored if null. The selected
	 *
	 * @param points Set of points.
	 * @param data Optional set of data associated with the points.  Can be null.
	 * @param left Storage for
	 * @param dataLeft
	 * @param right
	 * @param dataRight
	 */
	public void splitData( List<double[]> points , List<D> data ,
						   List<double[]> left , List<D> dataLeft ,
						   List<double[]> right , List<D> dataRight );

	public double[] getSplitPoint();

	public D getSplitData();

	public int getSplitAxis();
}
