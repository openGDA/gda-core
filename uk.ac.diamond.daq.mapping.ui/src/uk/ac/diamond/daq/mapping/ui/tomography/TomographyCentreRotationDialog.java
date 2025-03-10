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

package uk.ac.diamond.daq.mapping.ui.tomography;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.DF;
import static uk.ac.diamond.daq.mapping.ui.tomography.TomographyUtils.textBox;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.ui.tomography.TomographyConfigurationDialog.Motor;

public class TomographyCentreRotationDialog extends TitleAreaDialog {
	private static final Logger logger = LoggerFactory.getLogger(TomographyCentreRotationDialog.class);

	private static final int HORIZONTAL_SPACING = 5;
	private static final int VERTICAL_SPACING = 10;

	private Scannable sampleX;
	private Scannable sampleY;
	private Scannable sampleZ;
	private Scannable rotationStage;

	private Text x1Text;
	private Text y1Text;
	private Text z1Text;
	private Text rot1Text;

	private Text x2Text;
	private Text y2Text;
	private Text z2Text;
	private Text rot2Text;

	private List<Text> fields;

	private Text xResult;
	private Text yResult;
	private Text zResult;
	private Text rotResult;

	private List<Text> results;

	private Button calculateButton;
	private Button moveButton;

	private GridData labelGridData;
	private GridData buttonGridData;

	private Label statusLabel;

