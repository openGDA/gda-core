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

import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.v17.NDPluginBase;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.devices.excalibur.Fix;

/**
 *
 */
public class DummyConfigFixImpl implements Fix, InitializingBean{

	private NDPluginBase pluginBase;
	private int enableStats;
	private int enableScaleEdgePixels;
	private double stripeSum_RBV;
	private double stripeMax_RBV;
	private double stripeMin_RBV;

	@Override
	public NDPluginBase getPluginBase() {
		return pluginBase;
	}

	public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

	@Override
	public void enableStatistics() throws CAException, InterruptedException, Exception {
		enableStats=1;
	}

	@Override
	public void disableStatistics() throws CAException, InterruptedException, Exception {
		enableStats=0;
	}

	@Override
	public boolean isStatisticsEnabled() throws TimeoutException, CAException, InterruptedException, Exception {
		return enableStats == 1;
	}

	@Override
	public void enableScaleEdgePixels() throws CAException, InterruptedException, Exception {
		enableScaleEdgePixels=1;
	}

	@Override
	public void disableScaleEdgePixels() throws CAException, InterruptedException, Exception {
		enableScaleEdgePixels=0;
	}

	@Override
	public boolean isScaleEdgePixelsEnabled() throws TimeoutException, CAException, InterruptedException, Exception {
		return enableScaleEdgePixels == 1;
	}

	@Override
	public double getStripeSum_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return stripeSum_RBV;
	}

	@Override
	public double getStripeMin_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return stripeMin_RBV;
	}

	@Override
	public double getStripeMax_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return stripeMax_RBV;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (pluginBase == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be provided");
		}
	}
}
