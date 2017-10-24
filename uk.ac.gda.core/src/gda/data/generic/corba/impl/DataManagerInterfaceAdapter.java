/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.generic.corba.impl;

import gda.data.DataManagerInterface;
import gda.data.generic.GenericData;
import gda.data.generic.IGenericData;
import gda.data.generic.corba.CorbaDataException;
import gda.data.generic.corba.CorbaDataManagerInterface;
import gda.data.generic.corba.CorbaDataManagerInterfaceHelper;
import gda.factory.corba.util.NetService;

import java.util.Vector;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client side implementation of the adapter pattern for the DataManagerInterface class
 */
public class DataManagerInterfaceAdapter implements DataManagerInterface {

	private static final Logger logger = LoggerFactory.getLogger(DataManagerInterfaceAdapter.class);

	private CorbaDataManagerInterface corbaData;

	private NetService netService;

	private String name;

	/**
	 * Create client side interface to the CORBA package.
	 *
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public DataManagerInterfaceAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		corbaData = CorbaDataManagerInterfaceHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public GenericData create(String dataName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return (GenericData) corbaData.create(dataName).extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (CorbaDataException ex) {
				logger.error("Could not create GenericData for {}", dataName, ex);
				return null;
			}
		}
		logger.error("Communication failure: retry failed");
		return null;
	}

	@Override
	public void add(String dataName, IGenericData data) {
		for (int i = 0; i < NetService.RETRY; i++) {
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			try {
				any.insert_Value(data);
				corbaData.add(dataName, any);
			} catch (COMM_FAILURE cf) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (CorbaDataException ex) {
				logger.error("Could not add {} to {}", data, dataName, ex);
			}
		}
		logger.error("Communication failure: retry failed");
	}

	@Override
	public void remove(String dataName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaData.remove(dataName);
			} catch (COMM_FAILURE cf) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (CorbaDataException ex) {
				logger.error("Could not remove {}", dataName, ex);
			}
		}
		logger.error("Communication failure: retry failed");
	}

	@Override
	public Vector<String> list() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Vector<String> theVList = new Vector<String>();
				String[] theList = corbaData.list();
				for (int j = 0; j < theList.length; j++) {
					theVList.add(theList[j]);
				}
				return theVList;
			} catch (COMM_FAILURE cf) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (CorbaDataException ex) {
				logger.error("Could not get list", ex);
				return null;
			}
		}
		logger.error("Communication failure: retry failed");
		return null;
	}

	@Override
	public GenericData get(String dataName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return (GenericData) corbaData.get(dataName).extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaData = CorbaDataManagerInterfaceHelper.narrow(netService.reconnect(name));
			} catch (CorbaDataException ex) {
				logger.error(ex.message);
				return null;
			}
		}
		logger.error("Communication failure: retry failed");
		return null;
	}

}
