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

import org.eclipse.scanning.api.ui.IModifyHandler;
import org.eclipse.scanning.event.ui.view.StatusQueueView;
import org.eclipse.ui.PlatformUI;

import gda.commandqueue.ExperimentCommandBean;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.components.ExperimentPerspective;

/**
 * Handler for edit action of {@link StatusQueueView} for {@link ExperimentCommandBean}s.
 * Opens the editors for the corresponding {@link IExperimentObject}.
 */
public class EditExperimentHandler extends AbstractQueueBeanHandler implements IModifyHandler<ExperimentCommandBean> {

	@Override
	public boolean modify(ExperimentCommandBean bean) throws Exception {
		final IExperimentEditorManager editorMgr = ExperimentFactory.getExperimentEditorManager();
		IExperimentObject exprObj = getExperimentObject(bean);

		if (exprObj != null) {
			PlatformUI.getWorkbench().showPerspective(ExperimentPerspective.ID,
					PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			editorMgr.openDefaultEditors(exprObj, true);
		}

		return true;
	}

}
