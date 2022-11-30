/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDController.PRESET_TYPES;

public interface IEDXDMappingController {

	void setCollectionMode(COLLECTION_MODES mode) throws DeviceException;

	void setIgnoreGate(boolean yes) throws DeviceException;

	void setPresetType(PRESET_TYPES mode) throws DeviceException;

}