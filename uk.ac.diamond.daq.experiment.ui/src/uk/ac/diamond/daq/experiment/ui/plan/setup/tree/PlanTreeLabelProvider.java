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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;
import uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils;

public class PlanTreeLabelProvider extends LabelProvider {

	private Map<String, Image> images = new HashMap<>();


	// FIXME obviously we plan/segment/trigger requests need common interface
	// with getName(), getParent(), getChildren()

	@Override
	public String getText(Object element) {
		if (element instanceof PlanRequest plan) {
			return plan.getName();
		} else if (element instanceof SegmentRequest segment) {
			return segment.getName();
		} else if (element instanceof TriggerRequest trigger) {
			return trigger.getName();
		}
		throw unknownElement(element);
	}

	@Override
	public Image getImage(Object element) {
		String path;
		if (element instanceof PlanRequest) {
			path = ExperimentUiUtils.PLAN_ICON;
		} else if (element instanceof SegmentRequest) {
			path = ExperimentUiUtils.SEGMENT_ICON;
		} else if (element instanceof TriggerRequest) {
			path = ExperimentUiUtils.TRIGGER_ICON;
		} else {
			throw unknownElement(element);
		}

		return images.computeIfAbsent(path, ExperimentUiUtils::getImage);

	}

	@Override
	public void dispose() {
		images.values().forEach(Image::dispose);
		super.dispose();
	}

	private RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown element type: " + element.getClass().getName());
	}

}
