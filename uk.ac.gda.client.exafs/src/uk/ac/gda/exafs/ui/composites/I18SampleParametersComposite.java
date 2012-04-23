/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites;

import org.eclipse.swt.graphics.Color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.exafs.i18.AttenuatorParameters;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;


/**
 *
 */
public final class I18SampleParametersComposite extends Composite {

	private SampleStageParametersComposite sampleStageParameters;
	private AttenuatorParametersComposite attenuator1;
	private AttenuatorParametersComposite attenuator2;
	private FieldComposite name;
	private FieldComposite description;
	private Button currentPosition;
	private Button attnCurrentPosition;

	@SuppressWarnings("unused")
	public I18SampleParametersComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		Label label = new Label(this, SWT.NONE);
		label.setSize(37, 17);
		label.setText("name");
		this.name = new TextWrapper(this, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		name.setSize(234, 21);

		label = new Label(this, SWT.NONE);
		label.setSize(72, 17);
		label.setText("description");
		this.description = new TextWrapper(this, SWT.NONE);
		description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		description.setSize(234, 21);
		final Composite sampleSatgeComp = new Composite(this, SWT.BORDER);
		{
			GridData gridData = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
			gridData.widthHint = 445;
			sampleSatgeComp.setLayoutData(gridData);
		}
		sampleSatgeComp.setLayout(new GridLayout(1,false));
		label = new Label(sampleSatgeComp, SWT.NONE);
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		label.setText("Sample Stage . Ignore this if using in a Microfocus Scan");
		sampleStageParameters = new SampleStageParametersComposite(sampleSatgeComp,SWT.NONE);
		sampleStageParameters.setEditorClass(uk.ac.gda.beans.exafs.i18.SampleStageParameters.class);
		{
			GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gridData.widthHint = 437;
			sampleStageParameters.setLayoutData(gridData);
		}
	
		currentPosition = new Button(sampleSatgeComp, SWT.NONE);
		currentPosition.setToolTipText("Fill the text boxes with the current motor values");
		currentPosition.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		currentPosition.setText("Get current values");
		//sampleStageParameters.setLayoutData(layoutData)

		final Composite attenComp = new Composite(this, SWT.BORDER);
		{
			GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
			gridData.widthHint = 448;
			gridData.heightHint = 167;
			attenComp.setLayoutData(gridData);
		}
		attenComp.setLayout(new GridLayout(1,false));
		//attenComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
		new Label(attenComp, SWT.NONE);
		label = new Label(attenComp, SWT.NONE);
		label.setText("Attenuators");
		final Composite centre = new Composite(attenComp, SWT.BORDER);
		centre.setLayout(new GridLayout(5, false));
		{
			GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, true);
			gridData.heightHint = 99;
			gridData.widthHint = 440;
			centre.setLayoutData(gridData);
		}
		attenuator1 = new AttenuatorParametersComposite(centre, SWT.NONE);
		attenuator1.setEditorClass(AttenuatorParameters.class);
		{
			GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gridData.heightHint = 90;
			gridData.widthHint = 190;
			attenuator1.setLayoutData(gridData);
		}
		new Label(centre, SWT.NONE);
		new Label(centre, SWT.NONE);
		attenuator2 = new AttenuatorParametersComposite(centre, SWT.NONE);
		attenuator2.setEditorClass(AttenuatorParameters.class);
		{
			GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
			gridData.heightHint = 92;
			gridData.widthHint = 173;
			attenuator2.setLayoutData(gridData);
		}
		attnCurrentPosition = new Button(attenComp, SWT.NONE);
		attnCurrentPosition.setToolTipText("Select the current attenuator values");
		attnCurrentPosition.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		attnCurrentPosition.setText("Get current values");

	}

	public FieldComposite getName() {
		return name;
	}

	public FieldComposite getDescription() {
		return description;
	}
	public SampleStageParametersComposite getSampleStageParameters() {
		return sampleStageParameters;
	}

	public AttenuatorParametersComposite getAttenuatorParameter1() {
		return attenuator1;
	}
	public AttenuatorParametersComposite getAttenuatorParameter2() {
		return attenuator2;
	}
	public Button getCurrentPosition()
	{
		return currentPosition;
	}
	public Button getAttnCurrentPosition()
	{
		return attnCurrentPosition;
	}
	
	public void disableSample()
	{
		sampleStageParameters.setEnabled(false);
	}

}
