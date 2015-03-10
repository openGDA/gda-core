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

package gda.device.detector.ccd;

import fr.esrf.Tango.DevEncoded;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoDs.TangoConst;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.device.detector.DetectorBase;
import gda.factory.FactoryException;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TangoCcd extends DetectorBase implements Detector {
	private static final Logger logger = LoggerFactory.getLogger(TangoCcd.class);

	private TangoDeviceProxy deviceProxy;
	private boolean busy = false;

	@Override
	public void configure() throws FactoryException {
		try {
			deviceProxy.isAvailable();
		} catch (DeviceException e) {
			logger.error(e.getMessage());
		}
		setConfigured(true);
	}

	/**
	 * @return Returns the Tango device proxy.
	 */
	public TangoDeviceProxy getTangoDeviceProxy() {
		return deviceProxy;
	}

	/**
	 * @param deviceProxy The Tango device proxy to set.
	 */
	public void setTangoDeviceProxy(TangoDeviceProxy deviceProxy) {
		this.deviceProxy = deviceProxy;
	}

	@Override
	public boolean isBusy() {
		return busy;
	}

	@Override
	public void collectData() throws DeviceException {
		deviceProxy.isAvailable();
		try {
			busy= true;
			deviceProxy.command_inout("Start");
		} catch (DevFailed e) {
			throw new DeviceException("failed to start data collection " + e.errors[0].desc);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public void endCollection() throws DeviceException {
		deviceProxy.isAvailable();
		try {
			deviceProxy.command_inout("Stop");
			busy = false;
		}
		catch (DevFailed e) {
			throw new DeviceException("failed to stop data collection " + e.errors[0].desc);
		}
	}
	
	@Override
	public int[] getDataDimensions() throws DeviceException {
		int[] dims = new int[2];
		try {
			dims[0] = deviceProxy.read_attribute("Width").extractLong();
			dims[1] = deviceProxy.read_attribute("Height").extractLong();
		}
		catch (DevFailed e) {
			throw new DeviceException("failed get data dimensions " + e.errors[0].desc);
		}
		return dims;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "ccd";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "Abstract";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "TangoCCD";
	}

	@Override
	public int getStatus() throws DeviceException {
		int status;
//		deviceProxy.isAvailable();
		try {
			DevState state = deviceProxy.state();
			switch (state.value()) {
			case DevState._ON:
				status = Detector.BUSY;
				break;
			case DevState._FAULT:
				status = Detector.FAULT;
				break;
			case DevState._OFF:
			default:
				status = Detector.IDLE;
				break;
			}
		} catch (DevFailed e) {
			throw new DeviceException("failed to get ccd state" + e.errors[0].desc);
		}
		return status;
	}

	@Override
	public Object readout() throws DeviceException {
		return read();
//		writeFile();
//		String s[] = (String[]) getAttribute("FileParams");
//		String imageNos = String.format("%04d", Integer.valueOf(s[2]));
//		String fileName = s[0] + "/" + s[1] + imageNos + "." + s[3];
//		return fileName;
	}
	
	@Override
	public void setCollectionTime(double time) {
		try {
			deviceProxy.isAvailable();
			deviceProxy.write_attribute(new DeviceAttribute("Exposure", time));
		} catch (DevFailed e) {
			logger.error("TangoCcd.setCollectionTime failed " + e.errors[0].desc);
		}
		catch (DeviceException e) {
			logger.error(e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		deviceProxy.isAvailable();
		try {
			if ("Exposure".equalsIgnoreCase(attributeName)) {
				deviceProxy.write_attribute(new DeviceAttribute(attributeName, (Double) value));
			} else if ("Trigger".equalsIgnoreCase(attributeName)) {
				deviceProxy.write_attribute(new DeviceAttribute(attributeName, (Short) value));
			} else if ("Frames".equalsIgnoreCase(attributeName)) {
				deviceProxy.write_attribute(new DeviceAttribute(attributeName, (Integer) value));
			} else if ("FileFormat".equalsIgnoreCase(attributeName)) {
				deviceProxy.write_attribute(new DeviceAttribute(attributeName, (String) value));
			} else if ("JpegQuality".equalsIgnoreCase(attributeName)) {
				deviceProxy.write_attribute(new DeviceAttribute(attributeName, (Short) value));
			} else if ("JpegCompression".equalsIgnoreCase(attributeName)) {
				deviceProxy.write_attribute(new DeviceAttribute(attributeName, (Boolean) value));
			} else if ("Roi".equalsIgnoreCase(attributeName)) {
				ArrayList<Integer> s = (ArrayList<Integer>) value;
				int[] ints = new int[s.size()];
				for (int i = 0; i < s.size(); i++)
					ints[i] = s.get(i);
				DeviceAttribute devAttr = new DeviceAttribute(attributeName);
				devAttr.insert(ints);
				deviceProxy.write_attribute(devAttr);
			} else if ("Binning".equalsIgnoreCase(attributeName)) {
				ArrayList<Short> s = (ArrayList<Short>) value;
				short[] shorts = new short[s.size()];
				for (int i = 0; i < s.size(); i++)
					shorts[i] = s.get(i);
				DeviceAttribute devAttr = new DeviceAttribute(attributeName);
				devAttr.insert(shorts);
				deviceProxy.write_attribute(devAttr);
			} else if ("FileParams".equalsIgnoreCase(attributeName)) {
				ArrayList<String> s = (ArrayList<String>) value;
				String[] strings = new String[s.size()];
				for (int i = 0; i < s.size(); i++)
					strings[i] = s.get(i);
				DeviceAttribute devAttr = new DeviceAttribute(attributeName);
				devAttr.insert(strings);
				deviceProxy.write_attribute(devAttr);
			} else if ("WriteFile".equalsIgnoreCase(attributeName)) {
				writeFile();
			}
		} catch (DevFailed e) {
			logger.error("TangoCcd.setAttribute failed for attribute " + attributeName + " " + e.errors[0].desc);
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		deviceProxy.isAvailable();
		try {
			if ("Exposure".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractDouble();
			} else if ("Trigger".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractShort();
			} else if ("Width".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractLong();
			} else if ("Height".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractLong();
			} else if ("Depth".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractShort();
			} else if ("Frames".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractLong();
			} else if ("FileFormat".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractString();
			} else if ("ImageFormat".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractString();
			} else if ("ImageCounter".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractLong();
			} else if ("JpegQuality".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractShort();
			} else if ("JpegCompression".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractBoolean();
			} else if ("Roi".equalsIgnoreCase(attributeName)) {
				int[] iss = deviceProxy.read_attribute(attributeName).extractLongArray();
				return iss;
			} else if ("Binning".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractShortArray();
			} else if ("FileParams".equalsIgnoreCase(attributeName)) {
				String[] s = deviceProxy.read_attribute(attributeName).extractStringArray();
				return s;
			} else if ("JpegImage".equalsIgnoreCase(attributeName)) {
				return deviceProxy.read_attribute(attributeName).extractUCharArray();
			}
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
		}
		return null;
	}
	
	public void writeFile()
	{
		try {
			deviceProxy.command_inout("WriteFile");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
		}
	}
	
	public byte[] read() throws DeviceException {
		byte[] byteData = null;	
		try {
			deviceProxy.isAvailable();
			DeviceAttribute attr = deviceProxy.read_attribute("Image");
			if (TangoConst.Tango_DEV_ENCODED == attr.getType()) {
				DevEncoded encoded = attr.extractDevEncoded();
				byteData = encoded.encoded_data;
			} else if (TangoConst.Tango_DEV_UCHAR == attr.getType()) {
				byteData = attr.extractCharArray();
			}
		} catch (DevFailed e) {
			logger.error("TangoCcd.readout failed " + e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
		return byteData;		
	}
}
