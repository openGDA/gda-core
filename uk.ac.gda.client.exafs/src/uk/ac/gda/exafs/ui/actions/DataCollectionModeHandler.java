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

package uk.ac.gda.exafs.ui.actions;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.components.ExperimentPerspective;
import uk.ac.gda.exafs.ui.AlignmentPerspective;
import uk.ac.gda.exafs.ui.PlottingPerspective;
import uk.ac.gda.perspectives.DataExplorationPerspective;
import uk.ac.gda.pydev.extension.ui.perspective.JythonPerspective;

/**
 * This class is here because Sofia requested action exist to switch between perspective groups.
 */
public class DataCollectionModeHandler extends AbstractHandler implements IWorkbenchWindowActionDelegate, IEditorActionDelegate   {

	private static final Logger logger = LoggerFactory.getLogger(DataCollectionModeHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		return doDataCollectionMode();		
	}

	/**
	 * Called by testing.
	 * @return boolean
	 */
	public static boolean doDataCollectionMode() {
		
		IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IPerspectiveDescriptor[] descriptors = win.getActivePage().getSortedPerspectives();

		try {
			PlatformUI.getWorkbench().showPerspective(JythonPerspective.ID, win);
			PlatformUI.getWorkbench().showPerspective(DataExplorationPerspective.ID, win);
			PlatformUI.getWorkbench().showPerspective(PlottingPerspective.ID, win);
			PlatformUI.getWorkbench().showPerspective(ExperimentPerspective.ID, win);
		} catch (WorkbenchException e) {
			logger.error("Cannot open " + AlignmentPerspective.ID, e);
			return Boolean.FALSE;
		}

		for (IPerspectiveDescriptor desc : descriptors) {
			if (!(desc.getId().equals(JythonPerspective.ID) || desc.getId().equals(DataExplorationPerspective.ID)
					|| desc.getId().equals(PlottingPerspective.ID) | desc.getId().equals(ExperimentPerspective.ID))) {
				win.getActivePage().closePerspective(desc, true, true);
			}
		}

		return Boolean.TRUE;
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void run(IAction action) {
		try {
			execute(null);
		} catch (ExecutionException e) {
			logger.error("Cannot switch to alignment.", e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		
	}


}
