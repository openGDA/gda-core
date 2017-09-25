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
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADActionUtils {

	private static final Logger logger = LoggerFactory.getLogger(ADActionUtils.class);

	private ADActionUtils() {
		// private construtor to prevent instantiation
	}

	public static IAction addAction(String name, final String commandId, final String commandParameterName, final String commandParameterValue) throws NotDefinedException{
		IAction action = new Action(name, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				try{
					ICommandService cs = PlatformUI.getWorkbench().getService(ICommandService.class);
					Command command = cs.getCommand(commandId);
					IParameter parameter = command.getParameter(commandParameterName);
					Parameterization[] parameterizations = new Parameterization[] { new Parameterization(parameter, commandParameterValue) };
					ParameterizedCommand cmd = new ParameterizedCommand(command, parameterizations);
					ExecutionEvent executionEvent = PlatformUI.getWorkbench().getService(IHandlerService.class).createExecutionEvent(cmd, null);
					command.executeWithChecks(executionEvent);
				}
				catch(Exception e){
					logger.error("Error running Set Exposure command", e);
				}
			}
		};
		ICommandService cs = PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = cs.getCommand(commandId);
		action.setToolTipText(command.getDescription());
		ICommandImageService service = PlatformUI.getWorkbench().getService(ICommandImageService.class);
		action.setImageDescriptor(service.getImageDescriptor(commandId));
		return action;
	}

	public static IAction addShowViewAction(final String name, final String viewId, final String secondaryId, String description, ImageDescriptor imageDesc){
		IAction action = new Action(name, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				try{
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window != null) {
						IWorkbenchPage page = window.getActivePage();
						if (page != null) {
							page.showView(viewId, secondaryId, IWorkbenchPage.VIEW_CREATE);
							page.showView(viewId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
						}
					}
				}
				catch(Exception e){
					logger.error("Error running show view action '" + name + "' for viewid:'"+viewId + "' secondaryId:'" + secondaryId + "'", e);
				}
			}
		};
		action.setToolTipText(description);
		if(imageDesc!=null)
			action.setImageDescriptor(imageDesc);
		return action;
	}

}