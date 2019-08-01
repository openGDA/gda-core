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

package uk.ac.gda.tomography.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;
import gda.jython.commandinfo.CommandThreadEvent;
import uk.ac.gda.tomography.service.Arrangement;
import uk.ac.gda.tomography.service.TomographyService;
import uk.ac.gda.tomography.service.TomographyServiceException;
import uk.ac.gda.tomography.service.message.TomographyRunMessage;

/**
 *
 *  @author Maurizio Nagni
 */
public class TomographyServiceImpl implements TomographyService {
	private static final Logger logger = LoggerFactory.getLogger(TomographyServiceImpl.class);

//	/**
//	 * Utility method equivalent to runAcquisition(message, script, null, null)
//	 *
//	 * @param message
//	 * @param script
//	 * @throws TomographyServiceException
//	 */
//	public void runAcquisition(TomographyRunMessage message, File script) throws TomographyServiceException {
//		runAcquisition(message, script, null, null);
//	}

	@Override
	public void runAcquisition(TomographyRunMessage message, File script, File onError, File onSuccess)
			throws TomographyServiceException {
		executeCommand(message, script, onError, onSuccess, "doAcquisition");
	}

	@Override
	public void resetInstruments(Arrangement arrangement) throws TomographyServiceException {
		arrangement.doArrangement();
	}

	@Override
	public Path takeDarkImage(TomographyRunMessage message, File script) throws TomographyServiceException {
		CommandThreadEvent event = runScript(message, script, "doDark");
		try {
			// do something with the event (?)
			return Files.createTempFile("", ""); //TBD
		} catch (IOException e) {
			throw new TomographyServiceException("Error", e);
		}
	}

	@Override
	public Path takeFlatImage(TomographyRunMessage message, File script) throws TomographyServiceException {
		CommandThreadEvent event = runScript(message, script, "doFlat");
		try {
			// do something with the event (?)
			return Files.createTempFile("", ""); //TBD
		} catch (IOException e) {
			throw new TomographyServiceException("Error", e);
		}
	}

	private void executeCommand(TomographyRunMessage message, File script, File onError, File onSuccess, String command)
			throws TomographyServiceException {
		try {
			runScript(message, script, command);
		} catch (TomographyServiceException e) {
			if (onError != null) {
				runScript(message, onError, null);
			}
			throw new TomographyServiceException("Error executing onError script", e);
		}
		if (onSuccess != null) {
			runScript(message, onSuccess, null);
		}
	}

	private File insertConfiguration(TomographyRunMessage message, File script, String command) throws IOException {
		File tmp = File.createTempFile("tomo", ".py");
		try (OutputStream os = new FileOutputStream(tmp)) {
			if (String.class.isInstance(message.getConfiguration())) {
				os.write(String.format("tomographyServiceMessage = '%s'%n", message.getConfiguration()).getBytes());
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

	private CommandThreadEvent runScript(TomographyRunMessage message, File script, String cmd)
			throws TomographyServiceException {
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
}
