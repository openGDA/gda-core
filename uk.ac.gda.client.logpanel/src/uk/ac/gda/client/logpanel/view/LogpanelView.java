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

package uk.ac.gda.client.logpanel.view;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.google.common.base.Optional;

import uk.ac.gda.client.logpanel.commands.CopyToClipboardHandler;

public class LogpanelView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "uk.ac.gda.client.logpanel.view";

	private Logpanel logpanel;
	public Logpanel getLogpanel() {
		return logpanel;
	}

	private IStatusLineManager statusLineManager;
	public IStatusLineManager getStatusLineManager() {
		return statusLineManager;
	}

	@Override
	public void createPartControl(Composite parent) {
		logpanel = new Logpanel(parent, SWT.NONE);
		setupStatusLine(logpanel, true); //TODO e property
		setupCommandActivationAndDeactivationHandlers(logpanel);
	}

	protected void setupStatusLine(final Logpanel logpanel, boolean setStatusToLatestMessageFirstLine) {
		statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		final String defaultMessage = "Configured to receive messages from log server " + logpanel.getLogServerAddress();
		statusLineManager.setMessage(defaultMessage);
		if (!setStatusToLatestMessageFirstLine) return;
		final IObservableList input = logpanel.getInput();
		input.addListChangeListener(new IListChangeListener() {
			@Override
			public void handleListChange(ListChangeEvent event) {
				String message = defaultMessage;
				if (!input.isEmpty()) {
					Optional<String> firstLineOfLatestMessage = logpanel.getLatestMessageFirstLine();
					if (firstLineOfLatestMessage.isPresent()) {
						message = "Latest: " + firstLineOfLatestMessage.get();
					}
				}
				statusLineManager.setMessage(message);
			}
		});
	}

	protected void setupCommandActivationAndDeactivationHandlers(final Logpanel logpanel) {
		// activate/deactivate Copy command depending on whether any log messages are selected or not
		ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class); // or PlatformUI.getWorkbench().getService(ICommandService.class);
		final String copyCommandId = CopyToClipboardHandler.ID;
		Command copyCommand = commandService.getCommand(copyCommandId);
		IHandler copyCommandHandler = copyCommand.getHandler();
		final IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class); // or PlatformUI.getWorkbench().getService(IHandlerService.class);
		final IHandlerActivation copyCommandHandlerActivation = handlerService.activateHandler(copyCommandId, copyCommandHandler);
		handlerService.deactivateHandler(copyCommandHandlerActivation);
		logpanel.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() > 0) handlerService.activateHandler(copyCommandHandlerActivation); // or handlerService.activateHandler(copyCommandId, command.getHandler());
				else handlerService.deactivateHandler(copyCommandHandlerActivation);
			}
		});
	}

	@Override
	public void setFocus() {
		logpanel.setFocus();
	}
}
