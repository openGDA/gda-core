/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector.tetramm;

import java.util.Random;

import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FindableConfigurableBase;

public class DummyNcdTetrammController extends FindableConfigurableBase implements NcdTetrammController {

	private static final Logger logger = LoggerFactory.getLogger(DummyNcdTetrammController.class);
	private int numberOfChannels;
	private int frames;
	private int points;
	private DoubleDataset data;
	private PositionIterator iter;
	private Random random = new Random();

	@Override
	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	@Override
	public int getValuesPerReading() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setValuesPerReading(int values) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getAveragingTime() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAveragingTime(double time) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getSampleTime() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSamplesToAverage() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfAveragedSamples() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAcquire(boolean state) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAcquiring() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setNumberOfChannels(int channels) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTriggerState(TriggerState state) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public TriggerState getTriggerState() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFilePath(String directory, String name) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLastFilePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDimensions(int framesPerPoint, int... scanDims) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRecording(boolean state) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRecording() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialise() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() throws DeviceException {
		// TODO Auto-generated method stub

	}
}
