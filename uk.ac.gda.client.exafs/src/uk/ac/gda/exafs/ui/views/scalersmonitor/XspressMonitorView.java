/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.views.scalersmonitor;

import gda.device.DeviceException;
import gda.device.detector.DetectorMonitorDataProvider.COLLECTION_TYPES;

public class XspressMonitorView extends MonitorViewBase {
	public static final String ID = "uk.ac.gda.exafs.ui.views.scalersmonitor";

	private COLLECTION_TYPES collectionType = COLLECTION_TYPES.XSPRESS2;

	public XspressMonitorView() {
	}

	@Override
	protected void updateDisplayDataFFValues(Double[] xspressStats, Double[] values) {
		Double FF = 0.0;
		double maxRate = 0;
		int maxElement = 0;
		for (int element = 0; element < numElements; element++) {

			FF += xspressStats[element * 3];
			// find which element gives the max rate
			if (xspressStats[element * 3] > maxRate) {
				maxRate = xspressStats[element * 3];
				maxElement = element;
			}
		}

		displayData.setFF(FF);

		// get the normalised in window counts for the highest rate element
		switch (numElements) {
		case 9:
			displayData.setFFI0(xspressStats[maxElement * 3 + 2] / values[0]);
			break;
		case 64:
			displayData.setFFI0(xspressStats[36 * 3 + 2] / values[0]); // use element 37 as I think this is one of
																		// the more central ones
			break;
		default:
			displayData.setFFI0(xspressStats[2] / values[0]);
			break;
		}
	}

	@Override
	protected Double[] getFluoDetectorCountRatesAndDeadTimes() throws DeviceException {
		numElements = dataProvider.getNumElements(collectionType);
		return dataProvider.getFluoDetectorCountRatesAndDeadTimes(collectionType);
	}

	@Override
	protected Double[] getIonChamberValues() throws Exception {
		return dataProvider.getIonChamberValues(collectionType);
	}
}
