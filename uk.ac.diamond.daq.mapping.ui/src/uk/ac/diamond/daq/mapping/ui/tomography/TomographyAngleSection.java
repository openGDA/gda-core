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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.DF;

import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;
import uk.ac.diamond.daq.mapping.ui.tomography.TomographyConfigurationDialog.Motor;
import uk.ac.gda.ui.tool.ClientVerifyListener;

public class TomographyAngleSection extends AbstractHideableMappingSection {
	private static final Logger logger = LoggerFactory.getLogger(TomographyAngleSection.class);

	private static final int EDITABLE_TEXT_SIZE = 50;
	private static final int DISPLAY_TEXT_SIZE = 90;
	private static final int NUM_PROJECTIONS_TEXT_SIZE = 120;

	private Text startText;
	private Text stopText;
	private Text stepText;
	private Text numProjectionsText;

	private Scannable rotationStage;
	private Text rotationText;

	private Scannable sampleZ;
	private Text zCentreText;

	public TomographyAngleSection() {
		rotationStage = Finder.find(Motor.R.getScannableName());
		sampleZ = Finder.find(Motor.Z.getScannableName());
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		content = createComposite(parent, 1, true);

		var angleLabel = LabelFactory.newLabel(SWT.WRAP).create(content);
		angleLabel.setText("Angle");

		var editComposite = createComposite(content, 8, true);

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Start");
		startText = numericTextBox(editComposite);

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Stop");
		stopText = numericTextBox(editComposite);

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Step");
		stepText = numericTextBox(editComposite);

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Projections: ");
		numProjectionsText = textBox(editComposite);
		numProjectionsText.setEnabled(false);
		var gridData = new GridData();
		gridData.widthHint = NUM_PROJECTIONS_TEXT_SIZE;
		numProjectionsText.setLayoutData(gridData);

		var rotationComposite = createComposite(content, 5, true);

		var recordButton = ButtonFactory.newButton(SWT.PUSH).create(rotationComposite);
		recordButton.setText("Record angle measured");
		recordButton.addSelectionListener(widgetSelectedAdapter(selection -> recordPosition()));

		LabelFactory.newLabel(SWT.NONE).create(rotationComposite).setText("Rotation");
		rotationText = textBox(rotationComposite);
		rotationText.setEnabled(false);

		LabelFactory.newLabel(SWT.NONE).create(rotationComposite).setText("SampleZ value");
		zCentreText = textBox(rotationComposite);
		zCentreText.setEnabled(false);

		setContentVisibility();
	}

	private void recordPosition() {
		try {
			var rotPosition = (double) rotationStage.getPosition();
			var zPosition = (double) sampleZ.getPosition();
			Display.getDefault().asyncExec(() -> {
				updateText(zCentreText, zPosition, "mm");
				updateText(rotationText, rotPosition, "deg");
			});
		} catch (DeviceException e) {
			logger.error("Could not get position of rotation stage", e);
		}
	}

	private Text numericTextBox(Composite parent) {
		var text = new Text(parent, SWT.BORDER);

		// text does not resize after entering input
		var gridData = new GridData();
		gridData.widthHint = EDITABLE_TEXT_SIZE;
		text.setLayoutData(gridData);
		text.addVerifyListener(ClientVerifyListener.verifyOnlyDoubleText);
		text.addModifyListener(e -> {
		    try {
		        int numProjections = calculateProjections(getStartAngle(), getStopAngle(), getStepAngle());
		        numProjectionsText.setText(String.valueOf(numProjections));
		    } catch (NumberFormatException ex) {
		        numProjectionsText.setText("0");
		    } catch (IllegalArgumentException ex) {
		        numProjectionsText.setText("Error: Invalid input");
		    }
		});
		return text;
	}

	private Text textBox(Composite parent) {
		var text = new Text(parent, SWT.BORDER);
		// text does not resize after entering input
		var gridData = new GridData();
		gridData.widthHint = DISPLAY_TEXT_SIZE;
		text.setLayoutData(gridData);
		return text;
	}

	private int calculateProjections(double start, double stop, double step) {
		if (step <= 0) {
            throw new IllegalArgumentException("Step size cannot be zero.");
        }

		double range = stop - start;

	    if (step > range) {
	        throw new IllegalArgumentException("Step size is too large for the given range.");
	    }

		int numProjections = (int) ((stop - start) / step) + 1;

		if (numProjections <= 0) {
			throw new IllegalArgumentException("Number of projections is less than 0.");
		}

        return numProjections;
	}

	private void updateText(Text textBox, double position, String units) {
		textBox.setText(DF.format(position) + " " + units);
	}

	private String trimNonNumericCharacters(String textString) {
		String filteredInput = textString.replaceAll("[a-zA-Z\\s]", "");
		if (filteredInput.isEmpty()) {
			throw new NumberFormatException("Input is empty or contains no numeric characters.");
		}
		return filteredInput;
	}

	public double getStartAngle() {
		return Double.parseDouble(trimNonNumericCharacters(startText.getText()));
	}

	public double getStopAngle() {
		return Double.parseDouble(trimNonNumericCharacters(stopText.getText()));
	}

	public double getStepAngle() {
		return Double.parseDouble(trimNonNumericCharacters(stepText.getText()));
	}

	public double getAngleMeasured() {
		return Double.parseDouble(trimNonNumericCharacters(rotationText.getText()));
	}

	public double getZValue() {
		return Double.parseDouble(trimNonNumericCharacters(zCentreText.getText()));
	}

	public int getNumProjections() {
		return Integer.parseInt(trimNonNumericCharacters(numProjectionsText.getText()));
	}

	public void clearInputs() {
	    if (startText != null) startText.setText("");
	    if (stopText != null) stopText.setText("");
	    if (stepText != null) stepText.setText("");
	    if (numProjectionsText != null) numProjectionsText.setText("");
	    if (rotationText != null) rotationText.setText("");
	    if (zCentreText != null) zCentreText.setText("");
	}
}
