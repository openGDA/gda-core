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
import org.eclipse.scanning.api.points.models.RasterModel;

class RasterModelExpresser extends PyModelExpresser<RasterModel> {

	@Override
	String pyExpress(RasterModel model, Collection<IROI> rois, boolean verbose) throws Exception {
		return new StringBuilder("grid(")
				.append((verbose?"axes=":"")+"(")
				.append("'"+model.getxAxisName()+"'"+", ")
				.append("'"+model.getyAxisName()+"'"+"), ")
				.append((verbose?"start=":"")+"(")
				.append(formatValue(model.getBoundingBox().getxAxisStart())+", ")
				.append(formatValue(model.getBoundingBox().getyAxisStart())+"), ")
				.append((verbose?"stop=":"")+"(")
				.append(formatValue(model.getBoundingBox().getxAxisEnd())+", ")
				.append(formatValue(model.getBoundingBox().getyAxisEnd())+"), ")
				.append((verbose?"step=":"")+"(")
				.append(model.getxAxisStep()+", ")
				.append(model.getyAxisStep()+")")
				.append(", "+getBooleanPyExpression("snake", model.isSnake(), verbose))
				.append(", "+getBooleanPyExpression("continuous", model.isContinuous(), verbose))
				.append(getROIPyExpression(rois, verbose))
				.append(")")
				.toString();
	}

}
