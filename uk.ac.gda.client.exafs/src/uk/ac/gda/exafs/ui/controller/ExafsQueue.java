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

package uk.ac.gda.exafs.ui.controller;

import gda.jython.JythonServerFacade;
import gda.jython.gui.JythonGuiConstants;
import gda.jython.scriptcontroller.ScriptExecutor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.validation.InvalidBeanException;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.exafs.ui.data.ScanObject;

/**
 * NOTE Could possibly be using Eclipse Job class
 */
@Deprecated
public class ExafsQueue /*extends ExperimentQueue*/ {

	protected static final Logger logger = LoggerFactory.getLogger(ExafsQueue.class);

//	@Override
	protected boolean process(IExperimentObject run) throws Exception {

		try {
			// Hack warning: A data-structure in the GDA sometimes throws a concurrent
			// modification if we proceed straight to the ScriptExecutor.Run(...) line.
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new Exception(e);
		}

//		if (run.isAborted())
//			return false;

		final ScanObject ob = (ScanObject) run;
		String command = null;
		try {

			checkForInvalidMixOfScanTypes(ob);

//			checkAvailable(ob);

			command = getCommandLine(ob);

			// Print the command so that users can see what is done.
//			if (run.isAborted())
//				return false;
			JythonServerFacade.getInstance().print(command);

			// Call the validation
//			if (run.isAborted())
//				return false;

			// use the extension point for beamline specific validation
			ExperimentFactory.getValidator().validate(ob);

			// Run command and block.
//			if (run.isAborted())
//				return false;
			ScriptExecutor.Run("ExafsScriptObserver", null, getBeans(ob), command,
					JythonGuiConstants.TERMINALNAME);
			return true;

		}/* catch (InvalidBeanException ne) {
			final List<InvalidBeanMessage> msgs = ne.getMessages();
//			final ExperimentException o = new ExperimentException(run);
			if (msgs.size() > 1) {
				o.setMultipleErrors(msgs);
				o.setErrorTitle("Validation Errors Reported");
				o.setUserMessage("There are validation errors with the current run and it cannot be executed.\n\nPlease use the 'Problems' view to fix them.");
			} else {
				final InvalidBeanMessage msg = msgs.get(0);
				o.setErrorTitle("Invalid " + msg.getLabel());
				o.setUserMessage(msg.toString());
			}
			throw o;

		}*/ /*catch (Exception ne) {
//			if (run.isAborted())
//				return false;
			throw ne;

		}*/ catch (Exception ne) {
			if (command != null) {
				logger.error("Cannot execute command " + command, ne);
			} else {
				logger.error("Cannot determine if parameters are Xanes", ne);
			}
			return false;
		}
	}

	/*
	 * For I20 where the XES and XAS/XANES scan types are incompatible.
	 */
	private void checkForInvalidMixOfScanTypes(final ScanObject ob) throws Exception, InvalidBeanException {

		// find type of current ScanObject
		boolean xesSeen = false;
		boolean otherSeen = false;
		if (testScanParametersIsXES(ob.getScanParameters())) {
			xesSeen = true;
		} else {
			otherSeen = true;
		}

		// test the rest
//		Object[] wholeQueue = this.queueList.toArray();
//		for (Object scanObj : wholeQueue) {
//			if (testScanParametersIsXES(((ScanObject) scanObj).getScanParameters())) {
//				xesSeen = true;
//			} else {
//				otherSeen = true;
//			}
//		}
//
//		if (xesSeen && otherSeen) {
//			throw new InvalidBeanException(
//					"A mixture of XES and other scan types has been queued.\nThis is not allowed due to potential hardware collisions.\nYou will need to resolve this in order to proceed.");
//		}
//
//		if (ScanObjectManager.isXESOnlyMode() && otherSeen) {
//			throw new InvalidBeanException(
//					"Non-XES scan types have been queued when running in XES mode.\nThis is not allowed due to potential hardware collisions.\nYou will need to switch to XAS mode or delete those scans.");
//		}
//
//		if (!ScanObjectManager.isXESOnlyMode() && xesSeen) {
//			throw new InvalidBeanException(
//					"XES scan types have been queued when running in XAS mode.\nThis is not allowed due to potential hardware collisions.\nYou will need to switch to XES mode or delete those scans.");
//		}

	}

	private boolean testScanParametersIsXES(IScanParameters scanParameters) {
		String className = scanParameters.getClass().getName();
		return className.endsWith("XesScanParameters");

	}

	private Map<String, Serializable> getBeans(final ScanObject run) throws Exception {

		final Map<String, Serializable> beans = new HashMap<String, Serializable>(4);
		if (run.isXanes()) {
			beans.putAll(getRunEntry(run, run.getScanFileName(), "XANES Parameters", "getScanParameters"));
		} else if (run.isQexafs()) {
			beans.putAll(getRunEntry(run, run.getScanFileName(), "QEXAFS Parameters", "getScanParameters"));
		} else if (run.isMicroFocus()) {
			beans.putAll(getRunEntry(run, run.getScanFileName(), "MicroFocus Parameters", "getScanParameters"));
		} else {
			beans.putAll(getRunEntry(run, run.getScanFileName(), "XAS Parameters", "getScanParameters"));
		}

		beans.putAll(getRunEntry(run, run.getSampleFileName(), "Sample Parameters", "getSampleParameters"));

		beans.putAll(getRunEntry(run, run.getDetectorFileName(), "Detector Parameters", "getDetectorParameters"));

		beans.putAll(getRunEntry(run, run.getOutputFileName(), "Output Parameters", "getOutputParameters"));

		return beans;

	}

