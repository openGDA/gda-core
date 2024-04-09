/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomography;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;
import uk.ac.gda.ui.tool.ClientVerifyListener;

public class TomographyAngleSection extends AbstractHideableMappingSection {

	private static final int TEXT_BOX_SIZE = 50;
	private static final int NUM_COLUMNS = 8;

	private Text startText;
	private Text stopText;
	private Text stepText;
	private Text angleMeasuredText;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		content = createComposite(parent, NUM_COLUMNS, true);

		var angleLabel = LabelFactory.newLabel(SWT.WRAP).create(content);
		GridDataFactory.swtDefaults().span(NUM_COLUMNS, 1).applyTo(angleLabel);
		angleLabel.setText("Angle");

		LabelFactory.newLabel(SWT.WRAP).create(content).setText("Start");
		startText = numericTextBox(content);

		LabelFactory.newLabel(SWT.WRAP).create(content).setText("Stop");
		stopText = numericTextBox(content);

		LabelFactory.newLabel(SWT.WRAP).create(content).setText("Step");
		stepText = numericTextBox(content);

		LabelFactory.newLabel(SWT.WRAP).create(content).setText("Angle measured");
		angleMeasuredText = numericTextBox(content);

		setContentVisibility();
	}

	public Text numericTextBox(Composite parent) {
		var text = new Text(parent, SWT.BORDER);

		// text does not resize after entering input
		var gridData = new GridData();
		gridData.widthHint = TEXT_BOX_SIZE;
		text.setLayoutData(gridData);

		text.addVerifyListener(ClientVerifyListener.verifyOnlyDoubleText);
		return text;
	}

	public double getStartAngle() {
		return Double.parseDouble(startText.getText());
	}

	public double getStopAngle() {
		return Double.parseDouble(stopText.getText());
	}

	public double getStepAngle() {
		return Double.parseDouble(stepText.getText());
	}

	public double getAngleMeasured() {
		return Double.parseDouble(angleMeasuredText.getText());
	}

}
