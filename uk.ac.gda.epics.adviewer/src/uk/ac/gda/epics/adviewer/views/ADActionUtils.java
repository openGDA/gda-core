/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.views;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.Ids;

public class ADActionUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ADActionUtils.class);
	
	public IAction addAction(String name, final String commandId, final ADController config, final IWorkbenchSite site){
		IAction action = new Action(name, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				try{
					ICommandService cs = (ICommandService) site.getService(
							ICommandService.class);
					Command command = cs.getCommand(commandId);
					IParameter parameter = command.getParameter(Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME);
					String name = config.getServiceName();
					Parameterization[] parameterizations = new Parameterization[] { new Parameterization(parameter,
							name	) };
					ParameterizedCommand cmd = new ParameterizedCommand(command, parameterizations);
					ExecutionEvent executionEvent = ((IHandlerService) site.getService(
							IHandlerService.class)).createExecutionEvent(cmd, null);
					command.executeWithChecks(executionEvent);
				}
				catch(Exception e){
					logger.error("Error running Set Exposure command", e);
				}
			}
		};
		action.setToolTipText("Set Exposure");
		ICommandImageService service = (ICommandImageService) site.getService(ICommandImageService.class);
		action.setImageDescriptor(service.getImageDescriptor(Ids.COMMANDS_SET_EXPOSURE));
		return action;
	}
	
}