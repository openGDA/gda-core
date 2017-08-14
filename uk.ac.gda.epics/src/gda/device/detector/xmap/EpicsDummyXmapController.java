/*-
 * Copyright © 2017 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.xmap;

import gda.device.detector.xmap.edxd.IEDXDMappingController;

/**
 * Should set in the XML this class to be always local. There is no corba implementation for it.
 */
public class EpicsDummyXmapController extends DummyXmapController implements XmapController {

	protected IEDXDMappingController edxdController;

	public IEDXDMappingController getEdxdController() {
		return edxdController;
	}

	public void setEdxdController(IEDXDMappingController edxdController) {
		this.edxdController = edxdController;
	}

}
