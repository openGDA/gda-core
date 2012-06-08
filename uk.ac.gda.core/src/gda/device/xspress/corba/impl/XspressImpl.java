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

package gda.device.xspress.corba.impl;

import gda.device.DeviceException;
import gda.device.Xspress;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.xspress.Detector;
import gda.device.xspress.DetectorReading;
import gda.device.xspress.corba.CorbaDetector;
import gda.device.xspress.corba.CorbaDetectorReading;
import gda.device.xspress.corba.CorbaXspressPOA;
import gda.factory.corba.CorbaFactoryException;

import java.io.Serializable;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed Xspress class
 */
public class XspressImpl extends CorbaXspressPOA {
	//
	// Private reference to implementation object
	//
	private Xspress xspress;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param xspress
	 *            the Xspress implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public XspressImpl(Xspress xspress, org.omg.PortableServer.POA poa) {
		this.xspress = xspress;
		this.poa = poa;
		deviceImpl = new DeviceImpl(xspress, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Xspress implementation object
	 */
	public Xspress _delegate() {
		return xspress;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param xspress
	 *            set the Xspress implementation object
	 */
	public void _delegate(Xspress xspress) {
		this.xspress = xspress;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public int getNumberOfDetectors() throws CorbaDeviceException {
		try {
			return xspress.getNumberOfDetectors();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setAttribute(String attributeName, org.omg.CORBA.Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public org.omg.CORBA.Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}

	@Override
	public CorbaDetectorReading readDetector(int which) throws CorbaDeviceException {
		CorbaDetectorReading cdr = null;
		try {
			DetectorReading dr = xspress.readDetector(which);

			cdr = DetectorReadingConverter.toCorbaDetectorReading(dr);

		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
		return cdr;
	}

	@Override
	public CorbaDetectorReading[] readDetectors() throws CorbaDeviceException {
		CorbaDetectorReading[] cdrArray = null;
		try {
			DetectorReading[] drArray = xspress.readDetectors();
			cdrArray = new CorbaDetectorReading[drArray.length];
			for (int i = 0; i < drArray.length; i++)
				cdrArray[i] = DetectorReadingConverter.toCorbaDetectorReading(drArray[i]);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
		return cdrArray;
	}

	@Override
	public void quit() throws CorbaDeviceException {
		try {
			xspress.quit();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public Any getMCData(int detector, int startChannel, int endChannel, int time) throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object result = xspress.getMCData(detector, startChannel, endChannel, time);
			any.insert_Value((Serializable) result);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;
	}

	@Override
	public void setDetectorWindow(int detector, int start, int end) throws CorbaDeviceException {
		try {
			xspress.setDetectorWindow(detector, start, end);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public CorbaDetector getDetector(int which) throws CorbaDeviceException {
		CorbaDetector cd = null;
		try {
			Detector d = xspress.getDetector(which);
			cd = DetectorConverter.toCorbaDetector(d);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
		return cd;
	}

	@Override
	public void saveDetectors(String filename) throws CorbaDeviceException {
		try {
			xspress.saveDetectors(filename);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String loadAndInitializeDetectors(String filename) throws CorbaDeviceException

	{
		String result = null;

		try {
			result = xspress.loadAndInitializeDetectors(filename);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
		return result;
	}

	@Override
	public void setDetectorGain(int detector, double gain) throws CorbaDeviceException {
		try {
			xspress.setDetectorGain(detector, gain);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void setDetectorOffset(int detector, double offset) throws CorbaDeviceException {
		try {
			xspress.setDetectorOffset(detector, offset);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public Any readFrame(int startChannel, int channelCount, int frame) throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object result = xspress.readFrame(startChannel, channelCount, frame);
			any.insert_Value((Serializable) result);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
		return any;
	}

	@Override
	public Any readout() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object result = xspress.readout();
			any.insert_Value((Serializable) result);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
		return any;
	}

	@Override
	public void setReadoutMode(int newMode) throws CorbaDeviceException {
		try {
			xspress.setReadoutMode(newMode);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
	}
	
	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		return deviceImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		deviceImpl.setProtectionLevel(newLevel);
	}

}
