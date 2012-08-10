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

import org.apache.commons.lang.ArrayUtils;
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

import uk.ac.diamond.scisoft.analysis.rcp.DataExplorationPerspective;
import uk.ac.gda.exafs.ui.AlignmentPerspective;
import uk.ac.gda.pydev.extension.ui.perspective.JythonPerspective;

/**
 * This class is here because Sofia requested action exist to switch between perspective groups.
 */
public class AlignmentModeHandler extends AbstractHandler implements IWorkbenchWindowActionDelegate,
		IEditorActionDelegate {

	private static final Logger logger = LoggerFactory.getLogger(AlignmentModeHandler.class);

	private static final String[] idsToShow = new String[] { DataExplorationPerspective.ID, AlignmentPerspective.ID,
			JythonPerspective.ID };

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doAlignemtMode();
	}

	public static boolean doAlignemtMode() {
		try {
			IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IPerspectiveDescriptor[] descriptors = win.getActivePage().getSortedPerspectives();

			for (String id : idsToShow) {
				PlatformUI.getWorkbench().showPerspective(id, win);
			}

			for (IPerspectiveDescriptor desc : descriptors) {
				if (!ArrayUtils.contains(idsToShow, desc.getId())) {
					win.getActivePage().closePerspective(desc, true, true);
				}
			}
		} catch (WorkbenchException e) {
			logger.error("Cannot open " + AlignmentPerspective.ID, e);
			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	@Override
	public void init(IWorkbenchWindow window) {
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
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

}
