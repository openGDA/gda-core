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

package uk.ac.gda.devices.excalibur.impl;

import gda.device.detector.areadetector.v17.NDPluginBase;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.devices.excalibur.Fix;

/**
 *
 */
public class ConfigFixImpl extends BasePvProvidingImpl implements Fix {

	private static final String STRIPE_MAX_RBV = "StripeMax_RBV";
	private static final String STRIPE_MIN_RBV = "StripeMin_RBV";
	private static final String STRIPE_SUM_RBV = "StripeSum_RBV";
	private static final String SCALE_EDGE_PIXELS = "ScaleEdgePixels";
	private static final String ENABLE_STATISTICS = "EnableStatistics";
	private NDPluginBase pluginBase;

	@Override
	public NDPluginBase getPluginBase() {
		return pluginBase;
	}

	public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

	@Override
	public void enableStatistics() throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(ENABLE_STATISTICS), 1);
	}

	@Override
	public void disableStatistics() throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(ENABLE_STATISTICS), 0);
	}

	@Override
	public boolean isStatisticsEnabled() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ENABLE_STATISTICS)) == 1;
	}

	@Override
	public void enableScaleEdgePixels() throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(SCALE_EDGE_PIXELS), 1);
	}

	@Override
	public void disableScaleEdgePixels() throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(SCALE_EDGE_PIXELS), 0);
	}

	@Override
	public boolean isScaleEdgePixelsEnabled() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(SCALE_EDGE_PIXELS)) == 1;
	}

	@Override
	public double getStripeSum_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(STRIPE_SUM_RBV));
	}

	@Override
	public double getStripeMin_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(STRIPE_MIN_RBV));
	}

	@Override
	public double getStripeMax_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(STRIPE_MAX_RBV));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (pluginBase == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be provided");
		}
	}
}
