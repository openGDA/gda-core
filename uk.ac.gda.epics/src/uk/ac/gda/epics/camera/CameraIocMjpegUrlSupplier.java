/*-
 * Copyright © 2026 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.camera;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.LazyPVFactory;
import gda.epics.ReadOnlyPV;
import gda.factory.FindableBase;

/**
 * Uses PV hosted on a camera IOC to report the up to date URL for camera mjpeg stream.
 * This URL is in principle subject to change every time the IOC reboots.
 */
public final class CameraIocMjpegUrlSupplier extends FindableBase {

	private static final Logger logger = LoggerFactory.getLogger(CameraIocMjpegUrlSupplier.class);

	private final ReadOnlyPV<String> urlSupplier;
	private final String errorMessageTemplate;

	public CameraIocMjpegUrlSupplier(String iocUrlLookupPv) {
		urlSupplier = LazyPVFactory.newReadOnlyStringFromWaveformPV(iocUrlLookupPv);
		errorMessageTemplate = "{} is unable to read required Mjpeg stream URL from given ioc PV (%s) {}".formatted(iocUrlLookupPv);
	}

	@Override
	public String toString() {
		try {
			return urlSupplier.get();
		} catch (IOException ioe) {
			var ownFindableBeanName = getName();
			var exceptionMessage = ioe.getMessage();
			logger.error(errorMessageTemplate, ownFindableBeanName, exceptionMessage);
			return "";
		}
	}
}
