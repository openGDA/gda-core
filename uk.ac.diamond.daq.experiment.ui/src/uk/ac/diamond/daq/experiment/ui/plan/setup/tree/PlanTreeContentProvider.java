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

package uk.ac.diamond.daq.experiment.ui.plan.setup.tree;

import java.util.UUID;

import org.eclipse.jface.viewers.ITreeContentProvider;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.PlanTreeComponent;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;

public class PlanTreeContentProvider implements ITreeContentProvider {

	private ExperimentPlanBean plan;

	public PlanTreeContentProvider(ExperimentPlanBean plan) {
		this.plan = plan;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ExperimentPlanBean bean) {
			return bean.getSegmentRequests().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ExperimentPlanBean planBean) {
			return planBean.getSegments().toArray();
		} else if (parentElement instanceof SegmentRequest segment) {
			return segment.getTriggerRequests().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof SegmentDescriptor) {
			return plan;
		}
		if (element instanceof PlanTreeComponent component) {
			UUID parentId = component.getParentId();
			return plan.getSegments().stream()
				.filter(segment -> segment.getComponentId().equals(parentId))
				.findFirst().orElseThrow();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof PlanRequest) return true;
		if (element instanceof SegmentRequest segment) {
			return !segment.getTriggerRequests().isEmpty();
		}
		return false;
	}

}
