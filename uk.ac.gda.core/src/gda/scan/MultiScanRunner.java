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

import static gda.jython.InterfaceProvider.getTerminalPrinter;
import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;

import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiScanRunner implements NestableScan, ContiguousScan{
	private static final Logger logger = LoggerFactory.getLogger(MultiScanRunner.class);
	List<MultiScanItem> scans;
	private int TotalNumberOfPoints=0;
	private ScanBase first;
	private ScanBase lastscan;
	protected ParentScanComponent parentComponent = new ParentScanComponent(ScanStatus.NOTSTARTED);
	
	public MultiScanRunner(List<MultiScanItem> scans) {
		super();
		this.scans = scans;
	}
	
	@Override
	public void runScan() throws InterruptedException, Exception {

		try{
			setStatus(ScanStatus.RUNNING);
			if (LocalProperties.check(ScanBase.GDA_SCANBASE_PRINT_TIMESTAMP_TO_TERMINAL)) {
				java.util.Date date= new java.util.Date();
				getTerminalPrinter().print("=== Scan started at "+new Timestamp(date.getTime()).toString()+" ===");
			}

			for (MultiScanItem item : scans) {
				TotalNumberOfPoints += item.scan.getTotalNumberOfPoints();
			}			
			
			MultiScanItem multiScanItem = scans.get(0);
			first =  multiScanItem.scan;
			first.prepareScanForCollection();
			int pointCount = -1;
			
			for (MultiScanItem item : scans) {
				ScanBase scan = item.scan;
				lastscan=scan;
				for( Detector det : scan.getDetectors()){
					if( det instanceof HardwareTriggeredDetector){
						((HardwareTriggeredDetector)det).setNumberImagesToCollect(scan.getTotalNumberOfPoints());
					}
				}

				scan.setIsChild(true);
				scan.setParent(this);

				scan.setScanDataPointPipeline(first.getScanDataPointPipeline());
				scan.setScanNumber(first.getScanNumber());

				// run the scan
				scan.currentPointCount = pointCount;
				scan.name = first.name;
				Runnable prescan = item.prescan;
				if( prescan != null){
					prescan.run();
				}
				scan.callScannablesAtScanStart();
				scan.run();
				pointCount = scan.currentPointCount;
				Runnable postscan = item.postscan;
				if( postscan != null){
					postscan.run();
				}
			}
			for (MultiScanItem item : scans) {
				ScanBase scan = item.scan;
				scan.callScannablesAtScanEnd();
				scan.callDetectorsEndCollection();
			}
			if( lastscan != null){
				lastscan.shutdownScandataPipeline(true);
			}

		} catch (Exception e){
			logger.error("Error running multiScan",e);
			if( lastscan != null){
				lastscan.shutdownScandataPipeline(false);
			}
			throw e;
		}	finally {

			switch (getStatus()) {
			case RUNNING:
				setStatus(ScanStatus.COMPLETED_OKAY);
				break;
			case TIDYING_UP_AFTER_FAILURE:
				setStatus(ScanStatus.COMPLETED_AFTER_FAILURE);
				break;
			case TIDYING_UP_AFTER_STOP:
				setStatus(ScanStatus.COMPLETED_AFTER_STOP);
				break;
			case FINISHING_EARLY:
				setStatus(ScanStatus.COMPLETED_EARLY);
				break;
			default:
				throw new AssertionError("Unexpected status at the end of scan:" + getStatus().toString());
			}
			
			
			if( lastscan != null){
				lastscan.signalScanComplete();
			}
		}
	}
	
	@Override
	public
	void run(){
		try {
			logger.error("Do not use run method of MultiScanRunner - use execute instead as it can throw exceptions");
			runScan();
		} catch (Exception e) {
			logger.error("Error running scans", e);
		}
	
	}
	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void doCollection() throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void prepareForCollection() throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Vector<Scannable> getScannables() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setScannables(Vector<Scannable> allScannables) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Vector<Detector> getDetectors() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setDetectors(Vector<Detector> allDetectors) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isChild() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setIsChild(boolean child) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public DataWriter getDataWriter() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setDataWriter(DataWriter dh) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setScanDataPointPipeline(ScanDataPointPipeline scanDataPointPipeline) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ScanDataPointPipeline getScanDataPointPipeline() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public NestableScan getParent() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setParent(NestableScan parent) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Scan getChild() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setChild(Scan child) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public IScanStepId getStepId() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setStepId(IScanStepId IScanStepId) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setScanPlotSettings(ScanPlotSettings scanPlotSettings) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ScanPlotSettings getScanPlotSettings() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getDimension() {
		return TotalNumberOfPoints;
	}
	@Override
	public int getTotalNumberOfPoints() {
		return TotalNumberOfPoints;
	}
	@Override
	public int getScanNumber() {
		return first.getScanNumber();
	}
	@Override
	public int getNumberOfContiguousPoints() {
		// TODO Auto-generated method stub
		return TotalNumberOfPoints;
	}
	@Override
	public void requestFinishEarly() {
		parentComponent.requestFinishEarly();
		
	}
	@Override
	public boolean isFinishEarlyRequested() {
		return parentComponent.isFinishEarlyRequested();
	}
	@Override
	public ScanStatus getStatus() {
		return parentComponent.getStatus();
	}
	@Override
	public void setStatus(ScanStatus status) {
		parentComponent.setStatus(status);
		
	}
}
