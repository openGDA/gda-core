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

package uk.ac.gda.client.tomo.configuration.view;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraMotionController;
import uk.ac.gda.client.tomo.configuration.view.handlers.ITomoScanController;
import uk.ac.gda.client.tomo.configuration.view.handlers.IScanControllerUpdateListener;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.TomoExperiment;

public class TomoConfigurationViewController implements InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(TomoConfigurationViewController.class);

	private ICameraMotionController cameraMotionController;

	private ICameraModuleController cameraModuleController;

	private ITomoScanController scanController;

	public ICameraMotionController getCameraMotionController() {
		return cameraMotionController;
	}

	public void setCameraMotionController(ICameraMotionController cameraMotionController) {
		this.cameraMotionController = cameraMotionController;
	}

	public ICameraModuleController getCameraModuleController() {
		return cameraModuleController;
	}

	public void setCameraModuleController(ICameraModuleController cameraModuleController) {
		this.cameraModuleController = cameraModuleController;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (cameraModuleController == null) {
			throw new IllegalArgumentException("cameraModuleController should be provided");
		}

		if (cameraMotionController == null) {
			throw new IllegalArgumentException("cameraMotionController should be provided");
		}
	}

	private class TomoScan extends Job {

		private final List<AlignmentConfiguration> configurationSet;

		public TomoScan(List<AlignmentConfiguration> configurationSet) {
			super("Queing Alignment Configurations");
			this.configurationSet = configurationSet;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IStatus status = Status.OK_STATUS;
			try {
				getScanController().runScan(configurationSet);
			} catch (Exception e) {
				status = Status.CANCEL_STATUS;
			} finally {
				monitor.done();
			}

			return status;
		}
	}

	public void startScan(TomoExperiment tomoExperiment) {
		logger.debug("Queueing scan configurations");
		final List<AlignmentConfiguration> configurationSet = tomoExperiment.getParameters().getConfigurationSet();
		TomoScan tomoScan = new TomoScan(configurationSet);
		tomoScan.schedule();
	}

	public void dispose() {
		getScanController().dispose();
	}

	public void isScanRunning() {
		getScanController().isScanRunning();
	}

	public void addScanControllerUpdateListener(IScanControllerUpdateListener scanControllerUpdateListener) {
		getScanController().addControllerUpdateListener(scanControllerUpdateListener);
	}

	public void removeScanControllerUpdateListener(IScanControllerUpdateListener scanControllerUpdateListener) {
		getScanController().removeControllerUpdateListener(scanControllerUpdateListener);
	}

	public ITomoScanController getScanController() {
		return scanController;
	}

	public void setScanController(ITomoScanController scanController) {
		this.scanController = scanController;
	}

	public void initialize() {
		scanController.initialize();
	}

}