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

package uk.ac.diamond.daq.mapping.ui.exafs;

import static uk.ac.diamond.daq.mapping.ui.exafs.ExafsParametersSection.numericTextBox;
import static uk.ac.diamond.daq.mapping.ui.exafs.ExafsParametersSection.trimNonNumericCharacters;

import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;

public class ExafsTimeParametersSection extends AbstractHideableMappingSection {

	private Text startTimeText;
	private Text endTimeText;
	private Text kWeightText;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		content = createComposite(parent, 1, true);

		LabelFactory.newLabel(SWT.WRAP).create(content).setText("EXAFS K Time Parameters");

		createControls();

		updateControls();
		setContentVisibility();
	}

	private void createControls() {
		var composite = createComposite(content, 6, true);

		LabelFactory.newLabel(SWT.NONE).create(composite).setText("Start time");
		startTimeText = numericTextBox(composite);
		startTimeText.setText("0.05"); // example value

		LabelFactory.newLabel(SWT.NONE).create(composite).setText("End time");
		endTimeText = numericTextBox(composite);
		endTimeText.setText("0.05"); // example value

		LabelFactory.newLabel(SWT.NONE).create(composite).setText("K Weight");
		kWeightText = numericTextBox(composite);
		kWeightText.setText("3"); // example value
	}

	public double getStartTime() {
		return Double.parseDouble(trimNonNumericCharacters(startTimeText.getText()));
	}
	public double getEndTime() {
		return Double.parseDouble(trimNonNumericCharacters(endTimeText.getText()));
	}

	public double getkWeight() {
		return Double.parseDouble(trimNonNumericCharacters(kWeightText.getText()));
	}
}
