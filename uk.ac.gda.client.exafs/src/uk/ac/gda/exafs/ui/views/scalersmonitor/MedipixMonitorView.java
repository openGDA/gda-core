/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import org.eclipse.swt.widgets.Composite;

import gda.device.DeviceException;
import gda.device.detector.DetectorMonitorDataProvider.COLLECTION_TYPES;

public class MedipixMonitorView extends MonitorViewBase {

	private COLLECTION_TYPES collectionType = COLLECTION_TYPES.MEDIPIX;

	public static final String ID = "uk.ac.gda.exafs.ui.views.medipixmonitor";

	private String[] titles = {"I1", "FF", "FF/I1"};
	private String[] formats = {"%.4f", "%.4f", "%.4f"};

	@Override
	protected void setupDisplayData(Composite parent) {
		displayData = new ScalersMonitorConfig(parent);
		displayData.setTitles(titles);
		displayData.setFormats(formats);
		displayData.createControls();
	}

	@Override
	protected void updateDisplayedData(Double[] statsValues, Double[] ionchamberValues) {
		double I1 = ionchamberValues[0]; // counts for I1 ionchamber
		double FF = statsValues[0]; // total counts in ROI from medipix
		double ffI1 = I1>0 ? FF/I1 : 0;

		displayData.setTextInColumn(0, I1);
		displayData.setTextInColumn(1, FF);
		displayData.setTextInColumn(2, ffI1);
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

	@Override
	protected void updateDisplayDataFFValues(Double[] statsValues, Double[] deadtimeValues) {
	}
}
