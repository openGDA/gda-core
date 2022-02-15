/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.live.stream.calibration.CsvCameraOffsetCalibration;

/**
 * Extension for {@link LiveStreamViewCameraControls} that creates a Calibrate button
 * that opens a dialog {@link EditCameraOffsetCalibrationDialog} to allow editing
 * Camera Offset calibration parameters.
 */

public class LiveStreamViewCameraControlsOffsetCalibrate implements LiveStreamViewCameraControlsExtension {

    private EditCameraOffsetCalibrationDialog dialog;
    private CsvCameraOffsetCalibration calibration;

    private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewCameraControlsOffsetCalibrate.class);

    @Override
    public void createUi(Composite composite, CameraControl cameraControl) {
        if (calibration == null) {
            logger.error("Camera offset calibration not set.");
            return;
        }

        final Composite mainComposite = new Composite(composite, SWT.NONE);
        GridDataFactory.fillDefaults().applyTo(mainComposite);
        GridLayoutFactory.swtDefaults().margins(0, 5).applyTo(mainComposite);

        final Button calibrateButton = new Button(mainComposite, SWT.PUSH);
        GridDataFactory.fillDefaults().applyTo(calibrateButton);
        calibrateButton.setText("Camera Offset");
        calibrateButton.setToolTipText("Set camera offset calibration");
        calibrateButton.addSelectionListener(widgetSelectedAdapter(e-> openDialog()));

        dialog = new EditCameraOffsetCalibrationDialog(Display.getCurrent().getActiveShell(),
        		calibration.getxOffset(), calibration.getyOffset(),
        		calibration.getxPixelScaling(), calibration.getyPixelScaling());
    }

    private void openDialog() {
        dialog.create();
        if (dialog.open() == Window.OK) {
            String timeStamp = new SimpleDateFormat("dd.MM.yyyy.HH.mm.ss").format(new Date());
            double xOffset = Double.parseDouble(dialog.getxOffset());
            double yOffset = Double.parseDouble(dialog.getyOffset());
            double xPixelScaling = Double.parseDouble(dialog.getxPixelScaling());
            double yPixelScaling = Double.parseDouble(dialog.getyPixelScaling());

            calibration.updateCalibrator(timeStamp, xOffset, yOffset, xPixelScaling, yPixelScaling);
        }
    }

    public void setCsvCameraOffsetCalibration(CsvCameraOffsetCalibration csvCalibration) {
        this.calibration = csvCalibration;
    }


}