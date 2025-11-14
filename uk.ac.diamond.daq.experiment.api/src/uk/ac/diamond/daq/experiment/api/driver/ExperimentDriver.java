/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.api.driver;

import java.util.List;

import gda.device.DeviceException;
import gda.factory.Findable;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;

/**
 * The ExperimentDriver is any apparatus or mechanism that modifies the specimen
 * over time. This can include a mechanical test rig, a furnace, a chemical
 * delivery system, etc.
 * <p>
 * The ExperimentDriver will follow a user-specified profile which may be software-
 * or hardware-driven (i.e. driven by GDA or by some external controller). This
 * profile is generally the process referred to as <i>the experiment</i>, where
 * as scans performed during this time are termed <i>measurements</i>.
 * <p>
 * Additionally, an ExperimentDriver will have a set of readouts which can be used to monitor
 * progress and to trigger GDA/Malcolm measurements (see {@link ISampleEnvironmentVariable}).
 * These readouts need not necessarily be part of the driven hardware, but more generally
 * signals which are affected by the profile. As such, they may need to be calibrated and used
 * as software limits and/or abort conditions.
 *
 *
 * @author Douglas Winter
 */
public interface ExperimentDriver extends Findable {

	void setModel(DriverModel model) throws DeviceException;

	DriverModel getModel();

	String getQuantityName();

	String getQuantityUnits();

	List<DriverSignal> getDriverSignals();

	/**
	 * Calibrate
	 */
	void zero();

	/**
	 * Allowed from {@link DriverState.IDLE}
	 */
	void start();

	/**
	 * Allowed from {@link DriverState.RUNNING}
	 */
	void pause();

	/**
	 * Allowed from {@link DriverState.PAUSED}
	 */
	void resume();

	/**
	 * Allowed from {@link DriverState.RUNNING} or {@link DriverState.PAUSED}
	 */
	void abort();

	DriverState getState();
}
