/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.CommandProvider;
import gda.commandqueue.IFindableQueueProcessor;
import gda.commandqueue.JythonCommandCommandProvider;
import gda.commandqueue.Processor;
import gda.commandqueue.Queue;
import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;

//With the use of RCP command handlers (see CommandView)
//could also implement:
//void beamlineHalt()
//void abortCommands()
//void pauseCurrentScript()
//void resumeCurrentScript()

public class CommandComponent {

	public static final String ID = "uk.ac.gda.client.command_component";

	private static IFindableQueueProcessor queueProcessor;
	private static boolean openQueueProcessorAlreadyAttempted = false;
	private static final Logger logger = LoggerFactory.getLogger(CommandComponent.class);

	private CommandComponent() { /* Do not instantiate */ }

	private static boolean hasPermissionToExecute(boolean doBatonCheck) {
		return !doBatonCheck || amIBatonHolder();
	}

	public static boolean amIBatonHolder() {
		return InterfaceProvider.getBatonStateProvider().amIBatonHolder();
	}

	public static boolean enqueueCommandProvider(CommandProvider provider) throws Exception {
		return enqueueCommandProvider(provider, true);
	}

	public static boolean enqueueCommandProvider(CommandProvider provider, boolean doBatonCheck) throws Exception {
		String commandDescription = provider.getCommand().getDescription();
		String jobLabel = String.format("Queue Job: %s", commandDescription);
		runJob(doBatonCheck, new Job(jobLabel) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean success = false;
				try {
					Queue q = CommandComponent.getQueue();
					if(q == null) logger.error("Queue is absent! Job {} could not be added",commandDescription);
					else success = null!= q.addToTail(provider);
				} catch (Exception e) {
					String errorMessage = String.format("FAIL to queue: %s", commandDescription);
					logger.error(errorMessage,e);
				}
				return success ? Status.OK_STATUS : Status.CANCEL_STATUS;
			}
		});
		return true;
	}

	public static boolean enqueueJythonCommand(String command, String description, String settingsPath) throws Exception {
		return enqueueJythonCommand(command, description, settingsPath, true);
	}

	public static boolean enqueueJythonCommand(String command, String description, String settingsPath, boolean doBatonCheck) throws Exception {
		CommandProvider provider = new JythonCommandCommandProvider(command, description, settingsPath);
		return enqueueCommandProvider(provider, doBatonCheck);
	}

	public static boolean enqueueScriptController(Scriptcontroller controller) throws Exception {
		return enqueueScriptController(controller, true);
	}

	public static boolean enqueueScriptController(Scriptcontroller controller, boolean doBatonCheck) throws Exception {
		return enqueueJythonCommand(controller.getCommand(), controller.getName(), controller.getParametersName(), doBatonCheck);
	}

	public static String evaluateJythonCommand(String command) {
		return evaluateJythonCommand(command,true);
	}

	public static String evaluateJythonCommand(String command, boolean doBatonCheck) {
		boolean canContinue = hasPermissionToExecute(doBatonCheck);
		if(!canContinue) {
			logger.info("Command {} not evaluated, because baton not held",command);
			return "";
		}
		return getCommandRunner().evaluateCommand(command);
	}

	public static ICommandRunner getCommandRunner() {
		return JythonServerFacade.getInstance();
	}

	public static Processor getProcessor() {
		return getQueueProcessor();
	}

	public static Queue getQueue() {
		return getQueueProcessor();
	}

	public static IFindableQueueProcessor getQueueProcessor() {
		if (queueProcessor == null && !openQueueProcessorAlreadyAttempted) {
			queueProcessor = Finder.listFindablesOfType(IFindableQueueProcessor.class)
									.stream()
									.findFirst().orElse(null);
		}
		return queueProcessor;
	}

	public static boolean runJob(boolean doBatonCheck, Job job) {
		if(!hasPermissionToExecute(doBatonCheck)) {
			logger.info("Job {} not executed, because baton not held",job.getName());
			return false;
		}
		runJobWithoutBatonCheck(job);
		return true;
	}

	private static void runJobWithoutBatonCheck(Job job) {
		logger.info(job.getName());
		job.setUser(false);
		job.schedule();
	}

	public static void runJythonCommand(String command) {
		runJythonCommand(command,true);
	}

	public static void runJythonCommand(String command, boolean doBatonCheck) {
		String jobLabel = String.format("Run command: %s", command);
		runJob(doBatonCheck, new Job(jobLabel) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				CommandComponent.getCommandRunner().runCommand(command);
				return Status.OK_STATUS;
			}
		});
	}

	public static boolean runJythonScript(File scriptFile) {
		return runJythonScript(scriptFile,true);
	}

	public static boolean runJythonScript(File scriptFile, boolean doBatonCheck) {
		String jobLabel = String.format("Run script: %s", scriptFile.toString());
		runJob(doBatonCheck,new Job(jobLabel) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				CommandComponent.getCommandRunner().runScript(scriptFile);
				return Status.OK_STATUS;
			}
		});
		return true;
	}

	public static boolean runJythonScript(String scriptName, boolean shared) {
		return runJythonScript(scriptName,shared,true);
	}

	public static boolean runJythonScript(String scriptName, boolean shared, boolean doBatonCheck) {
		String propertiesRef = shared
				? "gda.beamline.scripts.shared.procedure.dir"
				: "gda.beamline.scripts.procedure.dir";

		String folder = LocalProperties.get(propertiesRef, "");
		if(folder.isEmpty()) return false;

		File f = new File(folder,scriptName);
		if(!f.exists()) return false;
		runJythonScript(f,doBatonCheck);
		return true;
	}

	public static boolean runScriptController(Scriptcontroller controller, boolean doBatonCheck) {

		String jobLabel = String.format("Run script via controller: %s", controller.getName());

		runJobWithoutBatonCheck(new Job(jobLabel) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (controller instanceof ScriptControllerBase) {
					((ScriptControllerBase) controller).run();
				} else {
					CommandComponent.runJythonCommand(controller.getCommand(),doBatonCheck); // does incur a second baton check
				}
				return Status.OK_STATUS;
			}
		});
		return true;
	}
}
