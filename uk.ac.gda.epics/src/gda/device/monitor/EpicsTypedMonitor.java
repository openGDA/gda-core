/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.monitor;

import gda.device.DeviceException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_CTRL_Enum;
import gov.aps.jca.dbr.DBR_CTRL_Float;
import gov.aps.jca.dbr.DBR_CTRL_Int;
import gov.aps.jca.dbr.DBR_CTRL_Short;
import gov.aps.jca.dbr.DBR_CTRL_String;

/**
 * Version of EpicsMonitor in which the type of readback is explicitly requested.
 * <p>
 * Equivalent to caget with a -d flag
 */
public class EpicsTypedMonitor extends EpicsMonitor {

	private TYPES thisType = TYPES.DBR_FLOAT;
	private int type = 3;

	/**
	 * Set the type attribute according to the index in this enum
	 */
	public static enum TYPES {
		DBR_STRING, DBR_INT, DBR_SHORT, DBR_FLOAT, DBR_ENUM, DBR_DOUBLE
	}

	@Override
	protected Object getSingularValue() throws DeviceException {
		try {

			DBR dbr = controller.getDBR(theChannel, getEpicsType());
			
			switch (thisType) {

			case DBR_STRING:
				latestStrValue = ((DBR_CTRL_String) dbr).getStringValue()[0];
				return latestStrValue;
			case DBR_INT:
				latestIntValue = ((DBR_CTRL_Int) dbr).getIntValue()[0];
				return latestIntValue;
			case DBR_SHORT:
				latestShtValue = ((DBR_CTRL_Short) dbr).getShortValue()[0];
				return latestShtValue;
			case DBR_FLOAT:
				latestFltValue = ((DBR_CTRL_Float) dbr).getFloatValue()[0];
				return latestFltValue;
			case DBR_ENUM:
				latestShtValue = ((DBR_CTRL_Enum) dbr).getEnumValue()[0];
				return latestShtValue;
			case DBR_DOUBLE:
				latestDblValue = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
				return latestDblValue;
			default:
				return "No value is obtained from EPICS";
			}


		} catch (Throwable e) {
			throw new DeviceException("Can NOT get " + theChannel.getName(), e);
		}
	}


	private DBRType getEpicsType() {
		switch (thisType) {

		case DBR_STRING:
			return DBRType.CTRL_STRING;
		case DBR_INT:
			return DBRType.CTRL_INT;
		case DBR_SHORT:
			return DBRType.CTRL_SHORT;
		case DBR_FLOAT:
			return DBRType.CTRL_FLOAT;
		case DBR_ENUM:
			return DBRType.CTRL_ENUM;
		case DBR_DOUBLE:
			return DBRType.CTRL_DOUBLE;
		default:
			return DBRType.CTRL_DOUBLE;
		}
	}

	public void setType(int typeInt) {
		this.type = typeInt;

		switch (typeInt) {

		case 0:
			thisType = TYPES.DBR_STRING;
			break;
		case 1:
			thisType = TYPES.DBR_INT;
			break;
		case 2:
			thisType = TYPES.DBR_SHORT;
			break;
		case 3:
			thisType = TYPES.DBR_FLOAT;
			break;
		case 4:
			thisType = TYPES.DBR_ENUM;
			break;
		case 5:
		default:
			thisType = TYPES.DBR_DOUBLE;
			break;
		}
	}

	/**
	 * Corresponds to the TYPEs enum in this class
	 * 
	 * @return index in the TYPES enum of the type to be returned by the getPosition method
	 */
	public int getType() {
		return type;
	}

}
