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

package uk.ac.gda.exafs.ui.actions;

import gda.exafs.scan.RepetitionsProperties;
import gda.jython.InterfaceProvider;

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

public class SkipRepetition extends AbstractHandler implements IWorkbenchWindowActionDelegate, IEditorActionDelegate {

	private static final Logger logger = LoggerFactory.getLogger(SkipRepetition.class);
	
	@Override
	public void run(IAction arg0) {
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		
		logger.debug("SkipRepetition button pressed");
		
		InterfaceProvider.getTerminalPrinter().print(
				"** Request made to skip the current repetition and if available move on to the next one.");
		InterfaceProvider.getCommandRunner().runCommand(
				"LocalProperties.set(\"" + RepetitionsProperties.SKIP_REPETITION_PROPERTY + "\",\"true\")");
		InterfaceProvider.getCurrentScanController().requestFinishEarly();
		return null;
	}

	@Override
	public void setActiveEditor(IAction arg0, IEditorPart arg1) {
	}

	@Override
	public void init(IWorkbenchWindow arg0) {
	}

}
