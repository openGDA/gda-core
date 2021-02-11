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

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;

/**
 * @deprecated for removal in 9.21. See DAQ-3292
 */
@Deprecated
class CircularROIExpresser extends PyModelExpresser<CircularROI> {

	@Override
	public String pyExpress(CircularROI croi, boolean verbose) {
		return new StringBuilder().append("circ(")
				.append(verbose?"origin=":"")
				.append("("+formatValue(croi.getCentre()[0])+", "+formatValue(croi.getCentre()[1])+"), ")
				.append(verbose?"radius=":"")
				.append(formatValue(croi.getRadius()))
				.append(")")
				.toString();
	}
}
