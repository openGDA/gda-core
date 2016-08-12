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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.IHandler;
import org.eclipse.scanning.event.ui.view.StatusQueueView;

import gda.commandqueue.ExperimentCommandBean;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;

/**
 * Abstract superclass for handlers of actions in the GDA9 {@link StatusQueueView}
 * for {@link ExperimentCommandBean}s.
 */
public abstract class AbstractQueueBeanHandler implements IHandler<ExperimentCommandBean> {

	/**
	 * Returns the {@link IExperimentObject} for the given {@link ExperimentCommandBean}.
	 * @param commandBean an {@link ExperimentCommandBean}
	 * @return the {@link IExperimentObject}
	 */
	protected IExperimentObject getExperimentObject(ExperimentCommandBean commandBean) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFolder folder = root.getFolder(new Path(commandBean.getFolderPath()));

		IExperimentObjectManager objectMgr = ExperimentFactory.getManager(folder,
				commandBean.getMultiScanName());
		if (objectMgr != null) {
			for (IExperimentObject exprObj : objectMgr.getExperimentList()) {
				if (exprObj.getRunName().equals(commandBean.getRunName())) {
					return exprObj;
				}
			}
		}

		return null;
	}

	@Override
	public boolean isHandled(StatusBean bean) {
		// we only handle ExperimentCommandBeans
		return bean instanceof ExperimentCommandBean;
	}

}
