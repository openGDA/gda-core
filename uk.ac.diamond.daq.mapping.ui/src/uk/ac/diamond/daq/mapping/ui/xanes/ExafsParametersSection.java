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

import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;
import uk.ac.gda.ui.tool.ClientVerifyListener;

public class ExafsParametersSection extends AbstractHideableMappingSection {
	private static final Logger logger = LoggerFactory.getLogger(ExafsParametersSection.class);

	private static final int EDITABLE_TEXT_SIZE = 50;

	private Text edgeStep;
	private Text kMin;
	private Text kMax;
	private Text kStep;
	private Text kWeight;
	private Text startTime;
	private Text endTime;
	private Text restartK;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		content = createComposite(parent, 1, true);

		var sectionLabel = LabelFactory.newLabel(SWT.WRAP).create(content);
		sectionLabel.setText("Exafs parameters");

		var editComposite = createComposite(content, 8, true);

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("K Min");
		kMin = numericTextBox(editComposite);
		kMin.setText("3"); // example value

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("K Max");
		kMax = numericTextBox(editComposite);
		kMax.setText("11"); // example value

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("K Step");
		kStep = numericTextBox(editComposite);
		kStep.setText("11"); // example value

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("K Weight");
		kWeight = numericTextBox(editComposite);
		kWeight.setText("3"); // example value

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Edge Step");
		edgeStep = numericTextBox(editComposite);
		edgeStep.setText("0.0005"); // example value

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Start time");
		startTime = numericTextBox(editComposite);
		startTime.setText("0.05"); // example value

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("End time");
		endTime = numericTextBox(editComposite);
		endTime.setText("0.05"); // example value

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Restart K");
		restartK = numericTextBox(editComposite);
		restartK.setText("0.0"); // example value

		updateControls();

		setContentVisibility();
	}

	private Text numericTextBox(Composite parent) {
		var text = new Text(parent, SWT.BORDER);

		// text does not resize after entering input
		var gridData = new GridData();
		gridData.widthHint = EDITABLE_TEXT_SIZE;
		text.setLayoutData(gridData);
		text.addVerifyListener(ClientVerifyListener.verifyOnlyDoubleText);
		return text;
	}

	public double getkMin() {
		return Double.parseDouble(trimNonNumericCharacters(kMin.getText()));
	}
	public double getkMax() {
		return Double.parseDouble(trimNonNumericCharacters(kMax.getText()));
	}

	public double getkStep() {
		return Double.parseDouble(trimNonNumericCharacters(kStep.getText()));
	}

	public double getkWeight() {
		return Double.parseDouble(trimNonNumericCharacters(kWeight.getText()));
	}

	public double getEdgeStep() {
		return Double.parseDouble(trimNonNumericCharacters(edgeStep.getText()));
	}

	public double getStartTime() {
		return Double.parseDouble(trimNonNumericCharacters(startTime.getText()));
	}

	public double getEndTime() {
		return Double.parseDouble(trimNonNumericCharacters(endTime.getText()));
	}

	public double getRestartK() {
		return Double.parseDouble(trimNonNumericCharacters(restartK.getText()));
	}

	private String trimNonNumericCharacters(String textString) {
		String filteredInput = textString.replaceAll("[a-zA-Z\\s]", "");
		if (filteredInput.isEmpty()) {
			throw new NumberFormatException("Input is empty or contains no numeric characters.");
		}
		return filteredInput;
	}
}
