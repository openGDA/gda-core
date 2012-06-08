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

package gda.device.detector;

import gda.device.ContinuousParameters;
import gda.device.Detector;
import gda.device.DeviceException;

import org.apache.commons.lang.ArrayUtils;

/**
 * Dummy implementation for testing / simulations.
 */
public class DummyBufferedDetector extends DetectorBase implements SimulatedBufferedDetector {

	int numberFrames = 0;
	boolean slaveModeOn = false;
	int numChannels = 8;
	//store the data collected to be able to test readout is correct
	int[][] storedFrames = new int[0][];

	/**
	 * 
	 */
	public DummyBufferedDetector() {
		super();
		inputNames = new String[0];
		extraNames = getExtraNames();
		outputFormat = getOutputFormat();
	}

	/**
	 * For use by the DummyContinuousScannable class when testing / simulating this sub-system.
	 */
	@Override
	public void addPoint() {
		storedFrames = (int[][]) ArrayUtils.add(storedFrames, createFrame(numberFrames));
		numberFrames++;
	}
	
	private int[] createFrame(int frame){
		// simulate a numChannels adc
		int[] data = new int[numChannels];

		for (int i = 0; i < numChannels; i++) {
			data[i] = i + frame;
		}
		return data;
	}

	@Override
	public void collectData() throws DeviceException {
		addPoint();
	}

	@Override
	public void clearMemory() {
		numberFrames = 0;
		storedFrames = new int[0][];
	}

	@Override
	public int getNumberFrames() {
		return numberFrames;
	}

	@Override
	public boolean isContinuousMode() {
		return slaveModeOn;
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		return readFrames(0, numberFrames - 1);
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {

		if (finalFrame >= storedFrames.length){
			throw new DeviceException("requested frame number greater out of limits");
		}
		
		int numFrames = (finalFrame - startFrame) + 1;

		int[][] output = new int[numFrames][];

		for (int i = 0; i < numFrames; i++) {
			output[i] = storedFrames[i + startFrame];
		}

		return output;
	}

	@Override
	public void setContinuousMode(boolean on) {
		slaveModeOn = on;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return this.getClass().getName();
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "123";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return this.getClass().getName();
	}

	@Override
	public int getStatus() throws DeviceException {
		return Detector.IDLE;
	}

	@Override
	public Object readout() throws DeviceException {
		return readFrames(numberFrames-1,numberFrames-1);
	}

	@Override
	public String[] getExtraNames() {
		String[] data = new String[numChannels];

		for (Integer i = 0; i < numChannels; i++) {
			data[i] = getName() + "_" + i.toString();
		}
		return data;
	}

	@Override
	public String[] getOutputFormat() {
		String[] data = new String[numChannels + inputNames.length];
		
		for (Integer i = 0; i < inputNames.length; i++) {
			data[i] = "%5.2g";
		}

		for (Integer i = inputNames.length; i < numChannels + inputNames.length; i++) {
			data[i] = "%d";
		}
		return data;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		int[] dims = { numChannels };
		return dims;
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int maximumReadFrames() throws DeviceException {
		return Integer.MAX_VALUE;
	}


}
