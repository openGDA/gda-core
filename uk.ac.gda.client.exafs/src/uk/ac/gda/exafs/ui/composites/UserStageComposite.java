/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;

public class UserStageComposite extends FieldBeanComposite {

	private static final Logger logger = LoggerFactory.getLogger(UserStageComposite.class);

	private ScaleBox axis2;
	private ScaleBox axis4;
	private ScaleBox axis5;
	private ScaleBox axis6;
	private ScaleBox axis7;
	private ScaleBox axis8;

	private String axis2Name;
	private String axis4Name;
	private String axis5Name;
	private String axis6Name;
	private String axis7Name;
	private String axis8Name;

	public UserStageComposite(Composite parent, int style, String axis2Name, String axis4Name, String axis5Name,
			String axis6Name, String axis7Name, String axis8Name) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		this.axis2Name = axis2Name;
		this.axis4Name = axis4Name;
		this.axis5Name = axis5Name;
		this.axis6Name = axis6Name;
		this.axis7Name = axis7Name;
		this.axis8Name = axis8Name;

		Label lbl1 = new Label(this, SWT.NONE);
		lbl1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbl1.setText("User Axis 2");
		axis2 = new ScaleBox(this, SWT.NONE);
		axis2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		axis2.setDecimalPlaces(8);

		Label lbl2 = new Label(this, SWT.NONE);
		lbl2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbl2.setText("User Axis 4");
		axis4 = new ScaleBox(this, SWT.NONE);
		axis4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		axis4.setDecimalPlaces(8);

		Label lbl3 = new Label(this, SWT.NONE);
		lbl3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbl3.setText("User Axis 5");
		axis5 = new ScaleBox(this, SWT.NONE);
		axis5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		axis5.setDecimalPlaces(8);

		Label lbl4 = new Label(this, SWT.NONE);
		lbl4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbl4.setText("User Axis 6");
		axis6 = new ScaleBox(this, SWT.NONE);
		axis6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		axis6.setDecimalPlaces(8);

		Label lbl5 = new Label(this, SWT.NONE);
		lbl5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbl5.setText("User Axis 7");
		axis7 = new ScaleBox(this, SWT.NONE);
		axis7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		axis7.setDecimalPlaces(8);

		Label lbl6 = new Label(this, SWT.NONE);
		lbl6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbl6.setText("User Axis 8");
		axis8 = new ScaleBox(this, SWT.NONE);
		axis8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		axis8.setDecimalPlaces(8);

		new Label(this, SWT.NONE);

		Button btnGetCurrentValues = new Button(this, SWT.NONE);
		btnGetCurrentValues.setToolTipText("Fill the text boxes with the current motor values");
		btnGetCurrentValues.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnGetCurrentValues.setText("Get current values");

		btnGetCurrentValues.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				axis2.setValue(JythonServerFacade.getInstance().evaluateCommand(
						UserStageComposite.this.axis2Name + "()"));
				axis4.setValue(JythonServerFacade.getInstance().evaluateCommand(
						UserStageComposite.this.axis4Name + "()"));
				axis5.setValue(JythonServerFacade.getInstance().evaluateCommand(
						UserStageComposite.this.axis5Name + "()"));
				axis6.setValue(JythonServerFacade.getInstance().evaluateCommand(
						UserStageComposite.this.axis6Name + "()"));
				axis7.setValue(JythonServerFacade.getInstance().evaluateCommand(
						UserStageComposite.this.axis7Name + "()"));
				axis8.setValue(JythonServerFacade.getInstance().evaluateCommand(
						UserStageComposite.this.axis8Name + "()"));
			}
		});
	}

	public void setMotorLimits(String motorName, ScaleBox box) throws Exception {
		String lowerLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getLowerMotorLimit()");
		String upperLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getUpperMotorLimit()");
		if (!lowerLimit.equals("None") && lowerLimit != null && !lowerLimit.isEmpty())
			box.setMinimum(Double.parseDouble(lowerLimit));
		if (!upperLimit.equals("None") && upperLimit != null && !upperLimit.isEmpty())
			box.setMaximum(Double.parseDouble(upperLimit));
	}

	public ScaleBox getAxis2() {
		return axis2;
	}

	public ScaleBox getAxis4() {
		return axis4;
	}

	public ScaleBox getAxis5() {
		return axis5;
	}

	public ScaleBox getAxis6() {
		return axis6;
	}

	public ScaleBox getAxis7() {
		return axis7;
	}

	public ScaleBox getAxis8() {
		return axis8;
	}

}
