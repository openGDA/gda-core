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

import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper.TEXT_TYPE;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class FluoDetectorCountsComposite extends Composite {

	private static final String DEFAULT_LABEL_TEXT = "not calculated";

	private LabelWrapper enabledElementsCounts;
	private LabelWrapper selectedElementCounts;
	private LabelWrapper selectedRegionCounts;

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
		selectedElementCountsLabel.setText("Selected element: ");

		selectedElementCounts = createNewLabelWrapper(countsGroup);
		horizontalGrabGridData.applyTo(selectedElementCounts);

		Label selectedRegionCountsLabel = new Label(countsGroup, SWT.NONE);
		selectedRegionCountsLabel.setText("Selected region: ");

		selectedRegionCounts = createNewLabelWrapper(countsGroup);
		horizontalGrabGridData.applyTo(selectedRegionCounts);
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
}
