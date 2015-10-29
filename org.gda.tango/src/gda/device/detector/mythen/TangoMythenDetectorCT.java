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

package gda.device.detector.mythen;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.detector.pilatus.TangoLimaDetector;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Mythen counter timer detectors interface.
 */
@CorbaAdapterClass(DetectorAdapter.class)
@CorbaImplClass(DetectorImpl.class)
public class TangoMythenDetectorCT extends TangoLimaDetector implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(TangoMythenDetectorCT.class);
	private TangoDeviceProxy tangoDeviceProxy = null;
	private TangoDeviceProxy tangoROICounterProxy = null;
	private int counterValues;

	public TangoMythenDetectorCT() {
	}

	@Override
	public void configure() throws FactoryException {
		try {
			super.configure();
			// if (isConfigured()) {
//			tangoDeviceProxy.isAvailable();
//			width = getWidth();
			setConfigured(true);
			setInputNames(null);
		} catch (Exception e) {
			setConfigured(false);
			logger.error("TangoMythenDetector {} configure: {}", getName(), e.getMessage());
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (tangoDeviceProxy == null) {
			throw new IllegalArgumentException("tango mythen device proxy needs to be set");
		}
		if (tangoROICounterProxy == null) {
			throw new IllegalArgumentException("tango ROI device proxy needs to be set");
		}
	}

	public TangoDeviceProxy getTangoDeviceProxy() {
		return tangoDeviceProxy;
	}

	public void setTangoDeviceProxy(TangoDeviceProxy tangoDeviceProxy) {
		this.tangoDeviceProxy = tangoDeviceProxy;
	}

	public TangoDeviceProxy getTangoROICounterProxy() {
		return tangoROICounterProxy;
	}

	public void setTangoROICounterProxy(TangoDeviceProxy tangoROICounterProxy) {
		this.tangoROICounterProxy = tangoROICounterProxy;
	}

	public int getCounterValues() {
		return counterValues;
	}

	public void setCounterValues(int counterValues) {
		this.counterValues = counterValues;
	}

	@Override
	public void collectData() throws DeviceException {
		writeNbFrames(1);
	}

	@Override
	public Object readout() throws DeviceException {
		// read counters returns [0] = roi_id,
		//                       [1] frameNumber,
		//                       [2] sum,
		//                       [3] average,
		//                       [4] std,
		//                       [5] minValue,
		//                       [6] maxValue
		double data[] = new double[counterValues];
		try {
			DeviceData argin = new DeviceData();
			argin.insert(0); // ROI 0 is the whole detector.
			DeviceData argout = tangoROICounterProxy.command_inout("readCounters", argin);
			float[] floatData = argout.extractFloatArray();
			// for now we don't need the first 2 values
			for (int i=0; i<counterValues; i++) {
				data[i] = floatData[i+2];
				logger.debug("ROI value " + i + " is " + data[i]);
			}
		} catch (DevFailed e) {
			logger.error("failed to read ROI counters", e.getMessage());
			throw new DeviceException(e);
		}
		return data;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}
}
