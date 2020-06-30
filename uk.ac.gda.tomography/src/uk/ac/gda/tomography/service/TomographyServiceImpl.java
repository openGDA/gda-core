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

package uk.ac.gda.tomography.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import gda.jython.commandinfo.CommandThreadEvent;
import uk.ac.diamond.daq.mapping.api.IScanBeanSubmitter;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionRunEvent;
import uk.ac.diamond.daq.mapping.api.document.service.message.ScanningMessage;
import uk.ac.gda.api.exception.GDAException;

/**
 * @author Maurizio Nagni
 */
@Component("tomographyService")
public class TomographyServiceImpl implements TomographyService {
	private static final Logger logger = LoggerFactory.getLogger(TomographyServiceImpl.class);

	@Autowired
	private DocumentMapper documentMapper;

	public TomographyServiceImpl() {
		super();
	}

	@Override
	public void onApplicationEvent(ScanningAcquisitionRunEvent event) {
		try {
			runAcquisition(event.getScanningMessage(), null, null, null);
		} catch (TomographyServiceException e) {
			logger.error("TODO put description of error here", e);
		}
	}

	@Override
	public void runAcquisition(ScanningMessage message, File script, File onError, File onSuccess) throws TomographyServiceException {
		executeCommand(message, script, onError, onSuccess, "doAcquisition");
	}

	@Override
	public void resetInstruments(Arrangement arrangement) throws TomographyServiceException {
		arrangement.doArrangement();
	}

	@Override
	public URL takeDarkImage(ScanningMessage message, File script) throws TomographyServiceException {
		CommandThreadEvent event = runScript(message, script, "doDark");
		try {
			// do something with the event (?)
			return Files.createTempFile("", "").toUri().toURL(); // TBD
		} catch (IOException e) {
			throw new TomographyServiceException("Error", e);
		}
	}

	@Override
	public URL takeFlatImage(ScanningMessage message, File script) throws TomographyServiceException {
		CommandThreadEvent event = runScript(message, script, "doFlat");
		try {
			// do something with the event (?)
			return Files.createTempFile("", "").toUri().toURL(); // TBD
		} catch (IOException e) {
			throw new TomographyServiceException("Error", e);
		}
	}

	private void executeCommand(ScanningMessage message, File script, File onError, File onSuccess, String command) throws TomographyServiceException {
		submitScan(message);
	}

	private File insertConfiguration(ScanningMessage message, File script, String command) throws IOException {
		File tmp = File.createTempFile("tomo", ".py");
		try (OutputStream os = new FileOutputStream(tmp)) {
			if (String.class.isInstance(message.getAcquisition())) {
				os.write(String.format("tomographyServiceMessage = '%s'%n", message.getAcquisition()).getBytes());
				if (command != null) {
					os.write(String.format("cmd = '%s'%n", command).getBytes());
				}
				os.write(Files.readAllBytes(script.toPath()));
			}
		}
		return tmp;
	}

	private Arrangement getArrangement() {
		return new Arrangement() {

			@Override
			public void doArrangement() {
				// TODO Auto-generated method stub
			}
		};
	}

	private CommandThreadEvent runScript(ScanningMessage message, File script, String cmd) throws TomographyServiceException {
		if (message != null) {
			try {
				script = insertConfiguration(message, script, cmd);
			} catch (IOException e) {
				throw new TomographyServiceException("Cannot insert configuration into the script", e);
			}
		}
		return runScript(script);
	}

	private CommandThreadEvent runScript(File script) throws TomographyServiceException {
		if (script == null) {
			return null;
		}
		try {
			return JythonServerFacade.getInstance().runScript(script);
		} catch (Exception e) {
			throw new TomographyServiceException("Error executing script", e);
		}
	}

	private AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> deserializeAcquisition(
			ScanningMessage message) throws TomographyServiceException {
		try {
			return documentMapper.fromJSON((String) message.getAcquisition(), AcquisitionBase.class);
		} catch (GDAException e) {
			throw new TomographyServiceException("Json error", e);
		}
	}

	/**
	 * Submits the scan described by the current mapping bean to the submission service. An error dialog is displayed if the scan could not be successfully
	 * submitted.
	 *
	 * @param filePath
	 *            The filepath of the output NeXus file. If {@code null} it is generated through default properties.
	 */
	private void submitScan(ScanningMessage message) {
		final IScanBeanSubmitter submitter = PlatformUI.getWorkbench().getService(IScanBeanSubmitter.class);
		try {
			String sampleName = "MysampleName";
			String pathName = "MypathName";
			final ScanBean scanBean = new ScanBean();
			scanBean.setName(String.format("%s - %s Scan", sampleName, pathName));
			scanBean.setBeamline(System.getProperty("BEAMLINE", "dummy"));

			ScanRequestFactory tsr = new ScanRequestFactory(deserializeAcquisition(message));
			scanBean.setScanRequest(tsr.createScanRequest(getRunnableDeviceService()));
			submitter.submitScan(scanBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private IRunnableDeviceService getRunnableDeviceService() throws ScanningException {
		try {
			return getRemoteService(IRunnableDeviceService.class);
			//return PlatformUI.getWorkbench().getService(IRunnableDeviceService.class);
		} catch (Exception e) {
			throw new ScanningException();
		}
	}

	private <T> T getRemoteService(Class<T> klass) {
		IEclipseContext injectionContext = PlatformUI.getWorkbench().getService(IEclipseContext.class);
		IEventService eventService = injectionContext.get(IEventService.class);
		try {
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createRemoteService(jmsURI, klass);
		} catch (Exception e) {
			logger.error("Error getting remote service {}", klass, e);
			return null;
		}
	}
}
