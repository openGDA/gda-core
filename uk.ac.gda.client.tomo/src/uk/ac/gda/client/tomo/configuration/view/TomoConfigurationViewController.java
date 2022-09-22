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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.IScanResolutionLookupProvider;
import uk.ac.gda.client.tomo.TomoViewController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraHandler;
import uk.ac.gda.epics.client.views.model.AdBaseModel;
import uk.ac.gda.tomography.parameters.TomoExperiment;

public class TomoConfigurationViewController extends TomoViewController {
	private static final Logger logger = LoggerFactory.getLogger(TomoConfigurationViewController.class);

	private AdBaseModel adBaseModel;

	private ICameraHandler cameraHandler;

	private IScanResolutionLookupProvider scanResolutionLookupProvider;

	public void setScanResolutionLookupProvider(IScanResolutionLookupProvider scanResolutionLookupProvider) {
		this.scanResolutionLookupProvider = scanResolutionLookupProvider;
	}

	private class TomoScan extends Job {

		private final String configFilePath;

		public TomoScan(String configFilePath) {
			super("Queing Alignment Configurations");
			this.configFilePath = configFilePath;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IStatus status = Status.OK_STATUS;
			try {
				getScanController().runScan(configFilePath);
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
		TomoScan tomoScan = new TomoScan(tomoExperiment.eResource().getURI().toFileString());
		tomoScan.schedule();
	}

	public void stopScan() {
		getScanController().stopScan();
	}

	public String getDetectorPortName() throws Exception {
		return adBaseModel.getPortName();
	}

	public void setAdBaseModel(AdBaseModel adBaseModel) {
		this.adBaseModel = adBaseModel;
	}

	public void reset() throws Exception {
		cameraHandler.reset();
	}

	public void setCameraHandler(ICameraHandler cameraHandler) {
		this.cameraHandler = cameraHandler;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (cameraHandler == null) {
			throw new IllegalArgumentException("'cameraHandler' needs to be provided");
		}
	}
	
	public int getNumberOfProjections(int resolution) throws Exception{
		return scanResolutionLookupProvider.getNumberOfProjections(resolution);
	}

}