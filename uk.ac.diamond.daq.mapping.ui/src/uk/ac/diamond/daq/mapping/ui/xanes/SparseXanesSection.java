/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.xanes;

import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.createLabel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.SparseParameters;


public class SparseXanesSection extends XanesParametersSection {

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		createSparseSection();
		updateControls();
		setContentVisibility();
	}

	private void createSparseSection() {
		XanesEdgeParameters xanesParameters = getScanParameters();
		SparseParameters sparseParameters = new SparseParameters();
		xanesParameters.setSparseParameters(sparseParameters);

		Composite composite = createComposite(content, 2);

		createLabel(composite, "Percentage (%)", 1);

		Spinner spinner = new Spinner(composite, SWT.BORDER);
		spinner.setToolTipText("Set percentage of y positions to scan");
		spinner.addModifyListener(e -> xanesParameters.getSparseParameters().setPercentage(spinner.getSelection()));
		spinner.setSelection(xanesParameters.getSparseParameters().getPercentage());
		spinner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
	}
}
