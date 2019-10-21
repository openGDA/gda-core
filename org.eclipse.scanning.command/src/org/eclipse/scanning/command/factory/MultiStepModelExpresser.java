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
import java.util.Iterator;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.MultiStepModel;
import org.eclipse.scanning.api.points.models.StepModel;

class MultiStepModelExpresser extends AbstractPointsModelExpresser<MultiStepModel> {

	@Override
	public String pyExpress(MultiStepModel mmodel, Collection<IROI> rois, boolean verbose) {

		if (rois != null && !rois.isEmpty()) throw new IllegalStateException("StepModels cannot be associated with ROIs.");

		StringBuilder ret = new StringBuilder();
		ret.append("mstep(");
		ret.append(verbose?"axis=":"");
		ret.append("'"+mmodel.getName()+"'");
		ret.append(verbose?", stepModels=[":", [");


		for (Iterator<StepModel> it = mmodel.getStepModels().iterator(); it.hasNext();) {
			String step = getString(it.next(), verbose);
			ret.append(step);
            if (it.hasNext()) ret.append(", ");
		}
		ret.append("]");
		appendCommonProperties(ret, mmodel, verbose);
		ret.append(")");
		return ret.toString();
	}

	private final String getString(StepModel model, boolean verbose) {
		StringBuilder ret = new StringBuilder("StepModel(");
		ret.append(verbose?"axis=":"");
		ret.append("'"+model.getName()+"'"+", ");
		ret.append(verbose?"start=":"");
		ret.append(model.getStart()+", ");
		ret.append(verbose?"stop=":"");
		ret.append(model.getStop()+", ");
		ret.append(verbose?"step=":"");
		ret.append(model.getStep());
		ret.append(", ");
		ret.append(getBooleanPyExpression("alternating", model.isAlternating(), verbose));
		ret.append(", ");
		ret.append(getBooleanPyExpression("continuous", model.isContinuous(), verbose));
		ret.append(")");
		return ret.toString();
	}

}
