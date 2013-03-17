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

import gda.jython.JythonServerFacade;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import uk.ac.gda.beans.exafs.i18.AttenuatorParameters;
import uk.ac.gda.beans.exafs.i18.I18SampleParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;


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
	private I18SampleParameters bean;
	private GridData gridData_4;
	private ScaleBox vfmx;
	private ExpandableComposite kbExpandableComposite;
	private Boolean vfmxActive;
	
	@SuppressWarnings("unused")
	public I18SampleParametersComposite(Composite parent, int style, I18SampleParameters newBean) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		bean = newBean;
		
		createNameAndDescription();
		createSampleStageSection();
		createAttenuatorSection();
		createKBSection();
		
	}

	public void createNameAndDescription(){
		Composite nameAndDescriptionComposite = new Composite(this, SWT.NONE);
		GridData gd_nameAndDescriptionComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_nameAndDescriptionComposite.widthHint = 385;
		nameAndDescriptionComposite.setLayoutData(gd_nameAndDescriptionComposite);
		nameAndDescriptionComposite.setLayout(new GridLayout(2, false));

		Label lblName = new Label(nameAndDescriptionComposite, SWT.NONE);
		lblName.setSize(37, 17);
		lblName.setText("name");

		this.name = new TextWrapper(nameAndDescriptionComposite, SWT.NONE);
		GridData gd_name = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_name.widthHint = 294;
		name.setLayoutData(gd_name);
		
		Label lblDescription = new Label(nameAndDescriptionComposite, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblDescription.setSize(72, 17);
		lblDescription.setText("description");
	
		this.description = new TextWrapper(nameAndDescriptionComposite, SWT.MULTI);
		GridData gd_description = new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1);
		gd_description.heightHint = 62;
		gd_description.widthHint = 294;
		description.setLayoutData(gd_description);
	}
	
	private void createSampleStageSection(){
		
		Group sampleSatgeGroup = new Group(this, SWT.BORDER);
		GridData gd_sampleSatgeGroup = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_sampleSatgeGroup.widthHint = 384;
		sampleSatgeGroup.setLayoutData(gd_sampleSatgeGroup);
		sampleSatgeGroup.setText("Sample Stage");
		sampleSatgeGroup.setLayout(new GridLayout(1, false));
		
		Composite sampleStageComp = new Composite(sampleSatgeGroup, SWT.NONE);
		GridData gd_sampleStageComp = new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1);
		gd_sampleStageComp.widthHint = 372;
		sampleStageComp.setLayoutData(gd_sampleStageComp);
		sampleStageComp.setLayout(new GridLayout(1,false));
		
		Label label = new Label(sampleStageComp, SWT.NONE);
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		label.setText("Ignore this if using in a Microfocus Scan");
		
		sampleStageParameters = new SampleStageParametersComposite(sampleStageComp,SWT.NONE);
		((GridData) sampleStageParameters.getZ().getLayoutData()).grabExcessHorizontalSpace = false;
		((GridData) sampleStageParameters.getY().getLayoutData()).grabExcessHorizontalSpace = false;
		((GridData) sampleStageParameters.getX().getLayoutData()).grabExcessHorizontalSpace = false;
		
		GridData gridData_1 = (GridData) sampleStageParameters.getX().getControl().getLayoutData();
		gridData_1.grabExcessHorizontalSpace = false;
		gridData_1.widthHint = 100;
		new Label(sampleStageParameters.getX(), SWT.NONE);

		GridData gridData_2 = (GridData) sampleStageParameters.getZ().getControl().getLayoutData();
		gridData_2.widthHint = 100;
		gridData_2.grabExcessHorizontalSpace = false;
		new Label(sampleStageParameters.getZ(), SWT.NONE);
		
		GridData gridData_3 = (GridData) sampleStageParameters.getY().getControl().getLayoutData();
		gridData_3.widthHint = 100;
		gridData_3.grabExcessHorizontalSpace = false;
		new Label(sampleStageParameters.getY(), SWT.NONE);
		
		sampleStageParameters.setEditorClass(uk.ac.gda.beans.exafs.i18.SampleStageParameters.class);
		
		Composite composite = new Composite(sampleStageComp, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_composite.widthHint = 365;
		composite.setLayoutData(gd_composite);
		
		currentPosition = new Button(composite, SWT.NONE);
		currentPosition.setBounds(229, 0, 133, 29);
		currentPosition.setToolTipText("Fill the text boxes with the current motor values");
		currentPosition.setText("Get current values");
					
		currentPosition.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				sampleStageParameters.setXValue(JythonServerFacade.getInstance().evaluateCommand("sc_MicroFocusSampleX()"));
				sampleStageParameters.setYValue(JythonServerFacade.getInstance().evaluateCommand("sc_MicroFocusSampleY()"));
				sampleStageParameters.setZValue(JythonServerFacade.getInstance().evaluateCommand("sc_sample_z()"));
			}
		});
	}
	
	private void createAttenuatorSection(){

		Group attenGroup = new Group(this, SWT.NONE);
		attenGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		attenGroup.setText("Attenuators");
		attenGroup.setLayout(new GridLayout(1, false));
		
		final Composite attenComp = new Composite(attenGroup, SWT.NONE);

		GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		attenComp.setLayoutData(gridData);
		
		GridLayout gl_attenComp = new GridLayout(1,false);
		gl_attenComp.marginWidth = 0;
		gl_attenComp.marginHeight = 0;
		attenComp.setLayout(gl_attenComp);
		final Composite centre = new Composite(attenComp, SWT.NONE);
		GridLayout gl_centre = new GridLayout(4, false);
		gl_centre.verticalSpacing = 0;
		gl_centre.marginWidth = 0;
		centre.setLayout(gl_centre);
		
		gridData_4 = new GridData(SWT.LEFT, SWT.TOP, false, false);
		centre.setLayoutData(gridData_4);
		
		attenuator1 = new AttenuatorParametersComposite(centre, SWT.NONE);
		GridData gridData_1 = (GridData) attenuator1.getPosition().getLayoutData();
		gridData_1.grabExcessVerticalSpace = false;
		gridData_1.heightHint = -1;
		((GridData) attenuator1.getName().getLayoutData()).heightHint = 22;
		attenuator1.setEditorClass(AttenuatorParameters.class);
		
		gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gridData.widthHint = 190;
		attenuator1.setLayoutData(gridData);
		
		attenuator2 = new AttenuatorParametersComposite(centre, SWT.NONE);
		GridData gridData_2 = (GridData) attenuator2.getPosition().getLayoutData();
		gridData_2.grabExcessVerticalSpace = false;
		gridData_2.heightHint = -1;
		((GridData) attenuator2.getName().getLayoutData()).heightHint = 22;
		attenuator2.setEditorClass(AttenuatorParameters.class);
		
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gridData.widthHint = 173;
		attenuator2.setLayoutData(gridData);
		new Label(centre, SWT.NONE);
		new Label(centre, SWT.NONE);
		new Label(centre, SWT.NONE);
		
		attnCurrentPosition = new Button(centre, SWT.NONE);
		attnCurrentPosition.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		attnCurrentPosition.setToolTipText("Select the current attenuator values");
		attnCurrentPosition.setText("Get current values");
		new Label(centre, SWT.NONE);
	}
	
	private void createKBSection(){
		kbExpandableComposite = new ExpandableComposite(this, SWT.NONE);
		kbExpandableComposite.setText("KB Mirror");
		GridData gd_kbExpandableComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_kbExpandableComposite.heightHint = 243;
		kbExpandableComposite.setLayoutData(gd_kbExpandableComposite);
		
		kbExpandableComposite.setExpanded(bean.isVfmxActive());

		Composite kbComp = new Composite(kbExpandableComposite, SWT.NONE);
		kbComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		kbComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		kbComp.setLayout(new GridLayout(2,false));
		
		Composite axis = new Composite(kbComp, SWT.NONE);
		axis.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		axis.setLayout(new GridLayout(2, false));
		Label label = new Label(axis, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("x");
		
		vfmx = new ScaleBox(axis, SWT.NONE);
		GridData gridData_1_1 = (GridData) vfmx.getControl().getLayoutData();
		gridData_1_1.grabExcessHorizontalSpace = false;
		gridData_1_1.widthHint = 100;
		vfmx.setDecimalPlaces(4);
		vfmx.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(vfmx, SWT.NONE);
		
		currentPosition = new Button(kbComp, SWT.NONE);
		currentPosition.setToolTipText("Fill the text boxes with the current motor values");
		currentPosition.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		currentPosition.setText("Get current value");
		
		currentPosition.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				vfmx.setValue(JythonServerFacade.getInstance().evaluateCommand("kb_vfm_x()"));
			}
		});
		vfmx.setValue(JythonServerFacade.getInstance().evaluateCommand("kb_vfm_x()"));
		
		kbExpandableComposite.setClient(kbComp);
		
		String vfmXVal = JythonServerFacade.getInstance().evaluateCommand("kb_vfm_x()");
		bean.setVfmx(Double.parseDouble(vfmXVal));
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
	public void enableSample()
	{
		sampleStageParameters.setEnabled(true);
	}

	public ScaleBox getVfmx() {
		return vfmx;
	}

	public boolean isVfmxActive() {
		return vfmxActive;
	}

	public ExpandableComposite getKbExpandableComposite() {
		return kbExpandableComposite;
	}

	public void setKbExpandableComposite(ExpandableComposite kbExpandableComposite) {
		this.kbExpandableComposite = kbExpandableComposite;
	}

	public void setVfmxActive(Boolean vfmxActive) {
		this.vfmxActive = vfmxActive;
		bean.setVfmxActive(vfmxActive);
	}
	
	
}
