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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import gda.device.DeviceException;
import gda.device.detector.DetectorMonitorDataProvider.COLLECTION_TYPES;

public class IonChambersMonitorView extends MonitorViewBase {
	public static final String ID = "uk.ac.gda.exafs.ui.views.ionchambersmonitor";

	private COLLECTION_TYPES collectionType = COLLECTION_TYPES.IONCHAMBERS;

	@Override
	protected void setupGui(Composite parent) {
		Group grpCurrentCountRates = new Group(parent, SWT.BORDER);
		grpCurrentCountRates.setText("Current count rates");
		grpCurrentCountRates.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		grpCurrentCountRates.setLayout(new GridLayout());

		setupDisplayData(grpCurrentCountRates);
	}

	@Override
	protected void setupDisplayData(Composite parent) {
		// Make list of column names from ionchamber output names
		String[] extraNames = dataProvider.getIonChambersExtraNames();

		// Don't include 'time' column, if present
		int startIndex = 0;
		if (extraNames[0].contains("time")) {
			startIndex = 1;
		}
		String[] titles = new String[extraNames.length-startIndex];
		String[] formats = new String[extraNames.length-startIndex];
		for(int i=startIndex; i<extraNames.length; i++) {
			titles[i-startIndex] = extraNames[i];
			formats[i-startIndex] = "%8g";
		}

		displayData = new ScalersMonitorConfig(parent);
		displayData.setTitles(titles);
		displayData.setFormats(formats);
		displayData.createControls();
	}

	@Override
	protected void updateDisplayedData(Double[] statsValues, Double[] ionchamberValues) {
		for(int i=0; i<ionchamberValues.length; i++) {
			displayData.setTextInColumn(i, ionchamberValues[i]);
		}
	}

	@Override
	protected void updateDisplayDataFFValues(Double[] xspressStats, Double[] values) {
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
