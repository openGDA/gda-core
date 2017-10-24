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

package uk.ac.gda.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.GlobalBuildAction;

import uk.ac.gda.common.rcp.util.EclipseUtils;

/**
 * Calls a rebuild, getting any build handlers to recheck the project.
 */
public class RebuildHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchPage page = EclipseUtils.getActivePage();
			if (page != null) {
				page.saveAllEditors(false);
			}

			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			GlobalBuildAction build = new GlobalBuildAction(window, IncrementalProjectBuilder.FULL_BUILD);
			build.doBuild();

			return Boolean.TRUE;

		} catch (Exception ne) {
			throw new ExecutionException("Error handling rebuild", ne);

		}
	}

}
