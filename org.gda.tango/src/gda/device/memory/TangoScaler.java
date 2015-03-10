/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Memory;
import gda.device.TangoDeviceProxy;
import gda.factory.FactoryException;

public class TangoScaler extends DeviceBase implements Memory, InitializingBean{
	private static final Logger logger = LoggerFactory.getLogger(TangoScaler.class);
	private TangoDeviceProxy tangoDeviceProxy;
	private int width = 1;
	private int height = 1;
	private int totalFrames;
	private int[] supportedDimensions = new int[] {};
	private boolean transposed = false;

	@Override
	public void configure() throws FactoryException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.write_attribute(new DeviceAttribute("transposed", transposed));
		} catch (DevFailed e) {
			throw new FactoryException("failed to set transposed", e);
		} catch(DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (tangoDeviceProxy == null) {
			throw new IllegalArgumentException("scaler tango device proxy needs to be set");
		}
	}

	public TangoDeviceProxy getTangoDeviceProxy() {
		return tangoDeviceProxy;
	}

	public void setTangoDeviceProxy(TangoDeviceProxy tangoDeviceProxy) {
		this.tangoDeviceProxy = tangoDeviceProxy;
	}

	public boolean getTransposed() {
		return transposed;
	}

	public void setTransposed(boolean transposed) {
		this.transposed = transposed;
	}

	@Override
	public void clear() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("clear");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void clear(int start, int count) throws DeviceException {
		clear();
	}

	@Override
	public void clear(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		clear();
	}

	@Override
	public void start() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("enable");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("disable");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public double[] read(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		double[] values = null;
		int[] ivals = null;

		try {
			int[] argin = new int[6];
			argin[0] = x;
			argin[1] = y;
			argin[2] = t;
			argin[3] = dx;
			argin[4] = dy;
			argin[5] = dt;
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.isAvailable();
			DeviceData argout = tangoDeviceProxy.command_inout("read", args);
			ivals = argout.extractLongArray();
			values = new double[ivals.length];
			for (int i = 0; i<ivals.length; i++) {
				values[i] = ivals[i];
			}
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
		return values;
	}

	@Override
	public double[] read(int frame) throws DeviceException {
		return read(frame,0,0,1,1,totalFrames);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) throws DeviceException {
		int[] dims = { width, height };
		setDimension(dims);
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) throws DeviceException {
		int[] dims = { width, height };
		setDimension(dims);
	}
	@Override
	public void setDimension(int[] d) throws DeviceException {
		width = d[0];
		height = d[1];
	}

	@Override
	public int[] getDimension() throws DeviceException {
		int dims[] = {width, height};
		return dims;
	}

	public void setSupportedDimensions(int[] d) {
		this.supportedDimensions = d;
	}

	@Override
	public int[] getSupportedDimensions() {
		return supportedDimensions;
	}

	@Override
	public void write(double[] data, int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		// not implemented
		
	}

	@Override
	public void write(double[] data, int frame) throws DeviceException {
		// not implemented
	}

	@Override
	public void output(String file) throws DeviceException {
		// not implemented
	}

	@Override
	public int getMemorySize() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setAttribute(String attributeName, Object value) {
		if ("TotalFrames".equals(attributeName)) {
			totalFrames = ((Integer) value).intValue();
		}
	}

	@Override
	public Object getAttribute(String attributeName) {
		if ("TotalFrames".equals(attributeName)) {
			return totalFrames;
		}
		return null;
	}
}
