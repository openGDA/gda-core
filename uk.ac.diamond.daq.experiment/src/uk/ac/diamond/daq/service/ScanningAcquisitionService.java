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

package uk.ac.diamond.daq.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IScanBeanSubmitter;
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.exception.ScanningAcquisitionServiceException;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * @author Maurizio Nagni
 */
@Component("scanningAcquisitionService")
public class ScanningAcquisitionService {

	private ISubmitter<ScanBean> scanBeanSubmitter;

	/**
	 * Submits the acquisition request to an {@link IScanBeanSubmitter}
	 *
	 * @param message
	 *            the scanning acquisition message
	 * @throws ScanningAcquisitionServiceException
	 *             if not {@link IRunnableDeviceService} is available or if {@link ISubmitter#submit(Object)} raises an
	 *             {@link EventException}
	 */
	public void run(AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> acquisition) throws ScanningAcquisitionServiceException {
		try {
			var scanBean = new ScanBean();
			scanBean.setName(acquisition.getName());
			scanBean.setFilePath(acquisition.getAcquisitionLocation().toExternalForm());
			scanBean.setBeamline(System.getProperty("BEAMLINE", "dummy"));

			var tsr = new ScanRequestFactory(acquisition);
			scanBean.setScanRequest(tsr.createScanRequest(ServiceProvider.getService(IRunnableDeviceService.class)));
			getScanBeanSubmitter().submit(scanBean);
		} catch (Exception e) {
			throw new ScanningAcquisitionServiceException("Cannot submit acquisition", e);
		}
	}

	private ISubmitter<ScanBean> getScanBeanSubmitter() throws URISyntaxException {
		if (Objects.isNull(scanBeanSubmitter)) {
			var queueServerURI = new URI(LocalProperties.getBrokerURI());
			scanBeanSubmitter = ServiceProvider.getService(IEventService.class).createSubmitter(queueServerURI, EventConstants.SUBMISSION_QUEUE);
		}
		return scanBeanSubmitter;
	}
}
