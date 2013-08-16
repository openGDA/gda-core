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

import static gda.jython.InterfaceProvider.getScanStatusHolder;
import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.jython.Jython;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class MultiScanRunner implements Scan, ContiguousScan{
	private static final Logger logger = LoggerFactory.getLogger(MultiScanRunner.class);
	List<MultiScanItem> scans;
	private int TotalNumberOfPoints=0;
	private ScanBase first;
	private ScanBase lastscan;
	
	
	public MultiScanRunner(List<MultiScanItem> scans) {
		super();
		this.scans = scans;
	}
	@Override
	public
	void run(){
		
		try{
			
			for (MultiScanItem item : scans) {
				TotalNumberOfPoints += item.scan.getTotalNumberOfPoints();
			}			
			
			MultiScanItem multiScanItem = scans.get(0);
			first =  multiScanItem.scan;
			first.prepareScanForCollection();
			int pointCount = -1;
			
			for (MultiScanItem item : scans) {
				ScanBase scan = item.scan;
				for( Detector det : scan.allDetectors){
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
				lastscan=scan;
			}
			for (MultiScanItem item : scans) {
				ScanBase scan = item.scan;
				scan.callScannablesAtScanEnd();
				scan.callDetectorsEndCollection();
			}
			
			lastscan.shutdownScandataPipieline();
			lastscan.signalScanComplete();

			getScanStatusHolder().setScanStatus(Jython.IDLE);


		}	catch( Exception e){
			logger.error("Error executing scans",e);
		}
		//need to handle scan.interrupted as in ScanBase runScan
	
	}
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
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
	public void runScan() throws InterruptedException, Exception {
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
	public Scan getParent() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setParent(Scan parent) {
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
	public Long getScanNumber() {
		return first.getScanNumber();
	}
	@Override
	public int getNumberOfContiguousPoints() {
		// TODO Auto-generated method stub
		return TotalNumberOfPoints;
	}
}