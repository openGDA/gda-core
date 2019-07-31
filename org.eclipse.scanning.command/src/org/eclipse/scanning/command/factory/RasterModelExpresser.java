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

class RasterModelExpresser extends AbstractGridModelExpresser<RasterModel> {

	@Override
	String pyExpress(RasterModel model, Collection<IROI> rois, boolean verbose) throws Exception {
		final StringBuilder sb = new StringBuilder("grid(")
				.append((verbose?"axes=":"")+"(")
				.append("'"+model.getXAxisName()+"'"+", ")
				.append("'"+model.getYAxisName()+"'"+"), ")
				.append((verbose?"start=":"")+"(")
				.append(formatValue(model.getBoundingBox().getXAxisStart())+", ")
				.append(formatValue(model.getBoundingBox().getYAxisStart())+"), ")
				.append((verbose?"stop=":"")+"(")
				.append(formatValue(model.getBoundingBox().getXAxisEnd())+", ")
				.append(formatValue(model.getBoundingBox().getYAxisEnd())+"), ")
				.append((verbose?"step=":"")+"(")
				.append(model.getXAxisStep()+", ")
				.append(model.getYAxisStep()+"), ");
				appendCommonGridProperties(sb, model, verbose);
				sb.append(getROIPyExpression(rois, verbose))
				.append(")");
				return sb.toString();
	}

}
