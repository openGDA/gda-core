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

package gda.device.detector.xmap.edxd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.analyser.DummyEpicsMcaForXmap;
import gda.device.detector.analyser.IEpicsMCASimple;

/**
 * Version of EDXDMappingController for dummy mode testing
 * <p>
 * Intended as a minimal extension, using dummy subdetector elements instead of real ones.
 */
public class DummyXmapEDXDMappingController extends EDXDMappingController {

	private static final Logger logger = LoggerFactory.getLogger(DummyXmapEDXDMappingController.class);

	private long numberOfChannels = 1024;

	@Override
	protected void addElements() {
		final IEpicsMCASimple mcaSimple = new DummyEpicsMcaForXmap();
		try {
			mcaSimple.setNumberOfChannels(numberOfChannels);
			mcaSimple.configure();
		} catch (Exception e) {
			// This should never happen in dummy mode
			logger.error("Exception configuring DummyEpicsMcaForXmap", e);
		}

		final int elementOffset = getElementOffset();
		for (int i = elementOffset; i < (numberOfElements + elementOffset); i++)
			subDetectors.add(new EDXDMappingElement(xmap, i, mcaSimple));
	}

	public void setNumberOfChannels(long numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}
}
