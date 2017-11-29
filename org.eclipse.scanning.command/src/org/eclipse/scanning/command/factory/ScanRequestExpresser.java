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
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.command.ParserServiceImpl;

class ScanRequestExpresser extends PyModelExpresser<ScanRequest<?>> {

	@SuppressWarnings("squid:S3776")
	@Override
	String pyExpress(ScanRequest<?> request, boolean verbose) throws Exception {


		StringBuilder fragment = new StringBuilder("mscan(");
		boolean scanRequestPartiallyWritten = false;

		PyExpressionFactory factory = new PyExpressionFactory();
		if (request.getCompoundModel().getModels() != null
				&& !request.getCompoundModel().getModels().isEmpty()) {

			if (verbose) { fragment.append("path="); }

			if (verbose || request.getCompoundModel().getModels().size() > 1) fragment.append("[");
			boolean listPartiallyWritten = false;

			for (Object model : request.getCompoundModel().getModels()) {  // Order is important.
				if (listPartiallyWritten) fragment.append(", ");
				Collection<IROI> rois = (Collection<IROI>) ParserServiceImpl.getPointGeneratorService().findRegions(model, request.getCompoundModel().getRegions());

				String smodel = factory.pyExpress(model, rois, verbose);
				fragment.append(smodel);
				listPartiallyWritten |= true;
			}

			if (verbose || request.getCompoundModel().getModels().size() > 1) fragment.append("]");
			scanRequestPartiallyWritten |= true;
		}

		if (request.getMonitorNamesPerPoint() != null
				&& !request.getMonitorNamesPerPoint().isEmpty()) {

			if (scanRequestPartiallyWritten) fragment.append(", ");
			if (verbose || !scanRequestPartiallyWritten) { fragment.append("monitorsPerPoint="); }

			if (verbose || request.getMonitorNamesPerPoint().size() > 1) fragment.append("[");
			boolean listPartiallyWritten = false;

			for (String monitorName : request.getMonitorNamesPerPoint()) {
				if (listPartiallyWritten) fragment.append(", ");
				fragment.append("'"+monitorName+"'");
				listPartiallyWritten |= true;
			}

			if (verbose || request.getMonitorNamesPerPoint().size() > 1) fragment.append("]");
			scanRequestPartiallyWritten |= true;
		}

		if (request.getMonitorNamesPerScan() != null
				&& !request.getMonitorNamesPerScan().isEmpty()) {

			if (scanRequestPartiallyWritten) fragment.append(", ");
			if (verbose || !scanRequestPartiallyWritten) { fragment.append("monitorsPerScan="); }

			if (verbose || request.getMonitorNamesPerScan().size() > 1) fragment.append("[");
			boolean listPartiallyWritten = false;

			for (String monitorName : request.getMonitorNamesPerScan()) {
				if (listPartiallyWritten) fragment.append(", ");
				fragment.append("'"+monitorName+"'");
				listPartiallyWritten |= true;
			}

			if (verbose || request.getMonitorNamesPerScan().size() > 1) fragment.append("]");
			scanRequestPartiallyWritten |= true;
		}

		if (request.getDetectors() != null
				&& request.getDetectors().size() > 0) {

			if (scanRequestPartiallyWritten) fragment.append(", ");
			if (verbose || !scanRequestPartiallyWritten) { fragment.append("det="); }

			if (verbose || request.getDetectors().size() > 1) fragment.append("[");

			boolean listPartiallyWritten = false;
			for (String detectorName : request.getDetectors().keySet()) {
				if (listPartiallyWritten) fragment.append(", ");
				Object model = request.getDetectors().get(detectorName);
				fragment.append(factory.pyExpress(model, verbose));
				listPartiallyWritten |= true;
			}

			if (verbose || request.getDetectors().size() > 1) fragment.append("]");
		}

		fragment.append(")");
		return fragment.toString();

	}
}
