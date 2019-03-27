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
package org.eclipse.scanning.server.servlet;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IRequestHandler;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.server.application.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcquireRequestHandler implements IRequestHandler<AcquireRequest> {

	private static final Logger logger = LoggerFactory.getLogger(AcquireRequestHandler.class);

	private final AcquireRequest bean;
	private final IPublisher<AcquireRequest> publisher;

	public AcquireRequestHandler(AcquireRequest bean, IPublisher<AcquireRequest> publisher) {
		this.bean = bean;
		this.publisher = publisher;
	}

	@Override
	public AcquireRequest getBean() {
		return bean;
	}

	@Override
	public IPublisher<AcquireRequest> getPublisher() {
		return publisher;
	}

	@Override
	public AcquireRequest process(AcquireRequest bean) throws EventException {
		try {
			bean.setStatus(Status.RUNNING);
			IRunnableDevice<?> device = createRunnableDevice(bean);
			device.run(null);

			bean.setStatus(Status.COMPLETE);
			bean.setMessage(null);
		} catch (EventException eventException) {
			throw eventException;
		} catch (Exception e) {
			bean.setStatus(Status.FAILED);
			bean.setMessage(getRootCauseMessage(e));
			logger.error("Cannot acquire data for detector " + getBean().getDetectorName(), e);
		}

		return bean;
	}

	private IRunnableDevice<?> createRunnableDevice(AcquireRequest request) throws Exception {
		// get the services we need
		final IRunnableDeviceService deviceService = Services.getRunnableDeviceService();
		final IPointGeneratorService pointGenService = Services.getGeneratorService();

		final ScanModel scanModel = new ScanModel();

		IPointGenerator<?> gen = pointGenService.createGenerator(new StaticModel());
		scanModel.setPointGenerator(gen);
		scanModel.setFilePath(getOutputFilePath(request));
		IRunnableDevice<?> detector = deviceService.getRunnableDevice(bean.getDetectorName());
		scanModel.setDetectors(detector);
		scanModel.setScannables(Collections.emptyList());

		configureDetector(detector, request.getDetectorModel(), scanModel, gen);
		return deviceService.createRunnableDevice(scanModel, null);
	}

	private String getOutputFilePath(AcquireRequest request) throws Exception {
		if (request.getFilePath() == null) {
			IFilePathService filePathService = Services.getFilePathService();
			request.setFilePath(filePathService.getNextPath(request.getDetectorName() + "-acquire"));
		}

		return request.getFilePath();
	}

	@SuppressWarnings("unchecked")
	private void configureDetector(IRunnableDevice<?> detector, Object detectorModel,
			ScanModel scanModel, IPointGenerator<?> gen) throws ScanningException {

		try {
			ScanInformation info = new ScanInformation(gen, Arrays.asList(detectorModel), scanModel.getFilePath());
			AnnotationManager manager = new AnnotationManager(Activator.createResolver());
			manager.addContext(info);
			manager.addDevices(detector);

			manager.invoke(PreConfigure.class, detectorModel, gen);
			((IRunnableDevice<Object>) detector).configure(detectorModel);
			manager.invoke(PostConfigure.class, detectorModel, gen);
		} catch (Exception e) {
			throw new ScanningException(e);
		}
	}

}
