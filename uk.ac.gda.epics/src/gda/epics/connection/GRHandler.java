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
import gov.aps.jca.dbr.GR;

// TODO some methods are static some not... they can (shoud!) be
/**
 * GRHandler Class
 */
public class GRHandler extends STSHandler {
	GRHandler() {
	}

	/**
	 * returns the unit of this channel directly from EPICS server via compound DBR access.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return The Unit of this channel
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public String getUnits(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((GR) getCompoundData(ch)).getUnits();
	}

	/**
	 * returns the lower display limit of this channel as {@link Number} directly from EPICS server via compound DBR
	 * access.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the lower display limit, i.e. LOPR
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public Number getLowerDispLimit(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return (((GR) getCompoundData(ch)).getLowerDispLimit());
	}

	/**
	 * returns the upper display limit of this channel as {@link Number} directly from EPICS server via compound DBR
	 * access.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the upper display limit, i.e. HOPR
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public Number getUpperDispLimit(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return (((GR) getCompoundData(ch)).getUpperDispLimit());
	}

	/**
	 * returns the lower Alarm limit of this channel as {@link Number} directly from EPICS server via compound DBR
	 * access.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the lower alarm limit
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public Number getLowerAlarmLimit(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return (((GR) getCompoundData(ch)).getLowerAlarmLimit());
	}

	/**
	 * returns the upper alarm limit of this channel as {@link Number} directly from EPICS server via compound DBR
	 * access.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the upper alarm limit
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public Number getUpperAlarmLimit(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return (((GR) getCompoundData(ch)).getUpperAlarmLimit());
	}

	/**
	 * returns the lower warning limit of this channel as {@link Number} directly from EPICS server via compound DBR
	 * access.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the lower warning limit
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public Number getLowerWarningLimit(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return (((GR) getCompoundData(ch)).getLowerWarningLimit());
	}

	/**
	 * returns the upper warning limit of this channel as {@link Number} directly from EPICS server via compound DBR
	 * access.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the upper warning limit
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public Number getUpperWarningLimit(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return (((GR) getCompoundData(ch)).getUpperWarningLimit());
	}

	/**
	 * returns the unit of the DBR if the DBR is of GR type.
	 * 
	 * @param dbr
	 *            the compound GR type DBR, i.e. DBR_GR_XXXX
	 * @return the unit string
	 */
	public static String getUnits(DBR dbr) {
		return ((GR) dbr).getUnits();
	}

	/**
	 * returns the lower display limit of the DBR in {@link Number} type if the DBR is of GR type.
	 * 
	 * @param dbr
	 *            the compound GR type DBR, i.e. DBR_GR_XXXX
	 * @return the lower display limit, i.e. LOPR
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public static Number getLowerDispLimit(DBR dbr) {
		return (((GR) dbr).getLowerDispLimit());
	}

	/**
	 * returns the upper display limit of the DBR in {@link Number} type if the DBR is of GR type.
	 * 
	 * @param dbr
	 *            the compound GR type DBR, i.e. DBR_GR_XXXX
	 * @return the upper display limit, i.e. HOPR
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public static Number getUpperDispLimit(DBR dbr) {
		return (((GR) dbr).getUpperDispLimit());
	}

	/**
	 * returns the lower alarm limit of the DBR in {@link Number} type if the DBR is of GR type.
	 * 
	 * @param dbr
	 *            the compound GR type DBR, i.e. DBR_GR_XXXX
	 * @return the lower alarm limit
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public static Number getLowerAlarmLimit(DBR dbr) {
		return (((GR) dbr).getLowerAlarmLimit());
	}

	/**
	 * returns the upper alarm limit of the DBR as {@link Number} if the DBR is of GR type.
	 * 
	 * @param dbr
	 *            the compound GR type DBR, i.e. DBR_GR_XXXX
	 * @return the upper alarm limit
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public static Number getUpperAlarmLimit(DBR dbr) {
		return (((GR) dbr).getUpperAlarmLimit());
	}

	/**
	 * returns the lower warning limit of the DBR in {@link Number} type if the DBR is of GR type.
	 * 
	 * @param dbr
	 *            the compound GR type DBR, i.e. DBR_GR_XXXX
	 * @return the lower warning limit
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public static Number getLowerWarningLimit(DBR dbr) {
		return (((GR) dbr).getLowerWarningLimit());
	}

	/**
	 * returns the upper warning limit of the DBR in {@link Number} type if the DBR is of GR type.
	 * 
	 * @param dbr
	 *            the compound GR type DBR, i.e. DBR_GR_XXXX
	 * @return the upper warning limit.
	 * @Note the return type is <code>Number</code>. This needs to be casted to java primiary type before use.
	 */
	public static Number getUpperWarningLimit(DBR dbr) {
		return (((GR) dbr).getUpperWarningLimit());
	}

	/**
	 * returns a compound DBR value of the channel, including value, alarm status, alarm severity, units, display
	 * precision, and graphic limits. implements the abstract base class method.
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
		DBR value = con.getDBR(ch, getGRType(ch));
		return value;
	}

	/**
	 * returns the GR DBRType of the channel that corresponds to its native primiary type. This method ensures the
	 * automatic type conversion between primiary types is always avoided during the channel request.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the GR DBRType, i.e. DBR_GR_XXXX
	 */
	public static DBRType getGRType(Channel ch) {
		DBRType gr_type = DBRType.UNKNOWN;
		if (ch != null) {
			if (ch.getFieldType().isDOUBLE()) {
				gr_type = DBRType.GR_DOUBLE;
			} else if (ch.getFieldType().isFLOAT()) {
				gr_type = DBRType.GR_FLOAT;
			} else if (ch.getFieldType().isSHORT()) {
				gr_type = DBRType.GR_SHORT;
			} else if (ch.getFieldType().isINT()) {
				gr_type = DBRType.GR_INT;
			} else if (ch.getFieldType().isBYTE()) {
				gr_type = DBRType.GR_BYTE;
			}

			else {
				logger.error("native DBR type is not recognised");
			}
		}
		return gr_type;
	}
}