	private Map<String, Serializable> getRunEntry(final ScanObject run, final String fileName, final String typeName,
			final String methodName) throws Exception {
		try {

			final Method method = ScanObject.class.getMethod(methodName);
			final Serializable s = (Serializable) method.invoke(run);
			final Map<String, Serializable> ret = new HashMap<String, Serializable>(1);
			ret.put(ExafsQueue.getFileKey(fileName), s);
			return ret;

		} catch (InvocationTargetException ne) {
/*			if (ne.getCause() instanceof org.xml.sax.SAXParseException) {
				final Exception ee = new Exception(The " + typeName + " are not valid in the file '" + fileName
						+ "'.\n\nThe scan cannot be run.\n\n" + "Error message:\n"
						+ XMLBeanEditor.getSantitizedExceptionMessage(ne.getCause().getMessage()));
				ee.setErrorTitle("XML Validation Error");
				ee.setUserMessage(");
				throw ee;
			}*/
			throw ne;
		}
	}

	private static String getFileKey(final String fileName) {
		if (fileName == null || fileName.equals("None"))
			return "None";
		if (fileName.indexOf("[") != -1) {
			return fileName.substring(0, fileName.lastIndexOf('.'));
		}
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

	private String getCommandLine(final ScanObject run) throws Exception {
		if (run.isXanes()) {
			return "xas " + getArgs(run);
		} else if (run.isQexafs()) {
			return "qexafs " + getArgs(run);
		} else if (run.isMicroFocus()) {
			return "map " + getArgs(run);
		} else if (run.isXes()) {
			return "xes " + getArgs(run);
		}

		return "xas " + getArgs(run);
	}

	private String getArgs(ScanObject run) {
		final StringBuilder buf = new StringBuilder();
		buf.append("\"" + ExafsQueue.getFileKey(run.getSampleFileName()) + "\"");
		buf.append(" \"");
		buf.append(ExafsQueue.getFileKey(run.getScanFileName()));
		buf.append("\" ");
		buf.append(ExafsQueue.getFileKey(run.getDetectorFileName()));
		buf.append(" \"");
		buf.append(ExafsQueue.getFileKey(run.getOutputFileName()));
		buf.append("\" ");
		buf.append("\"" + run.getRunFileManager().getContainingFolder().getName() + "\"");
		buf.append(" " + run.getRepetition() + " ");
		buf.append("False");
		return buf.toString();
	}

	/**
	 * NOTE This should be on the super class to avoid duplication but the restrictions we have on class path are
	 * meaning that common.rcp is not compiling with core classes referenced.
	 * 
	 * @throws Exception
	 */
//	private void checkAvailable(IExperimentObject run) throws ExperimentException {
//
//		// You have to request the baton in case you are
//		if (JythonServerFacade.getInstance().isBatonHeld()) {
//			JythonServerFacade.getInstance().requestBaton();
//		} else if (!JythonServerFacade.getInstance().amIBatonHolder()) {
//			JythonServerFacade.getInstance().requestBaton();
//		}
//
//		if (JythonServerFacade.getInstance().isBatonHeld() && !JythonServerFacade.getInstance().amIBatonHolder()) {
//			final ClientDetails batonedUser = JythonServerFacade.getInstance().getBatonHolder();
//
//			try {
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(BatonView.ID);
//			} catch (PartInitException e) {
//				logger.error("PartInitException while trying to display the BatonView", e);
//			}
//
//			String message = "";
//			if (batonedUser == null) {
//				message = "You do not currently hold the baton.\n\nPlease take the baton to run scans.";
//			} else {
//				message = "You do not currently hold the baton, it is being held by user id " + batonedUser.getUserID()
//						+ ".\n\nPlease take the baton to run scans.";
//			}
//			final String finalMessage = message;
//			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//				@Override
//				public void run() {
//					MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
//							"Cannot run the queue", finalMessage);
//				}
//			});
//			throw new ExperimentException(message, run);
//		}
//
//		// NOTE: As many scans can be added by one client as you like, there is a local queue.
//		// This block is only for another client(s) accessing the server.
//		if (JythonServerFacade.getInstance().getScanStatus() != Jython.IDLE
//				|| JythonServerFacade.getInstance().getScriptStatus() != Jython.IDLE) {
//			final ClientDetails batonedUser = JythonServerFacade.getInstance().getBatonHolder();
//			String message = "";
//			if (batonedUser == null) {
//				message = "Currently a scan is already being run by another user.";
//			} else {
//				message = "Currently a scan is already being run.\nThe current baton holder is "
//						+ batonedUser.getUserID();
//			}
//			final String finalMessage = message;
//
//			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//				@Override
//				public void run() {
//					MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
//							"Cannot run the queue", finalMessage);
//				}
//			});
//			throw new ExperimentException(message, run);
//
//		}
//	}

}
