/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.client.actions;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.views.SequenceViewLive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Findable;
import gda.jython.InterfaceProvider;

public class CommandToClipboardHandler extends AbstractHandler implements IHandler {
	private static final Logger logger = LoggerFactory.getLogger(CommandToClipboardHandler.class);
	private static Findable detector;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the command
		String command = buildCommand(event);
		if (command == null) {
			logger.error("Building command failed");
			return null;
		}
		// Put it on the clipboard
		final Clipboard clipboard = new Clipboard(HandlerUtil.getActiveShell(event).getDisplay());
		clipboard.setContents(new Object[] { command }, new Transfer[] { TextTransfer.getInstance() });
		// Must return null
		return null;
	}

	@Override
	public boolean isEnabled() {
		// Always enabled
		return true;
	}

	protected static String buildCommand(ExecutionEvent event) {
		final SequenceViewLive sequenceView = HandlerUtil.getActivePart(event).getAdapter(SequenceViewLive.class);
		final String fileAbsPath = sequenceView.getFilename();

		logger.info("Saving: {}", fileAbsPath);
		sequenceView.doSave(new NullProgressMonitor());

		String fileName = fileAbsPath.substring(fileAbsPath.lastIndexOf(File.separatorChar) + 1);
		// Find out if any extraDetectors are configured, should exist and return a empty string if none are required
		String extraDetectors = InterfaceProvider.getCommandRunner().evaluateCommand("extraDetectors");
		if (extraDetectors == null) {
			logger.warn("extraDetectors was not in the Jython namespace, no extraDetectors will be used");
			extraDetectors = "";
		}
		logger.debug("Extra detectors configured: {}", extraDetectors);

		//Check if the default file path is the same as the absolute file path
		//If it is we can safely pass just the sequence file name, else provide the full path
		String fullDefaultFilePath = InterfaceProvider.getPathConstructor().createFromProperty("gda.ses.electronanalyser.seq.dir") + File.separator;
		if (!(fullDefaultFilePath + fileName).equals(fileAbsPath)) {
			fileName = fileAbsPath;
		}
		//Remove full path and provide only relative path if inside directory
		if (fileName.contains(fullDefaultFilePath)) {
			fileName = fileName.replace(fullDefaultFilePath, "");
		}
		// Will have a trailing space if extra detectors is empty
		final String command = ("analyserscan " + getDetector().getName() + " '" + fileName + "' " + extraDetectors).trim();

		logger.info("Command is: {}", command);
		return command;
	}

	public static Findable getDetector() {
		return detector;
	}

	/**
	 * @param detector to use for the command.
	 * Example springbean configuration to set on client:
	 * <pre>
	 * {@code
	 *	<bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
	 *		<property name="staticMethod" value="org.opengda.detector.electronanalyser.client.actions.CommandToClipboardHandler.setDetector"/>
	 *		<property name="arguments" ref="r4000"/>
	 *	</bean>
	 * }
	 * </pre>
	 */
	public static void setDetector(Findable detector) {
		CommandToClipboardHandler.detector = detector;
	}
}
