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

package uk.ac.gda.client.experimentdefinition.components;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.util.io.FileUtils;

/**
 * Class defines the nature of an exafs project. In this way other projects can be created and set to be exafs projects.
 * NOTE: This nature should be only added to existing (ie already created) and open projects so that the configure()
 * function can create default Scan Parameters
 */
public class ExperimentProjectNature implements IProjectNature {

	private static Logger logger = LoggerFactory.getLogger(ExperimentProjectNature.class);

	public static String ID = "uk.ac.gda.client.experimentdefinition.ExperimentNature";

	private IProject project;

	@Override
	public void configure() throws CoreException {

		if (project == null)
			return;

		// Create the default files if none are there.
		final File exafsProjectFolder = project.getLocation().toFile();

		if (noFolders(exafsProjectFolder)) {

			final IFolder folder = project.getFolder("Experiment_1");
			folder.create(true, true, null);
			try {
				final IExperimentObjectManager multiMan = ExperimentProjectNature.createNewScan(folder, "MultipleScan_");
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						final IExperimentEditorManager edMan = ExperimentFactory
								.getExperimentEditorManager();
						edMan.setSelected(multiMan.getExperimentList().get(0));
					}
				});

			} catch (Exception e) {
				logger.error("Cannot create default experiment and associated files.", e);
			}
		}
	}

	private boolean noFolders(File exafsProjectFolder) {

		final File[] fa = exafsProjectFolder.listFiles();
		if (fa == null)
			return true;

		for (int i = 0; i < fa.length; i++) {
			if (fa[i].isDirectory()) {
				return false;
			}
		}
		// There are no folders
		return true;
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

	public static IExperimentObjectManager createNewScan(final IFolder dir) throws Exception {
		return ExperimentProjectNature.createNewScan(dir, "MultipleScan_");
	}

	/**
	 * Call to create a default scan in a folder. Does not open editors. Setting scanName also assumes that name is
	 * unique - use with care.
	 * 
	 * @param dir
	 * @return the ScanObjectManager
	 * @throws Exception
	 */
	private static IExperimentObjectManager createNewScan(final IFolder dir, final String name)
			throws Exception {

		if (!dir.exists()) {
			dir.create(true, true, null);
		}

		final File scan = FileUtils.getUnique(dir.getLocation().toFile(), name, "scan", 1);
		String scanName = "Scan_1";
		if (!name.equals("MultipleScan_")) {
			scanName = "Scan_" + scan.getName().charAt(scan.getName().length() -1);
		}

		final IExperimentObjectManager man = ExperimentFactory.getManager(dir.getFile(scan.getName()));
		man.createNewExperiment(scanName); // this will make the create the files as well
		man.write();
		return man;
	}

	/**
	 * Call to create a New empty scan in a folder. Does not open editors. Setting scanName also assumes that name is
	 * unique - use with care.
	 * 
	 * @param dir
	 * @return the ScanObjectManager
	 * @throws Exception
	 */
	public static IExperimentObjectManager createNewEmptyScan(final IFolder dir, final String name, String scanName)
			throws Exception {

		if (!dir.exists())
			dir.create(true, true, null);

		final File scan = scanName == null ? FileUtils.getUnique(dir.getLocation().toFile(), name, "scan", 1)
				: new File(dir.getLocation().toFile(), name + ".scan");

		final IExperimentObjectManager man = ExperimentFactory.getManager(dir.getFile(scan.getName()));
		return man;
	}
}
