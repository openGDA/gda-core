/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import static gda.device.detector.mythen.client.Trigger.SINGLE;

import java.util.concurrent.FutureTask;

import gda.device.DeviceException;
import gda.device.detector.mythen.client.AcquisitionParameters;
import gda.device.detector.mythen.tasks.ScanTask;

public class ShutterControlledMythenDetectorImpl extends MythenDetectorImpl {

	private ScanTask openShutterTask;
	
	private ScanTask closeShutterTask;
	
	public void setOpenShutterTask(ScanTask openShutterTask) {
		this.openShutterTask = openShutterTask;
	}

	public void setCloseShutterTask(ScanTask closeShutterTask) {
		this.closeShutterTask = closeShutterTask;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		
		if (openShutterTask == null) {
			throw new IllegalStateException("You have not set the task that will open the shutter");
		}
		
		if (closeShutterTask == null) {
			throw new IllegalStateException("You have not set the task that will close the shutter");
		}
	}

	@Override
	public void collectData() throws DeviceException {
		beforeCollectData();
		
		// Invoke the client; data collection will not actually begin until the shutter
		// has been opened
		FutureTask<Void> collectionTask = new FutureTask<Void>(new Runnable() {
			@Override
			public void run() {
				try {
					AcquisitionParameters params = new AcquisitionParameters.Builder()
					.filename(rawFile.getAbsolutePath())
					.frames(1)
					.exposureTime(exposureTime)
					.trigger(SINGLE)
					.build();
					mythenClient.acquire(params);
				} catch (DeviceException e) {
					throw new RuntimeException("Unable to collect data", e);
				}
			}
		}, null);
		new Thread(collectionTask).start();
		
		// Give the client a while to start
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			throw new DeviceException("Could not collect data", e);
		}
		
		// Open the shutter
		openShutterTask.run();
		
		// Wait for Mythen client to finish
		try {
			collectionTask.get();
		} catch (Exception e) {
			throw new DeviceException("Unable to collect data", e);
		}
		
		// Close the shutter
		closeShutterTask.run();
		
		afterCollectData();
	}

	@Override
	public void stop() throws DeviceException {
		closeShutterTask.run();
		super.stop();
	}

}
