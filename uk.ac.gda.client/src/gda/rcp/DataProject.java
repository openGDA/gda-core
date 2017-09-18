/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.rcp;

import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import uk.ac.diamond.scisoft.analysis.rcp.GDADataNature;
import uk.ac.gda.preferences.PreferenceConstants;
import uk.ac.gda.preferences.PreferenceInitializer;
import uk.ac.gda.ui.utils.ProjectUtils;
import uk.ac.gda.ui.utils.ResourceFilterWrapper;

/**
 * This class holds a reference to the data project, so the logic for finding it does not have to be replicated in other places
 */
public class DataProject {
	private static final Logger logger = LoggerFactory.getLogger(DataProject.class);

	private static IPreferenceStore preferenceStore;

	private static IPreferenceStore getPreferenceStore() {
		if (preferenceStore == null)
			preferenceStore = GDAClientActivator.getDefault().getPreferenceStore();
		return preferenceStore;
	}

	/**
	 *
	 * @return name of project (no guarantee if that exists)
	 */
	public static String getDataProjectName() {
		String projName = getPreferenceStore().getString(PreferenceConstants.GDA_DATA_PROJECT_NAME);
		if( projName.equals(PreferenceInitializer.DATA_PROJECT_NAME_AS_VISIT)){
			projName = LocalProperties.get(LocalProperties.RCP_APP_VISIT, "Data");
		}
		return projName;
	}

	/**
	 *
	 * @return data project or null
	 */
	public static IProject getDataProjectIfExists() {
		IProject dataProject = ResourcesPlugin.getWorkspace().getRoot().getProject(getDataProjectName());
		if (dataProject.exists())
			return dataProject;
		return null;
	}

	/**
	 * create the data project as configured
	 *
	 * @param monitor
	 * @return data project
	 * @throws CoreException
	 */
	public static IProject create(IProgressMonitor monitor) throws CoreException {
		String projName = getDataProjectName();

		IProject project = getDataProjectIfExists();
		if (project != null)
			return project;

		return createDataProject(monitor, projName, PathConstructor.getClientVisitDirectory(),
			preferenceStore.getString(PreferenceConstants.GDA_DATA_PROJECT_FILTER),
			preferenceStore.getBoolean(PreferenceConstants.GDA_DATA_PROJECT_FILTER_IS_EXCLUDE));
	}

	private static IProject createDataProject(IProgressMonitor monitor, String projName, String dataPath, String filter, boolean excludes)
			throws CoreException {

			monitor.subTask("Creating Data project");
			List<ResourceFilterWrapper> resourceFilterWrapper = null;
			if (filter != null && !filter.isEmpty()) {
				resourceFilterWrapper = new Vector<ResourceFilterWrapper>();
				resourceFilterWrapper.add(ResourceFilterWrapper.createRegexFolderFilter(filter, true, excludes));
				resourceFilterWrapper.add(ResourceFilterWrapper.createRegexFolderFilter(filter, false, excludes));
			}
			String folderName="data";
			if (LocalProperties.check(LocalProperties.GDA_SHOW_VISIT_NAME_AS_DATA_FOLDER_NAME, false)) {
				//show visit name as data folder name
				folderName = LocalProperties.get(LocalProperties.RCP_APP_VISIT, "data");
				if (folderName.equalsIgnoreCase("data")) {
					logger.warn("Cannot find visit name!");
				}
			}
			return ProjectUtils.createImportProjectAndFolder(projName, folderName,dataPath,
					GDADataNature.ID, resourceFilterWrapper, monitor);
	}

	/**
	 * creates the data project if configured to do so during startup
	 * @param monitor
	 * @return data project
	 */
	public static IProject createOnStartup(IProgressMonitor monitor) {
		if (getPreferenceStore().getBoolean(PreferenceConstants.GDA_DATA_PROJECT_CREATE_ON_STARTUP)) {
			try {
				return create(monitor);
			} catch (CoreException e) {
				logger.error("Error creating data project", e);
			}
		}
		return null;
	}
}