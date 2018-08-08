/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.data;

/**
 * Base class for correction providers which read calibration files split by modules in the file system.
 * <p>
 * For the B18 6-module Mythen. The calibration files are split into three folders: fast, highgain and standard. This is
 * the 'mode' and this may be changed on-the-fly to get different corrections.
 */
public abstract class SplitCalibrationFilesBase {

	protected static final int CHANNELSPERMODULE = 1280;

	protected ModuleDefinitions modules;

	public ModuleDefinitions getModules() {
		return modules;
	}

	public void setModules(ModuleDefinitions modules) {
		this.modules = modules;
	}
}
