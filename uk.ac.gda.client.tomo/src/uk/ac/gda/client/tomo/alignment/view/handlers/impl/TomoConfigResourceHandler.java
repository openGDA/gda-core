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
import gda.jython.authenticator.UserAuthentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITomoConfigResourceHandler;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.tomography.TomographyResourceUtil;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.DetectorBin;
import uk.ac.gda.tomography.parameters.DetectorProperties;
import uk.ac.gda.tomography.parameters.DetectorRoi;
import uk.ac.gda.tomography.parameters.Module;
import uk.ac.gda.tomography.parameters.MotorPosition;
import uk.ac.gda.tomography.parameters.Resolution;
import uk.ac.gda.tomography.parameters.SampleWeight;
import uk.ac.gda.tomography.parameters.StitchParameters;
import uk.ac.gda.tomography.parameters.TomoExperiment;
import uk.ac.gda.tomography.parameters.TomoParametersFactory;

/**
 *
 */
public class TomoConfigResourceHandler implements ITomoConfigResourceHandler, InitializingBean {

	// private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	//
	private static final Logger logger = LoggerFactory.getLogger(TomoConfigResourceHandler.class);

	private String fileLocation;

	private TomographyResourceUtil resourceUtil;

	public TomoConfigResourceHandler() {
		resourceUtil = new TomographyResourceUtil();
	}

	@Override
	public void dispose() {

	}

	@Override
	public String saveConfiguration(IProgressMonitor monitor, final SaveableConfiguration saveableConfiguration)
			throws Exception {

		TomoExperiment experiment = getTomoConfigResource(monitor, true);

		if (experiment != null) {
			AddTomoExperimentCommand command = new AddTomoExperimentCommand(TomoClientActivator.getDefault()
					.getTomoConfigEditingDomain(), experiment, saveableConfiguration);

			TomoClientActivator.getDefault().getTomoConfigEditingDomain().getCommandStack().execute(command);
			Collection<?> result = TomoClientActivator.getDefault().getTomoConfigEditingDomain().getCommandStack()
					.getMostRecentCommand().getResult();

			String expConfigId = null;

			if (!result.isEmpty()) {
				expConfigId = result.iterator().next().toString();
			}
			resourceUtil.saveResource(experiment);
			return expConfigId;
		}

		return null;

	}

	private class AddTomoExperimentCommand implements Command {

		private final SaveableConfiguration saveableConfiguration;
		private final TomoExperiment experiment;
		private String configurationId;

		public AddTomoExperimentCommand(EditingDomain ed, TomoExperiment experiment,
				SaveableConfiguration saveableConfiguration) {
			this.experiment = experiment;
			this.saveableConfiguration = saveableConfiguration;
		}

		@Override
		public Collection<?> getResult() {
			return Collections.singletonList(configurationId);
		}

		@Override
		public boolean canExecute() {
			return true;
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public void execute() {

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
			configurationId = expConfiguration.getId();

		}

		@Override
		public void undo() {
			AlignmentConfiguration alignmentConfiguration = experiment.getParameters().getAlignmentConfiguration(
					configurationId);
			if (alignmentConfiguration != null) {
				experiment.getParameters().getConfigurationSet().remove(alignmentConfiguration);
			}
		}

		@Override
		public void redo() {
			execute();
		}

		@Override
		public Collection<?> getAffectedObjects() {
			return Collections.singletonList(experiment.getParameters());
		}

		@Override
		public String getLabel() {
			return "Add Tomo Experiment";
		}

		@Override
		public String getDescription() {
			return "Add Tomo Experiment";
		}

		@Override
		public void dispose() {

		}

		@Override
		public Command chain(Command command) {
			return null;
		}

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
	public TomoExperiment getTomoConfigResource(IProgressMonitor monitor, boolean shouldCreate) throws Exception {
		Resource res = resourceUtil.getResource(getResourceSet(), fileLocation, shouldCreate);

		TomoExperiment tomoExperiment = null;
		if (res != null) {
			EObject rootObject = res.getContents().get(0);
			tomoExperiment = (TomoExperiment) rootObject;
		}
		return tomoExperiment;
	}

	@Override
	public EditingDomain getEditingDomain() throws Exception {
		return TomoClientActivator.getDefault().getTomoConfigEditingDomain();
	}

	/**
	 * @return {@link ResourceSet}
	 * @throws Exception
	 */
	protected ResourceSet getResourceSet() throws Exception {
		return getEditingDomain().getResourceSet();
	}

	@Override
	public void reloadResource() throws Exception {
		TomoExperiment tomoConfigResource = getTomoConfigResource(new NullProgressMonitor(), false);
		if (tomoConfigResource != null) {
			resourceUtil.reloadResource(getResourceSet(), tomoConfigResource.eResource());
		}
	}
}
