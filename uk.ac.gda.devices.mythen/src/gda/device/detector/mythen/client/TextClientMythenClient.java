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

package gda.device.detector.mythen.client;

import gda.device.DeviceException;
import gda.jython.InterfaceProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;

/**
 * A Mythen client that wraps the SLS Mythen text client to interact with the
 * Mythen hardware.
 */
public class TextClientMythenClient implements MythenClient, InitializingBean {
	
	private static final Logger logger = LoggerFactory.getLogger(TextClientMythenClient.class);
	
	private String mythenClientCommand = "mcs_large_client";
	private AcquisitionParameters params;
	
	/**
	 * Sets the name of the text client executable.
	 */
	public void setMythenClientCommand(String mythenClientCommand) {
		this.mythenClientCommand = mythenClientCommand;
	}

	public String getMythenClientCommand() {
		return mythenClientCommand;
	}

	/**
	 * The hostname or IP address of the Mythen controller.
	 */
	protected String host;
	
	/**
	 * Returns the hostname of the Mythen controller.
	 * 
	 * @return the hostname or IP address of the Mythen controller
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * Sets the hostname for the Mythen controller.
	 * 
	 * @param host the hostname or IP address of the Mythen controller
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (host == null) {
			throw new IllegalStateException("You have not set the hostname of the Mythen controller");
		}
	}
	
	/**
	 * Lock used to prevent multiple simultaneous invocations of the text client.
	 */
	private static final Lock EXEC_LOCK = new ReentrantLock();
	
	@Override
	public void acquire(AcquisitionParameters params) throws DeviceException {
		this.params = params;
		List<String> args = createCommandLineArgs(params);
		
		MythenTextClientExecResult result = execProcess(args.toArray(new String[] {}));
		
		Pattern p = Pattern.compile(".+?(\\.\\.\\. data written .+? \\.\\.\\.\n\n)+frame incomplete -1\n");
		Matcher m = p.matcher(result.output);
//		if (!m.matches()) {
//			final String actualOutputEscaped = result.output.replaceAll("\n", "\\\\n");
//			final String msg = String.format("Unexpected output from Mythen client: '%s'", actualOutputEscaped);
//			throw new DeviceException(msg);
//		}
	}
	
	private MythenTextClientExecResult execProcess(String... args) throws DeviceException {
		
		if (!EXEC_LOCK.tryLock()) {
			throw new DeviceException("Cannot acquire: client is already running");
		}
		
		try {
			MythenTextClientExecResult result = new MythenTextClientExecResult();
			
			// Build argument list
			List<String> argList = new Vector<String>();
			argList.add(host);
			argList.addAll(Arrays.asList(args));
			logger.info("Starting acquisition");
			logger.debug("Executing Mythen client with args " + argList);
			
			// Prepend executable name to argument list
			argList.add(0, mythenClientCommand);
			
			ProcessBuilder pb = new ProcessBuilder(argList);
			try {
				Process p = pb.start();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					throw new DeviceException("Unable to wait for text client to finish", e);
				}
				
				result.output = FileCopyUtils.copyToString(br);
				
				result.exitValue = p.exitValue();
				if (result.exitValue != 0) {
					throw new DeviceException(String.format("Client exited with non-zero status: %d", result.exitValue));
				}
				InterfaceProvider.getTerminalPrinter().print("Save to file " + this.params.getFilename());
				logger.info("Acquisition completed successfully");
				logger.debug("Client successfully exited with status code " + result.exitValue);
				
				return result;
				
			} catch (IOException e) {
				throw new DeviceException("Client operation failed", e);
			}
		} finally {
			EXEC_LOCK.unlock();
		}
	}

	/**
	 * Converts the supplied {@link AcquisitionParameters} to a list of text client command line arguments.
	 */
	private static List<String> createCommandLineArgs(AcquisitionParameters params) {
		List<String> args = new ArrayList<String>();
		
		boolean trigen = (params.getTrigger() == Trigger.SINGLE);
		args.add("-trigen");
		args.add(trigen ? "1" : "0");
		
		boolean conttrigen = (params.getTrigger() == Trigger.CONTINUOUS);
		args.add("-conttrigen");
		args.add(conttrigen ? "1" : "0");
		
		args.add("-frames");
		args.add(Integer.toString(params.getFrames()));
		

		if (params.getDelayBeforeFrames() != BigDecimal.ZERO) {
			args.add("-delbef");
			args.add(params.getDelayBeforeFrames().toPlainString());
		}
		if (params.getExposureTime() != BigDecimal.ZERO) {
			args.add("-time");
			args.add(params.getExposureTime().toPlainString());
		}

		if (params.getDelayAfterFrames() != BigDecimal.ZERO) {
			args.add("-delafter");
			args.add(params.getDelayAfterFrames().toPlainString());
		}
		
		args.add("-fname");
		args.add(params.getFilename());
		
		if (params.getStartIndex() != null) {
			args.add("-startindex");
			args.add(Integer.toString(params.getStartIndex()));
		}
		
		args.add("-gateen");
		args.add(params.getGating() ? "1" : "0");
		
		args.add("-gates");
		args.add(Integer.toString(params.getGates()));
		
		args.add("-start");
		
		return args;
	}
	
}
