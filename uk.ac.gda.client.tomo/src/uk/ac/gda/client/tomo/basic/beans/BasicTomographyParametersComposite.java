/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.basic.beans;


import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

/**
 *
 */
public final class BasicTomographyParametersComposite extends Composite {

	private ComboWrapper camera;
	private ComboWrapper theta;
	private ComboWrapper flatFieldTranslation;
	private SpinnerWrapper cameraROIStartX;
	private SpinnerWrapper cameraROIStartY;
	private SpinnerWrapper cameraROISizeX;
	private SpinnerWrapper cameraROISizeY;
	private ComboWrapper cameraBinX;
	private ComboWrapper cameraBinY;
	private ScaleBox cameraExposureTime;
	private ScaleBox scanStartAngle;
	private ScaleBox scanEndAngle;
	private SpinnerWrapper scanNumberOfPointsPerSegment;
	private SpinnerWrapper scanNumberOfSegments;
	private SpinnerWrapper darkNumberOfImages;
	private SpinnerWrapper flatNumberOfImages;
	private ComboWrapper reconNumberOfChunks;
	private TextWrapper reconJobName;
	
	private ExpandableComposite advancedExpandableComposite;
	private ExpansionAdapter expansionListener;

	public BasicTomographyParametersComposite(Composite parent, int style) {
		
		super(parent, style);
		
		Label label;
		HashMap<String,Object> hashCameraBinX = new HashMap<String, Object>();
		hashCameraBinX.put("1 pixel per bin", 1);
		hashCameraBinX.put("2 pixels per bin", 2);
		hashCameraBinX.put("4 pixels per bin", 4);
		
		HashMap<String,Object>  hashCameraBinY = new HashMap<String, Object>();
		hashCameraBinY.put("1 pixel per bin", 1);
		hashCameraBinY.put("2 pixels per bin", 2);
		hashCameraBinY.put("4 pixels per bin", 4);
		
		HashMap<String,Object>  hashReconChunks = new HashMap<String, Object>();
		hashReconChunks.put("1 GPU (1/16th of the cluster)", 1);
		hashReconChunks.put("2 GPU (1/8th of the cluster)", 2);
		hashReconChunks.put("4 GPU (1/4th of the cluster)", 4);
		hashReconChunks.put("8 GPU (Half of the cluster)", 8);
		hashReconChunks.put("16 GPU (All of the cluster)", 16);
		
		String[] arrayCamera = {"pco"};
		
		String[] arrayTheta = {"dum.a","dum.b"};		
		
		String[] arrayFlatFieldTranslation = {"dum.c","dum.d"};		
		
		setLayout(new GridLayout(1, false));

		
		final Group cameraParametersGroup = new Group(this, SWT.NONE);
		cameraParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cameraParametersGroup.setText("Camera Parameters");
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		cameraParametersGroup.setLayout(gridLayout);

		label = new Label(cameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Camera Exposure Time");
		this.cameraExposureTime = new ScaleBox(cameraParametersGroup, SWT.NONE);
		cameraExposureTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cameraExposureTime.setMaximum(100.0);
		cameraExposureTime.setMinimum(0.0);
		cameraExposureTime.setUnit("seconds");

		label = new Label(cameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Camera ROI Start X");
		this.cameraROIStartX = new SpinnerWrapper(cameraParametersGroup, SWT.BORDER);		
		cameraROIStartX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		cameraROIStartX.setMinimum(0);
		cameraROIStartX.setMaximum(4007);

		label = new Label(cameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Camera ROI Size X");
		this.cameraROISizeX = new SpinnerWrapper(cameraParametersGroup, SWT.BORDER);
		cameraROISizeX.setMaximum(4008);
		cameraROISizeX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		label = new Label(cameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Camera ROI Start Y");
		this.cameraROIStartY = new SpinnerWrapper(cameraParametersGroup, SWT.BORDER);
		cameraROIStartY.setMaximum(2500);
		cameraROIStartY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		label = new Label(cameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Camera ROI Size Y");
		this.cameraROISizeY = new SpinnerWrapper(cameraParametersGroup, SWT.BORDER);
		cameraROISizeY.setMaximum(2500);
		cameraROISizeY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		label = new Label(cameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Camera Binning X");
		this.cameraBinX = new ComboWrapper(cameraParametersGroup, SWT.READ_ONLY);
		cameraBinX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		cameraBinX.setItems(hashCameraBinX);

		label = new Label(cameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Camera Binning Y");
		this.cameraBinY = new ComboWrapper(cameraParametersGroup, SWT.READ_ONLY);
		cameraBinY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		cameraBinY.setItems(hashCameraBinY);
		
		final Group scanParametersGroup = new Group(this, SWT.NONE);
		scanParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		scanParametersGroup.setLayout(gridLayout_1);
		scanParametersGroup.setText("Scan Parameters");
		
		label = new Label(scanParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Number Of Points Per Segment");
		this.scanNumberOfPointsPerSegment = new SpinnerWrapper(scanParametersGroup, SWT.BORDER);
		scanNumberOfPointsPerSegment.setMaximum(10000);
		scanNumberOfPointsPerSegment.setMinimum(1);
		scanNumberOfPointsPerSegment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(scanParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Number Of Segments");
		this.scanNumberOfSegments = new SpinnerWrapper(scanParametersGroup, SWT.BORDER);
		scanNumberOfSegments.setMaximum(10000);
		scanNumberOfSegments.setMinimum(1);
		scanNumberOfSegments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		label = new Label(scanParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Reconstruction Job Name");
		this.reconJobName = new TextWrapper(scanParametersGroup, SWT.BORDER);
		reconJobName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		
		
		this.advancedExpandableComposite = new ExpandableComposite(this, SWT.NONE);
		advancedExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		advancedExpandableComposite.setText("Advanced");
		
		final Composite advanced = new Composite(advancedExpandableComposite, SWT.NONE);
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 1;
		advanced.setLayout(gridLayout_2);

		final Group advancedCameraParametersGroup = new Group(advanced, SWT.NONE);
		advancedCameraParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayout_3 = new GridLayout();
		gridLayout_3.numColumns = 2;
		advancedCameraParametersGroup.setLayout(gridLayout_3);
		advancedCameraParametersGroup.setText("Camera Parameters");
		
		label = new Label(advancedCameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Camera Device");
		this.camera = new ComboWrapper(advancedCameraParametersGroup, SWT.NONE);
		camera.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		camera.setItems(arrayCamera);
		
		label = new Label(advancedCameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Number Of Dark Images");
		this.darkNumberOfImages = new SpinnerWrapper(advancedCameraParametersGroup, SWT.BORDER);
		darkNumberOfImages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(advancedCameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Number Of Flat Field Images");
		this.flatNumberOfImages = new SpinnerWrapper(advancedCameraParametersGroup, SWT.BORDER);
		flatNumberOfImages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		label = new Label(advancedCameraParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Number Of Reconstruction Nodes");
		this.reconNumberOfChunks = new ComboWrapper(advancedCameraParametersGroup, SWT.READ_ONLY);
		reconNumberOfChunks.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		reconNumberOfChunks.setItems(hashReconChunks);

		final Group advancedScanParametersGroup = new Group(advanced, SWT.NONE);
		advancedScanParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayout_4 = new GridLayout();
		gridLayout_4.numColumns = 2;
		advancedScanParametersGroup.setLayout(gridLayout_4);
		advancedScanParametersGroup.setText("Scan Parameters");
		
		label = new Label(advancedScanParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Theta Rotation Device");
		this.theta = new ComboWrapper(advancedScanParametersGroup, SWT.NONE);
		theta.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		theta.setItems(arrayTheta);

		label = new Label(advancedScanParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Flat Field Translation Device");
		this.flatFieldTranslation = new ComboWrapper(advancedScanParametersGroup, SWT.NONE);
		flatFieldTranslation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		flatFieldTranslation.setItems(arrayFlatFieldTranslation);	

		label = new Label(advancedScanParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Scan Start Angle");
		this.scanStartAngle = new ScaleBox(advancedScanParametersGroup, SWT.NONE);
		scanStartAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		label = new Label(advancedScanParametersGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText("Scan End Angle");
		this.scanEndAngle = new ScaleBox(advancedScanParametersGroup, SWT.NONE);
		scanEndAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		
		advancedExpandableComposite.setClient(advanced);
		this.expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
			    layout();
			}
		};
		advancedExpandableComposite.addExpansionListener(expansionListener);


	}

	public FieldComposite getCamera() {
		return camera;
	}

	public FieldComposite getTheta() {
		return theta;
	}

	public FieldComposite getFlatFieldTranslation() {
		return flatFieldTranslation;
	}

	public FieldComposite getCameraROIStartX() {
		return cameraROIStartX;
	}

	public FieldComposite getCameraROIStartY() {
		return cameraROIStartY;
	}

	public FieldComposite getCameraROISizeX() {
		return cameraROISizeX;
	}

	public FieldComposite getCameraROISizeY() {
		return cameraROISizeY;
	}

	public FieldComposite getCameraBinX() {
		return cameraBinX;
	}

	public FieldComposite getCameraBinY() {
		return cameraBinY;
	}

	public FieldComposite getCameraExposureTime() {
		return cameraExposureTime;
	}

	public FieldComposite getScanStartAngle() {
		return scanStartAngle;
	}

	public FieldComposite getScanEndAngle() {
		return scanEndAngle;
	}

	public FieldComposite getScanNumberOfPointsPerSegment() {
		return scanNumberOfPointsPerSegment;
	}

	public FieldComposite getScanNumberOfSegments() {
		return scanNumberOfSegments;
	}

	public FieldComposite getDarkNumberOfImages() {
		return darkNumberOfImages;
	}

	public FieldComposite getFlatNumberOfImages() {
		return flatNumberOfImages;
	}

	public FieldComposite getReconNumberOfChunks() {
		return reconNumberOfChunks;
	}

	public FieldComposite getReconJobName() {
		return reconJobName;
	}

	@Override
	public void dispose() {
		advancedExpandableComposite.removeExpansionListener(expansionListener);
		super.dispose();
	}
	
}
