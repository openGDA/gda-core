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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.excalibur.Gap;

/**
 *
 */
public class GapImpl extends BasePvProvidingImpl implements Gap {
	private static final String CLEAR_ROWS_RECEIVED = "ClearRowsReceived";

	private static final String ENABLE_GAP_FILLING = "EnableGapFilling";

	private static final String GAP_FILL_MODE = "GapFillMode";

	private static final String DROPPED_FRAME_OLDER = "DroppedFrameOlder";

	private static final String ADJACENT_OVERRUN = "AdjacentOverrun";

	private static final String FRAME_OVERRUN = "FrameOverrun";

	private static final String GAP_FILL_CONSTANT_RBV = "GapFillConstant_RBV";

	private static final String GAP_FILL_CONSTANT = "GapFillConstant";

	private static final String DROPPED_ADJACENT_OLDER = "DroppedAdjacentOlder";

	private static final String ADJACENT_ROWS_RECEIVED = "AdjacentRowsReceived";

	private static final String ADJACENT_PORT = "AdjacentPort";

	private static final String ADJACENT_ADDRESS = "AdjacentAddress";

	private static final Logger logger = LoggerFactory.getLogger(GapImpl.class);

	private NDPluginBase pluginBase;

	@Override
	public NDPluginBase getPluginBase() {
		return pluginBase;
	}

	public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

	@Override
	public String getAdjacentAddress() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.caget(getChannel(ADJACENT_ADDRESS));
	}

	@Override
	public int getAdjacentPort() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ADJACENT_PORT));
	}

	@Override
	public boolean isAdjacentConnected() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetShort(getChannel("AdjacentConnected")) == 1;
	}

	@Override
	public int getAdjacentRowsReceived() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ADJACENT_ROWS_RECEIVED));
	}

	@Override
	public int getDroppedAdjacentOlder() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DROPPED_ADJACENT_OLDER));
	}

	@Override
	public int getDroppedFrameOlder() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DROPPED_FRAME_OLDER));
	}

	@Override
	public int getAdjacentOverrun() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ADJACENT_OVERRUN));
	}

	@Override
	public int getFrameOverrun() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FRAME_OVERRUN));
	}

	@Override
	public void clearRowsReceived() throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(CLEAR_ROWS_RECEIVED), 1);
	}

	@Override
	public void enableGapFilling() throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(ENABLE_GAP_FILLING), 1);
	}

	@Override
	public void disableGapFilling() throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(ENABLE_GAP_FILLING), 0);
	}

	@Override
	public boolean isGapFillingEnabled() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(ENABLE_GAP_FILLING)) == 1;
	}

	@Override
	public void setGapFillMode(int mode) throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(GAP_FILL_MODE), mode);
	}

	@Override
	public int getGapFillMode() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(GAP_FILL_MODE));
	}

	@Override
	public void setGapFillConstant(int gapFillConstant) throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(GAP_FILL_CONSTANT), gapFillConstant);
	}

	@Override
	public int getGapFillConstant() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(GAP_FILL_CONSTANT));
	}

	@Override
	public int getGapFillConstant_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(GAP_FILL_CONSTANT_RBV));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (pluginBase == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be provided");
		}
	}
}
