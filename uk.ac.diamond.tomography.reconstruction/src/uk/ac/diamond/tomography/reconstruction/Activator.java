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
package uk.ac.diamond.tomography.reconstruction;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsFactory;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.util.ReconresultsResourceImpl;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	public static final String NXS_FILE_EXTN = "nxs";
	
	private static final String RECON_RESULTS_FILE_NAME = "list.reconresults";


	public static final String PREF_COARSE_TOTAL_STEPS = "pref_coarse_total_steps";

	public static final String PREF_COARSE_STEP_SIZE = "pref_coarse_step_size";

	public static final String PREF_FINE_TOTAL_STEPS = "pref_fine_total_steps";

	public static final String PREF_FINE_STEP_SIZE = "pref_fine_step_size";

	public static final String PREF_VERY_FINE_TOTAL_STEPS = "pref_very_fine_total_steps";

	public static final String PREF_VERY_FINE_STEP_SIZE = "pref_very_fine_step_size";

	private EditingDomain reconResultsEditingDomain;
	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.diamond.tomography.reconstruction"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	public static final String PROJECT_TOMOGRAPHY_SETTINGS = "Tomography Files";

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		for (String imgPath : ImageConstants.IMAGES) {
			reg.put(imgPath, imageDescriptorFromPlugin(PLUGIN_ID, imgPath));
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	public IProject getTomoFilesProject() {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_TOMOGRAPHY_SETTINGS);
		if (!project.exists()) {
			try {
				new WorkspaceModifyOperation() {

					@Override
					protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
							InterruptedException {
						try {
							project.create(monitor);
						} catch (IllegalArgumentException ex) {
							logger.debug("Problem identified - eclipse doesn't refresh the right folder");
						}
					}
				}.run(new NullProgressMonitor());
			} catch (InvocationTargetException e) {
				logger.error("Unable to create project", e);
			} catch (InterruptedException e) {
				logger.error("unable to create project - interrupted.", e);
			}
		}
		if (!project.isAccessible()) {
			try {
				new WorkspaceModifyOperation() {

					@Override
					protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
							InterruptedException {
						try {
							project.open(monitor);
						} catch (IllegalArgumentException ex) {
							logger.debug("Problem identified - eclipse doesn't refresh the right folder");
						}
					}
				}.run(new NullProgressMonitor());
			} catch (InvocationTargetException e) {
				logger.error("Unable to open project", e);
			} catch (InterruptedException e) {
				logger.error("Unable to open project - interrupted.", e);
			}
		}
		return project;
	}

	public EditingDomain getReconResultsEditingDomain() {
		if (reconResultsEditingDomain == null) {

			// Create an adapter factory that yields item providers.
			//
			ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
					ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

			BasicCommandStack commandStack = new BasicCommandStack();

			reconResultsEditingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack,
					new HashMap<Resource, Boolean>());

		}
		return reconResultsEditingDomain;
	}
	
	public Resource getReconResultsResource() {
		IProject tomoSettingsProject = Activator.getDefault().getTomoFilesProject();
		IFile reconResultsFile = tomoSettingsProject.getFile(RECON_RESULTS_FILE_NAME);

		Resource reconResultsRes = null;
		ResourceSet resourceSet = Activator.getDefault().getReconResultsEditingDomain().getResourceSet();
		URI uri = URI.createPlatformResourceURI(reconResultsFile.getFullPath().toOSString(), true);
		if (!reconResultsFile.exists()) {
			reconResultsRes = resourceSet.createResource(uri);

			uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults reconResults = ReconresultsFactory.eINSTANCE
					.createReconResults();

			reconResultsRes.getContents().add(reconResults);

			final Resource reconResToSave = reconResultsRes;
			try {
				new WorkspaceModifyOperation() {

					@Override
					protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
							InterruptedException {
						Map<Object, Object> defaultSaveOptions = ((ReconresultsResourceImpl) reconResToSave)
								.getDefaultSaveOptions();

						try {
							reconResToSave.save(defaultSaveOptions);
						} catch (IOException e) {
							logger.error("Problem creating resource for recon results", e);
						}
					}
				}.run(new NullProgressMonitor());
			} catch (InvocationTargetException e) {
				logger.error("Problem creating recon results resource", e);
			} catch (InterruptedException e) {
				logger.error("Interrupted creating recon results resource", e);
			}

		} else {
			reconResultsRes = resourceSet.getResource(uri, true);
		}

		return reconResultsRes;

	}

}
