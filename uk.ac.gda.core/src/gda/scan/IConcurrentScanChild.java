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
	public IConcurrentScanChild getChild();

	/**
	 * @param child
	 */
	public void setChild(IConcurrentScanChild child);

	/**
	 * @return Returns the scannableLevels.
	 */
	public SortedMap<Integer, Scannable[]> getScannableLevels();

	/**
	 * @param scannableLevels
	 *            The scannableLevels to set.
	 */
	public void setScannableLevels(SortedMap<Integer, Scannable[]> scannableLevels);

	/**
	 * @return Returns the allScanObjects.
	 */
	public List<IScanObject> getAllScanObjects();

	/**
	 * @param allScanObjects
	 *            The allScanObjects to set.
	 */
	public void setAllScanObjects(List<IScanObject> allScanObjects);

	/**
	 * @return Returns the allChildScans.
	 */
	public List<IConcurrentScanChild> getAllChildScans();

	/**
	 * @param allChildScans
	 *            The allChildScans to set.
	 */
	public void setAllChildScans(List<IConcurrentScanChild> allChildScans);

	/**
	 * @return Returns the allScannables.
	 */
	public List<Scannable> getAllScannables();

	/**
	 * @param allScannables
	 *            The allScannables to set.
	 */
	public void setAllScannables(List<Scannable> allScannables);

	/**
	 * @return Returns the allDetectors.
	 */
	public List<Detector> getAllDetectors();

	/**
	 * @param allDetectors
	 *            The allDetectors to set.
	 */
	public void setAllDetectors(List<Detector> allDetectors);

	/**
	 * @return Returns the command.
	 */
	public String getCommand();

	/**
	 * The total number of points in the multi-dimesional scan i.e. the number of ScanDataPoints which would need
	 * displaying/recording
	 *
	 * @return int
	 */
	@Override
	public int getTotalNumberOfPoints();

	/**
	 * @return true if the type of scan must tbe the innermost dimension of a set of nested scans (a multi-dimensional
	 *         scan)
	 */
	public boolean isMustBeFinal();

	public void setCommand(String command);

	/**
	 * Enables parent scans, when creating the list of nested scans, to tell child scans the totalnumber of points in
	 * the overall multi-dimensional scan
	 *
	 * @param totalNumberOfPoints
	 */
	public void setTotalNumberOfPoints(int totalNumberOfPoints);

}