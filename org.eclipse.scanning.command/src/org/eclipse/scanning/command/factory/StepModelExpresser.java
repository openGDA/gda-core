/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.command.factory;

import java.util.Collection;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.models.StepModel;

class StepModelExpresser extends AbstractPointsModelExpresser<StepModel> {

	@Override
	public String pyExpress(StepModel model, Collection<IROI> rois, boolean verbose) {
		if (rois != null && !rois.isEmpty() && !rois.stream().allMatch(m -> LinearROI.class.isInstance(m)))
			throw new IllegalStateException("StepModels cannot be associated with ROIs.");

		return getString(model, verbose);
	}

	private final String getString(StepModel model, boolean verbose) {
		StringBuilder sb = new StringBuilder("step(")
				.append(verbose ? "axis=" : "").append("'" + model.getName() + "'" + ", ")
				.append(verbose ? "start=" : "").append(model.getStart() + ", ")
				.append(verbose ? "stop=" : "").append(model.getStop() + ", ")
				.append(verbose ? "step=" : "").append(model.getStep());
		appendCommonProperties(sb, model, verbose);
		sb.append(")");
		return sb.toString();
	}

}
