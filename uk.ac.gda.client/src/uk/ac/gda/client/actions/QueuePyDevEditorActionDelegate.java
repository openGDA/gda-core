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

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.common.rcp.util.EclipseUtils;

public class QueuePyDevEditorActionDelegate implements IEditorActionDelegate {
	private static final Logger logger = LoggerFactory.getLogger(QueuePyDevEditorActionDelegate.class);

	private IEditorPart ePart;
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null){
			ePart = targetEditor;		
		}
	}

	@Override
	public void run(IAction action) {
		try{
			if (ePart != null){
				IEditorInput input = ePart.getEditorInput();
				final File fileToRun = EclipseUtils.getFile(input);
				Queue queue = CommandQueueViewFactory.getQueue();
				if( queue != null) {
					queue.addToTail(new JythonScriptFileCommandProvider(fileToRun.getAbsolutePath()));
				}
			}
		} catch (Exception e){
			logger.error("QueuePyDevEditorActionDelegate.run", e);
			
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(isEnabled(selection));
	}
	
	private boolean isEnabled(@SuppressWarnings("unused") ISelection selection) {
		if( CommandQueueViewFactory.getQueue() == null)
			return false;
		return true;
	}
}
