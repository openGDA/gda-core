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
import java.util.HashMap;

import gda.factory.FactoryException;

/**
 * EpicsBase class provides the common implementation to all EPICS classes. It implements all methods required by
 * interfaces except the monitorChanged() which must be implemented by sub-classes. This method is normally providing
 * object-specific behaviour.
 */
// TODO revise
public abstract class EpicsBase implements Epics {
	protected String name;

	protected String epicsRecordName;

	protected String pvName;

	protected ArrayList<String> epicsRecordNames = new ArrayList<String>();

	protected ArrayList<String> pvNames = new ArrayList<String>();

	protected HashMap<String, Integer> connectionCountRegister = new HashMap<String, Integer>();

	/**
	 * configure the object after instantiation.
	 *
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
	}

	@Override
	public String getEpicsRecordName() throws EpicsException {
		return epicsRecordName;
	}

	@Override
	public String getPvName() throws EpicsException {
		return pvName;
	}

	@Override
	public void setEpicsRecordName(String epicsRecord) throws EpicsException {
		this.epicsRecordName = epicsRecord;
	}

	@Override
	public void setPvName(String pv) throws EpicsException {
		this.pvName = pv;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String objectname) {
		this.name = objectname;
	}

	@Override
	public ArrayList<String> getEpicsRecordNames() throws EpicsException {
		return epicsRecordNames;
	}

	@Override
	public ArrayList<String> getPvNames() throws EpicsException {
		return pvNames;
	}

	@Override
	public void setEpicsRecordNames(ArrayList<String> epicsRecords) throws EpicsException {
		this.epicsRecordNames = epicsRecords;
	}

	@Override
	public void setPvNames(ArrayList<String> pvs) throws EpicsException {
		this.pvNames = pvs;
	}

}
