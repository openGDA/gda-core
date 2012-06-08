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

package uk.ac.diamond.scisoft.analysis.plotserver.corba.impl;

import java.io.Serializable;

import org.omg.CORBA.Any;

import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerDevice;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.corba.CorbaPlotServerPOA;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.factory.FactoryException;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Motor class
 */
public class PlotserverImpl extends CorbaPlotServerPOA {

	//
	// Private reference to implementation object
	//
	private PlotServerDevice plotServer;

	// this is here for legacy reasons i think, but the warning is suppressed
	// for the time being
	// in case something requires it.
	@SuppressWarnings("unused")
	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param plotServer
	 *            The phantom object which has been passed by corba
	 * @param poa
	 *            the portable object adapter
	 */
	public PlotserverImpl(PlotServerDevice plotServer, org.omg.PortableServer.POA poa) {
		this.plotServer = plotServer;
		this.poa = poa;
		deviceImpl = new DeviceImpl(plotServer, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Motor implementation object
	 */
	public PlotServer _delegate() {
		return plotServer;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param plotServer
	 */
	public void _delegate(PlotServerDevice plotServer) {
		this.plotServer = plotServer;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public Any getGuiState(String arg0) throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = plotServer.getGuiState(arg0);
			any.insert_Value((Serializable) obj);
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils
					.getFullStackMsg(ex));
		}
		return any;
	}

	@Override
	public Any getPlotData(String arg0) throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = plotServer.getData(arg0);	
			any.insert_Value((Serializable) obj);
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils
					.getFullStackMsg(ex));
		}
		return any;
	}

	@Override
	public void setPlotData(String arg0, Any arg1) throws CorbaDeviceException {
		try {
			plotServer.setData(arg0, (DataBean) arg1.extract_Value());
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils
					.getFullStackMsg(ex));
		}

	}

	@Override
	public void updateGui(String arg0, Any arg1) throws CorbaDeviceException {
		try {
			plotServer.updateGui(arg0, (GuiBean) arg1.extract_Value());
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils
					.getFullStackMsg(ex));
		}

	}

	@Override
	public void setAttribute(String attributeName, org.omg.CORBA.Any any)
			throws CorbaDeviceException {
		try {
			plotServer.setAttribute(attributeName, any.extract_Value());
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils
					.getFullStackMsg(ex));
		}
	}

	@Override
	public org.omg.CORBA.Any getAttribute(String attributeName)
			throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = plotServer.getAttribute(attributeName);
			any.insert_Value((Serializable) obj);
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils
					.getFullStackMsg(ex));
		}
		return any;
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		try {
			plotServer.reconfigure();
		} catch (FactoryException ex) {
			throw new CorbaFactoryException(ex.getMessage());
		}
	}

	@Override
	public void close() throws CorbaDeviceException {
		try {
			plotServer.close();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		try {
			return plotServer.getProtectionLevel();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		try {
			plotServer.setProtectionLevel(newLevel);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void updateData(String arg0) throws CorbaDeviceException {
		try {
			plotServer.updateData(arg0);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		
	}

	@Override
	public org.omg.CORBA.Any getGuiNames() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = plotServer.getGuiNames();
			any.insert_Value((Serializable) obj);
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils
					.getFullStackMsg(ex));
		}
		return any;
	}

}
