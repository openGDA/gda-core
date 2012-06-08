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

import gda.device.Detector;
import gda.device.Scannable;

import java.util.TreeMap;
import java.util.Vector;

/**
 * Interface for access to scan objects which can be part of a multidimensional ConcurrentScan
 */
public interface IConcurrentScanChild extends Scan {

	@Override
	public IConcurrentScanChild getParent();

	/**
	 * @param parent
	 */
	public void setParent(IConcurrentScanChild parent);

	@Override
	public IConcurrentScanChild getChild();

	/**
	 * @param child
	 */
	public void setChild(IConcurrentScanChild child);

	/**
	 * @return Returns the scannableLevels.
	 */
	public TreeMap<Integer, Scannable[]> getScannableLevels();

	/**
	 * @param scannableLevels
	 *            The scannableLevels to set.
	 */
	public void setScannableLevels(TreeMap<Integer, Scannable[]> scannableLevels);

	/**
	 * @return Returns the allScanObjects.
	 */
	public Vector<ScanObject> getAllScanObjects();

	/**
	 * @param allScanObjects
	 *            The allScanObjects to set.
	 */
	public void setAllScanObjects(Vector<ScanObject> allScanObjects);

	/**
	 * @return Returns the allChildScans.
	 */
	public Vector<IConcurrentScanChild> getAllChildScans();

	/**
	 * @param allChildScans
	 *            The allChildScans to set.
	 */
	public void setAllChildScans(Vector<IConcurrentScanChild> allChildScans);

	/**
	 * @return Returns the allScannables.
	 */
	public Vector<Scannable> getAllScannables();

	/**
	 * @param allScannables
	 *            The allScannables to set.
	 */
	public void setAllScannables(Vector<Scannable> allScannables);

	/**
	 * @return Returns the allDetectors.
	 */
	public Vector<Detector> getAllDetectors();

	/**
	 * @param allDetectors
	 *            The allDetectors to set.
	 */
	public void setAllDetectors(Vector<Detector> allDetectors);

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