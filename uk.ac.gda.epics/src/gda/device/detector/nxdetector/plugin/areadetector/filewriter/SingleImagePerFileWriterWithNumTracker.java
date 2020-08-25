/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector.filewriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Use {@link gda.device.detector.addetector.filewriter.SingleImagePerFileWriterWithNumTracker} directly
 * <p>
 * This will be removed in GDA 9.20
 */
@Deprecated
public class SingleImagePerFileWriterWithNumTracker extends gda.device.detector.addetector.filewriter.SingleImagePerFileWriterWithNumTracker {

	private static final Logger logger = LoggerFactory.getLogger(SingleImagePerFileWriterWithNumTracker.class);

	public SingleImagePerFileWriterWithNumTracker() {
		logger.warn("Instance of deprecated class: {} created, this will be removed in GDA 9.20", SingleImagePerFileWriterWithNumTracker.class);
	}

	SingleImagePerFileWriterWithNumTracker(String detectorName) {
		super(detectorName);
	}
}
