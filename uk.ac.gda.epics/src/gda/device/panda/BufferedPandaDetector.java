/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.device.panda;

import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;

public class BufferedPandaDetector extends DetectorBase implements BufferedDetector {
	private final Logger logger = LoggerFactory.getLogger(BufferedPandaDetector.class);
	private PandaDetector pandaDetector;
	private PandaController controller;
	private ContinuousParameters parameters;
	private int maximumReadFrames = 100;


	public BufferedPandaDetector() {
		setInputNames(new String[] {});
	}

	public PandaDetector getPandaDetector() {
		return pandaDetector;
	}

	public void setPandaDetector(PandaDetector pandaDetector) {
		this.pandaDetector = pandaDetector;
		controller = pandaDetector.getController();
	}


	@Override
	public void collectData() throws DeviceException {
		controller.setSeqBitA("ONE");
	}

	@Override
	public int getStatus() throws DeviceException {
		return pandaDetector.getStatus();
	}

	@Override
	public Object readout() throws DeviceException {
		return pandaDetector.readout();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public void clearMemory() throws DeviceException {
		// nothing to do here
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		logger.info("SetContinuousMode : {}", on);

		if (on) {
			// set collection time on pandaScalers, so at atScanStart uses correct time per point.
			double timePerPoint = parameters.getTotalTime()/parameters.getNumberDataPoints();
			pandaDetector.setCollectionTime(timePerPoint);

			// prepare for the scan
			pandaDetector.atScanStart();
		}

		String state = on ? PandaDetector.ONE:PandaDetector.ZERO;
		controller.setSeqBitA(state);
	}

	@Override
	public void atScanStart() throws DeviceException {
		// pandaScalers.atScanStart(); is called in

	}

	@Override
	public void atScanEnd() throws DeviceException {
		pandaDetector.atScanEnd();
	}

	@Override
	public void stop() throws DeviceException {
		pandaDetector.stop();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		pandaDetector.atCommandFailure();
	}

	@Override
	public String[] getExtraNames() {
		return pandaDetector.getExtraNames();
	}

	@Override
	public String[] getOutputFormat() {
		return pandaDetector.getOutputFormat();
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return false;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		this.parameters = parameters;
	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return parameters;
	}

	/**
	 * Return current line repetition number in the sequence table.
	 * We assume that this corresponds to number of frames captured by Panda and
	 * available for readback via Socket or Hdf file (once data has been flushed to disc etc).
	 */
	@Override
	public int getNumberFrames() throws DeviceException {
		return controller.getSeqTableLineRepeat();
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		if (pandaDetector.isReadPandaData()) {
			return controller.readData(startFrame, finalFrame);
		}
		return IntStream.range(startFrame,  finalFrame+1).mapToObj(i->i).toArray();
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		return readFrames(0, getNumberFrames());
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		return maximumReadFrames;
	}

	public void setMaximumReadFrames(int maximumReadFrames) {
		this.maximumReadFrames = maximumReadFrames;
	}
}
