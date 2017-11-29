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

class CircularROIExpresser extends PyModelExpresser<CircularROI> {

	@Override
	public String pyExpress(CircularROI croi, boolean verbose) {
		return new StringBuilder().append("circ(")
				.append(verbose?"origin=":"")
				.append("("+croi.getCentre()[0]+", "+croi.getCentre()[1]+"), ")
				.append(verbose?"radius=":"")
				.append(croi.getRadius())
				.append(")")
				.toString();
	}
}
