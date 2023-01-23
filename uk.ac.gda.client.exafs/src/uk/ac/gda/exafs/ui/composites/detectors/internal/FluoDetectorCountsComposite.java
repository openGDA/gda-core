/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper;
import org.eclipse.richbeans.widgets.wrappers.LabelWrapper.TEXT_TYPE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.common.rcp.util.GridUtils;

public class FluoDetectorCountsComposite extends Composite {

	private static final String DEFAULT_LABEL_TEXT = "not calculated";

	private LabelWrapper enabledElementsCounts;
	private LabelWrapper selectedElementCounts;
	private LabelWrapper selectedRegionCounts;
	private LabelWrapper deadtimeCorrectionFactor;
	private LabelWrapper inputEstimate;
	private List<Control> dtcWidgets;

	public FluoDetectorCountsComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new FillLayout());

		Group countsGroup = new Group(this, SWT.NONE);
		countsGroup.setText("Counts");

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(countsGroup);

		GridDataFactory horizontalGrabGridData = GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.FILL);

		Label enabledElementsCountsLabel = new Label(countsGroup, SWT.NONE);
		enabledElementsCountsLabel.setText("All enabled elements: ");

		enabledElementsCounts = createNewLabelWrapper(countsGroup);
		horizontalGrabGridData.applyTo(enabledElementsCounts);

		Label selectedElementCountsLabel = new Label(countsGroup, SWT.NONE);
		selectedElementCountsLabel.setText("Total counts: ");

		selectedElementCounts = createNewLabelWrapper(countsGroup);
		horizontalGrabGridData.applyTo(selectedElementCounts);

		Label selectedRegionCountsLabel = new Label(countsGroup, SWT.NONE);
		selectedRegionCountsLabel.setText("In-window counts: ");

		selectedRegionCounts = createNewLabelWrapper(countsGroup);
		horizontalGrabGridData.applyTo(selectedRegionCounts);

		// Deadtime correction factor widgets
		final Label dtcFactorLabel = new Label(countsGroup, SWT.NONE);
		dtcFactorLabel.setText("Deadtime correction factor: ");
		deadtimeCorrectionFactor = createNewLabelWrapper(countsGroup);
		horizontalGrabGridData.applyTo(deadtimeCorrectionFactor);

		final Label inputEstimateLabel = new Label(countsGroup, SWT.NONE);
		inputEstimateLabel.setText("Corrected in-window counts: ");
		inputEstimate = createNewLabelWrapper(countsGroup);
		horizontalGrabGridData.applyTo(inputEstimate);

		// Store the widgets so they can be made visible/hidden by call to setDeadtimeParametersVisible()
		dtcWidgets = Arrays.asList(dtcFactorLabel, deadtimeCorrectionFactor, inputEstimateLabel, inputEstimate);
	}

	private LabelWrapper createNewLabelWrapper(Composite parent) {
		LabelWrapper labelWrapper = new LabelWrapper(parent, SWT.NONE);
		labelWrapper.setTextType(TEXT_TYPE.PLAIN_TEXT);
		labelWrapper.setText(DEFAULT_LABEL_TEXT);
		return labelWrapper;
	}

	public IFieldWidget getEnabledElementsCounts() {
		return enabledElementsCounts;
	}

	public IFieldWidget getSelectedElementCounts() {
		return selectedElementCounts;
	}

	public IFieldWidget getSelectedRegionCounts() {
		return selectedRegionCounts;
	}

	/**
	 * @return In-window 'deadtime corrected' counts widget
	 */
	public IFieldWidget getInputEstimateCounts() {
		return inputEstimate;
	}

	/**
	 * @return deadtime correction (DTC) factor widget
	 */
	public IFieldWidget getDtcFactor() {
		return deadtimeCorrectionFactor;
	}

	public void setDeadtimeParametersVisible(boolean isVisible) {
		for(Control c : dtcWidgets) {
			GridUtils.setVisibleAndLayout(c, isVisible);
		}
	}
}
