package org.eclipse.scanning.api.points;

import java.util.Iterator;

/**
 * An iterator over {@link IPosition}s that knows the
 * size, shape and rank of the points it iterates over.
 *
 * <em>Note:</em> This class is an implementation class and should not
 * be used outside the scanning framework. Iterators that wrap jython
 * point generators should implement this.
 * While most {@link AbstractGenerator} get Size/Shape/Rank information from their
 * PPointGenerator, JythonGenerator currently still requires this information from
 * the iterator.
 * For PPointGenerators and generators that use them, the information is taken directly from 
 * the [Python] CompoundGenerator (see jython_spg_interface.py)
 *
 */
public interface ScanPointIterator extends Iterator<IPosition> {

	/**
	 * Returns the number of points iterated over by this iterator.
	 * @return size
	 */
	public int getSize();

	/**
	 * Returns the shape of the points iterated over by this iterator.
	 * In some cases dimensions may be flattened out, for example when the
	 * inner most scan is a grid scan within a circular region.
	 * @return shape of scan
	 */
	public int[] getShape();

	/**
	 * Returns the rank of the points iterated over by this iterator.
	 * In some cases dimensions may be flattened out, for example when the
	 * inner most scan is a grid scan within a circular region.
	 * @return rank of scan
	 */
	public int getRank();

	/**
	 * Returns index of the next position to be returned by {@link #next()}
	 * @return
	 */
	public int getIndex();

}
