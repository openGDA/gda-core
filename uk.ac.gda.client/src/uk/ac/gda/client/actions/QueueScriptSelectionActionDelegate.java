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

package uk.ac.gda.client.actions;

import gda.commandqueue.JythonScriptFileCommandProvider;
import gda.commandqueue.Queue;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.CommandQueueViewFactory;

public class QueueScriptSelectionActionDelegate implements IActionDelegate {
	private static final Logger logger = LoggerFactory.getLogger(QueueScriptSelectionActionDelegate.class);

	IFile fileSelected;

	@Override
	public void run(IAction action) {
		try {
			if( fileSelected != null){
				Queue queue = CommandQueueViewFactory.getQueue();
				if (queue != null) {
						queue.addToTail(new JythonScriptFileCommandProvider(fileSelected.getLocation().toFile().getAbsolutePath()));
				}
			}
		} catch (Exception e) {
			logger.error("Error in QueueScriptSelectionActionDelegate.run",e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(isEnabled(selection));
	}

	private boolean isEnabled(ISelection selection) {
		if( CommandQueueViewFactory.getQueue() == null)
			return false;
		fileSelected = null;
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement != null) {
				if (firstElement instanceof IFile) {
					fileSelected = (IFile) firstElement;
				}
			}
		}
		return fileSelected!=null;
	}

}
