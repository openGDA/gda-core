/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.ui.plan.setup;

import java.util.Set;

import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.ui.plan.setup.context.ContextControls;
import uk.ac.diamond.daq.experiment.ui.plan.setup.tree.PlanTreeEditor;

public class PlanParametersControls {

	private ExperimentPlanBean bean;

	private ContextControls context;

	public PlanParametersControls(ExperimentPlanBean bean) {
		this.bean = bean;
		context = new ContextControls();
	}

	public void create(Composite parent) {
		var tree = new PlanTreeEditor(bean);
		tree.create(parent);

		context.createControls(parent);

		tree.setSelectionListener(context::showContextControls);
	}

	public void setPrioritySignals(Set<String> signals) {
		context.setPrioritySignals(signals);
	}


}
