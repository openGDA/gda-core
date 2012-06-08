/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.gda.menu.JythonControlsFactory;

import com.swtdesigner.ResourceManager;

/**
 *
 */
public class CommandQueueContributionFactory extends ExtensionContributionFactory {

	public static final String UK_AC_GDA_CLIENT_PAUSE_COMMAND_QUEUE = "uk.ac.gda.client.PauseCommandQueue";
	public static final String UK_AC_GDA_CLIENT_START_COMMAND_QUEUE = "uk.ac.gda.client.StartCommandQueue";
	public static final String UK_AC_GDA_CLIENT_SKIP_COMMAND_QUEUE = "uk.ac.gda.client.SkipCommandQueue";
	public static final String UK_AC_GDA_CLIENT_STOP_COMMAND_QUEUE = "uk.ac.gda.client.StopCommandQueue";
	public static final String UK_AC_GDA_CLIENT_STOP_AFTER_CURRENT_COMMAND_QUEUE = "uk.ac.gda.client.StopAfterCurrentCommandQueue";

	/**
	 * 
	 */
	public CommandQueueContributionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		additions.addContributionItem(getStopContributionItem(serviceLocator), Expression.TRUE);
		additions.addContributionItem(getPauseContributionItem(serviceLocator), Expression.TRUE);
		additions.addContributionItem(getStartContributionItem(serviceLocator), Expression.TRUE);
		additions.addContributionItem(getSkipContributionItem(serviceLocator), Expression.TRUE);
		additions.addContributionItem(getStopContributionItem(serviceLocator), Expression.TRUE);
		additions.addContributionItem(getStopAfterCurrentContributionItem(serviceLocator), Expression.TRUE);
	}

	public IContributionItem getStopContributionItem(IServiceLocator serviceLocator) {
		return new ActionContributionItem(createStopAction(serviceLocator));
	}

	public IContributionItem getPauseContributionItem(IServiceLocator serviceLocator) {
		return new ActionContributionItem(createPauseAction(serviceLocator));
	}

	public IContributionItem getStartContributionItem(IServiceLocator serviceLocator) {
		return new ActionContributionItem(createStartAction(serviceLocator));
	}

	public IContributionItem getSkipContributionItem(IServiceLocator serviceLocator) {
		return new ActionContributionItem(createSkipAction(serviceLocator));
	}

	public IContributionItem getStopAfterCurrentContributionItem(IServiceLocator serviceLocator) {
		return new ActionContributionItem(createStopAfterCurrentAction(serviceLocator));
	}

	public Action createSkipAction(IServiceLocator serviceLocator) {
		return createAction(serviceLocator, "Skip", UK_AC_GDA_CLIENT_SKIP_COMMAND_QUEUE, "/control_skip_blue.png");
	}

	public Action createStartAction(IServiceLocator serviceLocator) {
		return createAction(serviceLocator, "Start", UK_AC_GDA_CLIENT_START_COMMAND_QUEUE, "/control_start_blue.png");
	}

	public Action createPauseAction(IServiceLocator serviceLocator) {
		return createAction(serviceLocator, "Pause", UK_AC_GDA_CLIENT_PAUSE_COMMAND_QUEUE, "/control_pause_blue.png");
	}

	public Action createStopAction(IServiceLocator serviceLocator) {
		return createAction(serviceLocator, "Stop", UK_AC_GDA_CLIENT_STOP_COMMAND_QUEUE, "/control_stop_blue.png");
	}

	public Action createStopAfterCurrentAction(IServiceLocator serviceLocator) {
		return createAction(serviceLocator, "Stop After Current", UK_AC_GDA_CLIENT_STOP_AFTER_CURRENT_COMMAND_QUEUE, "/control_pause_blue.png");
	}

	public void executeCommand(IServiceLocator serviceLocator, String commandId) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
		((IHandlerService) serviceLocator.getService(IHandlerService.class)).executeCommand(commandId, new Event());
	}

	private Action createAction(final IServiceLocator serviceLocator, String label, final String commandId,
			String imagePath) {
		Action action = new Action(label, SWT.NONE) {
			@Override
			public void run() {
				try {
					executeCommand(serviceLocator, commandId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		action.setImageDescriptor(ResourceManager.getImageDescriptor(JythonControlsFactory.class,
				imagePath));
		return action;
	}
}
