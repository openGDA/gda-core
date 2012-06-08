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
import gov.aps.jca.dbr.BYTE;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DOUBLE;
import gov.aps.jca.dbr.ENUM;
import gov.aps.jca.dbr.FLOAT;
import gov.aps.jca.dbr.INT;
import gov.aps.jca.dbr.SHORT;
import gov.aps.jca.dbr.STRING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CompoundDataTypeHandler Abstract Class
 */
public abstract class CompoundDataTypeHandler {
	protected static final Logger logger = LoggerFactory.getLogger(CompoundDataTypeHandler.class);

	EpicsController con = EpicsController.getInstance();

	CompoundDataTypeHandler() {
	}

	/**
	 * returns the values of this channel directly from EPICS server via compound DBR if the channel's native primiary
	 * type is double.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the channel's values in an array.
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public double[] getDouble(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((DOUBLE) getCompoundData(ch)).getDoubleValue();
	}

	/**
	 * returns the values of this channel directly from EPICS server via compound DBR if the channel's native primiary
	 * type is float.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the channel's values in an array.
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public float[] getFloat(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((FLOAT) getCompoundData(ch)).getFloatValue();
	}

	/**
	 * returns the values of this channel directly from EPICS server via compound DBR if the channel's native primiary
	 * type is integer.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the channel's values in an array.
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public int[] getInt(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((INT) getCompoundData(ch)).getIntValue();
	}

	/**
	 * returns the values of this channel directly from EPICS server via compound DBR if the channel's native primiary
	 * type is short.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the channel's values in an array.
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public short[] getShort(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((SHORT) getCompoundData(ch)).getShortValue();
	}

	/**
	 * returns the values of this channel directly from EPICS server via compound DBR if the channel's native primiary
	 * type is byte.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the channel's values in an array.
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public byte[] getByte(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((BYTE) getCompoundData(ch)).getByteValue();
	}

	/**
	 * returns the values of this channel directly from EPICS server via compound DBR if the channel's native primiary
	 * type is String.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the channel's values in an array.
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public String[] getString(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((STRING) getCompoundData(ch)).getStringValue();
	}

	/**
	 * returns the values of this channel directly from EPICS server via compound DBR if the channel's native primiary
	 * type is enum.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the channel's values in an array.
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public short[] getEnum(Channel ch) throws CAException, TimeoutException, InterruptedException {
		return ((ENUM) getCompoundData(ch)).getEnumValue();
	}

	/**
	 * returns the values of this compound DBR if the DBR is of the type double.
	 * 
	 * @param dbr
	 *            the compound DBR
	 * @return the DBR's values in an array.
	 */
	public static double[] getDouble(DBR dbr) {
		return ((DOUBLE) dbr).getDoubleValue();
	}

	/**
	 * returns the values of this compound DBR if the DBR is of the type float.
	 * 
	 * @param dbr
	 *            the compound DBR
	 * @return the DBR's values in an array.
	 */
	public static float[] getFloat(DBR dbr) {
		return ((FLOAT) dbr).getFloatValue();
	}

	/**
	 * returns the values of this compound DBR if the DBR is of the type integer.
	 * 
	 * @param dbr
	 *            the compound DBR
	 * @return the DBR's values in an array.
	 */
	public static int[] getInt(DBR dbr) {
		return ((INT) dbr).getIntValue();
	}

	/**
	 * returns the values of this compound DBR if the DBR is of the type short.
	 * 
	 * @param dbr
	 *            the compound DBR
	 * @return the DBR's values in an array.
	 */
	public static short[] getShort(DBR dbr) {
		return ((SHORT) dbr).getShortValue();
	}

	/**
	 * returns the values of this compound DBR if the DBR is of the type byte.
	 * 
	 * @param dbr
	 *            the compound DBR
	 * @return the DBR's values in an array.
	 */
	public static byte[] getByte(DBR dbr) {
		return ((BYTE) dbr).getByteValue();
	}

	/**
	 * returns the values of this compound DBR if the DBR is of the type String.
	 * 
	 * @param dbr
	 *            the compound DBR
	 * @return the DBR's values in an array.
	 */
	public static String[] getString(DBR dbr) {
		return ((STRING) dbr).getStringValue();
	}

	/**
	 * returns the values of this compound DBR if the DBR is of the type enum.
	 * 
	 * @param dbr
	 *            the compound DBR
	 * @return the DBR's values in an array.
	 */
	public static short[] getEnum(DBR dbr) {
		return ((ENUM) dbr).getEnumValue();
	}

	/**
	 * returns a compound DBR value of this channel, using compound data type. Subclasses must implement this method.
	 * 
	 * @param ch
	 *            the CA channel
	 * @return the Channel's value
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	protected abstract DBR getCompoundData(Channel ch) throws CAException, TimeoutException, InterruptedException;
}
