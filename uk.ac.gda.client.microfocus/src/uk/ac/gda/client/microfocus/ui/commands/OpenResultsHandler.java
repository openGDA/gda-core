/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.ui.commands;

import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.IResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.ExperimentCommandBean;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.ui.handlers.AbstractOpenResultsHandler;
import uk.ac.gda.client.microfocus.ui.MicroFocusPerspective;
import uk.ac.gda.exafs.ui.data.ScanObject;

/**
 * An {@link IResultHandler} to open results of an {@link ExperimentCommandBean} for a microfocus scan.
 * This handles the 'Open Results' action in the StatusQueueView.
 */
public class OpenResultsHandler extends AbstractOpenResultsHandler {

	private static final Logger logger = LoggerFactory.getLogger(OpenResultsHandler.class);

	@Override
	public boolean isHandled(StatusBean bean) {
		// we handle ExperimentCommandBeans for microfocus scans only
		if (bean instanceof ExperimentCommandBean) {
			IExperimentObject object = getExperimentObject((ExperimentCommandBean) bean);
			try {
				if (((ScanObject) object).isMicroFocus()) {
					return true;
				}
			} catch (Exception e) {
				logger.error("An error occurred getting the type of scan: " + object.getRunName());
			}
		}
		return false;
	}

	@Override
	protected void showResults(IExperimentObject experimentObject) {
		// TODO mattd 2016-08-12: at present we only switch to the microfocus perspective.
		// Ideally we would then show the results for this scan in that perspective. However
		// this would be tricky to do, and this code is only temporary anyway as microfocus
		// scans will be replaced by the mapping project.
		super.showResults(experimentObject);
	}

	@Override
	protected String getPerspectiveIdToOpen(IExperimentObject experimentObject) {
		return MicroFocusPerspective.ID;
	}

}
