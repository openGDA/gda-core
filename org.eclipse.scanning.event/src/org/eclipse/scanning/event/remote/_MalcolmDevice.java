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
import java.util.List;

import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;

public class _MalcolmDevice<M extends MalcolmModel> extends _RunnableDevice<M> implements IMalcolmDevice<M> {

	// TODO to implement IMalcolmDevice we have to implement loads of methods that don't make sense on the client.
	// We should try to fix this. Some methods probably shouldn't be declared on IMalcolmDevice, e.g setFileDir
	// but on MalcolmDevice, and others, e.g. fire... methods should not be part of the API.

	_MalcolmDevice(DeviceInformation<M> info, URI uri, IEventService eventService) throws EventException {
		super(info, uri, eventService);
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
	public List<String> getAvailableAxes() throws MalcolmDeviceException {
		try {
			updateDeviceInfo();
			return info.getAvailableAxes();
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, e);
		}
	}

	@Override
	public boolean isNewMalcolmVersion() throws MalcolmDeviceException {
		try {
			updateDeviceInfo();
			return info.isNewMalcolm();
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, e);
		}
	}

	private void updateDeviceInfo() throws EventException, InterruptedException {
		DeviceRequest req = new DeviceRequest(name);
		DeviceRequest res = requester.post(req);
		merge((DeviceInformation<M>)res.getDeviceInformation());
	}

	@Override
	public MalcolmTable getDatasets() throws MalcolmDeviceException {
		try {
			DeviceRequest req = new DeviceRequest(name);
			req.setGetDatasets(true);
			DeviceRequest res = requester.post(req);
			merge((DeviceInformation<M>)res.getDeviceInformation());
			return req.getDatasets();
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, e);
		}
	}

	@Override
	public void setPointGenerator(IPointGenerator<?> pointGenerator) {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public void setFileDir(String fileDir) {
		throw new UnsupportedOperationException("This method is not supported on the client");
	}

	@Override
	public String getFileDir() {
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
