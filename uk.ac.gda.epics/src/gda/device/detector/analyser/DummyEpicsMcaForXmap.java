/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.analyser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

public class DummyEpicsMcaForXmap extends AnalyserBase {

	private static final Logger logger = LoggerFactory.getLogger(DummyEpicsMcaForXmap.class);


	private Roi[] regions = new Roi[32];

	private long numberOfChannels = 1024;
	
	private double[] lastCollectedSpectrum = new double[0];

	private EpicsMCAPresets presets;

	public DummyEpicsMcaForXmap() {
	}

	@Override
	public void startAcquisition() throws DeviceException {
		//log(".startAcquisition()");
		lastCollectedSpectrum = createSpectrum();
	}

	@Override
	public void stopAcquisition() throws DeviceException {
		log(".stopAcquisition()");
		
	}

	@Override
	public void addRegionOfInterest(int regionIndex, double regionLow, double regionHigh, int regionBackground,
			double regionPreset, String regionName) throws DeviceException {
		regions[regionIndex] = new Roi(regionIndex, regionLow, regionHigh, regionBackground, regionPreset, regionName);
		log(".addRegionOfInterest() <-- " + regions[regionIndex]);

	}

	@Override
	public void deleteRegionOfInterest(int regionIndex) throws DeviceException {
		//log(".deleteRegionOfInterest() <-- " + regionIndex);
		regions[regionIndex] = null;
	}

	@Override
	public void clear() throws DeviceException {
		//log(".clear()");

	}

	@Override
	public void setPresets(Object data) throws DeviceException {
		presets = (EpicsMCAPresets) data;
		log(".setPresets() <-- " + presets);
	}
	
	@Override
	public Object getPresets() throws DeviceException {
		return presets;
	}
	
	@Override
	public void collectData() throws DeviceException {
		//log(".collectData()");
		lastCollectedSpectrum = createSpectrum();
		pointsSinceScanStart ++;
	}


	@Override
	public Object getData() throws DeviceException {
		return lastCollectedSpectrum;
	}

	@Override
	public int getNumberOfRegions() throws DeviceException {
		return regions.length;
	}

	@Override
	public long getNumberOfChannels() throws DeviceException {
		return numberOfChannels;
	}

	@Override
	public double[][] getRegionsOfInterestCount() throws DeviceException {
		List<Double> grossCounts = new ArrayList<Double>();
		for (int i = 0; i < regions.length; i++) {
			Roi roi = regions[i];
			if (roi != null) {
				double sum = 0;
				for (int element = (int) roi.getRegionLow(); element < roi.getRegionHigh(); element++) {
					sum = sum + lastCollectedSpectrum[element];
				}
				grossCounts.add(sum);
			}
		}
		double[][] s = new double [grossCounts.size()][2];
		for (int i = 0; i < grossCounts.size(); i++) {
			s[i][0] = grossCounts.get(i);
			s[i][1] = -99;
		}
		return s;
	}

	@Override
	public void setNumberOfRegions(int regions) throws DeviceException {
		this.regions = new Roi[regions];
	}

	@Override
	public void setNumberOfChannels(long channels) throws DeviceException {
		log(".setNumberOfChannels() <-- " + channels);
		numberOfChannels = channels;
	}

	@Override
	public int getStatus() throws DeviceException {
		return Detector.IDLE;
	}

	@Override
	public Object readout() throws DeviceException {
		return getData();
	}

	@Override
	public String getDescription() throws DeviceException {
		return "";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "";
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public long getSequence() throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public Object getCalibrationParameters() throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public void setCalibration(Object calibrate) throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public Object getElapsedParameters() throws DeviceException {
		return new float[] {(float) getCollectionTime(), (float) getCollectionTime()};
	}

	@Override
	public void setData(Object data) throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public Object getRegionsOfInterest() throws DeviceException {
		throw new RuntimeException("Not supported");
	}
	
	@Override
	public void setRegionsOfInterest(Object lowHigh) throws DeviceException {
		throw new RuntimeException("Not supported");
	}

	@Override
	public void setSequence(long sequence) throws DeviceException {
		throw new RuntimeException("Not supported");
	}
	
	class Roi {
		
		private final int regionIndex;
		private final double regionLow;
		private final double regionHigh;
		private final int regionBackground;
		private final double regionPreset;
		private final String regionName;
	
		public Roi(int regionIndex, double regionLow, double regionHigh, int regionBackground,
				double regionPreset, String regionName) {
					this.regionIndex = regionIndex;
					this.regionLow = regionLow;
					this.regionHigh = regionHigh;
					this.regionBackground = regionBackground;
					this.regionPreset = regionPreset;
					this.regionName = regionName;
		}
		
		
		@Override
		public String toString() {
			String s = " regionIndex:" + regionIndex;
			s += " regionLow:" + regionLow ;
			s += " regionHigh:" + regionHigh;
			s += " regionBackground:" + regionBackground;
			s += " regionPreset:" + regionPreset ;
			s += " regionName:" + regionName ;
			return s;
		}
		
		public int getRegionIndex() {
			return regionIndex;
		}
	
		public double getRegionLow() {
			return regionLow;
		}
	
		public double getRegionHigh() {
			return regionHigh;
		}
	
		public int getRegionBackground() {
			return regionBackground;
		}
	
		public double getRegionPreset() {
			return regionPreset;
		}
	
		public String getRegionName() {
			return regionName;
		}
	}

	public int scanWidth = 10;
	
	public int scanHeight = 10;
	
	private int pointsSinceScanStart = 0;
	
	@Override
	public void atScanStart() throws DeviceException {
		pointsSinceScanStart = 0;
	}
	
	double getX() {
		return (pointsSinceScanStart % scanWidth) / ((double) scanWidth);
	}
	
	double getY() {
		return ((pointsSinceScanStart / scanHeight) % scanWidth) / ((double) scanHeight);
	}
	
	public double[] createSpectrum() throws DeviceException {
		Random random = new Random();
		double[] counts = new double[(int) getNumberOfChannels()];
		
		double p1mu = .2;
		double p1sigma = .02;
		double p2mu = .3;
		double p2sigma = .01;
		double p1amp = getX();
		double p2amp = getY(); 
		
		for (int channel = 0; channel < counts.length; channel++) {
			double x = (float) channel / getNumberOfChannels();
			double noise = random.nextDouble();
			double p1 = gaussian(x, p1mu, p1sigma) * (p1amp + random.nextDouble()/10.);
			double p2 = gaussian(x, p2mu, p2sigma) * (p2amp + random.nextDouble()/10.);
			counts[channel] = (noise + p1 + p2) * 1000;
		}
		return counts;
		
	}
	
	private double gaussian(double x, double mu, double sigma) {
		double a = 1/ (sigma * Math.sqrt(2*Math.PI));
		double b = mu;
		double c = sigma;
		return a * Math.exp(-1 * (x-b) * (x-b) / (2*c*c));
	}
	
	private void log(String msg) {
		ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();
		msg = getName() + msg;
		logger.info(msg);
		try {
			terminalPrinter.print(msg);
		} catch (java.lang.IllegalStateException e) {
			// ignore, as print fails during configure
		}
		
	}

}
