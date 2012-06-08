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

package uk.ac.gda.client.experimentdefinition.ui.handlers;


import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentExperimentView;
import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;


/**
 * Refreshes the project
 */
public class RefreshProjectAction extends AbstractExperimentCommandHandler {

	/**
	 * 
	 */
	//public static final String ID = "uk.ac.gda.exafs.ui.actions.RefreshProjectAction";
	public static final String ID ="uk.ac.gda.experimentdefinition.ui.actions.RefreshProjectAction";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
        final IExperimentEditorManager man = ExperimentFactory.getExperimentEditorManager();
        if (man == null) return false;
        
        final IProject project = man.getCurrentProject();
        try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new ExecutionException("Cannot refresh project.", e);
		}
		
		// If ExperimentExperimentView selected, refresh that.
		final ExperimentExperimentView ev = man.getViewer();
		if (ev != null) ev.refreshTree();
        
		final ExperimentFolderEditor fe = man.getActiveFolderEditor();
		if (fe != null) fe.refresh();
 
		return true;
	}

}
