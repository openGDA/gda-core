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

package uk.ac.gda.epics.client.views.model.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.NDArray;
import uk.ac.gda.epics.client.views.model.NdArrayModel;

/**
 *
 */
public class NDArrayModelImpl extends EPICSBaseModel implements NdArrayModel {
	private final static Logger logger = LoggerFactory.getLogger(NDArrayModelImpl.class);

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected void doCheckAfterPropertiesSet() throws Exception {
		// nothing to do
	}

	@Override
	public int[] getArrayData() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetIntArray(getChannel(NDArray.ARRAY_DATA, null));
		} catch (Exception ex) {
			logger.warn("problem with g.d.d.a.v.i.NDArrayImpl ->getArrayData()", ex);
			throw ex;
		}
	}

	@Override
	public int[] getArrayData(int numberOfElements) throws Exception {
		try {
			return EPICS_CONTROLLER.cagetIntArray(getChannel(NDArray.ARRAY_DATA, null), numberOfElements);
		} catch (Exception ex) {
			logger.warn("problem with g.d.d.a.v.i.NDArrayImpl ->getArrayData(int numberOfElements)", ex);
			throw ex;
		}
	}

	@Override
	public byte[] getByteArrayData(int numberOfElements) throws Exception {
		try {
			return EPICS_CONTROLLER.cagetByteArray(getChannel(NDArray.ARRAY_DATA, null), numberOfElements);
		} catch (Exception ex) {
			logger.warn("problem with g.d.d.a.v.i.NDArrayImpl ->getByteArrayData()", ex);
			throw ex;
		}
	}

	@Override
	public short[] getShortArray(int numberOfElements) throws Exception {
		try {
			return EPICS_CONTROLLER.cagetShortArray(getChannel(NDArray.ARRAY_DATA, null), numberOfElements);
		} catch (Exception ex) {
			logger.warn("problem with g.d.d.a.v.i.NDArrayImpl ->getByteArrayData()", ex);
			throw ex;
		}
	}

	@Override
	public short[] getShortArray() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetShortArray(getChannel(NDArray.ARRAY_DATA, null));
		} catch (Exception ex) {
			logger.warn("problem with g.d.d.a.v.i.NDArrayImpl ->getByteArrayData()", ex);
			throw ex;
		}
	}
}
