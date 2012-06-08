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
import uk.ac.gda.devices.excalibur.Gap;

/**
 *
 */
public class DummyGapImpl  implements Gap, InitializingBean{

	private NDPluginBase pluginBase;

	private String adjacentAddress="adjacentAddress";

	private int adjacentPort;

	private int adjacentConnected;

	private int adjacentRowsReceived;

	private int droppedAdjacentOlder;

	private int droppedFrameOlder;

	private int adjacentOverrun;

	private int frameOverrun;

	private int rowsRecieved;

	private int gapFilling;

	private int gapFillMode;

	private int gapFillConstant;

	private int gapFillConstant_RBV;

	@Override
	public NDPluginBase getPluginBase() {
		return pluginBase;
	}

	public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

	@Override
	public String getAdjacentAddress() throws TimeoutException, CAException, InterruptedException, Exception {
		return adjacentAddress;
	}

	@Override
	public int getAdjacentPort() throws TimeoutException, CAException, InterruptedException, Exception {
		return adjacentPort;
	}

	@Override
	public boolean isAdjacentConnected() throws TimeoutException, CAException, InterruptedException, Exception {
		return adjacentConnected == 1;
	}

	@Override
	public int getAdjacentRowsReceived() throws TimeoutException, CAException, InterruptedException, Exception {
		return adjacentRowsReceived;
	}

	@Override
	public int getDroppedAdjacentOlder() throws TimeoutException, CAException, InterruptedException, Exception {
		return droppedAdjacentOlder;
	}

	@Override
	public int getDroppedFrameOlder() throws TimeoutException, CAException, InterruptedException, Exception {
		return droppedFrameOlder;
	}
	
	@Override
	public int getAdjacentOverrun() throws TimeoutException, CAException, InterruptedException, Exception {
		return adjacentOverrun;
	}

	@Override
	public int getFrameOverrun() throws TimeoutException, CAException, InterruptedException, Exception {
		return frameOverrun;
	}

	@Override
	public void clearRowsReceived() throws CAException, InterruptedException, Exception {
		rowsRecieved=1;
	}

	@Override
	public void enableGapFilling() throws CAException, InterruptedException, Exception {
		gapFilling = 1;
	}

	@Override
	public void disableGapFilling() throws CAException, InterruptedException, Exception {
		gapFilling = 0;
	}

	@Override
	public boolean isGapFillingEnabled() throws TimeoutException, CAException, InterruptedException, Exception {
		return gapFilling == 1;
	}

	@Override
	public void setGapFillMode(int mode) throws CAException, InterruptedException, Exception {
		gapFillMode = mode;
	}

	@Override
	public int getGapFillMode() throws TimeoutException, CAException, InterruptedException, Exception {
		return gapFillMode;
	}

	@Override
	public void setGapFillConstant(int gapFillConstant) throws CAException, InterruptedException, Exception {
		this.gapFillConstant = gapFillConstant;
	}

	@Override
	public int getGapFillConstant() throws TimeoutException, CAException, InterruptedException, Exception {
		return gapFillConstant;
	}

	@Override
	public int getGapFillConstant_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return gapFillConstant_RBV;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (pluginBase == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be provided");
		}
	}
}
