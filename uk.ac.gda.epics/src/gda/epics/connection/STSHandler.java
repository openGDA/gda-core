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
import gov.aps.jca.dbr.STS;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;

/**
 * 
 * 
 */
public class STSHandler extends CompoundDataTypeHandler {
	public STSHandler() {
	}

	/**
	 * returns the Alarm Status of this channel directly from EPICS server. The Status are listed in
	 * {@link gov.aps.jca.dbr.Status}.
	 * 
	 * @param ch
	 *            the CA Channel
	 * @return the Alarm Status of this channel
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public Status getStatus(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((STS) getCompoundData(ch)).getStatus();
	}

	/**
	 * returns the Alarm Severity of this channel directly from EPICS server. There are four severity level for each
	 * alarm in EPICS: INVALID_ALARM, MAJOR_ALARM, MINOR_ALARM, NO_ALARM. Based on the alarm severity different
	 * responses can be implemented.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the Alarm severity of this channel
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public Severity getSeverity(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((STS) getCompoundData(ch)).getSeverity();
	}

	/**
	 * returns the Alarm Status of this compound BDR. Before call this method one must test the DBR type of dbr is at
	 * least STS using <code>dbr.isSTS()</code> The Status are listed in {@link gov.aps.jca.dbr.Status}.
	 * 
	 * @param dbr
	 *            the compound BDR value
	 * @return the Alarm Status of this dbr
	 */
	public static Status getStatus(DBR dbr) {
		return ((STS) dbr).getStatus();
	}

	/**
	 * returns the Alarm Severity of this Compound DBR. Before call this method one must test the DBR type of dbr is at
	 * least STS using <code>dbr.isSTS()</code>
	 * 
	 * @param dbr
	 *            the compound DBR value
	 * @return the Alarm severity of the given dbr
	 */
	public static Severity getSeverity(DBR dbr) {
		return ((STS) dbr).getSeverity();
	}

	/**
	 * returns a compound DBR value of this channel, including value, alarm status and alarm severity. implements the
	 * abstract base class method.
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
		DBR value = con.getDBR(ch, getSTSType(ch));
		return value;
	}

	/**
	 * returns the STS DBRType of the channel that corresponds to its native primiary type. This method ensures the
	 * automatic type conversion between primiary types is always avoided during the channel request.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the STS DBRType, i.e. DBR_STS_XXXX
	 */
	public static DBRType getSTSType(Channel ch) {
		DBRType sts_type = DBRType.UNKNOWN;
		if (ch != null) {
			if (ch.getFieldType().isDOUBLE()) {
				sts_type = DBRType.STS_DOUBLE;
			} else if (ch.getFieldType().isFLOAT()) {
				sts_type = DBRType.STS_FLOAT;
			} else if (ch.getFieldType().isSHORT()) {
				sts_type = DBRType.STS_SHORT;
			} else if (ch.getFieldType().isINT()) {
				sts_type = DBRType.STS_INT;
			} else if (ch.getFieldType().isBYTE()) {
				sts_type = DBRType.STS_BYTE;
			} else if (ch.getFieldType().isSTRING()) {
				sts_type = DBRType.STS_STRING;
			} else if (ch.getFieldType().isENUM()) {
				sts_type = DBRType.STS_ENUM;
			} else {
				logger.debug("the channel " + ch.getName() + "'s native field type is not recognised");
			}
		}
		return sts_type;
	}

}
