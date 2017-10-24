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

package gda.device.scannable.corba.impl;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.scannable.corba.CorbaScannablePOA;
import gda.factory.corba.CorbaFactoryException;
import gda.util.exceptionUtils;

import java.io.Serializable;

import org.omg.CORBA.Any;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A server side implementation for a distributed Scannable class
 */
public class ScannableImpl extends CorbaScannablePOA {
	private static final Logger logger = LoggerFactory.getLogger(ScannableImpl.class);
	//
	// Private reference to implementation objects
	//

	private Scannable scannable;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param scannable
	 *            the Scannable implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public ScannableImpl(Scannable scannable, org.omg.PortableServer.POA poa) {
		this.scannable = scannable;
		this.poa = poa;
		deviceImpl = new DeviceImpl(this.scannable, poa);
	}

	/**
	 * Get the implementation object
	 *
	 * @return the Scannable implementation object
	 */
	public Scannable _delegate() {
		return scannable;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param scannable
	 *            set the Scannable implementation object
	 */
	public void _delegate(Scannable scannable) {
		this.scannable = scannable;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public String _toString() {
		return scannable.toString();
	}

	@Override
	public void asynchronousMoveTo(Any arg0) throws CorbaDeviceException {
		try {
			this.scannable.asynchronousMoveTo(arg0.extract_Value());
		} catch (Throwable e) {
			logger.error("asynchronousMoveTo error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void atPointEnd() throws CorbaDeviceException {
		try {
			this.scannable.atPointEnd();
		} catch (Throwable e) {
			logger.error("atPointEnd error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void atLevelMoveStart() throws CorbaDeviceException {
		try {
			this.scannable.atLevelMoveStart();
		} catch (Throwable e) {
			logger.error("atLevelMoveStart error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void atLevelStart() throws CorbaDeviceException {
		try {
			this.scannable.atLevelStart();
		} catch (Throwable e) {
			logger.error("atLevelStart error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void atLevelEnd() throws CorbaDeviceException {
		try {
			this.scannable.atLevelEnd();
		} catch (Throwable e) {
			logger.error("atLevelEnd error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void atPointStart() throws CorbaDeviceException {
		try {
			this.scannable.atPointStart();
		} catch (Throwable e) {
			logger.error("atPointStart error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void atScanEnd() throws CorbaDeviceException {
		try {
			this.scannable.atScanEnd();
		} catch (Throwable e) {
			logger.error("atScanEnd error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void atScanLineEnd() throws CorbaDeviceException {
		try {
			this.scannable.atScanLineEnd();
		} catch (Throwable e) {
			logger.error("atScanLineEnd error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void atScanLineStart() throws CorbaDeviceException {
		try {
			this.scannable.atScanLineStart();
		} catch (Throwable e) {
			logger.error("atScanLineStart error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void atScanStart() throws CorbaDeviceException {
		try {
			this.scannable.atScanStart();
		} catch (Throwable e) {
			logger.error("atScanStart error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public String[] getExtraNames() {
		return this.scannable.getExtraNames();
	}

	@Override
	public String[] getInputNames() {
		return this.scannable.getInputNames();
	}

	@Override
	public int getLevel() {
		return this.scannable.getLevel();
	}

	@Override
	public Any getPosition() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = scannable.getPosition();
			any.insert_Value((Serializable) obj);
		} catch (Throwable e) {
			logger.error("getPosition error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
		return any;
	}

	@Override
	public boolean isBusy() throws CorbaDeviceException {
		try {
			return this.scannable.isBusy();
		} catch (Throwable e) {
			logger.error("isBusy error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void moveTo(Any arg0) throws CorbaDeviceException {
		try {
			this.scannable.moveTo(arg0.extract_Value());
		} catch (Throwable e) {
			logger.error("moveTo error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void setExtraNames(String[] arg0) {
		this.setExtraNames(arg0);
	}

	@Override
	public void setInputNames(String[] arg0) {
		this.setInputNames(arg0);
	}

	@Override
	public void setLevel(int arg0) {
		this.setLevel(arg0);
	}

	@Override
	public void stop() throws CorbaDeviceException {
		try {
			this.scannable.stop();
		} catch (Throwable e) {
			logger.error("stop error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void waitWhileBusy() throws CorbaDeviceException {
		try {
			this.scannable.waitWhileBusy();
		} catch (Throwable e) {
			logger.error("waitWhileBusy error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void close() throws CorbaDeviceException {
		this.deviceImpl.close();
	}

	@Override
	public Any getAttribute(String arg0) throws CorbaDeviceException {
		return this.deviceImpl.getAttribute(arg0);
	}

	@Override
	public void setAttribute(String arg0, Any arg1) throws CorbaDeviceException {
		this.deviceImpl.setAttribute(arg0, arg1);
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		this.deviceImpl.reconfigure();
	}

	@Override
	public String[] getOutputFormat() {
		return this.scannable.getOutputFormat();
	}

	@Override
	public Any checkPositionValid(Any arg0) throws CorbaDeviceException  {
		try {
			String ret = this.scannable.checkPositionValid(arg0.extract_Value());
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			if (ret == null){
				any.insert_Value(new NullString());
			} else {
				any.insert_Value(ret);
			}
			return any;
		} catch (Exception e) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		try {
			return this.scannable.isAt(arg0.extract_Value());
		} catch (DeviceException e) {
			logger.error("isAt error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void setOutputFormat(String[] arg0) {
		this.scannable.setOutputFormat(arg0);

	}

	@Override
	@SuppressWarnings("deprecation")
	public void atEnd() throws CorbaDeviceException {
		try {
			this.scannable.atEnd();
		} catch (Throwable e) {
			logger.error("atEnd error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void atStart() throws CorbaDeviceException {
		try {
			this.scannable.atStart();
		} catch (Throwable e) {
			logger.error("atStart error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
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
	public void atCommandFailure() throws CorbaDeviceException {
		try {
			scannable.atCommandFailure();
		} catch (DeviceException e) {
			logger.error("atCommandFailure error", e);
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public String toFormattedString() throws CorbaDeviceException {
		return scannable.toFormattedString();
	}

}
