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

import static gda.configuration.properties.LocalProperties.GDA_BEAMLINE_NAME;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;
import gda.device.timer.FrameSet;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.scan.ScanInformation;
import uk.ac.gda.server.ncd.subdetector.tetramm.NcdTetrammController;

public class NcdTetrammDetector extends NcdSubDetector {
	private static final Logger logger = LoggerFactory.getLogger(NcdTetrammDetector.class);
	/** Delegate to provide link to hardware */
	private NcdTetrammController controller;
	/** Reference to time frame generator to provide number of frames per point */
	private Timer timer;
	/** The timer channel used to trigger this detector */
	private int channel;
	/** The number of frames to collect for each scan point */
	private int frameCount;
	/** The averaging time for each frame */
	private double frameLengthS = 1;
	/** The number of values per frame - set indirectly by adjusting values per reading */
	private int targetValuesPerFrame = 1_000;
	/** The base sampling rate of the detector - should not change */
	private int baseSampleRate = 10_000;

	/** Event handler to respond to frame length changes from the timer */
	@SuppressWarnings("unchecked") // unchecked cast to Collection<FrameSet>
	private IObserver handleUpdate = (src, evt) -> {
		if (evt instanceof Collection<?>) {
			updateFrames((Collection<FrameSet>)evt);
		}
	};

	@Override
	public void configure() throws FactoryException {
		requireNonNull(controller, "NcdTetrammController must be non-null");
		setConfigured(true);
	}

	@Override
	public void start() throws DeviceException {
		// Does anything need to happen at the start of each point? detector acquires continuously
	}

	@Override
	public void stop() throws DeviceException {
		controller.setAcquire(false);
		controller.setRecording(false);
		controller.reset();
	}

	@Override
	public void clear() throws DeviceException {
		logger.debug("{} - clearing detector", getName());
	}

	@Override
	public void atScanStart(ScanInformation info) throws DeviceException {
		if (inactive()) return;
		super.atScanStart(info);
		logger.debug("{} - Starting scan {}", getName(), info.getScanNumber());
		setFilePath(info);
		controller.setAveragingTime(frameLengthS);
		controller.setValuesPerReading((int)(frameLengthS * baseSampleRate / targetValuesPerFrame));
		controller.setDimensions(frameCount, info.getDimensions());
		controller.initialise();
		controller.setAcquire(true);
		controller.setRecording(true);
	}

	private boolean inactive() {
		if (frameCount == 0) {
			logger.debug("{} - No frames configured for this detector", getName());
			return true;
		}
		return false;
	}

	private void setFilePath(ScanInformation info) throws DeviceException {
		String beamline = LocalProperties.get(GDA_BEAMLINE_NAME);
		String directory = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		String filename = String.format("%s-%d-%s" , beamline, info.getScanNumber(), getName());
		controller.setFilePath(directory, filename);
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if (inactive()) return;
		logger.debug("{} - atScanStart", getName());
		try {
			stop();
		} catch (DeviceException e) {
			throw new DeviceException(getName() + " - Could not stop detector at the end of scan", e);
		}
		super.atScanEnd();
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		if (inactive()) return;
		logger.debug("{} - Writing out {} frames", getName(), frames);
		dataTree.addScanFileLink(getTreeName(), "nxfile://" + controller.getLastFilePath() + "#entry/instrument/detector/data");

		addMetadata(dataTree);
	}

	@Override
	public int getMemorySize() throws DeviceException {
		logger.debug("{} - Getting memory size", getName());
		return 0;
	}

	@Override
	public List<DataDimension> getSupportedDimensions() throws DeviceException {
		return null;
	}

	@Override
	public void setDataDimensions(int[] detectorSize) throws DeviceException {
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		try {
			return new int[] {controller.getNumberOfChannels()};
		} catch (DeviceException e) {
			throw new DeviceException(getName() + " - Could not read number of channels", e);
		}
	}

	@Override
	public double getPixelSize() throws DeviceException {
		return 0;
	}

	@Override
	public void setTimer(Timer timer) {
		if (this.timer != null) {
			this.timer.deleteIObserver(handleUpdate);
		}
		this.timer = timer;
		timer.addIObserver(handleUpdate);
	}

	public void updateFrames(Collection<FrameSet> frames) {
		// Only count the groups that are triggering this detector
		Predicate<FrameSet> frameFilter = fs -> (fs.getLivePort() & (1 << channel)) != 0;
		frameCount = frames.stream()
				.filter(frameFilter)
				.mapToInt(FrameSet::getFrameCount)
				.sum();
		if (frameCount == 0) {
			logger.info("{} - channel is not active in new frame sets", getName());
			return;
		}
		frameLengthS = frames.stream()
				.filter(frameFilter)
				.mapToDouble(FrameSet::getRequestedLiveTime)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No frames configured for " + getName())) // should never reach here
				/ 1000;
	}

	public NcdTetrammController getController() {
		return controller;
	}

	public void setController(NcdTetrammController controller) {
		this.controller = controller;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getTargetValuesPerFrame() {
		return targetValuesPerFrame;
	}

	public void setTargetValuesPerFrame(int targetValuesPerFrame) {
		this.targetValuesPerFrame = targetValuesPerFrame;
	}

	public int getBaseSampleRate() {
		return baseSampleRate;
	}

	public void setBaseSampleRate(int baseSampleRate) {
		this.baseSampleRate = baseSampleRate;
	}
}
