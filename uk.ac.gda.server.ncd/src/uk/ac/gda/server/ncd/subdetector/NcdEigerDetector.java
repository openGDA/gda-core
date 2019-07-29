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

package uk.ac.gda.server.ncd.subdetector;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.NXDetectorData;
import gda.device.timer.FrameSet;
import gda.device.timer.Tfg;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.scan.ScanInformation;
import uk.ac.gda.server.ncd.subdetector.eiger.NcdEigerController;

public class NcdEigerDetector extends NcdSubDetector {
	private static final Logger logger = LoggerFactory.getLogger(NcdEigerDetector.class);
	private NcdEigerController controller;

	private Timer timer;
	private IObserver timeListener = this::updateTimes; // hold reference so it can be removed
	private int framesPerPoint;

	@Override
	public void atScanStart() throws DeviceException {
		logger.debug("Start of scan");
		ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		int scanNumber = scanInformation.getScanNumber();
		int[] scanDimensions = scanInformation.getDimensions();
		String beamline = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);
		controller.setDataOutput(PathConstructor.createFromDefaultProperty(), String.format("%s-%d-%s", beamline, scanNumber, getName()));
		controller.setScanDimensions(scanDimensions);
		controller.startRecording();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		// stop data writer
		// stop acquire
		logger.debug("End of scan");
		controller.endRecording();
		controller.stopCollection();
	}

	@Override
	public void setTimer(Timer timer) {
		if (this.timer != null) {
			this.timer.deleteIObserver(timeListener);
		}
		this.timer = timer;
		if (timer != null) {
			this.timer.addIObserver(timeListener);
			if (timer instanceof Tfg) {
				updateTimes(timer, ((Tfg)timer).getFramesets());
			}
		}
	}

	private void updateTimes(@SuppressWarnings("unused") Object source, Object arg) {
		if (!(arg instanceof List<?>)) { return; }
		logger.info("Updating times for {}", getName());
		@SuppressWarnings("unchecked")
		List<FrameSet> framesets = (List<FrameSet>) arg;
		if (framesets.isEmpty()) {
			return;
		}
		FrameSet last = framesets.get(framesets.size() - 1); // use last to ignore initial shutter opening
		framesPerPoint = framesets.stream().mapToInt(FrameSet::getFrameCount).sum();
		try {
			controller.setExposureTimes(framesPerPoint, last.requestedLiveTime, last.requestedDeadTime);
		} catch (DeviceException e) {
			logger.error("Couldn't update exposure times for {} detector", getName(), e);
		}
		logger.debug("Setting {} to collect {} frames of {}(L)+{}(D)ms", getName(), framesPerPoint, last.requestedLiveTime, last.requestedDeadTime);
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		logger.debug("Writing out {}", getName());
		controller.endRecording();
		String filename = controller.getLastFile();
		dataTree.addScanFileLink(getTreeName(), "nxfile://" + filename + "#/data");

		addMetadata(dataTree);

		logger.info("{} - we think we are ready for the next acquisition now.", getName());
	}

	@Override
	public void start() throws DeviceException {
		logger.debug("Starting collection");
		// start acquiring
		controller.startCollection();
	}

	@Override
	public void stop() throws DeviceException {
		logger.debug("Stopping collection");
		// stop acquiring
		// stop data writer
		controller.stopCollection();
		controller.endRecording();
	}

	public void setController(NcdEigerController controller) {
		this.controller = controller;
	}

	public NcdEigerController getController() {
		return controller;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return controller.getDataDimensions();
	}
}
