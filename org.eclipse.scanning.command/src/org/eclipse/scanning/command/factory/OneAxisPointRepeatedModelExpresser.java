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
import org.eclipse.scanning.api.points.models.OneAxisPointRepeatedModel;

class OneAxisPointRepeatedModelExpresser extends PyModelExpresser<OneAxisPointRepeatedModel> {

	@Override
	public String pyExpress(OneAxisPointRepeatedModel model, Collection<IROI> rois, boolean verbose) {

		if (rois != null && !rois.isEmpty()) throw new IllegalStateException("RepeatedPointModel cannot be associated with ROIs.");

		return new StringBuilder("repeat(")
					.append(verbose?"axis=":"")
					.append("'"+model.getName()+"'"+", ")
					.append(verbose?"count=":"")
					.append(model.getCount()+", ")
					.append(verbose?"value=":"")
					.append(model.getValue()+", ")
					.append(verbose?"sleep=":"")
					.append(model.getSleep()+")")
					.toString();
	}

}
