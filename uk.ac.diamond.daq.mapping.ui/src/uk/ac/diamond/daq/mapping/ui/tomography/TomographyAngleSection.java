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
import gda.observable.IObserver;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;
import uk.ac.diamond.daq.mapping.ui.tomography.TomographyConfigurationDialog.Motor;
import uk.ac.gda.ui.tool.ClientVerifyListener;

public class TomographyAngleSection extends AbstractHideableMappingSection {
	private static final Logger logger = LoggerFactory.getLogger(TomographyAngleSection.class);

	private static final int EDITABLE_TEXT_SIZE = 50;
	private static final int DISPLAY_TEXT_SIZE = 90;

	private Text startText;
	private Text stopText;
	private Text stepText;

	private Scannable rotationStage;
	private IObserver rotationHandler;
	private Text rotationText;

	private Scannable sampleZ;
	private Text zCentreText;

	public TomographyAngleSection() {
		rotationHandler = (source, arg) -> handleRotationUpdate();
		rotationStage = Finder.find(Motor.R.getScannableName());
		rotationStage.addIObserver(rotationHandler);

		sampleZ = Finder.find(Motor.Z.getScannableName());
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		content = createComposite(parent, 1, true);

		var angleLabel = LabelFactory.newLabel(SWT.WRAP).create(content);
		angleLabel.setText("Angle");

		var editComposite = createComposite(content, 6, true);

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Start");
		startText = numericTextBox(editComposite);

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Stop");
		stopText = numericTextBox(editComposite);

		LabelFactory.newLabel(SWT.NONE).create(editComposite).setText("Step");
		stepText = numericTextBox(editComposite);

		var rotationComposite = createComposite(content, 5, true);
		LabelFactory.newLabel(SWT.NONE).create(rotationComposite).setText("Rotation");
		rotationText = textBox(rotationComposite);
		rotationText.setEnabled(false);

		var recordButton = ButtonFactory.newButton(SWT.PUSH).create(rotationComposite);
		recordButton.setText("Record position");
		recordButton.addSelectionListener(widgetSelectedAdapter(selection -> recordPosition()));

		LabelFactory.newLabel(SWT.NONE).create(rotationComposite).setText("SampleZ value");
		zCentreText = textBox(rotationComposite);
		zCentreText.setEnabled(false);

		handleRotationUpdate();
		setContentVisibility();
	}

	private void handleRotationUpdate() {
		try {
			var position = (double) rotationStage.getPosition();
			Display.getDefault().asyncExec(() -> {
				updateText(rotationText, position, "deg");
				zCentreText.setText("");
			});
		} catch (DeviceException e) {
			logger.error("Could not get position of rotation stage", e);
		}
	}

	private void recordPosition() {
		try {
			var position = (double) sampleZ.getPosition();
			Display.getDefault().asyncExec(() -> updateText(zCentreText, position, "mm"));
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

	private void updateText(Text textBox, double position, String units) {
		textBox.setText(DF.format(position) + " " + units);
	}

	private String trimNonNumericCharacters(String textString) {
		return textString.replaceAll("[a-zA-Z\\s]", "");
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
		return Double.parseDouble(trimNonNumericCharacters(rotationText.getText()));
	}

	public double getZValue() {
		return Double.parseDouble(trimNonNumericCharacters(zCentreText.getText()));
	}

}
