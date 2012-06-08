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
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.TIME;
import gov.aps.jca.dbr.TimeStamp;

/**
 * TIMEHandler Class
 */
public class TIMEHandler extends STSHandler {
	TIMEHandler() {
	}

	/**
	 * returns a timestamp for the channel request directly from EPICS server via compound DBR_TIME_XXXX.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return timestamp of the channel request
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public TimeStamp getTimeStamp(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((TIME) getCompoundData(ch)).getTimeStamp();
	}

	/**
	 * returns a timestamp for the DBR if the DBR is of the tyep TIME.
	 * 
	 * @param dbr
	 *            the coumpound DBR value
	 * @return timestamp of the compound DBR
	 */
	public static TimeStamp getTimeStamp(DBR dbr) {
		return ((TIME) dbr).getTimeStamp();
	}

	/**
	 * returns a compound DBR value of the channel, including value, alarm status, alarm severity, timestamp. Implements
	 * the abstract base class method.
	 * 
	 * @param ch
	 *            channel
	 * @return a compound DBR value
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	@Override
	protected DBR getCompoundData(Channel ch) throws CAException, TimeoutException, InterruptedException {
		DBR value = con.getDBR(ch, getTIMEType(ch));
		return value;
	}

	/**
	 * returns the TIME DBRType of the channel that corresponds to its native primiary type. This method ensures the
	 * automatic type conversion between primiary types is always avoided during the channel request.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the TIME DBRType, i.e. DBR_TIME_XXXX
	 */
	public static DBRType getTIMEType(Channel ch) {
		DBRType time_type = DBRType.UNKNOWN;
		if (ch != null) {
			if (ch.getFieldType().isDOUBLE()) {
				time_type = DBRType.TIME_DOUBLE;
			} else if (ch.getFieldType().isFLOAT()) {
				time_type = DBRType.TIME_FLOAT;
			} else if (ch.getFieldType().isSHORT()) {
				time_type = DBRType.TIME_SHORT;
			} else if (ch.getFieldType().isINT()) {
				time_type = DBRType.TIME_INT;
			} else if (ch.getFieldType().isBYTE()) {
				time_type = DBRType.TIME_BYTE;
			} else if (ch.getFieldType().isSTRING()) {
				time_type = DBRType.TIME_STRING;
			} else if (ch.getFieldType().isENUM()) {
				time_type = DBRType.TIME_ENUM;
			} else {
				logger.debug("the channel " + ch.getName() + "'s native field type is not recognised");
			}
		}
		return time_type;
	}
}
