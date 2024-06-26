/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.event.remote;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.MalcolmVersion;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;

public class MalcolmDeviceProxy extends RunnableDeviceProxy<IMalcolmModel> implements IMalcolmDevice {

	private static final long MALCOLM_TIMEOUT_MS = Duration.ofSeconds(5).toMillis();

	// TODO to implement IMalcolmDevice we have to implement loads of methods that don't make sense on the client.
	// We should try to fix this. Some methods probably shouldn't be declared on IMalcolmDevice, e.g setFileDir
	// but on MalcolmDevice, and others, e.g. fire... methods should not be part of the API.

	MalcolmDeviceProxy(DeviceInformation<IMalcolmModel> info, URI uri, IEventService eventService) throws EventException {
		super(info, MALCOLM_TIMEOUT_MS, uri, eventService); // use a longer timeout as the server needs to communicate over EPICS with the actual malcolm device
		if (info.getDeviceRole() != DeviceRole.MALCOLM)
			throw new IllegalArgumentException("Not a malcolm device: " + info.getName());
	}

	@Override
	public void addRunListener(IRunListener l) throws ScanningException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void removeRunListener(IRunListener l) throws ScanningException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void fireRunWillPerform(IPosition position) throws ScanningException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void fireRunPerformed(IPosition position) throws ScanningException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void fireWriteWillPerform(IPosition position) throws ScanningException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void fireWritePerformed(IPosition position) throws ScanningException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void pause() throws ScanningException, InterruptedException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void seek(int stepNumber) throws ScanningException, InterruptedException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void resume() throws ScanningException, InterruptedException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void initialize() throws MalcolmDeviceException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public boolean isLocked() throws MalcolmDeviceException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public MalcolmVersion getVersion() throws MalcolmDeviceException {
		try {
			updateDeviceInfo();
			return info.getMalcolmVersion();
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, e);
		}
	}

	@Override
	public List<String> getAvailableAxes() throws MalcolmDeviceException {
		try {
			updateDeviceInfo();
			return info.getAvailableAxes();
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, e);
		}
	}

	@Override
	public List<String> getConfiguredAxes() throws ScanningException {
		try {
			updateDeviceInfo();
			return info.getConfiguredAxes();
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, e);
		}
	}

	@Override
	public List<MalcolmDetectorInfo> getDetectorInfos() throws MalcolmDeviceException {
		try {
			updateDeviceInfo();
			return info.getMalcolmDetectorInfos();
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, e);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateDeviceInfo() throws EventException, InterruptedException {
		DeviceRequest request = new DeviceRequest(name);
		DeviceRequest response = requester.post(request);
		merge((DeviceInformation<IMalcolmModel>) response.getDeviceInformation());
	}

	@SuppressWarnings("unchecked")
	@Override
	public MalcolmTable getDatasets() throws MalcolmDeviceException {
		try {
			DeviceRequest request = new DeviceRequest(name);
			request.setGetDatasets(true);
			DeviceRequest response = requester.post(request);
			merge((DeviceInformation<IMalcolmModel>) response.getDeviceInformation());
			return request.getDatasets();
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, e);
		}
	}

	@Override
	public void setPointGenerator(IPointGenerator<? extends IScanPointGeneratorModel> pointGenerator) {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public IPointGenerator<? extends IScanPointGeneratorModel> getPointGenerator() {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void setOutputDir(String fileDir) {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public String getOutputDir() {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void addMalcolmListener(IMalcolmEventListener listener) {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void removeMalcolmListener(IMalcolmEventListener listener) {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

}
