/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetectorBase;
import gda.device.scannable.PositionStreamIndexer;

import java.util.List;
import java.util.concurrent.Callable;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyHardwareTriggeredNexusXmap extends HardwareTriggerableDetectorBase implements HardwareTriggeredNexusXmap{
	public DummyHardwareTriggeredNexusXmap() {
		super();
		this.inputNames = new String[0];
	}

	private NexusXmap xmap;
	private List<String> fileNames ;
	private long currentTime;
	private long scanStartTime;
	private static final Logger logger = LoggerFactory.getLogger(DummyHardwareTriggeredNexusXmap.class);
	public List<String> getFileNames() {
		return fileNames;
	}

	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}

	private PositionStreamIndexer<NexusTreeProvider> indexer;
	private boolean bufferMode;
	public void setBufferMode(boolean bufferMode) {
		this.bufferMode = bufferMode;
	}

	@Override
	public void arm() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean integratesBetweenPoints() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void collectData() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getStatus() throws DeviceException {
		return xmap.getStatus();
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		try {
			return getPositionCallable().call();
		} catch (Exception e) {
			throw new DeviceException("Exception in " + getName() + " readout: ", e);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Dummy  Hardware Triggered Xmap detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Object source, Object arg) {
		// TODO Auto-generated method stub

	}

	public NexusXmap getXmap() {
		return xmap;
	}

	public void setXmap(NexusXmap xmap) {
		this.xmap = xmap;
	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		return indexer.getPositionCallable();
	}
	@Override
	public void atScanLineStart() throws DeviceException
	{		
		this.clearAndStart();
		this.indexer  = new PositionStreamIndexer<NexusTreeProvider>(new XmapPositionInputStream(this, xmap.isSumAllElementData()));
	}

	@Override
	public void clearAndStart() throws DeviceException {
		xmap.clearAndStart();
		
	}

	@Override
	public void clear() throws DeviceException {
		xmap.clear();
		
	}

	@Override
	public void start() throws DeviceException {
		xmap.start();
		
	}

	@Override
	public void setNumberOfBins(int numberOfBins) throws DeviceException {
		xmap.setNumberOfBins(numberOfBins);
		
	}

	@Override
	public int getNumberOfBins() throws DeviceException {
		return xmap.getNumberOfBins();
		
	}

	@Override
	public void setStatusRate(double statusRate) throws DeviceException {
		xmap.setStatusRate(statusRate);
		
	}

	@Override
	public double getStatusRate() throws DeviceException {
		return xmap.getStatusRate();
	}

	@Override
	public void setReadRate(double readRate) throws DeviceException {
		xmap.setReadRate(readRate);
		
	}

	@Override
	public double getReadRate() throws DeviceException {
		return xmap.getReadRate();
	}

	@Override
	public double getRealTime() throws DeviceException {
		return xmap.getRealTime();
	}

	@Override
	public void setAcquisitionTime(double time) throws DeviceException {
		xmap.setAcquisitionTime(time);
	}

	@Override
	public double getAcquisitionTime() throws DeviceException {
		return xmap.getAcquisitionTime();
	}

	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		return xmap.getData(mcaNumber);
	}

	@Override
	public int[][] getData() throws DeviceException {
		return xmap.getData();
	}

	@Override
	public double[] getROIsSum() throws DeviceException {
		return xmap.getROIsSum();
	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		xmap.setROIs(rois);
	}

	@Override
	public int getNumberOfROIs() throws DeviceException {
		return xmap.getNumberOfROIs();
	}

	@Override
	public int getNumberOfMca() throws DeviceException {
		return xmap.getNumberOfMca();
	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
		xmap.setNthROI(rois, roiIndex);
	}

	@Override
	public List<String> getChannelLabels() {
		return xmap.getChannelLabels();
	}

	@Override
	public double[] getROICounts(int iRoi) throws DeviceException {
		return xmap.getROICounts(iRoi);
	}

	@Override
	public double readoutScalerData() throws DeviceException {
		return xmap.readoutScalerData();
	}

	@Override
	public String getHDFFileName() throws DeviceException {
		if(fileNames != null && fileNames.size() != 0)
			return fileNames.get(0);
		else
			return null;
			
	}

	@Override
	public void waitForFile(String fileName) throws DeviceException, InterruptedException {
		double timeoutMilliSeconds = getCollectionTime() * getHardwareTriggerProvider().getNumberTriggers() * 1000;
		double waitedSoFarMilliSeconds = 0;
		int waitTime = 1000;
		while (isStillWriting(fileName) || waitedSoFarMilliSeconds <= timeoutMilliSeconds) {
			Thread.sleep(waitTime);
			waitedSoFarMilliSeconds += waitTime;
		}
		
	}
		public boolean isStillWriting(String fileName)  {
			currentTime = System.currentTimeMillis();
			try {
				if((currentTime - scanStartTime) <= (long)getHardwareTriggerProvider().getTotalTime())
					return true;
			} catch (DeviceException e) {
				logger.error("TODO put description of error here", e);
			}
			return false;
		}

	@Override
	public boolean isInBufferMode() throws Exception {
		// TODO Auto-generated method stub
		return bufferMode;
	}
	
	@Override
	public String[] getExtraNames() {
		return xmap.getExtraNames();
	}
	
	@Override
	public int[] getDataDimensions() throws DeviceException {
		return xmap.getDataDimensions();	
	}

	@Override
	public void waitForCurrentScanFile() throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub
		
	}
	
}
