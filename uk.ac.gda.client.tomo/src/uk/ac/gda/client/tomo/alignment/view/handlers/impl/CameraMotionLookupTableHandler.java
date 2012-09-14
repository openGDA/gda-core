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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.device.DeviceException;
import gda.function.Lookup;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraMotionLookupTableHandler;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public class CameraMotionLookupTableHandler implements ICameraMotionLookupTableHandler {

	private Lookup cameraMotionLookup;
	private static final Logger logger = LoggerFactory.getLogger(CameraMotionLookupTableHandler.class);

	//
	private static final String MODULE_4_T3_1M1Y = "m4_t3_m1y";
	private static final String MODULE_3_T3_1M1Y = "m3_t3_m1y";
	private static final String MODULE_2_T3_1M1Y = "m2_t3_m1y";
	private static final String MODULE_1_T3_1M1Y = "m1_t3_m1y";
	private static final String MODULE_4_T3_X = "m4_t3_x";
	private static final String MODULE_3_T3_X = "m3_t3_x";
	private static final String MODULE_2_T3_X = "m2_t3_x";
	private static final String MODULE_1_T3_X = "m1_t3_x";

	public void setCameraMotionLookup(Lookup cameraMotionLookup) {
		this.cameraMotionLookup = cameraMotionLookup;
	}

	@Override
	public double lookupT3X(CAMERA_MODULE module, double t3m1zValue) throws IllegalArgumentException, DeviceException {

		double[] lookupKeys = cameraMotionLookup.getLookupKeys();
		t3m1zValue = MathUtils.round(t3m1zValue, 1);
		boolean containsT3M1zValue = ArrayUtils.contains(lookupKeys, t3m1zValue);
		if (containsT3M1zValue) {
			return getT3xLookupValue(module, t3m1zValue);
		}
		// using interpolation

		int binarySearch = Arrays.binarySearch(lookupKeys, t3m1zValue);
		int index = Math.abs(binarySearch);

		if (index >= 2) {
			double z0 = lookupKeys[index - 2];
			double z1 = lookupKeys[index - 1];

			double x0 = getT3xLookupValue(module, z0);
			double x1 = getT3xLookupValue(module, z1);

			// x = x0 +[(x1-x0)*(z-z0)/(z1-z0)]
			double x = x0 + ((x1 - x0) * (t3m1zValue - z0) / (z1 - z0));
			logger.debug("x value as interpolated:{}", x);
			return x;
		}

		throw new IllegalArgumentException("Cannot find lookupvalue for " + t3m1zValue);
	}

	private double getT3xLookupValue(CAMERA_MODULE module, double t3m1zValue) throws IllegalArgumentException,
			DeviceException {
		switch (module.getValue()) {
		case 1:
			return cameraMotionLookup.lookupValue(t3m1zValue, MODULE_1_T3_X);
		case 2:
			return cameraMotionLookup.lookupValue(t3m1zValue, MODULE_2_T3_X);
		case 3:
			return cameraMotionLookup.lookupValue(t3m1zValue, MODULE_3_T3_X);
		case 4:
			return cameraMotionLookup.lookupValue(t3m1zValue, MODULE_4_T3_X);
		}
		throw new IllegalArgumentException("Cannot find lookupvalue for " + t3m1zValue);
	}

	@Override
	public double lookupT3M1Y(CAMERA_MODULE module, double t3m1z) throws IllegalArgumentException, DeviceException {
		double[] lookupKeys = cameraMotionLookup.getLookupKeys();
		double z = MathUtils.round(t3m1z, 1);
		boolean containsT3M1zValue = ArrayUtils.contains(lookupKeys, z);
		if (containsT3M1zValue) {
			return getT3m1yLookupValue(module, z);
		}
		int binarySearch = Arrays.binarySearch(lookupKeys, z);
		int index = Math.abs(binarySearch);

		if (index >= 2) {
			double z0 = lookupKeys[index - 2];
			double z1 = lookupKeys[index - 1];

			double y0 = getT3m1yLookupValue(module, z0);
			double y1 = getT3m1yLookupValue(module, z1);

			// x = x0 +[(x1-x0)*(z-z0)/(z1-z0)]
			double y = y0 + ((y1 - y0) * (z - z0) / (z1 - z0));
			logger.debug("y value as interpolated:{}", y);
			return y;
		}

		throw new IllegalArgumentException("Cannot find lookupvalue for " + t3m1z);
	}

	private double getT3m1yLookupValue(CAMERA_MODULE module, double t3m1zValue) throws IllegalArgumentException,
			DeviceException {
		switch (module.getValue()) {
		case 1:
			return cameraMotionLookup.lookupValue(t3m1zValue, MODULE_1_T3_1M1Y);
		case 2:
			return cameraMotionLookup.lookupValue(t3m1zValue, MODULE_2_T3_1M1Y);
		case 3:
			return cameraMotionLookup.lookupValue(t3m1zValue, MODULE_3_T3_1M1Y);
		case 4:
			return cameraMotionLookup.lookupValue(t3m1zValue, MODULE_4_T3_1M1Y);
		}
		throw new IllegalArgumentException("Cannot find lookupvalue for " + t3m1zValue);
	}

	@Override
	public double[] getT3M1zValues() throws DeviceException {
		return cameraMotionLookup.getLookupKeys();
	}
}
