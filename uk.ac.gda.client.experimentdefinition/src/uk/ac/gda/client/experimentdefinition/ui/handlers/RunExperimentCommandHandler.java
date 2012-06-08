/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;

public class RunExperimentCommandHandler extends AbstractExperimentCommandHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (event.getCommand().getId().equals("uk.ac.gda.client.experimentdefinition.RunMultiExperimentCommand")) {

			final IExperimentObjectManager man = getController().getSelectedMultiScan();
			if (man == null)
				return false;

			List<IExperimentObject> exptList = man.getExperimentList();
			for (IExperimentObject expt : exptList) {
				addExperimentToQueue(expt);
			}

		} else if (event.getCommand().getId()
				.equals("uk.ac.gda.client.experimentdefinition.RunSingleExperimentCommand")) {
			
			final IExperimentObject ob = getController().getSelectedScan();
			addExperimentToQueue(ob);

		} else if (event.getCommand().getId().equals("uk.ac.gda.client.experimentdefinition.RunSingleScanOnlyCommand")) {

			final IExperimentObject ob = getController().getSelectedScan();
			if (ob == null)
				return false;

			final IExperimentObject single = ExperimentFactory.getManager(ob).cloneExperiment(ob);
			single.setNumberRepetitions(1);
			addExperimentToQueue(single);
		}
		return true;
	}

	private void addExperimentToQueue(final IExperimentObject ob) throws ExecutionException {
		ExperimentCommandProvider command;
		try {
			command = new ExperimentCommandProvider(ob);
		} catch (Exception e) {
			throw new ExecutionException("Exception creating ExperimentCommandProvider.", e);
		}

		try {
			CommandQueueViewFactory.getQueue().addToTail(command);
		} catch (Exception e) {
			throw new ExecutionException("Exception adding ExperimentCommandProvider to CommandQueue.", e);
		}
	}

}
