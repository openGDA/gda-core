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

import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.nexus.NexusException;
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

		String state = on ? "ONE":"ZERO";
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

	@Override
	public int getNumberFrames() throws DeviceException {
		if (!pandaDetector.isReadSwmrFile()) {
			return parameters.getNumberDataPoints();
		}
		return controller.getHdfNumCaptured();
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		if (!pandaDetector.isReadSwmrFile()) {
			return IntStream.range(startFrame, finalFrame+1).mapToObj(i -> i).toArray();
		}

		if (startFrame==0) {
			pandaDetector.readout();
		}
		try {
			var reader = pandaDetector.getPandaHdfReader();
			reader.waitForFrames(finalFrame);
			List<double[]> data = reader.readData(startFrame, finalFrame);
			int nFrames = data.get(0).length;
			Object[][] frameData = new Object[nFrames][data.size()];

			for(int i=0; i<data.size(); i++) {
				var itemData = data.get(i);
				for(int frame=0; frame<itemData.length; frame++) {
					frameData[frame][i] = itemData[frame];
				}
			}
			return frameData;
		}catch(NexusException ne) {
			throw new DeviceException("Problem reading out frames "+startFrame+" to "+finalFrame);
		}
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		int totalFrames = controller.getHdfNumCaptured();
		return readFrames(0, totalFrames);
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		return maximumReadFrames;
	}

	public void setMaximumReadFrames(int maximumReadFrames) {
		this.maximumReadFrames = maximumReadFrames;
	}
}
