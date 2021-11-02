/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

/**
 * Dialog to allow editing of camera offset calibration parameters.
 * The dialog consists of four text fields that display the current values and
 * allows updating them.
 * The text inputs are checked against a regular expression that matches only numbers.
 *
 */

public class EditCameraOffsetCalibrationDialog extends TitleAreaDialog {

    private String xOffset;
    private String yOffset;
    private String xPixelScaling;
    private String yPixelScaling;

    private Text xOffsetText;
    private Text yOffsetText;
    private Text xPixelScalingText;
    private Text yPixelScalingText;

    private static final Pattern INPUT_NUMERIC_PATTERN = Pattern.compile("-?\\d+([.]?\\d+)?");
	private DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));


    public EditCameraOffsetCalibrationDialog(Shell parentShell,
        double xOffset, double yOffset, double xPixelScaling, double yPixelScaling) {
        super(parentShell);

    	df.setMaximumFractionDigits(340);

        this.xOffset = df.format(xOffset);
        this.yOffset = df.format(yOffset);
        this.xPixelScaling = df.format(xPixelScaling);
        this.yPixelScaling = df.format(yPixelScaling);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Camera offset calibration");
        setMessage("Set the camera offset calibration.");

    }

    @Override
    protected Control createDialogArea(Composite parent) {

        Composite dialogComposite = (Composite) super.createDialogArea(parent);
        Composite parameterComposite = new Composite(dialogComposite, SWT.NONE);

        parameterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parameterComposite.setLayout(new GridLayout(2, true));

        createWidgets(parameterComposite);

        return dialogComposite;

    }

    private void createWidgets(Composite parameterComposite) {

        createLabel(parameterComposite, "X Offset: ");
        xOffsetText = createText(parameterComposite);
        xOffsetText.setText(xOffset);

        createLabel(parameterComposite, "Y Offset: ");
        yOffsetText = createText(parameterComposite);
        yOffsetText.setText(yOffset);

        createLabel(parameterComposite, "X Pixel Scaling: ");
        xPixelScalingText = createText(parameterComposite);
        xPixelScalingText.setText(xPixelScaling);

        createLabel(parameterComposite, "Y Pixel Scaling: ");
        yPixelScalingText = createText(parameterComposite);
        yPixelScalingText.setText(yPixelScaling);

    }

    private Label createLabel(Composite parent, String name) {

        final Label label = new Label(parent, SWT.NONE);
        GridDataFactory.swtDefaults().applyTo(label);
        label.setText(name);
        return label;

    }

    private Text createText(Composite parent) {

        final Text textBox = new Text(parent, SWT.BORDER);
        GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).applyTo(textBox);

        textBox.addModifyListener(e -> {
        	if (INPUT_NUMERIC_PATTERN.matcher(textBox.getText()).matches()) {
        		textBox.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        	} else {
        		textBox.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        	}
        	getButton(IDialogConstants.OK_ID).setEnabled(validateText());

        });

        return textBox;

    }

    private boolean validateText() {
    	return Arrays.asList(xOffsetText.getText(), yOffsetText.getText(), xPixelScalingText.getText(), yPixelScalingText.getText())
    			.stream()
    			.allMatch(text -> INPUT_NUMERIC_PATTERN.matcher(text).matches());

    }


    private void saveInput() {
        xOffset = xOffsetText.getText();
        yOffset = yOffsetText.getText();
        xPixelScaling = xPixelScalingText.getText();
        yPixelScaling = yPixelScalingText.getText();

    }

    @Override
    protected boolean isResizable() {
        return false;
    }

    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }

    public String getxOffset() {
        return xOffset;
    }

    public String getyOffset() {
        return yOffset;
    }

    public String getxPixelScaling() {
        return xPixelScaling;
    }

    public String getyPixelScaling() {
        return yPixelScaling;
    }


}
