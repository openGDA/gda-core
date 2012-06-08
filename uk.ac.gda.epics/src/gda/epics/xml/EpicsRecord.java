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

package gda.epics.xml;

import gda.epics.generated.Subsystem;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EpicsRecord class
 */
public class EpicsRecord implements Configurable, Findable, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(EpicsRecord.class);

	// the following 3 properties are set by castor using values in XML file
	private String name;

	private String deviceName;

	private String subsystemName;

	private String recordNameSeparator = ":"; // can be altered via xml file

	// the following 6 properties are used to cache the device and subsystem
	// values.
	// The may be set by castor using values in XML files. If these are not
	// specified
	// in XML file they will be initialised using values stored in
	// devices.xml
	// and
	// types.xml.
	private String epicsDeviceName; // OE or device name in GDA

	private String deviceDescription;// OE or Device description

	private String shortRecordName; // name of the EPICS record instance

	// excluding the ${device} part
	private String fullRecordName; // = epicsDeviceName + shortRecordName

	private String recordType; // map to EPICS record type

	private String recordDescription;// Description for DOF in GDA?

	private String shortName; // short name used by EpicsDevice

	private DevicesParser dp;

	private TypesParser tp;

	/**
	 * Constructor
	 */
	public EpicsRecord() {
	}

	@Override
	public void configure() throws FactoryException {
		dp = DevicesParser.createDevicesParser();
		tp = TypesParser.createTypesParser();
		if (epicsDeviceName == null) {
			epicsDeviceName = dp.getDeviceEpicsName(deviceName);

		}
		if (deviceDescription == null) {
			deviceDescription = dp.getDeviceDescrition(deviceName);
		}
		if (shortRecordName == null) {
			shortRecordName = tp.getSubsystemPV(getSubsystem(deviceName, subsystemName));

		}
		if (fullRecordName == null) {
			fullRecordName = epicsDeviceName;
			if (!shortRecordName.equalsIgnoreCase("")) {
				if (!shortRecordName.startsWith("/")) {
					fullRecordName += recordNameSeparator;
				} else {
					shortRecordName = shortRecordName.substring(1);
				}
				fullRecordName += shortRecordName;
			}
		}
		if (recordType == null) {
			recordType = tp.getSubsystemType(getSubsystem(deviceName, subsystemName));

		}
		if (recordDescription == null) {
			recordDescription = tp.getSubsystemDescription(getSubsystem(deviceName, subsystemName));
		}
	}

	private Subsystem getSubsystem(String devicename, String subsystemname) {
		Subsystem namedSubsystem = null;
		String[] typeNames = dp.getDeviceTypeNames(devicename);
		int itfCount = typeNames.length;
		int i = 0;

		while (namedSubsystem == null && i < itfCount) {

			namedSubsystem = tp.getSubsystemByName(subsystemname, typeNames[i]);
			i++;
		}

		if (namedSubsystem == null) {
			logger.error("Can't find the required Epics Record for Device: " + deviceName + " Subsystem name: "
					+ subsystemName);
		}

		return namedSubsystem;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * @param shortName
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return short name
	 */
	public String getShortName() {
		return this.shortName;
	}

	/**
	 * @return subsystem name
	 */
	public String getSubsystemName() {
		return subsystemName;
	}

	/**
	 * @param subsystemName
	 */
	public void setSubsystemName(String subsystemName) {
		this.subsystemName = subsystemName;
	}

	/**
	 * @return deviceName
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * @return deviceDescription
	 */
	public String getDeviceDescription() {
		return deviceDescription;
	}

	/**
	 * @param deviceDescription
	 */
	public void setDeviceDescription(String deviceDescription) {
		this.deviceDescription = deviceDescription;
	}

	/**
	 * @return epicsDeviceName
	 */
	public String getEpicsDeviceName() {
		return epicsDeviceName;
	}

	/**
	 * @param epicsDeviceName
	 */
	public void setEpicsDeviceName(String epicsDeviceName) {
		this.epicsDeviceName = epicsDeviceName;
	}

	/**
	 * @return recordDescription
	 */
	public String getRecordDescription() {
		return recordDescription;
	}

	/**
	 * @param recordDescription
	 */
	public void setRecordDescription(String recordDescription) {
		this.recordDescription = recordDescription;
	}

	/**
	 * @return recordType
	 */
	public String getRecordType() {
		return recordType;
	}

	/**
	 * @param recordType
	 */
	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	/**
	 * @return fullRecordName
	 */
	public String getFullRecordName() {
		return fullRecordName;
	}

	/**
	 * @param fullRecordName
	 */
	public void setFullRecordName(String fullRecordName) {
		this.fullRecordName = fullRecordName;
	}

	/**
	 * @return shortRecordName
	 */
	public String getShortRecordName() {
		return shortRecordName;
	}

	/**
	 * @param shortRecordName
	 */
	public void setShortRecordName(String shortRecordName) {
		this.shortRecordName = shortRecordName;
	}

}
