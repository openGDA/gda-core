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

import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.roundDouble;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;
import uk.ac.diamond.daq.mapping.ui.xanes.XanesEdgeCombo;
import uk.ac.diamond.daq.mapping.ui.xanes.XanesElementsList;
import uk.ac.gda.ui.tool.ClientVerifyListener;

public class ExafsParametersSection extends AbstractHideableMappingSection {

	private static final int EDITABLE_TEXT_SIZE = 60;
	private static final int DEFAULT_PERCENTAGE_VALUE = 20;

	private XanesElementsList elementAndEdgesList;

	private String edgeElement;

	private Text edgeEnergyText;
	private Text edgeStepText;
	private Spinner percentageSpinner;

	private Text kMinText;
	private Text kMaxText;
	private Text kStepText;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		content = createComposite(parent, 1, true);

		LabelFactory.newLabel(SWT.WRAP).create(content).setText("EXAFS parameters");

		createEdgeControls();
		createPostEdgeControls();

		updateControls();
		setContentVisibility();
	}

	private void createEdgeControls() {
		var composite = createComposite(content, 7, true);

		final XanesEdgeCombo elementsAndEdgeCombo = new XanesEdgeCombo(composite, elementAndEdgesList);
		elementsAndEdgeCombo.addSelectionChangedListener(e ->
			handleEdgeSelectionChanged(elementsAndEdgeCombo.getSelection()));

		LabelFactory.newLabel(SWT.NONE).create(composite).setText("Edge Energy");
		edgeEnergyText = numericTextBox(composite);
		edgeEnergyText.setEnabled(false);
		edgeEnergyText.setText("0.0");

		LabelFactory.newLabel(SWT.NONE).create(composite).setText("Edge Step");
		edgeStepText = numericTextBox(composite);
		edgeStepText.setText("0.0005"); // example value

		createLabel(composite, "Percentage (%)", 1);
		percentageSpinner = new Spinner(composite, SWT.BORDER);
		percentageSpinner.setToolTipText("Set percentage of y positions to scan");
		percentageSpinner.setSelection(DEFAULT_PERCENTAGE_VALUE);
		percentageSpinner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
	}

	private void createPostEdgeControls() {
		var composite = createComposite(content, 6, true);

		LabelFactory.newLabel(SWT.NONE).create(composite).setText("K Min");
		kMinText = numericTextBox(composite);
		kMinText.setText("3"); // example value

		LabelFactory.newLabel(SWT.NONE).create(composite).setText("K Max");
		kMaxText = numericTextBox(composite);
		kMaxText.setText("11"); // example value

		LabelFactory.newLabel(SWT.NONE).create(composite).setText("K Step");
		kStepText = numericTextBox(composite);
		kStepText.setText("11"); // example value
	}

	private void handleEdgeSelectionChanged(IStructuredSelection selection) {
		final EdgeToEnergy selectedEdge = (EdgeToEnergy) selection.getFirstElement();
		if (selectedEdge == null) {
			return;
		}

		edgeElement = selectedEdge.getEdge();
		double edgeEnergyValue = roundDouble(selectedEdge.getEnergy());
		edgeEnergyText.setText(String.valueOf(edgeEnergyValue));
	}

	private Label createLabel(Composite parent, String text, int span) {
		final Label label = new Label(parent, SWT.WRAP);
		GridDataFactory.swtDefaults().span(span, 1).applyTo(label);
		label.setText(text);
		return label;
	}

	public static Text numericTextBox(Composite parent) {
		var text = new Text(parent, SWT.BORDER);

		// text does not resize after entering input
		var gridData = new GridData();
		gridData.widthHint = EDITABLE_TEXT_SIZE;
		text.setLayoutData(gridData);
		text.addVerifyListener(ClientVerifyListener.verifyOnlyDoubleText);
		return text;
	}

	public static String trimNonNumericCharacters(String textString) {
		String filteredInput = textString.replaceAll("[a-zA-Z\\s]", "");
		if (filteredInput.isEmpty()) {
			throw new NumberFormatException("Input is empty or contains no numeric characters.");
		}
		return filteredInput;
	}

	public int getPercentage() {
		return percentageSpinner.getSelection();
	}

	public double getEdgeEnergy() {
		return Double.parseDouble(trimNonNumericCharacters(edgeEnergyText.getText()));
	}

	public double getEdgeStep() {
		return Double.parseDouble(trimNonNumericCharacters(edgeStepText.getText()));
	}

	public double getkMin() {
		return Double.parseDouble(trimNonNumericCharacters(kMinText.getText()));
	}
	public double getkMax() {
		return Double.parseDouble(trimNonNumericCharacters(kMaxText.getText()));
	}

	public double getkStep() {
		return Double.parseDouble(trimNonNumericCharacters(kStepText.getText()));
	}

	public String getEdgeElement() {
		return edgeElement;
	}

	public void setElementAndEdgesList(XanesElementsList elementAndEdgesList) {
		this.elementAndEdgesList = elementAndEdgesList;
	}
}
