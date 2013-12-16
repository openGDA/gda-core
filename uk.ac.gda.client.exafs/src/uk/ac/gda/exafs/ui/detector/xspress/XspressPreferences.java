/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.xspress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;

public class XspressPreferences {
	private BooleanWrapper onlyShowFF;
	private BooleanWrapper showDTRawValues;
	private BooleanWrapper saveRawSpectrum;
	
	public XspressPreferences(Composite parent) {
		Group xspressParametersGroup = new Group(parent, SWT.NONE);
		xspressParametersGroup.setText("Output Preferences");
		xspressParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		xspressParametersGroup.setLayout(gridLayout);
		onlyShowFF = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
		onlyShowFF.setText("Hide individual elements");
		onlyShowFF.setToolTipText("In ascii output, only display the total in-window counts (FF) from the Xspress detector");
		onlyShowFF.setValue(Boolean.FALSE);
		showDTRawValues = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
		showDTRawValues.setText("Show DT values");
		showDTRawValues.setToolTipText("Add the raw scaler values used in deadtime (DT) calculations to ascii output");
		showDTRawValues.setValue(Boolean.FALSE);
		saveRawSpectrum = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
		saveRawSpectrum.setText("Save raw spectrum to file");
		saveRawSpectrum.setValue(false);
	}

	public BooleanWrapper getOnlyShowFF() {
		return onlyShowFF;
	}

	public BooleanWrapper getShowDTRawValues() {
		return showDTRawValues;
	}

	public BooleanWrapper getSaveRawSpectrum() {
		return saveRawSpectrum;
	}

}