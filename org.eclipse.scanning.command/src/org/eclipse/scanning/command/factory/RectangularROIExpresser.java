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

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

class RectangularROIExpresser extends PyModelExpresser<RectangularROI> {

	@Override
	public String pyExpress(RectangularROI rroi, boolean verbose) {
		return new StringBuilder("rect(")
					.append((verbose?"origin=":"")+"(")
					.append(formatValue(rroi.getPointX())+", "+formatValue(rroi.getPointY()))
					.append("), ")
					.append((verbose?"size=":"")+"(")
					.append(formatValue(rroi.getLengths()[0])+", "+formatValue(rroi.getLengths()[1]))
					.append("))")
					.toString();
	}

}