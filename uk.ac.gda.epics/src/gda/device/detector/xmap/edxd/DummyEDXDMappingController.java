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

import java.util.LinkedHashMap;
import java.util.Map;

import gda.device.DeviceException;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDController.PRESET_TYPES;

public class DummyEDXDMappingController implements IEDXDMappingController {

	private Map<Integer, IEDXDElement> subDetectors = new LinkedHashMap<>();
	private COLLECTION_MODES collectionMode;
	private boolean ignoreGate;
	private PRESET_TYPES presetType;

	public IEDXDElement getSubDetector(int index) {
		if (subDetectors.containsKey(index)) {
			return subDetectors.get(index);
		}
		else {
			IEDXDElement subDetector = new DummyEDXDElement();
			subDetectors.put(index, subDetector);
			return subDetector;
		}
	}

	public COLLECTION_MODES getCollectionMode() {
		return collectionMode;
	}

	@Override
	public void setCollectionMode(COLLECTION_MODES mode) throws DeviceException {
		this.collectionMode = mode;
	}

	public boolean getIgnoreGate() {
		return ignoreGate;
	}

	@Override
	public void setIgnoreGate(boolean yes) throws DeviceException {
		this.ignoreGate = yes;
	}

	public PRESET_TYPES getPresetType() {
		return presetType;
	}

	@Override
	public void setPresetType(PRESET_TYPES mode) throws DeviceException {
		this.presetType = mode;
	}

}
