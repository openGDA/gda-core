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

package uk.ac.gda.server.ncd.subdetector.eiger;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;

public class DummyEigerController implements NcdEigerController {
	private static final Logger logger = LoggerFactory.getLogger(DummyEigerController.class);
	private String filename;

	@Override
	public int[] getDataDimensions() {
		return new int[] {32, 48};
	}

	@Override
	public void startCollection() {
		logger.debug("Starting collection");
	}

	@Override
	public void stopCollection() {
		logger.debug("Ending Collection");
	}

	@Override
	public void endRecording() {
		logger.debug("Ending recording");
	}

	@Override
	public void startRecording() throws DeviceException {
		logger.debug("Starting recording");
	}

	@Override
	public void setDataOutput(String directory, String prefix) {
		logger.debug("Setting file out put to {}/{}", directory, prefix);
		filename = directory + prefix + ".h5";
	}

	@Override
	public void setScanDimensions(int[] dims) throws DeviceException {
		logger.debug("Setup to collect {} dimensions", Arrays.toString(dims));
	}

	@Override
	public void setExposureTimes(int frames, double requestedLiveTime, double requestedDeadTime) throws DeviceException {
		logger.debug("Setting exposure times to {}ms live / {}ms wait", requestedLiveTime, requestedDeadTime);
	}

	@Override
	public String getLastFile() {
		return filename;
	}
}
