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

package uk.ac.gda.exafs.ui.composites;


import gda.jython.JythonServerFacade;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.RangeBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;

public class B18FurnaceComposite extends FieldBeanComposite {
	private ScaleBox temperature;
	private ScaleBox tolerance;
	private ScaleBox time;
	private BooleanWrapper controlFlag;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public B18FurnaceComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		final Logger logger = LoggerFactory.getLogger(XYThetaStageComposite.class);
		Label lblTemperature = new Label(this, SWT.NONE);
		lblTemperature.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTemperature.setText("Temperature");

		temperature = new ScaleBox(this, SWT.NONE);
		temperature.setUnit("C");
		temperature.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		try {
			setMotorLimits("eurotherm", temperature);
			
		} catch (Exception e) {
			logger.warn("exception while fetching hardware limits: " + e.getMessage(), e);
		}
		
		Label lblTolerance = new Label(this, SWT.NONE);
		lblTolerance.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTolerance.setText("Tolerance");

		tolerance = new ScaleBox(this, SWT.NONE);
		tolerance.setUnit("C");
		tolerance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblTime = new Label(this, SWT.NONE);
		lblTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTime.setText("Wait for stability time");

		time = new ScaleBox(this, SWT.NONE);
		time.setUnit("s");
		time.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		this.controlFlag = new BooleanWrapper(this, SWT.NONE);
		controlFlag.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		controlFlag.setText("Only read, no control");
	}

	public void setMotorLimits(String motorName, ScaleBox box) throws Exception{
		String lowerLimit = JythonServerFacade.getInstance().evaluateCommand(motorName+".getLowerLimit()");
		String upperLimit = JythonServerFacade.getInstance().evaluateCommand(motorName+".getUpperLimit()");
		box.setMinimum(Double.parseDouble(lowerLimit));
		box.setMaximum(Double.parseDouble(upperLimit));
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public FieldComposite getTemperature(){
		return temperature;
	}

	public FieldComposite getTolerance(){
		return tolerance;
	}

	public FieldComposite getTime(){
		return time;
	}

	public FieldComposite getControlFlag() {
		return controlFlag;
	}
	

}