	public TomographyCentreRotationDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);

		sampleX = Finder.find(Motor.X.getScannableName());
		sampleY = Finder.find(Motor.Y.getScannableName());
		sampleZ = Finder.find(Motor.Z.getScannableName());
		rotationStage = Finder.find(Motor.R.getScannableName());
	}

	@Override
	public int open() {
	    if (sampleX == null || sampleY == null || sampleZ == null || rotationStage == null) {
	        MessageDialog.openError(getShell(), "Error", "Scannables not found. Cannot open dialog.");
	        return Window.CANCEL;
	    }
	    return super.open();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure centre of rotation");
		newShell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
	}

	@Override
    public void create() {
        super.create();
        setTitle("Configure centre of rotation");
        setMessage("Define the centre of rotation", IMessageProvider.INFORMATION);
    }

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	    // only create the OK button, remove the Cancel button
	    createButton(parent, IDialogConstants.OK_ID, "OK", true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
	    var dialogComposite = (Composite) super.createDialogArea(parent);
	    dialogComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

	    var content = CompositeFactory.newComposite(SWT.NONE).create(dialogComposite);
	    GridLayout layout = new GridLayout(4, false);
	    layout.marginTop = 20;
	    layout.marginBottom = 20;
	    layout.marginLeft = 20;
	    layout.marginRight = 20;
	    content.setLayout(layout);

	    GridData contentGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
	    content.setLayoutData(contentGridData);
	    content.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

	    labelGridData = new GridData();
	    labelGridData.horizontalSpan = 2;

	    buttonGridData = new GridData();
	    buttonGridData.horizontalSpan = 2;
	    buttonGridData.grabExcessHorizontalSpace = true;
	    buttonGridData.horizontalAlignment = SWT.RIGHT;

	    createFirstPositionsTable(content);
	    createSecondPositionsTable(content);

	    fields = List.of(x1Text, y1Text, z1Text, rot1Text, x2Text, y2Text, z2Text, rot2Text);

	    var calculateButtonComposite = createComposite(content);

	    calculateButton = ButtonFactory.newButton(SWT.PUSH).create(calculateButtonComposite);
	    calculateButton.setText("Calculate");
	    calculateButton.setEnabled(false);
	    calculateButton.addSelectionListener(widgetSelectedAdapter(selection -> calculateCentreOfRotation()));

	    var centreTableComposite = createCentrePositionsTable(content);

	    results = List.of(xResult, yResult, zResult, rotResult);

	    moveButton = ButtonFactory.newButton(SWT.PUSH).create(centreTableComposite);
	    moveButton.setText("Move to centre");
	    moveButton.setEnabled(false);
	    moveButton.addSelectionListener(widgetSelectedAdapter(selection -> checkIfBusy()));

	    statusLabel = LabelFactory.newLabel(SWT.NONE).create(centreTableComposite);
	    statusLabel.setText("");
	    GridData statusLabelData = new GridData();
	    statusLabelData.widthHint = 120;
	    statusLabel.setLayoutData(statusLabelData);

	    return dialogComposite;
	}

	private void createFirstPositionsTable(Composite parent) {
	    var composite = createComposite(parent);

	    var firstLabel = LabelFactory.newLabel(SWT.NONE).create(composite);
	    firstLabel.setText("First rotation");
	    firstLabel.setLayoutData(labelGridData);

	    x1Text = textBox(composite, "x1");
	    y1Text = textBox(composite, "y1");
	    z1Text = textBox(composite, "z1");
	    rot1Text = textBox(composite, "rot1");

	    var button = ButtonFactory.newButton(SWT.PUSH).create(composite);
	    button.setText("Record positions");
	    button.setLayoutData(buttonGridData);
	    button.addSelectionListener(widgetSelectedAdapter(selection -> getScannablePositions(
	            x1Text, y1Text, z1Text, rot1Text)));
	}

	private void createSecondPositionsTable(Composite parent) {
	    var composite = createComposite(parent);

	    var label = LabelFactory.newLabel(SWT.NONE).create(composite);
	    label.setText("Second rotation");
	    label.setLayoutData(labelGridData);

	    x2Text = textBox(composite, "x2");
	    y2Text = textBox(composite, "y2");
	    z2Text = textBox(composite, "z2");
	    rot2Text = textBox(composite, "rot2");

	    var button = ButtonFactory.newButton(SWT.PUSH).create(composite);
	    button.setText("Record positions");
	    button.setLayoutData(buttonGridData);
	    button.addSelectionListener(widgetSelectedAdapter(selection -> getScannablePositions(
	            x2Text, y2Text, z2Text, rot2Text)));
	}

	private Composite createCentrePositionsTable(Composite parent) {
		var composite = createComposite(parent);

	    var label = LabelFactory.newLabel(SWT.NONE).create(composite);
	    label.setText("Centre of rotation");
	    label.setLayoutData(labelGridData);

	    xResult = textBox(composite, "x centre");
	    yResult = textBox(composite, "y centre");
	    zResult = textBox(composite, "z centre");
	    rotResult = textBox(composite, "rot centre");

	    return composite;
	}

	private void getScannablePositions(Text xText, Text yText, Text zText, Text rotText) {
		try {
			var xPos = (double) sampleX.getPosition();
			var yPos = (double) sampleY.getPosition();
			var zPos = (double) sampleZ.getPosition();
			var rotPos = (double) rotationStage.getPosition();

			Display.getDefault().asyncExec(() -> {
				updateText(xText, xPos);
				updateText(yText, yPos);
				updateText(zText, zPos);
				updateText(rotText, rotPos);

				boolean allNotEmpty = fields.stream().noneMatch(t -> t.getText().isEmpty());
				calculateButton.setEnabled(allNotEmpty);
				});

		} catch (DeviceException e) {
			logger.error("Could not get position of rotation stage", e);
		}
	}

	private void updateText(Text textBox, double position) {
		textBox.setText(DF.format(position));
	}

	private void calculateCentreOfRotation() {
		var x1 = Double.parseDouble(x1Text.getText());
		var x2 = Double.parseDouble(x2Text.getText());
		var y1 = Double.parseDouble(y1Text.getText());
		var y2 = Double.parseDouble(y2Text.getText());
		var z1 = Double.parseDouble(z1Text.getText());
		var z2 = Double.parseDouble(z2Text.getText());
		var rot1 = Double.parseDouble(rot1Text.getText());
		var rot2 = Double.parseDouble(rot2Text.getText());

		double centerX = Math.abs(x2 - x1) / 2.0 + Math.min(x1, x2);
		double centerY = Math.abs(y2 - y1) / 2.0 + Math.min(y1, y2);
		double centerZ = Math.abs(z2 - z1) / 2.0 + Math.min(z1, z2);
		double centerRotation = Math.abs(rot2 - rot1) / 2.0 + Math.min(rot1, rot2);

		Display.getDefault().asyncExec(() -> {
			updateText(xResult, centerX);
			updateText(yResult, centerY);
			updateText(zResult, centerZ);
			updateText(rotResult, centerRotation);

			boolean allNotEmpty = results.stream().noneMatch(t -> t.getText().isEmpty());
			moveButton.setEnabled(allNotEmpty);
		});
	}

	private void checkIfBusy() {
		try {
	        if (sampleX.isBusy() || sampleY.isBusy() || sampleZ.isBusy() || rotationStage.isBusy()) {
	            Display.getDefault().asyncExec(() ->
	                MessageDialog.openWarning(Display.getDefault().getActiveShell(),
	                "Move Error", "One of the devices is busy. Please wait for it to finish before moving.")
	            );
	            return;
	        }
	    } catch (DeviceException e) {
	        logger.error("Error checking device busy state", e);
	        return;
	    }
		// if devices are not busy, move to the centre of rotation
		moveToCentreOfRotation();
	}

	private void moveToCentreOfRotation() {
	    var xPos = Double.parseDouble(xResult.getText());
	    var yPos = Double.parseDouble(yResult.getText());
	    var zPos = Double.parseDouble(zResult.getText());
	    var rotPos = Double.parseDouble(rotResult.getText());

	    Display.getDefault().asyncExec(() -> {
	    	statusLabel.setText("Moving stages...");
	    	statusLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
	    	});

	    Async.execute(() -> {
	        try {
	        	sampleX.asynchronousMoveTo(xPos);
	            sampleY.asynchronousMoveTo(yPos);
	            sampleZ.asynchronousMoveTo(zPos);
	            rotationStage.asynchronousMoveTo(rotPos);

	        	sampleX.waitWhileBusy();
	        	sampleY.waitWhileBusy();
	        	sampleZ.waitWhileBusy();
	        	rotationStage.waitWhileBusy();

	            Display.getDefault().asyncExec(() -> {
	            	statusLabel.setText("");
	                MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Info", "Move completed");
	                });
	        } catch (DeviceException e) {
	            logger.error("Could not move to target position", e);
	            Display.getDefault().asyncExec(() ->
	                MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", e.getMessage()));
	        } catch (InterruptedException e) {
	        	Thread.currentThread().interrupt();
	        	logger.error("Thread was interrupted while moving stages.", e);
	        	Display.getDefault().asyncExec(() ->
	        		MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Warning", "Stage movement was interrupted."));
	        } finally {
	        	Display.getDefault().asyncExec(() -> statusLabel.setText(""));
	        }
	    });
	}

	private Composite createComposite(Composite parent) {
		var composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
		composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
		((GridLayout) composite.getLayout()).horizontalSpacing = HORIZONTAL_SPACING;
		((GridLayout) composite.getLayout()).verticalSpacing = VERTICAL_SPACING;
		GridData compositeGridData = new GridData();
		compositeGridData.grabExcessHorizontalSpace = false;
		compositeGridData.horizontalAlignment = SWT.LEFT;
		composite.setLayoutData(compositeGridData);
		return composite;
	}
}
