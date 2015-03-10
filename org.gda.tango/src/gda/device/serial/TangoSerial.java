/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.serial;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.DeviceData;
import gda.device.DeviceException;
import gda.device.Serial;
import gda.device.TangoDeviceProxy;
import gda.factory.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class TangoSerial extends SerialBase implements Serial, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(TangoSerial.class);
	private TangoDeviceProxy tangoDeviceProxy;

	//private static final short SL_RAW = 0;  	// raw read/write mode
	//private static final short SL_NCHAR = 1;	// character read/write mode
	private static final short SL_LINE = 2; 	// line read mode
	//private static final short SL_RETRY = 3;	// retry read mode

	private static final short SL_NONE = 0;
	private static final short SL_ODD  = 1;
	private static final short SL_EVEN = 3;

	private static final short SL_DATA8 = 0;
	private static final short SL_DATA7 = 1;
	private static final short SL_DATA6 = 2;
	private static final short SL_DATA5 = 3;

	private static final short SL_STOP1  = 0;
	private static final short SL_STOP15 = 1;
	private static final short SL_STOP2  = 2;
	
	public TangoDeviceProxy getTangoDeviceProxy() {
		return tangoDeviceProxy;
	}

	public void setTangoDeviceProxy(TangoDeviceProxy dev) {
		this.tangoDeviceProxy = dev;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (tangoDeviceProxy == null) {
			throw new IllegalArgumentException("tango device proxy needs to be set");
		}
	}

	@Override
	public void setBaudRate(int baudRate) throws DeviceException {
		DeviceData argin;
		try {
			argin = new DeviceData();
			argin.insert_ul(baudRate);
			tangoDeviceProxy.command_inout("DevSerSetBaudrate", argin);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void setByteSize(int byteSize) throws DeviceException {
		DeviceData argin;
		short bsize;
		if (byteSize == Serial.BYTESIZE_5) {
			bsize = SL_DATA5;
		} else if (byteSize == Serial.BYTESIZE_6) {
			bsize = SL_DATA6;
		} else if (byteSize == Serial.BYTESIZE_7) {
			bsize = SL_DATA7;
		} else if (byteSize == Serial.BYTESIZE_8) {
			bsize = SL_DATA8;
		} else {
			throw new DeviceException("Unsupported ByteSize: Using default");
		}
		try {
			argin = new DeviceData();
			argin.insert(bsize);
			tangoDeviceProxy.command_inout("DevSerSetCharLength", argin);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void setParity(String parity) throws DeviceException {
		short sparity;
		if (Serial.PARITY_NONE.equals(parity)) {
			sparity = SL_NONE;
		} else if (Serial.PARITY_ODD.equals(parity)) {
			sparity = SL_ODD;
		} else if (Serial.PARITY_EVEN.equals(parity)) {
			sparity = SL_EVEN;
		} else {
			throw new DeviceException("Unsupported Parity: Using default");
		}
		DeviceData argin;
		try {
			argin = new DeviceData();
			argin.insert(sparity);
			tangoDeviceProxy.command_inout("DevSerSetParity", argin);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void setStopBits(int stopBits) throws DeviceException {
		DeviceData argin;
		short stops;
		if (stopBits == Serial.STOPBITS_1) {
			stops = SL_STOP1;
		} else if (stopBits == Serial.STOPBITS_1_5) {
			stops = SL_STOP15;
			
		} else if (stopBits == Serial.STOPBITS_2) {
			stops = SL_STOP2;
		} else {
			throw new DeviceException("Unsupported stop bits: Using default");
		}
		try {
			argin = new DeviceData();
			argin.insert(stops);
			tangoDeviceProxy.command_inout("DevSerSetStopbit", argin);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public int getReadTimeout() throws DeviceException {
		return 0;
	}

	@Override
	public void setFlowControl(String flowControl) throws DeviceException {
		logger.error("Not implemented");
	}

	@Override
	public char readChar() throws DeviceException {
		DeviceData argout;
		DeviceData argin;
		int nchar = 1;
		try {
			argin = new DeviceData();
			argin.insert(nchar);
			argout = tangoDeviceProxy.command_inout("DevSerReadNChar", argin);
			return argout.extractString().charAt(0);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void flush() throws DeviceException {
		DeviceData argin;
		try {
			argin = new DeviceData();
			argin.insert(2);// flush input and output streams
			tangoDeviceProxy.command_inout("DevSerFlush", argin);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void setReadTimeout(int time) throws DeviceException {
		DeviceData argin;
		try {
			argin = new DeviceData();
			argin.insert((short)time);
			tangoDeviceProxy.command_inout("DevSerSetTimeout",argin);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	public void setNewLine(int code) throws DeviceException {
		DeviceData argin;
		try {
			argin = new DeviceData();
			argin.insert((short)code);
			tangoDeviceProxy.command_inout("DevSerSetNewline",argin);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void writeChar(char c) throws DeviceException {
		byte[] args = new byte[1];
		DeviceData argin;
		DeviceData argout;
		try {
			args[0] = (byte) c;
			argin = new DeviceData();
			argin.insert(args);
			argout = tangoDeviceProxy.command_inout("DevSerWriteChar", argin);
			if (argout.extractLong() != 1) {
				DeviceException ex = new DeviceException("Failed to send character");
				logger.error(ex.getMessage());
				throw ex;
			}
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void configure() throws FactoryException {
		configured = true;
	}
	
	public String readString() throws DeviceException {
		try {
			return tangoDeviceProxy.command_inout("DevSerReadRaw").extractString();
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}
	public void writeString(String s) throws DeviceException {
		DeviceData argin;
		try {
			argin = new DeviceData();
			argin.insert(s);
			tangoDeviceProxy.command_inout("DevSerWriteString", argin);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	public String writeReadString(String s) throws DeviceException {
		DeviceData argin;
		try {
			short newline = 0xd;
			argin = new DeviceData();
			argin.insert(newline);
			tangoDeviceProxy.command_inout("DevSerSetNewline", argin);
//			short time = 2048;
//			argin = new DeviceData();
//			argin.insert(time);
//			tangoDeviceProxy.command_inout("DevSerSetTimeout",argin);

			int lvalue[]  = new int[1];
			lvalue[0] = SL_LINE;
			String svalue[] = new String[1];
			svalue[0] = s;
			DevVarLongStringArray longStringArr = new DevVarLongStringArray(lvalue, svalue);
			argin = new DeviceData();
			argin.insert(longStringArr);
			return tangoDeviceProxy.command_inout("WriteRead", argin).extractString();
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

}
