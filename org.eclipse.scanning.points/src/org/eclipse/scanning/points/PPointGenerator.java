/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.points;

import java.util.List;

import org.eclipse.scanning.api.points.IPosition;

/**
 * Interface for [Jython] CompoundGenerator, itself wrapped by [Java] IPointGenerator.
 * Additionally passes information about shape/rank/size/dimensions of the scan from the [Jython] generator.
 * <em>Note:</em> This class is an implementation class and should not be used outside the scanning framework
 *
 */
public interface PPointGenerator extends Iterable<IPosition>, PySerializable {

	/**
	 * @return the number of points iterated over by this iterator.
	 */
	public int getSize();

	/**
	 * @return the shape of the points iterated over by this iterator as an int array
	 * In some cases multiple dimensions will be squashed into a single dimension,
	 * for example when the inner most scan is a grid scan within a circular region.
	 */
	public int[] getShape();

	/**
	 * @return the rank of the points iterated over by this iterator.
	 * In some cases dimensions may be flattened out, for example when the
	 * inner most scan is a grid scan within a circular region.
	 */
	public int getRank();

	/**
	 * @return list of names of the scannable axes that this generator includes
	 */
	public List<String> getNames();

	/**
	 * @return The final Bound (half step beyond the final Point) of the underlying
	 * Python Generator. Used for ConsecutiveMultiModels
	 */
	public IPosition getFinalBounds();

	/**
	 * @return The initial Bound (half step before the first Point) of the underlying
	 * Python Generator. Used for ConsecutiveMultiModels
	 */
	public IPosition getInitialBounds();

}
