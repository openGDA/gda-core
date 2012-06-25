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

package uk.ac.gda.client.tomo;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;
import uk.ac.gda.tomography.parameters.Parameters;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersFactory;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;
import uk.ac.gda.tomography.parameters.util.TomoParametersResourceFactoryImpl;

/**
 *
 */
public class TomoAlignmentConfigurationHolder {

	private ResourceSet resourceSet;
	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentViewController.class);

	private TomoAlignmentConfigurationHolder() {

	}

	private static TomoAlignmentConfigurationHolder configHolder;

	public static Resource getAlignmentConfigResource(IProgressMonitor monitor, boolean shouldCreate)
			throws CoreException {
		if (configHolder == null) {
			configHolder = new TomoAlignmentConfigurationHolder();
		}

		return configHolder.getTomographyConfigurationResource(monitor, shouldCreate);

	}

	private Resource getTomographyConfigurationResource(IProgressMonitor monitor, boolean shouldCreate)
			throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject project = root.getProject(".tomoAlignment");

		if (!project.exists() && !shouldCreate) {
			return null;
		}

		if (!project.exists()) {
			try {
				project.create(monitor);
			} catch (CoreException e) {
				logger.error("TODO put description of error here", e);
			}
			project.open(monitor);
		} else if (!project.isAccessible()) {
			project.open(monitor);
		}

		IFile file = project.getFile("alignment.tomoparameters");
		Resource res = null;

		if (!file.exists() && !shouldCreate) {
			return null;
		}

		if (!file.exists()) {
			res = createResource(file.getLocation().toOSString());
			TomoExperiment experiment = TomoParametersFactory.eINSTANCE.createTomoExperiment();
			Parameters parameters = TomoParametersFactory.eINSTANCE.createParameters();
			experiment.setParameters(parameters);
			res.getContents().add(experiment);
		} else {
			res = getResourceSet().getResource(URI.createFileURI(file.getLocation().toOSString()), true);
		}
		return res;
	}

	/**
	 * @param fileName
	 * @return {@link Resource}
	 */
	private Resource createResource(String fileName) {
		ResourceSet rSet = getResourceSet();
		// Get the URI of the model file.
		//
		URI fileURI = URI.createFileURI(fileName);
		// Create a resource for this file.
		//
		return rSet.createResource(fileURI);
	}

	/**
	 * @return {@link ResourceSet}
	 */
	protected ResourceSet getResourceSet() {
		if (resourceSet == null) {
			resourceSet = new ResourceSetImpl();
			// To initialize the resourceset resource factory registry with the excalibur config package
			EPackage.Registry.INSTANCE.put(TomoParametersPackage.eNS_URI, TomoParametersPackage.eINSTANCE);
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("tomoparameters",
					new TomoParametersResourceFactoryImpl());
		}
		return resourceSet;
	}
}
