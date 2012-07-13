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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.device.DeviceException;
import gda.jython.authenticator.UserAuthentication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITomoConfigResourceHandler;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.Parameters;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersFactory;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;
import uk.ac.gda.tomography.parameters.util.TomoParametersResourceFactoryImpl;

/**
 *
 */
public class TomoConfigResourceHandler implements ITomoConfigResourceHandler, InitializingBean {
	private static final String TOMOPARAMETERS = "tomoparameters";

	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	//
	private static final Logger logger = LoggerFactory.getLogger(TomoConfigResourceHandler.class);

	private String fileLocation;

	private ResourceSet resourceSet;

	@Override
	public void dispose() {

	}

	@Override
	public void saveConfiguration(IProgressMonitor monitor, final SaveableConfiguration saveableConfiguration)
			throws DeviceException, InvocationTargetException, InterruptedException {

		WorkspaceModifyOperation saveConfigurationOperation = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {

				Resource res = getTomoConfigResource(monitor, true);
				EObject eObject = res.getContents().get(0);
				if (eObject instanceof TomoExperiment) {
					TomoExperiment experiment = (TomoExperiment) eObject;

					AlignmentConfiguration expConfiguration = TomoParametersFactory.eINSTANCE
							.createAlignmentConfiguration();
					logger.debug("sample description:{}", saveableConfiguration.getSampleDescription());
					expConfiguration.setDescription(saveableConfiguration.getSampleDescription());

					DetectorProperties detectorProperties = TomoParametersFactory.eINSTANCE.createDetectorProperties();
					detectorProperties.setModule(saveableConfiguration.getModuleNumber());

					DetectorRoi detectorRoi = TomoParametersFactory.eINSTANCE.createDetectorRoi();
					int[] roiPoints = saveableConfiguration.getRoiPoints();
					detectorRoi.setMinX(roiPoints[0]);
					detectorRoi.setMinY(roiPoints[1]);
					detectorRoi.setMaxX(roiPoints[2]);
					detectorRoi.setMaxY(roiPoints[3]);

					detectorProperties.setDetectorRoi(detectorRoi);
					logger.warn("Need to know about the acquisition time divider");
					logger.warn("Need to know about the 3d resolution");
					logger.warn("Need to know about the detector bin value");
					logger.warn("Need to know about the number of frames per projection");

					expConfiguration.setDetectorProperties(detectorProperties);

					expConfiguration.setSampleExposureTime(saveableConfiguration.getSampleAcquisitonTime());
					expConfiguration.setFlatExposureTime(saveableConfiguration.getFlatAcquisitionTime());

					expConfiguration.setEnergy(saveableConfiguration.getEnergy());

					expConfiguration.setCreatedUserId(UserAuthentication.getUsername());
					expConfiguration.setCreatedDateTime(dateFormat.format(new Date()));
					experiment.getParameters().getConfigurationSet().add(expConfiguration);
				}

				Map<Object, Object> options = new HashMap<Object, Object>();
				options.put(XMLResource.OPTION_ENCODING, "UTF-8");
				try {
					res.save(options);
				} catch (IOException e) {
					logger.error("Exception saving the configuration model", e);
				}

			}
		};
		saveConfigurationOperation.run(monitor);

	}

	/**
	 * @param tomoConfigUri
	 * @return {@link Resource}
	 */
	private Resource createResource(URI tomoConfigUri) {
		return getResourceSet().createResource(tomoConfigUri);
	}

	/**
	 * @return {@link ResourceSet}
	 */
	protected ResourceSet getResourceSet() {
		if (resourceSet == null) {
			resourceSet = new ResourceSetImpl();
			// To initialize the resourceset resource factory registry with the excalibur config package
			EPackage.Registry.INSTANCE.put(TomoParametersPackage.eNS_URI, TomoParametersPackage.eINSTANCE);
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(TOMOPARAMETERS,
					new TomoParametersResourceFactoryImpl());
		}
		return resourceSet;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		logger.debug("File location:{}", fileLocation);
		this.fileLocation = fileLocation;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (fileLocation == null) {
			throw new IllegalStateException("'fileLocation' should be provided");
		}
	}

	@Override
	public Resource getTomoConfigResource(IProgressMonitor monitor, boolean shouldCreate)
			throws InvocationTargetException, InterruptedException {
		final URI tomoConfigUri = URI.createFileURI(fileLocation);
		Resource res = null;

		boolean fileExists = new File(fileLocation).exists();
		if (!fileExists && !shouldCreate) {
			return null;
		}

		if (!fileExists) {

			final Resource[] resources = new Resource[1];
			WorkspaceModifyOperation saveConfigurationOperation = new WorkspaceModifyOperation() {

				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
						InterruptedException {
					resources[0] = createResource(tomoConfigUri);
					TomoExperiment experiment = TomoParametersFactory.eINSTANCE.createTomoExperiment();
					Parameters parameters = TomoParametersFactory.eINSTANCE.createParameters();
					experiment.setParameters(parameters);
					resources[0].getContents().add(experiment);
					Map<Object, Object> options = new HashMap<Object, Object>();
					options.put(XMLResource.OPTION_ENCODING, "UTF-8");
					try {
						resources[0].save(options);
					} catch (IOException e) {
						logger.error("Exception saving the configuration model", e);
					}
				}
			};
			saveConfigurationOperation.run(monitor);
			res = resources[0];
		} else {
			res = getResourceSet().getResource(tomoConfigUri, true);
		}
		return res;
	}
}
