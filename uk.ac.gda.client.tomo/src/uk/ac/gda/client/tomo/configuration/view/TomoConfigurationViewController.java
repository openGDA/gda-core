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

import uk.ac.gda.client.tomo.TomoViewController;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.TomoExperiment;

public class TomoConfigurationViewController extends TomoViewController {
	private static final Logger logger = LoggerFactory.getLogger(TomoConfigurationViewController.class);

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

	public void stopScan() {
		getScanController().stopScan();
	}

}