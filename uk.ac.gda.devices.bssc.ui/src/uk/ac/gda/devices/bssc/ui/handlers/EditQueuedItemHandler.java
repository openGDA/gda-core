/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.ui.handlers;

import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandDetailsPath;
import gda.commandqueue.Queue;
import gda.commandqueue.QueuedCommandSummary;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IStorage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.QueueEntry;
import uk.ac.gda.richbeans.xml.string.StringInput;
import uk.ac.gda.richbeans.xml.string.StringStorage;

public class EditQueuedItemHandler implements IHandler {
	private static final Logger logger = LoggerFactory.getLogger(EditQueuedItemHandler.class);

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		QueuedCommandSummary qCmdSummary = null;

		Queue queue = CommandQueueViewFactory.getQueue();

		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		Object obj = context.getDefaultVariable();
		List<?> list = (List<?>) obj;

		if (list.get(0) instanceof QueueEntry) {
			final QueueEntry item = (QueueEntry) list.get(0);

			qCmdSummary = item.getQueueCommandSummary();
		}

		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			if (qCmdSummary != null) {
				CommandDetails commandDetails = queue.getCommandDetails(qCmdSummary.id);

				if (commandDetails instanceof CommandDetailsPath) {
					String path = ((CommandDetailsPath) commandDetails).getPath();

					File fileToOpen = new File(path);

					try {
						IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
						IEditorPart part = IDE.openEditorOnFileStore(page, fileStore);
						// part.addPropertyListener(new CommandDetailsEditorListener(tableViewer));
					} catch (PartInitException e) {
						logger.error("Error opening file " + path, e);
					}
				} else {
					String description = queue.getCommandSummary(qCmdSummary.id).getDescription();
					IStorage storage = new StringStorage(commandDetails.getSimpleDetails(), description);
					IStorageEditorInput input = new StringInput(storage);
					if (page != null) {
						IEditorPart part = page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
						// part.addPropertyListener(new CommandDetailsEditorListener(tableViewer));
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error showing command details", ex);
		}

		return null;
	}

	@Override
	public boolean isEnabled() {
		// Need to add condition here so that this menu is only enabled if an experiment is selected in the queue
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}
}
