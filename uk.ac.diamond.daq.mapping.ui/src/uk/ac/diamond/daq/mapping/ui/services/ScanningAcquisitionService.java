/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.services;

import java.net.URI;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IScanBeanSubmitter;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionRunEvent;
import uk.ac.diamond.daq.mapping.api.document.exception.ScanningAcquisitionServiceException;
import uk.ac.diamond.daq.mapping.api.document.service.IScanningAcquisitionService;
import uk.ac.diamond.daq.mapping.api.document.service.message.ScanningAcquisitionMessage;
import uk.ac.gda.api.exception.GDAException;

/**
 * @author Maurizio Nagni
 */
@Component("scanningAcquisitionService")
public class ScanningAcquisitionService implements IScanningAcquisitionService {
	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionService.class);

	@Autowired
	private DocumentMapper documentMapper;

	@Override
	public void onApplicationEvent(ScanningAcquisitionRunEvent event) {
		try {
			runAcquisition(event.getScanningMessage());
		} catch (ScanningAcquisitionServiceException e) {
			logger.error(String.format("Cannot run ScanningAcquisitionRunEvent %s", event), e);
		}
	}

	private AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> deserializeAcquisition(
			ScanningAcquisitionMessage message) throws ScanningAcquisitionServiceException {
		try {
			return documentMapper.fromJSON((String) message.getAcquisition(), AcquisitionBase.class);
		} catch (GDAException e) {
			throw new ScanningAcquisitionServiceException("Json error", e);
		}
	}

	/**
	 * Submits the acquisition request to an {@link IScanBeanSubmitter}
	 *
	 * @param message
	 *            the scanning acquisition message
	 * @throws ScanningAcquisitionServiceException
	 *             if not {@link IRunnableDeviceService} is available or if {@link ISubmitter#submit(Object)} raises an
	 *             {@link EventException}
	 */
	@Override
	public void runAcquisition(ScanningAcquisitionMessage message) throws ScanningAcquisitionServiceException {
		final IScanBeanSubmitter submitter = PlatformUI.getWorkbench().getService(IScanBeanSubmitter.class);
		try {
			AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> acquisition = deserializeAcquisition(
					message);
			// default path name
			String pathName = "ScanningAcquisition";
			final ScanBean scanBean = new ScanBean();
			scanBean.setName(String.format("%s - %s Scan", acquisition.getName(), pathName));
			scanBean.setFilePath(acquisition.getAcquisitionLocation().toExternalForm());
			scanBean.setBeamline(System.getProperty("BEAMLINE", "dummy"));

			ScanRequestFactory tsr = new ScanRequestFactory(acquisition);
			scanBean.setScanRequest(tsr.createScanRequest(getRunnableDeviceService()));
			submitter.submitScan(scanBean);
		} catch (Exception e) {
			throw new ScanningAcquisitionServiceException("Cannot submit acquisition", e);
		}
	}

	private IRunnableDeviceService getRunnableDeviceService() throws ScanningException {
		return getRemoteService(IRunnableDeviceService.class);
	}

	private <T> T getRemoteService(Class<T> klass) throws ScanningException {
		IEclipseContext injectionContext = PlatformUI.getWorkbench().getService(IEclipseContext.class);
		IEventService eventService = injectionContext.get(IEventService.class);
		try {
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createRemoteService(jmsURI, klass);
		} catch (Exception e) {
			logger.error("Error getting remote service {}", klass, e);
			throw new ScanningException(e);
		}
	}
}
