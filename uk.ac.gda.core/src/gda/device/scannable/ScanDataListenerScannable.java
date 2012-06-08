/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;

/*
 * Abstract class to help with creation of scannables that want to handle scan data points Derived classes simply
 * provide an implementation of handleScanDataPoint and handleScanEnd There are also protected methods that classes may
 * find useful to extract data from ScanDataPoints Overrides of atScanStart or atScanEnd must call on the base class
 * implementation
 */
public abstract class ScanDataListenerScannable extends ScanEventHandlerScannable implements IScanDataPointObserver {

	@Override
	public void atScanEnd() throws DeviceException {
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);
		try {
			handleScanEnd();
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("Error handling scan end", e);
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
	}

	abstract public void handleScanDataPoint(IScanDataPoint sdp) throws Exception;

	abstract public void handleScanEnd() throws Exception;

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof IScanDataPoint)
			try {
				handleScanDataPoint((IScanDataPoint) arg);
			} catch (Exception e) {
				logger.error("Error handling scanDataPoint", e);
			}
	}

	/*
	 * Useful functions that derived classes may want
	 */
	protected int getPositionOfScannable(String columnName, IScanDataPoint sdp) {
		return org.apache.commons.lang.ArrayUtils.indexOf(sdp.getScannableHeader(), columnName);
	}

	protected int getPositionOfDetector(String columnName, IScanDataPoint sdp) {
		Object[] headers = sdp.getDetectorHeader().toArray();
		return org.apache.commons.lang.ArrayUtils.indexOf(headers, columnName);
	}

	protected int[] getScanDimensions(IScanDataPoint sdp) {
		return sdp.getScanDimensions();
	}
}
