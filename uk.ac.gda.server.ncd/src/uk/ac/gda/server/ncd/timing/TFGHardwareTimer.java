/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.timing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.timer.Tfg;
import uk.ac.gda.server.ncd.timing.data.TFGCoreConfiguration;
import uk.ac.gda.server.ncd.timing.data.TFGGroupConfiguration;

public class TFGHardwareTimer implements HardwareTimer {
	private static final Logger log = LoggerFactory.getLogger(TFGHardwareTimer.class);

	private static final String CONFIGURATION_FILE_EXTENSION = "*.tfg";

	private Tfg tfg;
	private double minimumExposure;
	private double minimumDelay;
	private double fastShutterOpeningTime;

	private TFGCoreConfiguration tfgCoreConfiguration;
	private TFGGroupConfiguration tfgGroupConfiguration;

	public TFGHardwareTimer (Tfg tfg, TFGCoreConfiguration tfgCoreConfiguration, TFGGroupConfiguration tfgGroupConfiguration,
			double minimumExposure, double minimumDelay, double fastShutterOpeningTime) {
		this.tfg = tfg;

		this.tfgCoreConfiguration = tfgCoreConfiguration;
		this.tfgGroupConfiguration = tfgGroupConfiguration;

		this.minimumExposure = minimumExposure;
		this.minimumDelay = minimumDelay;
		this.fastShutterOpeningTime = fastShutterOpeningTime;
	}

	/**
	 * The first frame ignores the user defined delay, but includes the delay required for the Fast Shutter to open.
	 * Subsequent frames the fast shutter is left open and the user defined delay is applied.
	 */
	@Override
	public void configureTimer(int frames, double exposure, double delayTime) throws DeviceException, HardwareTimerException {
		if (frames <= 0) {
			throw new HardwareTimerException("Invalid number of frames - " + frames);
		}
		if (exposure < minimumExposure) {
			throw new HardwareTimerException("Exposure time (" + exposure +
					"s) is less than the minimum time configurable (" + minimumExposure + "s)");
		}
		if (frames > 1 && delayTime < minimumDelay) {
			throw new HardwareTimerException("Delay time (" + delayTime +
					"s) is less than the minimum time configurable (" + minimumDelay + "s)");
		}

		log.debug("Started TFG configuration");
		if (tfg.getStatus() != gda.device.Timer.IDLE) {
			throw new DeviceException("Cannot configure while TFG running!");
		}
		tfg.setAttribute("Debounce", tfgCoreConfiguration.getDebounce());
		tfg.setAttribute("Threshold", tfgCoreConfiguration.getThresholds());
		tfg.setAttribute("Inversion", tfgCoreConfiguration.getInversion());
		tfg.setAttribute("Drive", tfgCoreConfiguration.getDrive());
		tfg.setAttribute("Start-Method", tfgCoreConfiguration.getStartMethod());
		tfg.setAttribute("Ext-Inhibit", tfgCoreConfiguration.isExtInhibit());
		tfg.setCycles(tfgCoreConfiguration.getCycles());
		if (frames > tfg.getMaximumFrames()) {
			throw new DeviceException(String.format("%d frames requested but only %d allowed", frames, tfg.getMaximumFrames()));
		}
		tfg.clearFrameSets();
		// First frame delay time is fast shutter opening time
		tfg.addFrameSet(1, fastShutterOpeningTime * 1000, exposure * 1000, tfgGroupConfiguration.getDeadPort(),
				tfgGroupConfiguration.getLivePort(), tfgGroupConfiguration.getDeadPause(), tfgGroupConfiguration.getLivePause());
		if (frames > 1) {
			tfg.addFrameSet(frames - 1, delayTime * 1000, exposure * 1000, tfgGroupConfiguration.getDeadPort(),
					tfgGroupConfiguration.getLivePort(), tfgGroupConfiguration.getDeadPause(),
					tfgGroupConfiguration.getLivePause());
			tfg.loadFrameSets();
		}
		log.debug("Ended TFG configuration");
	}

	@Override
	public String getConfigurationFileExtension () {
		return CONFIGURATION_FILE_EXTENSION;
	}

	@Override
	public double getMinimumDelay() {
		return minimumDelay;
	}
}
