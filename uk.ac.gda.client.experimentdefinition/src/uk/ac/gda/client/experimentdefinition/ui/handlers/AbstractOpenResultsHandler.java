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

package uk.ac.gda.client.experimentdefinition.ui.handlers;

import org.eclipse.scanning.api.ui.IResultHandler;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.ExperimentCommandBean;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;

/**
 * Abstract Handler class for open results action on StatusQueueView for ExperimentCommandBeans.
 * This class is abstract as opening the results depends on the type of scan, e.g.
 * Xas or Microfocus, and switching to those perspectives. We do not have access to those
 * APIs here, so this class is extended by handlers that know how to handle a scan
 * of the appropriate type.
 */
public abstract class AbstractOpenResultsHandler extends AbstractQueueBeanHandler implements IResultHandler<ExperimentCommandBean> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractOpenResultsHandler.class);

	@Override
	public boolean open(final ExperimentCommandBean bean) throws Exception {
		IExperimentObject experimentObject = getExperimentObject(bean);
		if (experimentObject != null) {
			switchPerspective(experimentObject);
			showResults(experimentObject);
		}

		return true;
	}

	/**
	 * Show the results for the given experiment object. Does nothing by default,
	 * subclasses may override.
	 * @param experimentObject
	 */
	protected void showResults(@SuppressWarnings("unused") IExperimentObject experimentObject) {
		// does nothing by default, subclasses may override
	}

	/**
	 * Switch to the appropriate perspective for the experiment object
	 * @param experimentObject
	 */
	protected void switchPerspective(final IExperimentObject experimentObject) {
		String perspectiveId = getPerspectiveIdToOpen(experimentObject);
		if (perspectiveId != null) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			try {
				PlatformUI.getWorkbench().showPerspective(perspectiveId, window);
			} catch (WorkbenchException e) {
				logger.error("Could not open perspective: ", perspectiveId);
			}
		}
	}

	/**
	 * Returns the id of the perspective to open to show the result of the given
	 * experiment object
	 * @param experimentObject
	 * @return perspective id
	 */
	protected abstract String getPerspectiveIdToOpen(IExperimentObject experimentObject);

}
