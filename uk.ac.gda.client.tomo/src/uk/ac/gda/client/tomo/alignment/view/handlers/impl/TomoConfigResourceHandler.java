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

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.jython.authenticator.UserAuthentication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITomoConfigResourceHandler;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorBin;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.Module;
import uk.ac.gda.tomography.parameters.MotorPosition;
import uk.ac.gda.tomography.parameters.Parameters;
import uk.ac.gda.tomography.parameters.Resolution;
import uk.ac.gda.tomography.parameters.SampleWeight;
import uk.ac.gda.tomography.parameters.StitchParameters;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersFactory;
import uk.ac.gda.tomography.parameters.TomoParametersPackage;
import uk.ac.gda.tomography.parameters.util.TomoParametersResourceFactoryImpl;

/**
 *
 */
public class TomoConfigResourceHandler implements ITomoConfigResourceHandler, InitializingBean {
	private static final String TOMOPARAMETERS = "tomoparameters";

	// private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	//
	private static final Logger logger = LoggerFactory.getLogger(TomoConfigResourceHandler.class);

	private String fileLocation;

	private ResourceSet resourceSet;

	@Override
	public void dispose() {

	}

	@Override
	public String saveConfiguration(IProgressMonitor monitor, final SaveableConfiguration saveableConfiguration)
			throws DeviceException, InvocationTargetException, InterruptedException {

		// IRunnableWithProgress saveConfigurationOperation = new IRunnableWithProgress() {
		//
		// @Override
		// public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		TomoExperiment experiment = getTomoConfigResource(monitor, true);

		if (experiment != null) {

			AlignmentConfiguration expConfiguration = TomoParametersFactory.eINSTANCE.createAlignmentConfiguration();

			// in beam position
			expConfiguration.setInBeamPosition(saveableConfiguration.getInBeamPosition());
			// out of beam position
			expConfiguration.setOutOfBeamPosition(saveableConfiguration.getOutOfBeamPosition());
			// proposal = visit
			expConfiguration.setProposalId(LocalProperties.get(LocalProperties.RCP_APP_VISIT));
			// energy
			expConfiguration.setEnergy(saveableConfiguration.getEnergy());
			// description
			expConfiguration.setDescription(saveableConfiguration.getSampleDescription());
			// Detector properties
			expConfiguration.setDetectorProperties(createDetectorProperties(saveableConfiguration));
			// SampleStageParameters
			addTomoAlignmentMotorPositions(saveableConfiguration.getMotorPositions(),
					expConfiguration.getMotorPositions());
			// expConfiguration.setSampleStageParameters(createSampleStage(saveableConfiguration));
			// sample exposure time
			expConfiguration.setSampleExposureTime(saveableConfiguration.getSampleAcquisitonTime());
			// flat exposure time
			expConfiguration.setFlatExposureTime(saveableConfiguration.getFlatAcquisitionTime());
			// created User Id
			expConfiguration.setCreatedUserId(UserAuthentication.getUsername());
			// created date time
			expConfiguration.setCreatedDateTime(new Date());
			// sampleWeight
			expConfiguration.setSampleWeight(getSampleWeight(saveableConfiguration.getSampleWeight()));
			// Tomo rotation axis
			expConfiguration.setTomoRotationAxis(saveableConfiguration.getTomoRotationAxis());
			// stitching theta angle
			expConfiguration.setStitchParameters(createStitchParameters(saveableConfiguration));
			// add the configuration to the parameter set
			experiment.getParameters().getConfigurationSet().add(expConfiguration);

			Map<Object, Object> options = new HashMap<Object, Object>();
			options.put(XMLResource.OPTION_ENCODING, "UTF-8");
			try {
				experiment.eResource().save(options);
			} catch (IOException e) {
				logger.error("Exception saving the configuration model", e);
			}
			return expConfiguration.getId();
		}

		return null;

		// }

		// };
		// saveConfigurationOperation.run(monitor);

	}

