/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.scan;

import java.util.List;
import java.util.SortedMap;

import gda.device.Detector;
import gda.device.Scannable;
import uk.ac.gda.api.scan.IScanObject;

/**
 * Interface for access to scan objects which can be part of a multidimensional ConcurrentScan
 */
public interface IConcurrentScanChild extends NestableScan {

	@Override
	IConcurrentScanChild getChild();

	/**
	 * @param child
	 */
	void setChild(IConcurrentScanChild child);

	/**
	 * @return Returns the scannableLevels.
	 */
	SortedMap<Integer, Scannable[]> getScannableLevels();

	/**
	 * @param scannableLevels
	 *            The scannableLevels to set.
	 */
	void setScannableLevels(SortedMap<Integer, Scannable[]> scannableLevels);

	/**
	 * @return Returns the allScanObjects.
	 */
	List<IScanObject> getAllScanObjects();

	/**
	 * @param allScanObjects
	 *            The allScanObjects to set.
	 */
	void setAllScanObjects(List<IScanObject> allScanObjects);

	/**
	 * @return Returns the allChildScans.
	 */
	List<IConcurrentScanChild> getAllChildScans();

	/**
	 * @param allChildScans
	 *            The allChildScans to set.
	 */
	void setAllChildScans(List<IConcurrentScanChild> allChildScans);

	/**
	 * @return Returns the allScannables.
	 */
	List<Scannable> getAllScannables();

	/**
	 * @param allScannables
	 *            The allScannables to set.
	 */
	void setAllScannables(List<Scannable> allScannables);

	/**
	 * @return Returns the allDetectors.
	 */
	List<Detector> getAllDetectors();

	/**
	 * @param allDetectors
	 *            The allDetectors to set.
	 */
	void setAllDetectors(List<Detector> allDetectors);

	/**
	 * @return Returns the command.
	 */
	String getCommand();

	/**
	 * @return true if the type of scan must tbe the innermost dimension of a set of nested scans (a multi-dimensional
	 *         scan)
	 */
	boolean isMustBeFinal();

	void setCommand(String command);

	/**
	 * Enables parent scans, when creating the list of nested scans, to tell child scans the totalnumber of points in
	 * the overall multi-dimensional scan
	 *
	 * @param totalNumberOfPoints
	 */
	void setTotalNumberOfPoints(int totalNumberOfPoints);
}