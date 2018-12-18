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

package uk.ac.gda.server.ncd.subdetector.corba.impl;

import java.util.List;

import org.omg.CORBA.Any;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.DataDimension;
import gda.factory.corba.CorbaFactoryException;
import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;
import uk.ac.gda.server.ncd.subdetector.corba.CorbaNcdsubDetectorPOA;

/**
 * A server side implementation for a distributed Detector class
 */
public class SubdetectorImpl extends CorbaNcdsubDetectorPOA {
	//
	// Private reference to implementation object
	//
	private INcdSubDetector ncdDetector;
	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Constructor
	 * 
	 * @param ncdDetector
	 *            the Detector
	 * @param poa
	 *            the POA
	 */
	public SubdetectorImpl(INcdSubDetector ncdDetector, org.omg.PortableServer.POA poa) {
		this.ncdDetector = ncdDetector;
		this.poa = poa;
		deviceImpl = new DeviceImpl(ncdDetector, poa);
	}

	/**
	 * Get the delegate
	 * 
	 * @return the Detector delegate
	 */
	public Device _delegate() {
		return ncdDetector;
	}

	/**
	 * Set the delegate
	 * 
	 * @param ncdDetector
	 *            the Detector delegate
	 */
	public void _delegate(INcdSubDetector ncdDetector) {
		this.ncdDetector = ncdDetector;
	}

	/**
	 * _default_POA method
	 * 
	 * @return the POA
	 */
	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void setAttribute(String attributeName, Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}


	@Override
	public void clear() throws CorbaDeviceException {
		try {
			ncdDetector.clear();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void start() throws CorbaDeviceException {
		try {
			ncdDetector.start();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void configure() throws CorbaFactoryException {
		deviceImpl.configure();
	}

	@Override
	public boolean isConfigured() throws CorbaDeviceException {
		return deviceImpl.isConfigured();
	}
	
	@Override
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
	}

	/**
	 * 
	 * @return string
	 */
	public String _toString() {
		return ncdDetector.toString();
	}

	@Override
	public void stop() throws CorbaDeviceException {
		try {
			ncdDetector.stop();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String getDetectorType() throws CorbaDeviceException {
		try {
			return ncdDetector.getDetectorType();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	/**
	 * @param o
	 * @param arg
	 */
	public void update(java.lang.Object o, java.lang.Object arg) {
		deviceImpl.update(o, arg);
	}
	
	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		return deviceImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		deviceImpl.setProtectionLevel(newLevel);
	}


	@Override
	public int getMemorySize() throws CorbaDeviceException {
		try {
			return ncdDetector.getMemorySize();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public Any getSupportedDimensions() throws CorbaDeviceException {
        org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
        try {
                List<DataDimension> list = ncdDetector.getSupportedDimensions();
                DataDimension[] dd = new DataDimension[list.size()];
                dd = list.toArray(new DataDimension[0]);
                any.insert_Value(dd);
        } catch (DeviceException ex) {
                throw new CorbaDeviceException(ex.getMessage());
        }
        return any;
	}

	@Override
	public void setDataDimensions(int[] arg0) throws CorbaDeviceException {
		try {
			ncdDetector.setDataDimensions(arg0);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int[] getDataDimensions() throws CorbaDeviceException {
		try {
			return ncdDetector.getDataDimensions();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}
	
	@Override
	public double getPixelSize() throws CorbaDeviceException {
		try {
			return ncdDetector.getPixelSize();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

    @Override
    public void atScanEnd() throws CorbaDeviceException {
            try {
                    ncdDetector.atScanEnd();
            } catch (DeviceException de) {
                    throw new CorbaDeviceException(de.getMessage());
            }
    }
    
    @Override
    public void atScanStart() throws CorbaDeviceException {
            try {
                    ncdDetector.atScanStart();
            } catch (DeviceException de) {
                    throw new CorbaDeviceException(de.getMessage());
            }
    }
}