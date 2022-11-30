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

package gda.epics;

import java.util.ArrayList;

import gda.factory.Configurable;
import gda.factory.Findable;

/**
 * An interface that all EPICS hardware and Communication channels must implement. This interface provides method
 * specification for getting and setting EPICS record name(s) and PV name(s) which required by CASTOR
 * marshall/unmarshall framework. In addition, this interface also extends many other interfaces thus objects
 * implementing it can
 * <li>
 * <ul>
 * be added to name server if required
 * </ul>
 * <ul>
 * be configured after instantiation if required
 * </ul>
 * <ul>
 * be observed by other objects
 * </ul>
 * <ul>
 * communicate asynchronouely with EPICS servers
 * </ul>
 */
public interface Epics extends Findable, Configurable {
	/**
	 * Gets the EPICS Record name. Record name is device's EPICS name plus subsystem's record name without record's
	 * field name. This method is required by EPICS integration through XML inferface files.
	 *
	 * @return the EPICS record name
	 * @throws EpicsException
	 *             if the EPICS record name can not be retrieved.
	 */
	public String getEpicsRecordName() throws EpicsException;

	/**
	 * Sets the name of the EPICS Record. This method is required by EPICS integration through XML inferface files.
	 *
	 * @param epicsRecordName
	 *            the name of the EPICS record
	 * @throws EpicsException
	 *             if the EPICS record name can not be set.
	 */
	public void setEpicsRecordName(String epicsRecordName) throws EpicsException;

	/**
	 * Gets the full PV name. The Process Variable (PV) name is device's EPICS name plus subsystem record name plus
	 * record's field name.
	 *
	 * @return the PV name
	 * @throws EpicsException
	 *             if the PV name can not be retrieved
	 */
	public String getPvName() throws EpicsException;

	/**
	 * Sets the full PV name.
	 *
	 * @param pvName
	 *            the value of the PV name
	 * @throws EpicsException
	 *             if the PV name can not be set.
	 */
	public void setPvName(String pvName) throws EpicsException;

	/**
	 * Gets a set of the EPICS Record names. Record name is device's EPICS name plus subsystem's record name without
	 * record's field name.
	 *
	 * @return the EPICS record name
	 * @throws EpicsException
	 *             if the EPICS record name can not be retrieved.
	 */
	public ArrayList<String> getEpicsRecordNames() throws EpicsException;

	/**
	 * Sets the multiple names of a set of EPICS Records.
	 *
	 * @param epicsRecordNames
	 *            the name of the EPICS record
	 * @throws EpicsException
	 *             if the EPICS record name can not be set.
	 */
	public void setEpicsRecordNames(ArrayList<String> epicsRecordNames) throws EpicsException;

	/**
	 * Gets a list of the full PV names. The Process Variable (PV) name is device's EPICS name plus subsystem record
	 * name plus record's field name.
	 *
	 * @return the PV name
	 * @throws EpicsException
	 *             if the PV name can not be retrieved
	 */
	public ArrayList<String> getPvNames() throws EpicsException;

	/**
	 * Sets a list of full PV names.
	 *
	 * @param pvNames
	 *            the value of the PV name
	 * @throws EpicsException
	 *             if the PV name can not be set.
	 */
	public void setPvNames(ArrayList<String> pvNames) throws EpicsException;
}
