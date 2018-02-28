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

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

public class DummyEpicsMcaForXmap extends AnalyserBase implements IEpicsMCASimple {

	private static final Logger logger = LoggerFactory.getLogger(DummyEpicsMcaForXmap.class);

	private static final String NOT_SUPPORTED = "Not supported";

	private static final int SCAN_WIDTH = 10;
	private static final int SCAN_HEIGHT = 10;
	private static final int NUM_ROIS = 32;

	private static final int SPECTRUM_GENERATOR_SEED = 42;
	private final Random spectrumGenerator = new Random(SPECTRUM_GENERATOR_SEED);

	private int pointsSinceScanStart = 0;

	private EpicsMCARegionOfInterest[] regions;

	private long numberOfChannels = 1024;

	private double[] lastCollectedSpectrum;

	private EpicsMCAPresets presets;

	public DummyEpicsMcaForXmap() {
		super();

		regions = new EpicsMCARegionOfInterest[NUM_ROIS];
		for (int i = 0; i < NUM_ROIS; i++) {
			regions[i] = new EpicsMCARegionOfInterest(i, 0.0, 0.0, 0, 0.0, String.format("region-%02d", i));
		}

		lastCollectedSpectrum = createSpectrum();
		configured = true;
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
		regions[regionIndex] = new EpicsMCARegionOfInterest(regionIndex, regionLow, regionHigh, regionBackground, regionPreset, regionName);
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
		final int defaultTotalCount = -99;
		final List<Double> grossCounts = new ArrayList<>();
		for (EpicsMCARegionOfInterest roi : regions) {
			if (roi != null) {
				double sum = 0;
				for (int element = (int) roi.getRegionLow(); element < roi.getRegionHigh(); element++) {
					sum = sum + lastCollectedSpectrum[element];
				}
				grossCounts.add(sum);
			}
		}
		final double[][] s = new double [grossCounts.size()][2];
		for (int i = 0; i < grossCounts.size(); i++) {
			s[i][0] = grossCounts.get(i);
			s[i][1] = defaultTotalCount;
		}
		return s;
	}

	@Override
	public void setNumberOfRegions(int regions) throws DeviceException {
		this.regions = new EpicsMCARegionOfInterest[regions];
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
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public Object getCalibrationParameters() throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public void setCalibration(Object calibrate) throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public Object getElapsedParameters() throws DeviceException {
		return new float[] {(float) getCollectionTime(), (float) getCollectionTime()};
	}

	@Override
	public void setData(Object data) throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public Object getRegionsOfInterest() throws DeviceException {
		return cloneRois(regions);
	}

	@Override
	public void setRegionsOfInterest(Object lowHigh) throws DeviceException {
		if (lowHigh instanceof EpicsMCARegionOfInterest[]) {
			setRegionsOfInterest((EpicsMCARegionOfInterest[]) lowHigh);
		} else {
			throw new NotImplementedException("setRegionsOfInterest() only implemented for array of EpicsMCARegionOfInterest");
		}
	}

	@Override
	public void setSequence(long sequence) throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public void atScanStart() throws DeviceException {
		pointsSinceScanStart = 0;
	}

	private double getX() {
		return (pointsSinceScanStart % SCAN_WIDTH) / ((double) SCAN_WIDTH);
	}

	private double getY() {
		return (((double) pointsSinceScanStart / SCAN_HEIGHT) % SCAN_WIDTH) / (SCAN_HEIGHT);
	}

	private double[] createSpectrum() {
		final double[] counts = new double[(int) numberOfChannels];
		final double p1mu = .2;
		final double p1sigma = .02;
		final double p2mu = .3;
		final double p2sigma = .01;
		final double p1amp = getX();
		final double p2amp = getY();

		for (int channel = 0; channel < counts.length; channel++) {
			final double x = (float) channel / numberOfChannels;
			final double noise = spectrumGenerator.nextDouble();
			final double p1 = gaussian(x, p1mu, p1sigma) * (p1amp + spectrumGenerator.nextDouble()/10.);
			final double p2 = gaussian(x, p2mu, p2sigma) * (p2amp + spectrumGenerator.nextDouble()/10.);
			counts[channel] = (noise + p1 + p2) * 1000;
		}
		return counts;
	}

	private double gaussian(double x, double mu, double sigma) {
		final double a = 1/ (sigma * Math.sqrt(2*Math.PI));
		final double b = mu;
		final double c = sigma;
		return a * Math.exp(-1 * (x-b) * (x-b) / (2*c*c));
	}

	private void log(String msg) {
		final ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();
		final  String message = getName() + msg;
		logger.info(message);
		try {
			terminalPrinter.print(message);
		} catch (java.lang.IllegalStateException e) {
			// ignore, as print fails during configure
		}
	}

	@Override
	public void clearWaitForCompletion() throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public double getDwellTime() throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public void setDwellTime(double time) throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public void eraseStartAcquisition() throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public EpicsMCARegionOfInterest getNthRegionOfInterest(int regionIndex) throws DeviceException {
		return regions[regionIndex];
	}

	@Override
	public void setRegionsOfInterest(EpicsMCARegionOfInterest[] epicsMcaRois) throws DeviceException {
		regions = cloneRois(epicsMcaRois);
	}

	@Override
	public void setCalibration(EpicsMCACalibration calibrate) throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public double getRoiCount(int index) throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public double getRoiNetCount(int index) throws DeviceException {
		throw new NotImplementedException(NOT_SUPPORTED);
	}

	@Override
	public void setMcaPV(String mcaPV) {
		// nothing to do
	}

	private static EpicsMCARegionOfInterest[] cloneRois(EpicsMCARegionOfInterest[] rois) {
		final EpicsMCARegionOfInterest[] result = new EpicsMCARegionOfInterest[rois.length];
		for (int i = 0; i < rois.length; i++) {
			if (rois[i] != null) {
				result[i] = new EpicsMCARegionOfInterest(rois[i]);
			}
		}
		return result;
	}
}
