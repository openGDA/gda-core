/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import java.util.ArrayList;
import java.util.List;

/**
 * A scan which holds a collection of scans. It runs them all, making sure that they use the same datahandler.
 * <p>
 * The scans must scan over the same scannable objects to keep the format of data output the same.
 * <p>
 * The multiscan must be created before any of the other scans. The other scans should be instantiated and the added
 * using the addScan method. The order is important the make sure the scans do not create their own datahandler objects.
 */
public class MultiRegionScan extends ScanBase {

	private List<ScanBase> listOfScans;

	private int pointCount = -1;

	private double totalTime = 0;

	/**
	 * @param createScanDataPointPipeline
	 * @throws Exception
	 */
	public MultiRegionScan(boolean createScanDataPointPipeline) throws Exception {
		super();
		this.name = generateRandomName();
		try {
			listOfScans = new ArrayList<ScanBase>();
			if (createScanDataPointPipeline) createScanDataPointPipeline();
		} catch (Exception ex) {
			throw new Exception("Error during MultiRegionScan setup", ex);
		}
	}

	/**
	 * Constructor
	 * @throws Exception
	 */
	public MultiRegionScan() throws Exception {
		this(true);
	}

	/**
	 * Add scan to scans that will be run.
	 * @param toAdd
	 */
	public void addScan(ScanBase toAdd) {
        addScan(toAdd,0);
	}
	/**
	 * Add scan to scans that will be run.
	 * @param toAdd
	 * @param stepTime
	 */
	public void addScan(ScanBase toAdd, double stepTime) {
		toAdd.isChild = true;

		listOfScans.add(toAdd);

		// loop through the scannables belonging to that scan and
		// add to an overall list
		for (Scannable scannable : toAdd.allScannables) {
			if (!allScannables.contains(scannable)) {
				for(Scannable s : allScannables){
					String name2 = s.getName();
					if(name2.equals(scannable.getName())){
						throw new IllegalArgumentException("No two scannables can have the same name in a scan " + name2);
					}
				}
				allScannables.add(scannable);
			}
		}

		// and in the same way build a list of all detectors
		for (Detector detector : toAdd.allDetectors) {
			if (!allDetectors.contains(detector)) {
				for(Detector s : allDetectors){
					if(s.getName().equals(detector.getName())){
						throw new IllegalArgumentException("No two detectors can have the same name in a scan");
					}
				}
				allDetectors.add(detector);
			}
		}

		final int added = toAdd.getTotalNumberOfPoints();
		if (added>0)    TotalNumberOfPoints += added;
		if (stepTime>0) totalTime           += (added*stepTime);
	}

	/**
	 * If each scan has been added with addScan(ScanBase,int) then the time
	 * will be recorded and can be used for estimating how long the scan will take.
	 * @return time
	 */
	public double getTotalTime() {
		return totalTime;
	}
	/**
	 * Return the first scan to be operated on.
	 *
	 * @return Scan
	 */
	public Scan getFirstScan() {
		return listOfScans.get(0);
	}

	/**
	 * Calls the doCollection method of all the scans in the array of scans in turn.
	 *
	 * @throws Exception
	 */
	@Override
	public void doCollection() throws Exception {
		try {
			boolean first = true;
			for (ScanBase scan : listOfScans) {
				// For nested multiregion scans, notify the GeneralDataHandler
				// that
				// the inner scan is complete and the display graph should
				// reset.
				if (first && scan instanceof GridScan && !((GridScan) scan).hasChild()) {
					first = false;
				}

				scan.setDetectors(this.allDetectors);
				scan.setScannables(this.allScannables);

				// if declared a child, then the scan will not close the
				// datahandler
				// or return the baton whne it finishes.
				scan.setIsChild(true);
				scan.setParent(this);

				// share the dataHandler with this scan
				scan.setScanDataPointPipeline(scanDataPointPipeline);

				// run the scan
				scan.currentPointCount = pointCount;
				scan.TotalNumberOfPoints = TotalNumberOfPoints;
				scan.name = name;
				scan.doCollection();
				pointCount = scan.currentPointCount;
			}
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				throw e;
			}
			throw new Exception("Error running MultiRegionScan collection", e);
		}

	}

	/**
	 * Access to all scans in the list.
	 * @return List
	 */
	public List<ScanBase> getScans() {
		return this.listOfScans;
	}

}
