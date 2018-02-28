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

import gda.device.detector.analyser.DummyEpicsMcaForXmap;
import gda.device.detector.analyser.IEpicsMCASimple;

/**
 * Version of EDXDMappingController for dummy mode testing
 * <p>
 * Intended as a minimal extension, using dummy subdetector elements instead of real ones.
 */
public class DummyXmapEDXDMappingController extends EDXDMappingController {

	private final IEpicsMCASimple mcaSimple = new DummyEpicsMcaForXmap();

	@Override
	protected void addElements() {
		final int elementOffset = getElementOffset();
		for (int i = elementOffset; i < (numberOfElements + elementOffset); i++)
			subDetectors.add(new EDXDMappingElement(xmap, i, mcaSimple));
	}
}
