/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.rest.device.camera;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.ui.tool.rest.device.DeviceRestServiceClientBase;

/**
 * Client to control a remote NDROI.
 *
 * <p>
 * This client assumes that the remote camera control name is <b>detectorName + "_roi"</b>
 * </p>
 *
 * @author Maurizio Nagni
 */
@Service
public class NDROIClient {

	@Autowired
	private DeviceRestServiceClientBase service;

	public static final String REMOTE_SERVICE = "roi";

	public DeviceValue getMaxSizeXRBV(String adName) throws GDAClientRestException {
		return service.getDeviceValue(adName, REMOTE_SERVICE, "getMaxSizeX_RBV");
	}

	public DeviceValue getMaxSizeYRBV(String adName) throws GDAClientRestException {
		return service.getDeviceValue(adName, REMOTE_SERVICE, "getMaxSizeY_RBV");
	}

	public DeviceValue getMinX(String adName) throws GDAClientRestException {
		return service.getDeviceValue(adName, REMOTE_SERVICE, "getMinX");
	}

	public void setMinX(String adName, double minX) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setMinX", minX);
	}

	public DeviceValue getMinY(String adName) throws GDAClientRestException {
		return service.getDeviceValue(adName, REMOTE_SERVICE, "getMinY");
	}

	public void setMinY(String adName, double minY) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setMinY", minY);
	}

	public DeviceValue getSizeX(String adName) throws GDAClientRestException {
		return service.getDeviceValue(adName, REMOTE_SERVICE, "getSizeX");
	}

	public void setSizeX(String adName, double sizeX) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setSizeX", sizeX);
	}

	public DeviceValue getSizeY(String adName) throws GDAClientRestException {
		return service.getDeviceValue(adName, REMOTE_SERVICE, "getSizeY");
	}

	public void setSizeY(String adName, double sizeY) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setSizeY", sizeY);
	}

	public DeviceValue getIocHasOverlayCentrePvs(String adName) throws GDAClientRestException {
		return service.getDeviceValue(adName, REMOTE_SERVICE, "getIocHasOverlayCentrePvs");
	}

	public void setIocHasOverlayCentrePvs(String adName, boolean iocHasOverlayCentrePvs) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setIocHasOverlayCentrePvs", iocHasOverlayCentrePvs);
	}
}
