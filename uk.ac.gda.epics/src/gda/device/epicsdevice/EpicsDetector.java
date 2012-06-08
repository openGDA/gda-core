/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.epicsdevice;

import gda.device.detector.DetectorBase;
import gda.device.DeviceException;

/**
 * Epics Detector Class
 */
public class EpicsDetector extends DetectorBase {
	final String record, field;
	final EpicsDevice epicsDevice;
	final int[] dimensions;
	final double putTimeOutInSec;

	EpicsDetector(EpicsDevice epicsDevice, String name, String record, String field, double putTimeOutInSec) {
		this.epicsDevice = epicsDevice;
		this.record = record != null ? record : "";
		this.field = field != null ? field : "";
		this.dimensions = new int[1];
		this.dimensions[0] = 0;
		this.putTimeOutInSec = putTimeOutInSec;
		setName(name != null ? name : epicsDevice.getName() + ":" + record + "." + field);
		setInputNames(new String[] { getName() });
	}

	@Override
	public void collectData() throws DeviceException {
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public Object readout() throws DeviceException {
		return epicsDevice.getValue(ReturnType.DBR_NATIVE, record, field);
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return getName();
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return getName();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return getName();
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		if (dimensions[0] == 0)
			this.dimensions[0] = epicsDevice.getElementCount(record, field);
		return dimensions;
	}

	/**
	 * @param _returnType
	 * @param subField
	 * @return Object
	 * @throws DeviceException
	 */
	public Object getValue(ReturnType _returnType, String subField) throws DeviceException {
		return epicsDevice.getValue(_returnType, record, field + subField);
	}

	/**
	 * @param subField
	 * @param position
	 * @throws DeviceException
	 */
	public void setValue(String subField, Object position) throws DeviceException {
		epicsDevice.setValue(null, record, field + subField, position, putTimeOutInSec);
	}
}