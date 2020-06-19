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

	public static void enqueueCommandProvider(CommandProvider provider) throws Exception {
		// Alternate could be return getQueue().addToTail(provider)
		String jobLabel = String.format("Queue Job: %s", provider.getCommand().getDescription());
		CommandComponent.runJob(new Job(jobLabel) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean success = false;
				try {
					success = null != CommandComponent.getQueue().addToTail(provider);
				} catch (Exception e) {
					logger.error(String.format("FAIL to queue: %s", jobLabel),e);
				}
				return success ? Status.OK_STATUS : Status.CANCEL_STATUS;
			}
		});
	}

	public static void enqueueJythonCommand(String command, String description, String settingsPath) throws Exception {
		CommandProvider provider = new JythonCommandCommandProvider(command, description, settingsPath);
		CommandComponent.enqueueCommandProvider(provider);
	}

	public static void enqueueScriptController(Scriptcontroller controller) throws Exception {
		CommandComponent.enqueueJythonCommand(controller.getCommand(), controller.getName(), controller.getParametersName());
	}

	public static String evaluateJythonCommand(String command) {
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
			queueProcessor = Finder.listFindablesOfType(IFindableQueueProcessor.class).stream().findFirst().orElse(null);
		}
		return queueProcessor;
	}

	public static void runJob(Job job) {
		logger.info(job.getName());
		job.setUser(false);
		job.schedule();
	}

	public static void runJythonCommand(String command) {
		String jobLabel = String.format("Run command: %s", command);
		CommandComponent.runJob(new Job(jobLabel) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				getCommandRunner().runCommand(command);
				return Status.OK_STATUS;
			}
		});
	}

	public static void runJythonScript(File scriptFile) {
		String jobLabel = String.format("Run script: %s", scriptFile.toString());
		CommandComponent.runJob(new Job(jobLabel) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				getCommandRunner().runScript(scriptFile);
				return Status.OK_STATUS;
			}
		});
	}

	public static void runJythonScript(String scriptName, boolean shared) {
		String folder = shared
			? LocalProperties.get("gda.beamline.scripts.shared.procedure.dir", "")
			: LocalProperties.get("gda.beamline.scripts.procedure.dir", "");

		if (!folder.isEmpty()) {
			File f = new File(folder + "/" + scriptName);
			if(f.exists()) {
				CommandComponent.runJythonScript(f);
			}
		}
	}

	public static void runScriptController(Scriptcontroller controller) {
		String jobLabel = String.format("Run script via controller: %s", controller.getName());
		CommandComponent.runJob(new Job(jobLabel) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (controller instanceof ScriptControllerBase) {
					((ScriptControllerBase) controller).run();
				} else {
					CommandComponent.runJythonCommand(controller.getCommand());
				}
				return Status.OK_STATUS;
			}
		});
	}
}
