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

package gda.epics.connection;

import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.CTRL;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;

/**
 * CTRLHandler Class
 */
public class CTRLHandler extends GRHandler {
	CTRLHandler() {
	}

	/**
	 * a generic method returning the lower control limit of the channel directly from EPICS server via compound
	 * DBR_CTRL type.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the lower control limit in {@link Number} type.
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public Number getLowerCrtlLimit(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((CTRL) getCompoundData(ch)).getLowerCtrlLimit();
	}

	/**
	 * a generic method returning the upper control limit of the channel directly from EPICS server via compound
	 * DBR_CTRL type.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the upper control limit in {@link Number} type.
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public Number getUpperCtrlLimit(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((CTRL) getCompoundData(ch)).getUpperCtrlLimit();
	}

	/**
	 * a generic method returning the lower control limit of the DBR if the DBR request is of the CTRL type.
	 * 
	 * @param dbr
	 * @return the lower control limit in {@link Number} type.
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public static Number getLowerCrtlLimit(DBR dbr) {
		return ((CTRL) dbr).getLowerCtrlLimit();
	}

	/**
	 * a generic method returning the upper control limit of the DBR if the DBR is of the type CTRL.
	 * 
	 * @param dbr
	 * @return the upper control limit in {@link Number} type.
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public static Number getUpperCtrlLimit(DBR dbr) {
		return ((CTRL) dbr).getUpperCtrlLimit();
	}

	/**
	 * returns a compound DBR value of the channel, including value, alarm status, alarm severity, units, display
	 * precision, graphic limits, and control limits implements the abstract base class method.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return DBR
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	@Override
	protected DBR getCompoundData(Channel ch) throws CAException, TimeoutException, InterruptedException {
		DBR value = con.getDBR(ch, getCTRLType(ch));
		return value;
	}

	/**
	 * returns the CTRL DBRType of the channel that corresponds to its native primiary type. This method ensures the
	 * automatic type conversion between primiary types is always avoided during the channel request.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return The DBR type name for CTRL, i.e. DBR_CTRL_XXXX
	 */
	public static DBRType getCTRLType(Channel ch) {
		DBRType ctrl_type = DBRType.UNKNOWN;
		if (ch != null) {
			if (ch.getFieldType().isDOUBLE()) {
				ctrl_type = DBRType.CTRL_DOUBLE;
			} else if (ch.getFieldType().isFLOAT()) {
				ctrl_type = DBRType.CTRL_FLOAT;
			} else if (ch.getFieldType().isSHORT()) {
				ctrl_type = DBRType.CTRL_SHORT;
			} else if (ch.getFieldType().isINT()) {
				ctrl_type = DBRType.CTRL_INT;
			} else if (ch.getFieldType().isBYTE()) {
				ctrl_type = DBRType.CTRL_BYTE;
			} else if (ch.getFieldType().isENUM()) {
				ctrl_type = DBRType.CTRL_ENUM;
			} else if (ch.getFieldType().isSTRING()) {
				ctrl_type = DBRType.CTRL_STRING;
			} else {
				logger.debug("the channel " + ch.getName() + "'s native field type is not recognised");
			}
		}
		return ctrl_type;
	}
}
