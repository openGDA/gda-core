/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.malcolm.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.dawnsci.nexus.IMultipleNexusDevice;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Base class for Malcolm devices
 *
 */
public abstract class AbstractMalcolmDevice extends AbstractRunnableDevice<IMalcolmModel>
		implements IMalcolmDevice, IMultipleNexusDevice {

	private static final Logger logger = LoggerFactory.getLogger(AbstractMalcolmDevice.class);

	// Events
	private MalcolmEventDelegate eventDelegate;

	protected IPointGenerator<?> pointGenerator;
	protected String outputDir;

	public AbstractMalcolmDevice(IRunnableDeviceService runnableDeviceService) {
		super(runnableDeviceService);
		this.eventDelegate = new MalcolmEventDelegate(this);
		setRole(DeviceRole.MALCOLM);
		setSupportedScanMode(ScanMode.HARDWARE);
	}

	@Override
	public void register() {
		try {
			super.register();
			initialize();
		} catch (MalcolmDeviceException e) {
			logger.error("Could not initialize malcolm device " + getName(), e);
		}
	}

	@Override
	public void initialize() throws MalcolmDeviceException {
		// does nothing by default, subclasses may override
	}

	@PreConfigure
	public void configureScan(ScanModel scanModel) {
		if (scanModel == null) return;

		logger.debug("Configuring malcolm device {} for scan", getName());
		setPointGenerator(scanModel.getPointGenerator());
		String outputDir = null;
		if (scanModel.getFilePath() != null) {
			outputDir = FilenameUtils.removeExtension(scanModel.getFilePath());
			File outputDirFile = new File(outputDir);
			outputDirFile.mkdirs();
		}
		setOutputDir(outputDir);
		logger.debug("Finished configuring malcolm device {} for scan, output dir set to {}", getName(), getOutputDir());
	}

	@Override
	public void setPointGenerator(IPointGenerator<?> pointGenerator) {
		this.pointGenerator = pointGenerator;
	}

	@Override
	public IPointGenerator<?> getPointGenerator() {
		return pointGenerator;
	}

	@Override
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public String getOutputDir() {
		return outputDir;
	}

	@ScanFinally
	public void scanFinally() throws ScanningException {
		// clear the point generator and output dir when the scan has finished. These are set per scan
		this.pointGenerator = null;
		this.outputDir = null;
	}

	@Override
	public DeviceInformation<IMalcolmModel> getDeviceInformation(boolean includeNonAlive) throws ScanningException {
		final DeviceInformation<IMalcolmModel> info = super.getDeviceInformation(includeNonAlive);
		if (includeNonAlive || info.isAlive()) {
			info.setAvailableAxes(getAvailableAxes());
			info.setMalcolmVersion(getVersion());
			info.setMalcolmDetectorInfos(getDetectorInfos());
		}

		return info;
	}

	/**
	 * Enacts any pre-actions or conditions before the device attempts to run the task block.
	 *
	 * @throws Exception
	 */
	protected void beforeExecute() throws Exception {
		logger.debug("Entering beforeExecute, state is " + getDeviceState());
	}

	/**
	 * Enacts any post-actions or conditions after the device completes a run of the task block.
	 *
	 * @throws Exception
	 */
	protected void afterExecute() throws Exception {
		logger.debug("Entering afterExecute, state is " + getDeviceState());
	}

	protected void setTemplateBean(MalcolmEvent bean) {
		eventDelegate.setTemplateBean(bean);
	}

	@Override
	public void start(final IPosition pos) throws ScanningException, InterruptedException {

		final List<Throwable> exceptions = new ArrayList<>(1);

		final Runnable runPosition = () -> {
			try {
				AbstractMalcolmDevice.this.run(pos);
			} catch (Exception e) {
				e.printStackTrace();
				exceptions.add(e);
			}
		};

		final Thread thread = new Thread(runPosition, "Device Runner Thread "+getName());
		thread.start();

		// We delay by 500ms just so that we can
		// immediately throw any connection exceptions
		Thread.sleep(500);

		if (!exceptions.isEmpty()) throw new ScanningException(exceptions.get(0));
	}

	protected void close() {
		eventDelegate.close();
	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		try {
			try {
				if (getDeviceState().isRunning()) abort();
			} finally {
				close();
			}
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, "Cannot dispose of '"+getName()+"'!", e);
		}
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		try {
			MalcolmNexusObjectBuilder malcolmNexusBuilder = new MalcolmNexusObjectBuilder(this);
			return malcolmNexusBuilder.buildNexusObjects(info);
		} catch (Exception e) {
			throw new NexusException("Could not create nexus objects for malcolm device " + getName(), e);
		}
	}

	@Override
	public void addMalcolmListener(IMalcolmEventListener listener) {
		eventDelegate.addMalcolmListener(listener);
	}

	@Override
	public void removeMalcolmListener(IMalcolmEventListener listener) {
		eventDelegate.removeMalcolmListener(listener);
	}

	protected void sendEvent(MalcolmEvent event) throws Exception {
		eventDelegate.sendEvent(event);
	}

}
