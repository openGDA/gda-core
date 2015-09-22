/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.countertimer.BufferedScaler;

/**
 * Supplies a normalised FF (total counts in ROIs over all channels) during ContinuousScans for Xspress3.
 * 
 * @author rjw82
 *
 */
public class Xspress3FFoverI0BufferedDetector extends DetectorBase implements BufferedDetector {

	private static final Logger logger = LoggerFactory.getLogger(Xspress3FFoverI0BufferedDetector.class);

	private Xspress3BufferedDetector qxspress = null;
	private BufferedScaler qscaler = null;
	protected ContinuousParameters continuousParameters = null;
	protected boolean isContinuousMode = true;
	private int i0_channel = 0;

	@Override
	public void configure() {
		setExtraNames(new String[] { "FFI0" });
		setInputNames(new String[0]);
		setOutputFormat(new String[] { "%.9f" });
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame)
			throws DeviceException {

		double[][] scalerFrames = (double[][]) qscaler.readFrames(startFrame, finalFrame);
		NexusTreeProvider[] expressFrames = (NexusTreeProvider[]) qxspress.readFrames(startFrame, finalFrame);

		Double[] ffio = new Double[finalFrame - startFrame + 1];

		for (int thisFrame = 0; thisFrame < finalFrame - startFrame + 1; thisFrame++) {
			double ff = getFF(expressFrames, thisFrame);
			double i0 = scalerFrames[thisFrame][i0_channel];
			
			double this_ffio = ff / i0;
			if (i0 == 0.0 || ff == 0.0){
				this_ffio = 0.0;
			}
		
			ffio[thisFrame] = this_ffio;
		}
		return ffio;
	}

	private double getFF(NexusTreeProvider[] expressFrames, int i) {
		NXDetectorData expressFrameData = (NXDetectorData) expressFrames[i];
		Double[] FFs = expressFrameData.getDoubleVals();
		String[] extraNames = expressFrameData.getExtraNames();

		// If we can find the FF value, return it
		for (int index = 0; index < extraNames.length && index < FFs.length; index++) {
			if ("FF".equals(extraNames[index])) {
				return FFs[index].doubleValue();
			}
		}

		logger.warn("FF not found; using the sum of all Xspress3 plottable values");
		double ffTotal = 0;
		for (Double ff : FFs){
			ffTotal += ff;
		}
		return ffTotal;
	}

	@Override
	public Double readout() throws DeviceException {
		return 1.0;
	}
	
	@Override
	public void clearMemory() throws DeviceException {
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		Integer lastFrame = getNumberFrames() - 1;
		return readFrames(0, lastFrame);
	}
	
	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return null;
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		if (!isContinuousMode)
			return 0;
//		return continuousParameters.getNumberDataPoints();
		int xspress3Frames = qxspress.getNumberFrames();
		int scalerFrames = qscaler.getNumberFrames();
		return Math.min(scalerFrames, xspress3Frames);
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return false;
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		return qxspress.maximumReadFrames(); 
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		isContinuousMode = on;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		continuousParameters = parameters;
	}

	@Override
	public void collectData() throws DeviceException {
		// do nothing - read from other detectors
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "QEXAFS Xspress3 FF over I0";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "Version 1";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Xspress3FFoverIO";
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	public Xspress3BufferedDetector getQexafsXspress() {
		return qxspress;
	}

	public void setQexafsXspress(Xspress3BufferedDetector xspress) {
		this.qxspress = xspress;
	}

	
	public BufferedScaler getQexafsScaler() {
		return qscaler;
	}

	public void setQexafsScaler(BufferedScaler scaler) {
		this.qscaler = scaler;
	}

	public void setI0_channel(int i0_channel) {
		this.i0_channel = i0_channel;
	}

	public int getI0_channel() {
		return i0_channel;
	}
}
