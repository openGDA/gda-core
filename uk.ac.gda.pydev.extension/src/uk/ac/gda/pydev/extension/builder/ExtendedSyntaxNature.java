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

package uk.ac.gda.pydev.extension.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import uk.ac.gda.common.rcp.util.EclipseUtils;

public class ExtendedSyntaxNature implements IProjectNature {
	
	protected IProject project;
	
	public final static String ID = "uk.ac.gda.pydev.extension.ExtendedJythonNature";

	@Override
	public void configure() throws CoreException {
		EclipseUtils.addBuilderToProject(project, ExtendedSyntaxBuilder.ID, null);

	}

	@Override
	public void deconfigure() throws CoreException {
		EclipseUtils.removeBuilderFromProject(project, ExtendedSyntaxBuilder.ID, null);
	}

	/**
	 * @return Returns the project.
	 */
	@Override
	public IProject getProject() {
		return project;
	}

	/**
	 * @param project The project to set.
	 */
	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
