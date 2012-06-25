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

package org.diamond.tomo.highrestomo.ui.editors;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class HighResTomoBeanEditor extends RichBeanEditorPart {

	private SpinnerWrapper imagesPerdark;
	private SpinnerWrapper numberOfFlatFields;
	private SpinnerWrapper imagesPerFlat;
	private SpinnerWrapper numberOfProjections;
	private ScaleBox exposureTime;
	private ScaleBox endAngle;
	public HighResTomoBeanEditor(String path, URL mappingURL,
			DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	private ScaleBox startAngle;
	public static final String ID = "org.diamond.tomo.ui.editors.HighResTomoBeanEditor"; //$NON-NLS-1$

	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent.setLayout(gridLayout);

		final Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		group.setLayout(gridLayout_1);

		final Label tomographyParametersLabel = new Label(group, SWT.NONE);
		final GridData gd_tomographyParametersLabel = new GridData();
		tomographyParametersLabel.setLayoutData(gd_tomographyParametersLabel);
		tomographyParametersLabel.setText("Tomography Parameters");
		new Label(group, SWT.NONE);

		final Label distanceToMoveLabel = new Label(group, SWT.NONE);
		distanceToMoveLabel.setLayoutData(new GridData());
		distanceToMoveLabel.setText("Start Angle");

		startAngle = new ScaleBox(group, SWT.NONE);
		startAngle.setUnit("degrees");
		startAngle.setMaximum(360.0);
		final GridData gd_startAngle = new GridData(SWT.FILL, SWT.CENTER, true, false);
		startAngle.setLayoutData(gd_startAngle);

		final Label numberOfFlatLabel = new Label(group, SWT.NONE);
		numberOfFlatLabel.setLayoutData(new GridData());
		numberOfFlatLabel.setText("End Angle");

		endAngle = new ScaleBox(group, SWT.NONE);
		endAngle.setUnit("degrees");
		endAngle.setMaximum(360.0);
		final GridData gd_endAngle = new GridData(SWT.FILL, SWT.CENTER, true, false);
		endAngle.setLayoutData(gd_endAngle);

		final Label cameraExposureTimeLabel = new Label(group, SWT.NONE);
		cameraExposureTimeLabel.setLayoutData(new GridData());
		cameraExposureTimeLabel.setText("Projection Exposure time");

		exposureTime = new ScaleBox(group, SWT.NONE);
		exposureTime.setUnit("ms");
		final GridData gd_exposureTime = new GridData(SWT.FILL, SWT.CENTER, true, false);
		exposureTime.setLayoutData(gd_exposureTime);

		final Label startAngleLabel = new Label(group, SWT.NONE);
		startAngleLabel.setLayoutData(new GridData());
		startAngleLabel.setText("Number Of Projections");

		numberOfProjections = new SpinnerWrapper(group, SWT.NONE);
		numberOfProjections.setMaximum(10000);
		final GridData gd_numberOfProjections = new GridData(SWT.FILL, SWT.CENTER, true, false);
		numberOfProjections.setLayoutData(gd_numberOfProjections);

		final Label helloLabel = new Label(parent, SWT.NONE);
		helloLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		final Group group_1 = new Group(parent, SWT.NONE);
		group_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		group_1.setLayout(gridLayout_2);

		final Label flatFieldParametersLabel = new Label(group_1, SWT.NONE);
		flatFieldParametersLabel.setText("Flat Field Parameters");
		new Label(group_1, SWT.NONE);

		final Label endAngleLabel = new Label(group_1, SWT.NONE);
		endAngleLabel.setText("Number of images taken per flatfield");

		imagesPerFlat = new SpinnerWrapper(group_1, SWT.NONE);
		final GridData gd_imagesPerFlat = new GridData(SWT.FILL, SWT.CENTER, true, false);
		imagesPerFlat.setLayoutData(gd_imagesPerFlat);

		final Label numberOfFlatLabel_1 = new Label(group_1, SWT.NONE);
		numberOfFlatLabel_1.setText("Number of Flat Fields taken");

		numberOfFlatFields = new SpinnerWrapper(group_1, SWT.NONE);
		numberOfFlatFields.setMaximum(10000);
		final GridData gd_numberOfFlatFields = new GridData(SWT.FILL, SWT.CENTER, true, false);
		numberOfFlatFields.setLayoutData(gd_numberOfFlatFields);
		new Label(parent, SWT.NONE);

		final Group group_2 = new Group(parent, SWT.NONE);
		final GridData gd_group_2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_group_2.heightHint = 54;
		gd_group_2.widthHint = 215;
		group_2.setLayoutData(gd_group_2);
		final GridLayout gridLayout_3 = new GridLayout();
		gridLayout_3.numColumns = 2;
		group_2.setLayout(gridLayout_3);

		final Label darkFieldParametersLabel = new Label(group_2, SWT.NONE);
		darkFieldParametersLabel.setText("Dark Field Parameters");
		new Label(group_2, SWT.NONE);

		final Label numberOfImagesLabel = new Label(group_2, SWT.NONE);
		numberOfImagesLabel.setText("Number of images to be taken");

		imagesPerdark = new SpinnerWrapper(group_2, SWT.NONE);
		final GridData gd_imagesPerdark = new GridData(SWT.FILL, SWT.CENTER, true, false);
		imagesPerdark.setLayoutData(gd_imagesPerdark);
		new Label(parent, SWT.NONE);
		//
	}

	public ScaleBox getStartAngle() {
		return startAngle;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Tomo Editor";
	}


	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	public ScaleBox getEndAngle() {
		return endAngle;
	}
	public ScaleBox getExposureTime() {
		return exposureTime;
	}
	public SpinnerWrapper getNumberOfProjections() {
		return numberOfProjections;
	}
	public SpinnerWrapper getImagesPerFlat() {
		return imagesPerFlat;
	}
	public SpinnerWrapper getNumberOfFlatFields() {
		return numberOfFlatFields;
	}
	public SpinnerWrapper getImagesPerdark() {
		return imagesPerdark;
	}

}
