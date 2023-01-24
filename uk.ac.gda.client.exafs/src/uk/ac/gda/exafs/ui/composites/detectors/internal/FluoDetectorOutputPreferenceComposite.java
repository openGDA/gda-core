/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Composite with checkboxes to control data saved to file from detector
 * @since 13/6/2016
 */
public class FluoDetectorOutputPreferenceComposite extends Composite {

	private BooleanWrapper onlyShowFF;
	private BooleanWrapper showDTRawValues;
	private BooleanWrapper saveRawSpectrum;

	public FluoDetectorOutputPreferenceComposite(Composite parentComposite, int style ) {
		super(parentComposite, style);
		this.setLayout(new FillLayout());

		Group prefGroup = new Group(this, SWT.NONE);
		prefGroup.setText("Output Preferences");
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(prefGroup);

		onlyShowFF = new BooleanWrapper(prefGroup, SWT.CHECK);
		onlyShowFF.setValue(true);
		onlyShowFF.setText("Hide individual elements");
		onlyShowFF.setToolTipText("In ascii output, only display the total in-window counts (FF) from the Xspress detector");

		showDTRawValues = new BooleanWrapper(prefGroup, SWT.CHECK);
		showDTRawValues.setValue(true);
		showDTRawValues.setText("Show DT values");
		showDTRawValues.setToolTipText("Add the raw scaler values used in deadtime (DT) calculations to ascii output");

		saveRawSpectrum = new BooleanWrapper(prefGroup, SWT.CHECK);
		saveRawSpectrum.setValue(true);
		saveRawSpectrum.setText("Save raw spectrum to file");
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
