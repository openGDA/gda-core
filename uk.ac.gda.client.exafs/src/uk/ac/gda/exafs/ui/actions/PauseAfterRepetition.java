/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.exafs.scan.RepetitionsProperties;
import gda.jython.InterfaceProvider;
import uk.ac.gda.client.CommandQueueViewFactory;

public class PauseAfterRepetition extends AbstractHandler implements IWorkbenchWindowActionDelegate,
		IEditorActionDelegate {

	private static final Logger logger = LoggerFactory.getLogger(PauseAfterRepetition.class);

	@Override
	public void run(IAction arg0) {
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
	}

	@Override
	public boolean isEnabled() {
		// this action is disabled if we're using the StatusQueueView
		return super.isEnabled() && !LocalProperties.check(CommandQueueViewFactory.GDA_USE_STATUS_QUEUE_VIEW);
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		logger.debug("PauseAfterRepetition button pressed");

		InterfaceProvider.getCommandRunner().runCommand(
				"LocalProperties.set(\"" + RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY + "\",\"true\")");
		InterfaceProvider.getTerminalPrinter().print(
				"Request made to pause the current scan once this repetition has completed.");
		return null;
	}

	@Override
	public void setActiveEditor(IAction arg0, IEditorPart arg1) {
	}

	@Override
	public void init(IWorkbenchWindow arg0) {
	}

}
