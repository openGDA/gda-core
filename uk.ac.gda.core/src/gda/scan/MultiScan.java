/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiScan extends ScanBase implements ContiguousScan{
	private static final Logger logger = LoggerFactory.getLogger(MultiScan.class);
	private int pointCount = -1;
	List<ScanBase> scans;
	private ScanBase currentRunningScan;

	/*
	 * public void runScan() throws InterruptedException, Exception { if( scans != null){ DataWriter writer =
	 * DefaultDataWriterFactory.createDataWriterFromFactory(); MyDataWriter mydw = new MyDataWriter(writer); for( Scan
	 * scan : scans){ scan.setDataWriter(mydw); scan.setIsChild(true); scan.setParent(this); scan.runScan(); }
	 * mydw.completeCollection(); } } Scan currentScan;
	 */public MultiScan(List<ScanBase> scans) {
		super();
		this.scans = scans;
		TotalNumberOfPoints = 0;

		allScannables.clear();
		for (ScanBase scan : scans) {
			TotalNumberOfPoints += scan.getTotalNumberOfPoints();
			for (Scannable sc : scan.getScannables()) {
				if (!allScannables.contains(sc)) {
					allScannables.add(sc);
				}
			}
		}

		allDetectors.clear();
		for (ScanBase scan : scans) {
			for (Detector det : scan.getDetectors()) {
				if (!allDetectors.contains(det)) {
					allDetectors.add(det);
				}
			}
		}
		
		setChild(scans.get(0)); //prevent calling of callScannablesAtScanLineStart in ScanBase.prepareDevicesForCollection
	}

	@Override
	public int getDimension() {
		return TotalNumberOfPoints;
	}

	@Override
	public int getNumberOfContiguousPoints() {
		return TotalNumberOfPoints;
	}

	@Override
	public void prepareForCollection() throws Exception {
		logger.info("prepareForCollection");
		super.prepareForCollection();
	}

	@Override
	protected void endScan() throws DeviceException, InterruptedException {
		logger.info("endScan");
		super.endScan();
	}

	@Override
	public void doCollection() throws Exception {

		for (ScanBase scan : scans) {
			for( Detector det : scan.allDetectors){
				if( det instanceof HardwareTriggeredDetector){
					((HardwareTriggeredDetector)det).setNumberImagesToCollect(scan.getTotalNumberOfPoints());
				}
			}

			scan.setIsChild(true);
			scan.setParent(this);

			scan.setScanDataPointPipeline(scanDataPointPipeline);

			// run the scan
			scan.currentPointCount = pointCount;
			scan.name = name;
			scan.setScanNumber(getScanNumber());
			
			setCurrentRunningScan(scan);
			scan.callScannablesAtScanLineStart();
			scan.doCollection();
			scan.callScannablesAtScanLineEnd();

			
			pointCount = scan.currentPointCount;

		}
	}

	public ScanBase getCurrentRunningScan() {
		return currentRunningScan;
	}

	public void setCurrentRunningScan(ScanBase currentRunningScan) {
		this.currentRunningScan = currentRunningScan;
	}


}