	private StitchParameters createStitchParameters(SaveableConfiguration saveableConfiguration) {
		StitchParameters stitchParameters = TomoParametersFactory.eINSTANCE.createStitchParameters();
		stitchParameters.setStitchingThetaAngle(saveableConfiguration.getStitchingAngle());
		stitchParameters.setImageAtTheta(saveableConfiguration.getImageLocationAtTheta());
		// Image location at theta +90
		stitchParameters.setImageAtThetaPlus90(saveableConfiguration.getImageLocationAtThetaPlus90());
		return stitchParameters;
	}

	private SampleWeight getSampleWeight(SAMPLE_WEIGHT sampleWeight) {
		switch (sampleWeight) {
		case LESS_THAN_ONE:
			return SampleWeight.LESS_THAN_1;
		case ONE_TO_TEN:
			return SampleWeight.ONE_TO_TEN;
		case TEN_TO_TWENTY:
			return SampleWeight.TEN_TO_TWENTY;
		case TWENTY_TO_FIFTY:
			return SampleWeight.TWENTY_TO_FIFTY;
		}
		return null;
	}

	private DetectorProperties createDetectorProperties(SaveableConfiguration saveableConfiguration) {
		DetectorProperties detectorProperties = TomoParametersFactory.eINSTANCE.createDetectorProperties();
		// 3d resolution
		detectorProperties.setDesired3DResolution(getResolution(saveableConfiguration.getResolution3D()));
		// number of frames per projection
		detectorProperties.setNumberOfFramerPerProjection(saveableConfiguration.getNumProjections());
		// acquisition time divider
		// TODO:acquisition time divider
		// detector roi
		DetectorRoi detectorRoi = TomoParametersFactory.eINSTANCE.createDetectorRoi();
		int[] roiPoints = saveableConfiguration.getRoiPoints();
		detectorRoi.setMinX(roiPoints[0]);
		detectorRoi.setMinY(roiPoints[1]);
		detectorRoi.setMaxX(roiPoints[2]);
		detectorRoi.setMaxY(roiPoints[3]);

		detectorProperties.setDetectorRoi(detectorRoi);
		// detector bin
		DetectorBin detectorBin = TomoParametersFactory.eINSTANCE.createDetectorBin();
		// detectorBin.setBinX(value)
		// detectorBin.setBinY(value)
		detectorProperties.setDetectorBin(detectorBin);
		// module parameters
		Module modulePosition = TomoParametersFactory.eINSTANCE.createModule();
		modulePosition.setModuleNumber(saveableConfiguration.getModuleNumber());
		modulePosition.setCameraMagnification(saveableConfiguration.getCameraMagnification());
		//
		detectorProperties.setModuleParameters(modulePosition);

		return detectorProperties;
	}

	private Resolution getResolution(RESOLUTION resolution3d) {
		switch (resolution3d) {
		case FULL:
			return Resolution.FULL;
		case TWO_X:
			return Resolution.X2;
		case FOUR_X:
			return Resolution.X4;
		case EIGHT_X:
			return Resolution.X8;
		}
		return null;
	}

	protected void addTomoAlignmentMotorPositions(
			ArrayList<uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration.MotorPosition> savedMotorPositions,
			List<MotorPosition> motorPositions) {

		for (uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration.MotorPosition motorPosition : savedMotorPositions) {
			MotorPosition mp = TomoParametersFactory.eINSTANCE.createMotorPosition();
			mp.setName(motorPosition.getName());
			mp.setPosition(motorPosition.getPosition());
			motorPositions.add(mp);
		}
		//
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
	public TomoExperiment getTomoConfigResource(IProgressMonitor monitor, boolean shouldCreate)
			throws InvocationTargetException, InterruptedException {
		final URI tomoConfigUri = URI.createFileURI(fileLocation);
		Resource res = null;

		boolean fileExists = new File(fileLocation).exists();
		if (!fileExists && !shouldCreate) {
			return null;
		}

		if (!fileExists) {

			final Resource[] resources = new Resource[1];
			IRunnableWithProgress saveConfigurationOperation = new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
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

		TomoExperiment tomoExperiment = null;
		if (res != null) {
			EObject rootObject = res.getContents().get(0);
			tomoExperiment = (TomoExperiment) rootObject;
		}
		return tomoExperiment;
	}
}
