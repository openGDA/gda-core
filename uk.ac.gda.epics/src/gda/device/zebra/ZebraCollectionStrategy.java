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

package gda.device.zebra;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * class that configures the zebra to generate a series of pulses when a motor moves. The data returned in read is a set
 * of items , one per pulse generated since last read
 */
public class ZebraCollectionStrategy implements NXCollectionStrategyPlugin {

	private int pulsesRead;
	private long linestarttime;
	
	int numPulsesPerLine=0;
	

	public int getNumPulsesPerLine() {
		return numPulsesPerLine;
	}

	public void setNumPulsesPerLine(int numPulsesPerLine) {
		this.numPulsesPerLine = numPulsesPerLine;
	}

	@Override
	public String getName() {
		return "zebra";
	}

	@Override
	public boolean willRequireCallbacks() {
		return false; // not EPICS areaDetector plugin
	}

	@Override
	public void prepareForLine() throws Exception {
		pulsesRead=0;
		linestarttime=System.currentTimeMillis();
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {

	}

	@Override
	public List<String> getInputStreamNames() {
		List<String> fieldNames = new ArrayList<String>();
		fieldNames.add("pulse");
		return fieldNames;
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> formats = new ArrayList<String>();
		formats.add("%d");
		return formats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		int pulsesGenerated;
		while( (pulsesGenerated = readPulsesGenerated()) <= pulsesRead){
			Thread.sleep(100);
		}
		Vector<NXDetectorDataAppender> vector = new Vector<NXDetectorDataAppender>();
		int pulsesToReturn = Math.min(numPulsesPerLine,Math.min(maxToRead, pulsesGenerated-pulsesRead));
		for( int i=0; i<pulsesToReturn; i++){
			List<Double> times = new ArrayList<Double>();
			times.add((double) (pulsesRead+i));
			vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), times));
		}
		pulsesRead += pulsesToReturn;
		return vector;

	}

	private int readPulsesGenerated() {
		//10 pulses per second
		return (int) ((System.currentTimeMillis() - linestarttime)/1000)*10;
	}

	@Override
	public double getAcquireTime() throws Exception {
		return 0;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return 0;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
	}

	@Override
	public void collectData() throws Exception {
	}

	@Override
	public int getStatus() throws Exception {
		return 0;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
	}

	@Override
	public void setGenerateCallbacks(boolean b) {
		// do nothing - not an EPICs areaDetector plugin
	}

	@Override
	public boolean isGenerateCallbacks() {
		return false;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return 0;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
	}

	@Override
	public void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo)
			throws Exception {
	}
	@Override
	public boolean callReadBeforeNextExposure() {
		return false;
	}

	@Override
	public boolean requiresCacheBackedPlugins() {
		return true;
	}

}
